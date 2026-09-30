package org.example.pages;

import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {
    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public DashboardPage waitUntilLoaded() {
        waitForAuthenticatedSession();
        return this;
    }

    public ProfilePage openMyProfile() {
        HomePage homePage = new HomePage(driver);
        homePage.openAccountMenu();
        clickWhenReady(textButton("My Profile"));
        return new ProfilePage(driver).waitUntilLoaded();
    }
}
