package org.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ProfilePage extends BasePage {
    private static final By EDIT_PROFILE = textButton("Edit Profile");
    private static final By IMAGE_INPUT = By.id("profilePicture");
    private static final By SAVE_CHANGES = textButton("Save Changes");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    public ProfilePage waitUntilLoaded() {
        waitForVisible(EDIT_PROFILE);
        return this;
    }

    public ProfilePage updatePicture(Path imagePath) throws Exception {
        if (!Files.isRegularFile(imagePath)) {
            throw new IllegalArgumentException("Profile picture file does not exist: " + imagePath);
        }

        clickWhenReady(EDIT_PROFILE);
        WebElement imageInput = wait.until(ExpectedConditions.presenceOfElementLocated(IMAGE_INPUT));
        imageInput.sendKeys(imagePath.toAbsolutePath().toString());
        clickWhenReady(SAVE_CHANGES);

        var alert = wait.until(ExpectedConditions.alertIsPresent());
        String message = alert.getText();
        alert.accept();
        assertTrue(message.contains("Profile updated successfully"),
                "Expected a successful profile update confirmation, but the site displayed: " + message);
        return this;
    }

    public String getDisplayedProfilePicture() {
        String picture = (String) ((JavascriptExecutor) driver)
                .executeScript("return localStorage.getItem('userProfilePicture');");
        assertNotNull(picture, "The application did not store the updated profile picture.");
        assertFalse(picture.isBlank(), "The updated profile picture URL is empty.");

        Boolean avatarDisplaysImage = wait.until(currentDriver -> (Boolean)
                ((JavascriptExecutor) currentDriver).executeScript(
                        "return [...document.querySelectorAll('div')].some(element => {" +
                                "const style = getComputedStyle(element);" +
                                "return style.borderRadius === '50%' && style.backgroundImage.startsWith('url(');" +
                                "});"));
        assertTrue(avatarDisplaysImage, "The profile avatar is not displaying an image.");
        return picture;
    }
}
