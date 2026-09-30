package org.example.hooks;

import org.openqa.selenium.WebDriver;

public final class BrowserSession {
    private static WebDriver driver;

    private BrowserSession() {
    }

    public static WebDriver getDriver() {
        if (driver == null) {
            throw new IllegalStateException("The browser has not been started.");
        }
        return driver;
    }

    public static void setDriver(WebDriver webDriver) {
        driver = webDriver;
    }

    public static void stop() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
