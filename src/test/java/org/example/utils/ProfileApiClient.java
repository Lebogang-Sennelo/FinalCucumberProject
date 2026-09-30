package org.example.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.testdata.TestData;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public final class ProfileApiClient {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final WebDriver driver;

    public ProfileApiClient(WebDriver driver) {
        this.driver = driver;
    }

    public void assertProfilePicturePersisted(String displayedPicture) throws IOException, InterruptedException {
        String token = (String) ((JavascriptExecutor) driver)
                .executeScript("return localStorage.getItem('authToken');");
        assertNotNull(token, "The authenticated session token is missing.");

        String profileUrl = TestData.apiBaseUrl() + "/profile";
        HttpRequest request = HttpRequest.newBuilder(URI.create(profileUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        ApiTraffic.record("GET", profileUrl, response.statusCode());
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300,
                "GET /profile returned HTTP " + response.statusCode());

        String persistedPicture = findProfilePicture(JSON.readTree(response.body()));
        assertNotNull(persistedPicture, "GET /profile did not return a profile picture.");
        assertFalse(persistedPicture.isBlank(), "GET /profile returned an empty profile picture.");
        assertTrue(persistedPicture.equals(displayedPicture)
                        || displayedPicture.contains(persistedPicture)
                        || persistedPicture.contains(displayedPicture),
                "The profile picture displayed by the UI does not match the persisted profile data.");

        ApiTraffic.collectBrowserResponses(driver);
        ApiTraffic.assertRequiredEndpoints();
    }

    private String findProfilePicture(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            var fields = node.fields();
            while (fields.hasNext()) {
                var field = fields.next();
                String key = field.getKey().toLowerCase(Locale.ROOT);
                if (Set.of("profilepicture", "profileimage", "profileimageurl", "imageurl").contains(key)
                        && field.getValue().isTextual()) {
                    return field.getValue().asText();
                }
                String nested = findProfilePicture(field.getValue());
                if (nested != null) {
                    return nested;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                String nested = findProfilePicture(child);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }
}
