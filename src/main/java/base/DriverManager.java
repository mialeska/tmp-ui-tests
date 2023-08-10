package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.SneakyThrows;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class DriverManager {
    private static final ThreadLocal<RemoteWebDriver> threadDriver = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(String.valueOf(Thread.currentThread().getId()));
    private static final String BASE = "a.blazemeter.com";
    private static final String CURL = String.format("https://%s/api/v4/grid/wd/hub", BASE);

    public static String browserType = "CHROME";

    @SneakyThrows
    public static synchronized WebDriver getDriver() {
        RemoteWebDriver driver = DriverManager.threadDriver.get();
        if(driver != null) {
            return driver;
        }
        String operatingSystem = System.getProperty("os.name");
        LoggingPreferences loggingPrefs = new LoggingPreferences();
        loggingPrefs.enable(LogType.BROWSER, Level.ALL);
        switch (browserType) {
            case "CHROME":
                WebDriverManager.chromedriver().setup();
                logger.info("Initialising the chrome browser");
                Map<String, Object> preferences = new HashMap<>();
                logger.info("to turns off multiple download warning");
                preferences.put("profile.default_content_settings.popups", 0);
                preferences.put("profile.content_settings.exceptions.automatic_downloads.*.setting", 1);
                preferences.put("download.prompt_for_download", false);
                ChromeOptions options = new ChromeOptions();
                options.setCapability(CapabilityType.LOGGING_PREFS, loggingPrefs);
                if (!operatingSystem.contains("Windows")) {
                    options.addArguments("--no-sandbox");
                    options.addArguments("--headless", "--window-size=1920,1200");
                    preferences.put("download.default_directory", "./target/downloads");
                }
                options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--incognito");
                options.setExperimentalOption("prefs", preferences);
                driver = new ChromeDriver(options);
                threadDriver.set(driver);
                break;
            case "REMOTE":
                ChromeOptions browserOptions = new ChromeOptions();
                Map<String, Object> bzmOptions = new HashMap<>();
                bzmOptions.put("blazemeter.apiKey", "some key");
                bzmOptions.put("blazemeter.apiSecret", "some secret");
                bzmOptions.put("blazemeter.projectId", 10000000);
                bzmOptions.put("blazemeter.testId", 10000000);
                bzmOptions.put("blazemeter.locationId","has-1234567890");
                browserOptions.setCapability("bzm:options", bzmOptions);
                browserOptions.setCapability(CapabilityType.LOGGING_PREFS, loggingPrefs);
                URL url = new URL(CURL);
                driver = new RemoteWebDriver(url, browserOptions);
                driver.setFileDetector(new LocalFileDetector());
                threadDriver.set(driver);
                break;
            default:
                logger.info("No browser is matching");
        }

        return driver;
    }

    public static void quitDriver() {
        getDriver().quit();
        DriverManager.threadDriver.remove();
    }
}
