package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pageActions.ExampleActions;

public class ExampleTest extends BaseTest {
    private ExampleActions exampleActions;

    @BeforeClass(alwaysRun = true)
    public void beforeClassSetUp() {
        classLogger = extentReport.createTest("tests.ExampleTest");
        logger.info("Creating object for ExampleTest :: beforeClassSetUp");
        exampleActions = new ExampleActions();
    }

    @Test(description = "exampleTest")
    public void exampleTest() {
        exampleActions.exampleStep();
        Assert.assertEquals(true, false, "Some assertion");
    }
}
