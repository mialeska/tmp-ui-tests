package pageActions;

import base.BaseTest;
import com.qmetry.qaf.automation.step.QAFTestStep;
import io.qameta.allure.Step;

public class ExampleActions extends BaseTest {

    @Step @QAFTestStep
    public void exampleStep() {
        logger.info("I am example step");
    }
}
