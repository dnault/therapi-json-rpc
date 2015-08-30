package com.github.dnault.therapi.core;

import com.github.dnault.therapi.core.annotation.Remotable;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Checks that when a method returns a subclass of its declared return type,
 * the fields from the subclass are not serialized.
 *
 * If this is not the desired behavior, the type should be registered with Jackson
 * as a polymorphic type, as tested by {@link PolymorphicGenericsTest}.
 */
public class ReturnValueSlicingTest extends AbstractMethodRegistryTest {

    @Before
    public void setup() {
        registry.scan(new SlicingServiceImpl());
        registry.scan(new ParameterizedSlicingServiceImpl());
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

    @Remotable("param")
    @SuppressWarnings("unused")
    private interface ParameterizedSlicingService<T> {
        T get();

        List<T> list();
    }

    private static class ParameterizedSlicingServiceImpl implements ParameterizedSlicingService<Base> {
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
    public void subclassPropertiesAreIgnored() throws Exception {
        check("get", "[]", "{base:'foo'}");
    }

    @Test
    public void subclassPropertiesAreIgnoredInList() throws Exception {
        check("list", "[]", "[{base:'foo'},{base:'baz'}]");
    }

    @Test
    public void subclassPropertiesAreIgnoredWhenReturnTypeIsVariable() throws Exception {
        check("param.get", "[]", "{base:'foo'}");
    }

    @Test
    public void subclassPropertiesAreIgnoredInListWhenReturnTypeIsVariable() throws Exception {
        check("param.list", "[]", "[{base:'foo'},{base:'baz'}]");
    }
}
