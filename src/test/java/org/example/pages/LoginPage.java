package org.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {
    private static final By EMAIL = By.id("login-email");
    private static final By PASSWORD = By.id("login-password");
    private static final By SUBMIT = By.id("login-submit");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage waitUntilLoaded() {
        waitForVisible(EMAIL);
        return this;
    }

    public DashboardPage signIn(String username, String password) {
        waitForVisible(EMAIL).sendKeys(username);
        driver.findElement(PASSWORD).sendKeys(password);
        clickWhenReady(SUBMIT);
        waitForAuthenticatedSession();
        return new DashboardPage(driver);
    }
}
