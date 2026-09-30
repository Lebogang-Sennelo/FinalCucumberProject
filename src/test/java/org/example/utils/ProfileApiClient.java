package org.example.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.testdata.TestData;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.Map;
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
        Object responseResult = ((JavascriptExecutor) driver).executeAsyncScript(
                "const url = arguments[0], token = arguments[1], done = arguments[arguments.length - 1];"
                        + "fetch(url, {headers: {Authorization: `Bearer ${token}`, Accept: 'application/json'}})"
                        + ".then(async response => done({status: response.status, body: await response.text()}))"
                        + ".catch(error => done({error: error.message}));",
                profileUrl, token);
        if (!(responseResult instanceof Map<?, ?> response)) {
            throw new AssertionError("GET /profile did not return a readable browser response.");
        }
        if (response.containsKey("error")) {
            throw new AssertionError("GET /profile failed in the browser: " + response.get("error"));
        }
        if (!(response.get("status") instanceof Number status)
                || !(response.get("body") instanceof String responseBody)) {
            throw new AssertionError("GET /profile returned an invalid browser response.");
        }
        ApiTraffic.record("GET", profileUrl, status.intValue());
        assertTrue(status.intValue() >= 200 && status.intValue() < 300,
                "GET /profile returned HTTP " + status);

        String persistedPicture = findProfilePicture(JSON.readTree(responseBody));
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
