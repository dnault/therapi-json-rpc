package com.github.dnault.therapi.example;

import com.github.dnault.therapi.core.AbstractMethodRegistryTest;
import org.junit.Before;
import org.junit.Test;

public class CalculatorServiceTest extends AbstractMethodRegistryTest {
    @Before
    public void setup() {
        registry.scan(new CalculatorServiceImpl());
    }

    @Test
    public void multiplyFractions() throws Exception {
        check("calculator.multiplyFractions", "[[{numerator:2, denominator:3},{numerator:4, denominator:5}]]", "{numerator:8, denominator:15}");
    }
}
