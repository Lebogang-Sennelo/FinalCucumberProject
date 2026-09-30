package org.example.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class TestHooks {
    private static final Path PROFILE_IMAGE = Path.of("target", "test-data", "new-profile-picture.png");

    @Before
    public void startBrowser() throws IOException {
        ApiTraffic.reset();
        Files.createDirectories(PROFILE_IMAGE.getParent());
        createProfileImage();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage",
                "--window-size=390,844");
        LoggingPreferences logging = new LoggingPreferences();
        logging.enable(LogType.PERFORMANCE, Level.ALL);
        options.setCapability("goog:loggingPrefs", logging);
        BrowserSession.setDriver(new ChromeDriver(options));
        BrowserSession.getDriver().manage().timeouts().implicitlyWait(java.time.Duration.ZERO);
    }

    @After
    public void finishScenario(Scenario scenario) throws IOException {
        try {
            if (BrowserSession.getDriver() instanceof TakesScreenshot screenshotDriver) {
                byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Profile workflow");
                Path screenshotPath = Path.of("target", "screenshots",
                        scenario.getName().replaceAll("[^A-Za-z0-9._-]", "_") + ".png");
                Files.createDirectories(screenshotPath.getParent());
                Files.write(screenshotPath, screenshot);
            }
            ApiTraffic.collectBrowserResponses(BrowserSession.getDriver());
        } finally {
            ApiTraffic.writeReport();
            BrowserSession.stop();
        }
    }

    public static Path profileImage() {
        return PROFILE_IMAGE;
    }

    private static void createProfileImage() throws IOException {
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(30, 58, 138));
            graphics.fillRect(0, 0, 128, 128);
            graphics.setColor(new Color(37, 211, 102));
            graphics.fillOval(24, 24, 80, 80);
            graphics.setColor(Color.WHITE);
            graphics.fillOval(50, 43, 10, 10);
            graphics.fillOval(68, 43, 10, 10);
            graphics.fillArc(45, 48, 40, 30, 200, 140);
        } finally {
            graphics.dispose();
        }
        ImageIO.write(image, "png", PROFILE_IMAGE.toFile());
    }
}
