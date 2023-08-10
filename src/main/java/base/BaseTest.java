package base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.qmetry.qaf.automation.core.TestBaseProvider;
import com.qmetry.qaf.automation.ui.WebDriverTestCase;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebDriver;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class BaseTest extends WebDriverTestCase {

    protected static ExtentReports extentReport;
    protected static ExtentTest classLogger;
    protected static ExtentTest testLogger;
    protected static final Logger logger = LoggerFactory.getLogger(String.valueOf(Thread.currentThread().getId()));

    @Parameters({"browserType"})
    @BeforeSuite
    public static void configSetUpMethod(@Optional("CHROME") String browserSelected) {
        logger.info("Selected browserType is: " + browserSelected);
        DriverManager.browserType = browserSelected;
        extentReport = new ExtentReports();
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
        Date date = new Date();
        String reportPath = System.getProperty("user.dir") + "/extent-report/" + dateFormat.format(date) + "_Automation_Results.html";
        ExtentSparkReporter sparkReporter  = new ExtentSparkReporter(reportPath);
        extentReport.attachReporter(sparkReporter);
        sparkReporter.config().setReportName("Regression Results");
        sparkReporter.config().setTheme(Theme.STANDARD);
    }

    @BeforeMethod(alwaysRun = true)
    @Step
    public static void beforeMethodSetUp(Method method, ITestContext context) {
        logger.info("Initialisation the browser  DriverManager.getDriver()::beforeMethodSetUp");
        testLogger = classLogger.createNode(method.getName());
        DriverManager.getDriver().manage().window().maximize();
        DriverManager.getDriver().manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        // for qmetry:
        TestBaseProvider.instance().get()
                .setDriver("chrome", new QAFExtendedWebDriver(DriverManager.getDriver()));
    }

    @AfterMethod(alwaysRun = true)
    @Step
    public static synchronized void updateTestStatus(ITestResult result) throws IOException {
        logTestStatusToReport(DriverManager.getDriver(), result);
        DriverManager.quitDriver();
        testLogger.log(Status.PASS, "Closed the browser successfully");
    }


    @SneakyThrows
    protected static synchronized void logTestStatusToReport(WebDriver driver, ITestResult result) {
        logger.info("Executing logTestStatusToReport() method");
        if (result.getStatus() == ITestResult.SUCCESS) {
            testLogger.log(Status.PASS,
                    MarkupHelper.createLabel(result.getName() + " - Test Case PASSED", ExtentColor.GREEN));
        } else if (result.getStatus() == ITestResult.FAILURE) {
            testLogger.log(Status.FAIL,
                    MarkupHelper.createLabel(result.getName() + " - Test Case FAILED", ExtentColor.RED));

            String screenShotLocation = takeScreenshot(driver, result.getName());
            if ((new File(screenShotLocation)).exists()) {
                logger.info("Screenshot available at the location and trying to attach to the report");
                Allure.addAttachment("Screenshot", Files.newInputStream(Paths.get(screenShotLocation)));
                testLogger.fail("Test Case failed check the screenshot below " + testLogger.addScreenCaptureFromPath("screenshots\\" + result.getName() + "_screenshot.png"));
            } else {
                logger.warn("Screenshot doesn't exist at the location");
            }

        } else if (result.getStatus() == ITestResult.SKIP) {
            testLogger.log(Status.SKIP,
                    MarkupHelper.createLabel(result.getName() + " - Test Case SKIPPED", ExtentColor.BLUE));

        }
    }

    @SneakyThrows
    public static String takeScreenshot(WebDriver driver, String testName) {

        logger.info("Capturing the screenshot :: takeScreenshot");

        String screenShotPath;

        TakesScreenshot takesScreenshot = (TakesScreenshot) driver;

        File src = takesScreenshot.getScreenshotAs(OutputType.FILE);
        screenShotPath = System.getProperty("user.dir") + "/extent-report/screenshots/" + testName + "_screenshot.png";

        logger.info("The screenshot is saved at " + screenShotPath);

        FileUtils.copyFile(src, new File(screenShotPath));

        return screenShotPath;
    }
}
