package utils.listeners;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

public class ScenariosListener implements IMethodInterceptor {
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        String skipped = "skipped";

        return methods.stream()
                .filter(testMethod ->
                        !testMethod.getMethod()
                                        .getConstructorOrMethod()
                                        .getMethod()
                                        .getAnnotation(Test.class).description().contains(skipped))
                .collect(Collectors.toList());
    }
}
