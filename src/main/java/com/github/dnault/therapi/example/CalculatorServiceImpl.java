package com.github.dnault.therapi.example;

import java.util.List;

public class CalculatorServiceImpl implements CalculatorService {
    @Override
    public int add(List<Integer> addends) {
        return addends.stream().reduce(0, (x, y) -> x + y);
    }
}
