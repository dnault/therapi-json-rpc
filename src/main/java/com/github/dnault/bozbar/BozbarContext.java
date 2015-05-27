package com.github.dnault.bozbar;

import java.util.Collection;
import java.util.HashMap;

import com.github.dnault.bozbar.internal.BozbarMethod;

public class BozbarContext {
    private final HashMap<String, BozbarMethod> methodsByName = new HashMap<>();

    private BozbarScanner scanner = new StandardBozbarScanner();

    public void scan(Object o) {
        scanner.scan(o);
    }

    public Object invoke(String methodName, String args) {
        BozbarMethod method = methodsByName.get(methodName);
        if (method == null) {
         //   throw new NoSuchMethodException(methodName);
        }
        return null;
    }
}
