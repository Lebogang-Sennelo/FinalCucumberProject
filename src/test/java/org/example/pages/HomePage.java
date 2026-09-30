package org.example.pages;

import org.example.testdata.TestData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {
    private static final By LOGIN_BUTTON = By.xpath(
            "//div[contains(@class, 'nav-user-section')]//button[contains(@class, 'user-pill') "
                    + "and contains(normalize-space(.), 'Login')]");
    private static final By MENU_BUTTON = By.xpath(
            "//div[contains(@class, 'nav-user-section')]//button[contains(@class, 'user-pill') "
                    + "and contains(normalize-space(.), 'Menu')]");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open() {
        driver.get(TestData.siteUrl());
        return this;
    }

    public LoginPage openLogin() {
        clickWhenReady(LOGIN_BUTTON);
        return new LoginPage(driver);
    }

    public DashboardPage openAccountMenu() {
        clickWhenReady(MENU_BUTTON);
        return new DashboardPage(driver);
    }
}
