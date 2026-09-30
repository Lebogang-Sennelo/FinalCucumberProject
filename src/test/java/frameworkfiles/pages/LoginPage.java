package frameworkfiles.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
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
        wait.until(currentDriver -> {
            try {
                currentDriver.switchTo().alert();
                return true;
            } catch (NoAlertPresentException ignored) {
                return Boolean.TRUE.equals(
                        ((JavascriptExecutor) currentDriver).executeScript(
                                "return Boolean(localStorage.getItem('authToken'));"));
            }
        });

        try {
            Alert alert = driver.switchTo().alert();
            String message = alert.getText();
            alert.accept();
            throw new AssertionError("Sign-in was rejected by the site: " + message);
        } catch (NoAlertPresentException ignored) {
            // A stored auth token indicates a successful sign-in.
        }
        return new DashboardPage(driver);
    }
}
