package com.github.dnault.bozbar;

import java.util.Collection;

import com.github.dnault.bozbar.internal.BozbarMethod;

public interface BozbarScanner {
    Collection<BozbarMethod> scan(Object o);
}
