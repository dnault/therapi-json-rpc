package com.github.dnault.therapi.core;

import com.github.dnault.therapi.core.annotation.Remotable;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class ParameterBindingTest extends AbstractMethodRegistryTest {

    @Before
    public void setup() {
        registry.scan(newEchoProxyInstance(ParamService.class));
    }

    @Remotable("")
    @SuppressWarnings("unused")
    private interface ParamService {
        List<?> echo(int a, String b, List<String> c);
    }

    @Test
    public void canEchoPositional() throws Exception {
        check("echo", "[1,'xyzzy',['one','two','three']]", "[1,'xyzzy',['one','two','three']]");
    }

    @Test
    public void canEchoNamed() throws Exception {
        check("echo", "{a:1,b:'xyzzy',c:['one','two','three']}", "[1,'xyzzy',['one','two','three']]");
    }

    @Test
    public void missingPositionalArgument() throws Exception {
        MissingArgumentException e = check("echo", "[1,'xyzzy']", MissingArgumentException.class);
        assertEquals(Optional.of("c"), e.getParameterName());
    }

    @Test
    public void tooManyPositionalArguments() throws Exception {
        TooManyPositionalArguments e = check("echo", "[1,'xyzzy',5,6,7]", TooManyPositionalArguments.class);
        assertEquals(Optional.empty(), e.getParameterName());
        assertEquals(3, e.getExpected());
        assertEquals(5, e.getActual());
    }

    @Test
    public void missingNamedArgument() throws Exception {
        MissingArgumentException e = check("echo", "{a:1,b:'xyzzy'}", MissingArgumentException.class);
        assertEquals(Optional.of("c"), e.getParameterName());
    }

    @Test
    public void tooManyNamedArguments() throws Exception {
        ParameterBindingException e = check("echo", "{a:1,b:'xyzzy',c:['one','two','three'],foo:1}", ParameterBindingException.class);
        assertEquals(Optional.of("foo"), e.getParameterName());
    }
}
