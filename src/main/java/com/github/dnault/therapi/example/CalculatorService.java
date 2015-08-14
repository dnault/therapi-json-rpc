package com.github.dnault.therapi.example;

import com.github.dnault.therapi.core.annotation.ExampleModel;
import com.github.dnault.therapi.core.annotation.Remotable;

import java.util.List;

@Remotable("calculator")
public interface CalculatorService {
    /**
     * Multiplies a list of fractions together and returns the result.
     * If the list is empty, returns 1/1.
     *
     * @param multiplicands The fractions to multiply together
     * @return The result of multiplying the given fractions together
     */
    Fraction multiplyFractions(List<Fraction> multiplicands);

    /**
     * Fractional representation of a rational number.
     */
    class Fraction {
        private final int numerator;
        private final int denominator;

        public Fraction(int numerator, int denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }

        public int getNumerator() {
            return numerator;
        }

        public int getDenominator() {
            return denominator;
        }

        public Fraction multiplyBy(Fraction other) {
            return new Fraction(numerator * other.numerator,
                    denominator * other.denominator);
        }
    }

    /**
     * A decent approximation of π.
     */
    @ExampleModel
    static Fraction exampleFraction() {
        return new Fraction(22, 7);
    }
}
