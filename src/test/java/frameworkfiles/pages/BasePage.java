package frameworkfiles.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    protected WebElement clickWhenReady(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
        return element;
    }

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void waitForAuthenticatedSession() {
        wait.until(currentDriver -> Boolean.TRUE.equals(
                ((org.openqa.selenium.JavascriptExecutor) currentDriver).executeScript(
                        "return Boolean(localStorage.getItem('authToken'));")));
    }

    protected static By textButton(String text) {
        return By.xpath("//button[contains(normalize-space(.), '" + text + "')]");
    }
}
