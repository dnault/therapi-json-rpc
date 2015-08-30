package com.github.dnault.therapi.core;

import com.github.dnault.therapi.core.annotation.Default;
import com.github.dnault.therapi.core.annotation.Remotable;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;

public class DefaultArgsTest extends AbstractMethodRegistryTest {

    @Before
    public void setup() {
        registry.scan(new EchoServiceImpl());
    }

    @Remotable("")
    private interface EchoService {
        Boolean echoBooleanObjectDefaultNull(@Default Boolean value);

        Boolean echoBooleanObjectDefaultTrueNullable(@Nullable @Default("true") Boolean value);

        Boolean echoBooleanObjectDefaultTrue(@Default("true") Boolean value);

        Boolean echoBooleanObjectDefaultFalse(@Default("false") Boolean value);

        boolean echoBooleanPrimitiveDefaultNull(@Default boolean value);

        boolean echoBooleanPrimitiveDefaultTrue(@Default("true") boolean value);

        boolean echoBooleanPrimitiveDefaultFalse(@Default("false") boolean value);

        String echoStringDefaultNull(@Default String value);

        String echoStringDefaultEmpty(@Default("") String value);

        String echoStringDefaultNonEmpty(@Default("xyzzy") String value);
    }

    private static class EchoServiceImpl implements EchoService {
        @Override
        public Boolean echoBooleanObjectDefaultNull(Boolean value) {
            return value;
        }

        @Override
        public Boolean echoBooleanObjectDefaultTrueNullable(Boolean value) {
            return value;
        }

        @Override
        public Boolean echoBooleanObjectDefaultTrue(Boolean value) {
            return value;
        }

        @Override
        public Boolean echoBooleanObjectDefaultFalse(Boolean value) {
            return value;
        }

        @Override
        public boolean echoBooleanPrimitiveDefaultNull(boolean value) {
            return value;
        }

        @Override
        public boolean echoBooleanPrimitiveDefaultTrue(boolean value) {
            return value;
        }

        @Override
        public boolean echoBooleanPrimitiveDefaultFalse(boolean value) {
            return value;
        }

        @Override
        public String echoStringDefaultNull(String value) {
            return value;
        }

        @Override
        public String echoStringDefaultEmpty(String value) {
            return value;
        }

        @Override
        public String echoStringDefaultNonEmpty(String value) {
            return value;
        }
    }

    @Test
    public void booleanObjectDefaultValues() throws Exception {
        check("echoBooleanObjectDefaultNull", "[]", "null");
        check("echoBooleanObjectDefaultTrue", "[]", "true");
        check("echoBooleanObjectDefaultFalse", "[]", "false");

        check("echoBooleanObjectDefaultTrueNullable", "[null]", "null");
        check("echoBooleanObjectDefaultTrueNullable", "{value:null}", "null");

        check("echoBooleanObjectDefaultNull", "{}", "null");
        check("echoBooleanObjectDefaultTrue", "{}", "true");
        check("echoBooleanObjectDefaultFalse", "{}", "false");

        check("echoBooleanObjectDefaultNull", "[null]", "null");
        check("echoBooleanObjectDefaultNull", "[true]", "true");
        check("echoBooleanObjectDefaultNull", "[false]", "false");
        check("echoBooleanObjectDefaultNull", "{value:null}", "null");
        check("echoBooleanObjectDefaultNull", "{value:true}", "true");
        check("echoBooleanObjectDefaultNull", "{value:false}", "false");

        check("echoBooleanObjectDefaultTrue", "[true]", "true");
        check("echoBooleanObjectDefaultTrue", "[false]", "false");
        check("echoBooleanObjectDefaultTrue", "{value:true}", "true");
        check("echoBooleanObjectDefaultTrue", "{value:false}", "false");

        check("echoBooleanObjectDefaultFalse", "[true]", "true");
        check("echoBooleanObjectDefaultFalse", "[false]", "false");
        check("echoBooleanObjectDefaultFalse", "{value:true}", "true");
        check("echoBooleanObjectDefaultFalse", "{value:false}", "false");
    }

    @Test(expected = NullParameterException.class)
    public void echoBooleanObjectDefaultTrueRequiresNonNullPositional() throws Exception {
        check("echoBooleanObjectDefaultTrue", "[null]", "null");
    }

    @Test(expected = NullParameterException.class)
    public void echoBooleanObjectDefaultTrueRequiresNonNullNamed() throws Exception {
        check("echoBooleanObjectDefaultTrue", "{value:null}", "null");
    }
}
