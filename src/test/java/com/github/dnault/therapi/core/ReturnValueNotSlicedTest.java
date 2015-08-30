package com.github.dnault.therapi.core;

import com.github.dnault.therapi.core.annotation.Remotable;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ReturnValueNotSlicedTest extends AbstractMethodRegistryTest {

    @Before
    public void setup() {
        registry.scan(new SlicingServiceImpl());
    }

    @Remotable("")
    @SuppressWarnings("unused")
    private interface SlicingService {
        Base get();

        List<Base> list();
    }

    private static class SlicingServiceImpl implements SlicingService {
        @Override
        public Base get() {
            return new Subclass("foo", "bar");
        }

        @Override
        public List<Base> list() {
            return Arrays.asList(new Subclass("foo", "bar"), new Subclass("baz", "zot"));
        }
    }

    private static class Base {
        private final String base;

        public Base(String base) {
            this.base = base;
        }

        public String getBase() {
            return base;
        }
    }

    private static class Subclass extends Base {
        private final String subclass;

        public Subclass(String base, String subclass) {
            super(base);
            this.subclass = subclass;
        }

        public String getSubclass() {
            return subclass;
        }
    }

    @Test
    public void subclassPropertiesAreIncludedEvenThoughDeclaredReturnTypeIsBase() throws Exception {
        check("get", "[]", "{base:'foo',subclass:'bar'}");
    }

    @Test
    public void subclassPropertiesAreIncludedInListEvenThoughDeclaredReturnTypeIsBase() throws Exception {
        check("list", "[]", "[{base:'foo',subclass:'bar'},{base:'baz',subclass:'zot'}]");
    }
}
