package com.github.therapi.core;

import javax.annotation.Nullable;

import com.github.therapi.core.annotation.Default;
import com.github.therapi.core.annotation.Remotable;
import org.junit.Before;
import org.junit.Test;

public class DefaultArgsTest extends AbstractMethodRegistryTest {

    enum Flavor {
        CHOCOLATE, FRENCH_VANILLA
    }

    @Before
    public void setup() {
        registry.scan(newEchoProxyInstance(EchoService.class));
    }

    @SuppressWarnings("unused")
    private static class Widget {
        private String serialNumber;

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }
    }

    @Remotable("")
    @SuppressWarnings("unused")
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

        Widget echoDefaultModel(@Default("{serialNumber:'12345'}") Widget widget);

        Flavor echoDefaultEnumUnquoted(@Default("chocolate") Flavor flavor);

        Flavor echoDefaultEnumQuoted(@Default("'frenchVanilla'") Flavor flavor);
    }

    @Remotable("")
    @SuppressWarnings("unused")
    private interface InvalidDefaultService {
        Widget echoDefaultModel(@Default("{badProperty:'12345'}") Widget widget);
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

    @Test(expected = NullArgumentException.class)
    public void echoBooleanObjectDefaultTrueRequiresNonNullPositional() throws Exception {
        check("echoBooleanObjectDefaultTrue", "[null]", "null");
    }

    @Test(expected = NullArgumentException.class)
    public void echoBooleanObjectDefaultTrueRequiresNonNullNamed() throws Exception {
        check("echoBooleanObjectDefaultTrue", "{value:null}", "null");
    }

    @Test
    public void echoDefaultModel() throws Exception {
        check("echoDefaultModel", "[]", "{serialNumber:'12345'}");
    }

    @Test
    public void echoModel() throws Exception {
        check("echoDefaultModel", "[{serialNumber:'7777'}]", "{serialNumber:'7777'}");
    }

    @Test
    public void echoEnum() throws Exception {
        check("echoDefaultEnumUnquoted", "[]", "'chocolate'");
        check("echoDefaultEnumQuoted", "[]", "'frenchVanilla'");
        check("echoDefaultEnumUnquoted", "['frenchVanilla']", "'frenchVanilla'");
    }

    @Test(expected = IllegalArgumentException.class)
    public void failFastOnBadDefault() throws Exception {
        registry.scan(newEchoProxyInstance(InvalidDefaultService.class));
    }

}
