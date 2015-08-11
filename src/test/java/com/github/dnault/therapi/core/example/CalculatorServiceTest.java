package com.github.dnault.therapi.core.example;

import static org.junit.Assert.assertEquals;

import com.github.dnault.therapi.core.MethodRegistry;
import com.github.dnault.therapi.core.internal.JacksonHelper;
import com.github.dnault.therapi.example.CalculatorServiceImpl;
import org.junit.Test;

public class CalculatorServiceTest {
    MethodRegistry context = new MethodRegistry(JacksonHelper.newLenientObjectMapper());
    {
        context.scan(new CalculatorServiceImpl());
    }

    @Test
    public void foo() throws Exception {
        check("calculator.add", "[[1,2,3]]", "6");
    }

    protected void check(String methodName, String args, String expectedResult) throws Exception {
        assertEquals(context.getObjectMapper().readTree(expectedResult), context.invoke(methodName, context.getObjectMapper().readTree(args)));
    }
}
