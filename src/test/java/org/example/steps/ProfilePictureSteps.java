package org.example.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.hooks.ApiTraffic;
import org.example.hooks.BrowserSession;
import org.example.hooks.TestHooks;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ProfilePictureSteps {
    private static final String SITE_URL =
            System.getProperty("site.url", "https://ndosisimplifiedautomation.vercel.app/");
    private static final String API_BASE_URL = "https://www.ndosiautomation.co.za/APIDEV";
    private static final ObjectMapper JSON = new ObjectMapper();
    private WebDriver driver;
    private WebDriverWait wait;
    private String uploadedProfilePicture;

    @Given("I open the Ndosi Automation website")
    public void openWebsite() {
        driver = BrowserSession.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        driver.get(SITE_URL);
    }

    @When("I sign in with the configured account")
    public void signIn() {
        String username = requiredEnvironmentVariable("SITE_USERNAME");
        String password = requiredEnvironmentVariable("SITE_PASSWORD");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email"))).sendKeys(username);
        driver.findElement(By.id("login-password")).sendKeys(password);
        driver.findElement(By.id("login-submit")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".nav-burger")));
    }

    @When("I open the menu and select My Profile")
    public void openProfile() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".nav-burger"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(normalize-space(.), 'My Profile')]"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[contains(normalize-space(.), 'Edit Profile')]")));
    }

    @When("I edit my profile and upload a new picture")
    public void uploadProfilePicture() {
        driver.findElement(By.xpath("//button[contains(normalize-space(.), 'Edit Profile')]")).click();
        WebElement imageInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("profilePicture")));
        Path image = TestHooks.profileImage().toAbsolutePath();
        imageInput.sendKeys(image.toString());
        driver.findElement(By.xpath("//button[contains(normalize-space(.), 'Save Changes')]")).click();

        var alert = wait.until(ExpectedConditions.alertIsPresent());
        String message = alert.getText();
        alert.accept();
        assertTrue(message.contains("Profile updated successfully"),
                "Expected a successful profile update confirmation.");
    }

    @Then("the new profile picture is displayed and persisted")
    public void verifyProfilePicture() throws IOException, InterruptedException {
        uploadedProfilePicture = (String) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return localStorage.getItem('userProfilePicture');");
        assertNotNull(uploadedProfilePicture, "The application did not store the updated profile picture.");
        assertFalse(uploadedProfilePicture.isBlank(), "The updated profile picture URL is empty.");
        Boolean avatarDisplaysImage = wait.until(currentDriver -> (Boolean)
                ((JavascriptExecutor) currentDriver).executeScript(
                        "return [...document.querySelectorAll('div')].some(element => {" +
                                "const style = getComputedStyle(element);" +
                                "return style.borderRadius === '50%' && style.backgroundImage.startsWith('url(');" +
                                "});"));
        assertTrue(avatarDisplaysImage, "The profile avatar is not displaying an image.");

        String token = (String) ((JavascriptExecutor) driver)
                .executeScript("return localStorage.getItem('authToken');");
        assertNotNull(token, "The authenticated session token is missing.");

        HttpRequest request = HttpRequest.newBuilder(URI.create(API_BASE_URL + "/profile"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        ApiTraffic.record("GET", API_BASE_URL + "/profile", response.statusCode());
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300,
                "GET /profile returned HTTP " + response.statusCode());

        JsonNode profileResponse = JSON.readTree(response.body());
        String persistedPicture = findProfilePicture(profileResponse);
        assertNotNull(persistedPicture, "GET /profile did not return a profile picture.");
        assertFalse(persistedPicture.isBlank(), "GET /profile returned an empty profile picture.");
        assertTrue(persistedPicture.equals(uploadedProfilePicture)
                        || uploadedProfilePicture.contains(persistedPicture)
                        || persistedPicture.contains(uploadedProfilePicture),
                "The profile picture displayed by the UI does not match the persisted profile data.");

        ApiTraffic.collectBrowserResponses(driver);
        ApiTraffic.assertRequiredEndpoints();
    }

    private String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set the " + name + " environment variable to run this scenario.");
        }
        return value;
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
