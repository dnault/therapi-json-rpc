package com.github.therapi.example;

import java.util.List;

public class CalculatorServiceImpl implements CalculatorService {
    private static final Fraction ONE = new Fraction(1, 1);

    @Override
    public Fraction multiplyFractions(List<Fraction> multiplicands) {
        return multiplicands.stream().reduce(ONE, Fraction::multiplyBy);
    }
}
