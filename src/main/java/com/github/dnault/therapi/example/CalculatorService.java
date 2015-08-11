package com.github.dnault.therapi.example;

import java.util.List;

import com.github.dnault.therapi.core.annotation.Remotable;

@Remotable("calculator")
public interface CalculatorService {
    /**
     * Sums a list of integers and returns the result.
     *
     * @param addends The integers to add together
     * @return The sum of the given integers
     */
    int add(List<Integer> addends);
}
