package org.example.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.example.utils.ApiTraffic;
import org.example.utils.DriverFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestHooks {
    @Before
    public void startBrowser() {
        ApiTraffic.reset();
        BrowserSession.setDriver(DriverFactory.createChromeDriver());
    }

    @After
    public void finishScenario(Scenario scenario) throws IOException {
        try {
            var driver = BrowserSession.getDriverOrNull();
            if (driver instanceof TakesScreenshot screenshotDriver) {
                byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Profile workflow");
                Path screenshotPath = Path.of("target", "screenshots",
                        scenario.getName().replaceAll("[^A-Za-z0-9._-]", "_") + ".png");
                Files.createDirectories(screenshotPath.getParent());
                Files.write(screenshotPath, screenshot);
            }
            if (driver != null) {
                ApiTraffic.collectBrowserResponses(driver);
            }
        } finally {
            try {
                ApiTraffic.writeReport();
            } finally {
                BrowserSession.stop();
            }
        }
    }
}
