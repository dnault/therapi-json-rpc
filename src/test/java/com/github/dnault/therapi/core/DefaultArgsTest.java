package com.github.dnault.therapi.core;

import com.github.dnault.therapi.core.annotation.Default;
import com.github.dnault.therapi.core.annotation.Remotable;
import com.github.dnault.therapi.core.internal.JacksonHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DefaultArgsTest {

    MethodRegistry context = new MethodRegistry(JacksonHelper.newLenientObjectMapper());

    {
        context.scan(new EchoServiceImpl());
    }

    @Remotable("")
    public interface EchoService {
        Boolean echoBooleanObjectDefaultNull(@Default Boolean value);

        Boolean echoBooleanObjectDefaultTrue(@Default("true") Boolean value);

        Boolean echoBooleanObjectDefaultFalse(@Default("false") Boolean value);

        boolean echoBooleanPrimitiveDefaultNull(@Default boolean value);

        boolean echoBooleanPrimitiveDefaultTrue(@Default("true") boolean value);

        boolean echoBooleanPrimitiveDefaultFalse(@Default("false") boolean value);

        String echoStringDefaultNull(@Default String value);

        String echoStringDefaultEmpty(@Default("") String value);

        String echoStringDefaultNonEmpty(@Default("xyzzy") String value);
    }

    public static class EchoServiceImpl implements EchoService {

        @Override
        public Boolean echoBooleanObjectDefaultNull(@Default Boolean value) {
            return value;
        }

        @Override
        public Boolean echoBooleanObjectDefaultTrue(@Default("true") Boolean value) {
            return value;
        }

        @Override
        public Boolean echoBooleanObjectDefaultFalse(@Default("false") Boolean value) {
            return value;
        }

        @Override
        public boolean echoBooleanPrimitiveDefaultNull(@Default boolean value) {
            return value;
        }

        @Override
        public boolean echoBooleanPrimitiveDefaultTrue(@Default("true") boolean value) {
            return value;
        }

        @Override
        public boolean echoBooleanPrimitiveDefaultFalse(@Default("false") boolean value) {
            return value;
        }

        @Override
        public String echoStringDefaultNull(@Default String value) {
            return value;
        }

        @Override
        public String echoStringDefaultEmpty(@Default("") String value) {
            return value;
        }

        @Override
        public String echoStringDefaultNonEmpty(@Default("xyzzy") String value) {
            return value;
        }
    }

    @Test
    public void booleanObjectDefaultValues() throws Exception {
        check("echoBooleanObjectDefaultNull", "[]", "null");
        check("echoBooleanObjectDefaultTrue", "[]", "true");
        check("echoBooleanObjectDefaultFalse", "[]", "false");

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

    @Test(expected = ParameterBindingException.class)
    public void echoBooleanObjectDefaultTrueRequiresNonNullPositional() throws Exception {
        check("echoBooleanObjectDefaultTrue", "[null]", "null");
    }

    @Test(expected = ParameterBindingException.class)
    public void echoBooleanObjectDefaultTrueRequiresNonNullNamed() throws Exception {
        check("echoBooleanObjectDefaultTrue", "{value:null}", "null");
    }

    protected void check(String methodName, String args, String expectedResult) throws Exception {
        Object result = context.invoke(methodName, context.getObjectMapper().readTree(args));

        if (expectedResult.equals("null")) {
            assertNull(result);
        } else {
            assertEquals(context.getObjectMapper().readTree(expectedResult), result);
        }
    }
}
