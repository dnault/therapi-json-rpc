package com.github.therapi.core;

import com.github.therapi.core.annotation.Remotable;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.cglib.proxy.Factory;

import java.io.IOException;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpringAopProxyTest extends AbstractMethodRegistryTest {

    private interface PlainInterface {
        String getMagicWord();
    }

    @Remotable("grimoire")
    private interface RemotableInterface {
        String getMagicWord();
    }

    private interface EmptyInterface {
    }

    private static class PlainClassWithRemotableInterface implements RemotableInterface {
        @Override
        public String getMagicWord() {
            return "xyzzy";
        }
    }

    @Remotable("grimoire")
    private static class RemotableClassWithPlainInterface implements PlainInterface {
        @Override
        @Remotable
        public String getMagicWord() {
            return "xyzzy";
        }
    }

    @Remotable("grimoire")
    private static class RemotableClassWithEmptyInterface implements EmptyInterface {
        @Remotable
        public String getMagicWord() {
            return "xyzzy";
        }
    }

    @Remotable("grimoire")
    private static class RemotableClass {
        @Remotable
        public String getMagicWord() {
            return "xyzzy";
        }
    }

    @Remotable("grimoire")
    private static class RemotableClassWithRemotableInterface implements RemotableInterface {
        @Remotable
        public String getMagicWord() {
            return "xyzzy";
        }
    }

    @Test
    public void plainClassWithRemotableInterfaceIsInvokedViaProxy() throws Exception {
        RemotableInterface proxy = protectMagicWord(new PlainClassWithRemotableInterface());
        assertJdkDynamicProxy(proxy);
        assertInvocationViaProxy(proxy);
    }

    @Test
    public void remotableClassWithPlainInterfaceIsInvokedViaProxy() throws Exception {
        PlainInterface proxy = protectMagicWord(new RemotableClassWithPlainInterface());
        assertJdkDynamicProxy(proxy);
        assertInvocationViaProxy(proxy);
    }

    @Test
    public void remotableClassIsInvokedViaProxy() throws Exception {
        RemotableClass proxy = protectMagicWord(new RemotableClass());
        assertCglibProxy(proxy);
        assertInvocationViaProxy(proxy);
    }

    @Test
    public void remotableClassWithRemotableInterfaceIsInvokedViaProxy() throws Exception {
        RemotableInterface proxy = protectMagicWord(new RemotableClassWithRemotableInterface());
        assertJdkDynamicProxy(proxy);
        assertInvocationViaProxy(proxy);
    }

    @Test(expected = InvalidAnnotationException.class)
    public void remotableClassWithEmptyInterfaceIsInvalid() throws Exception {
        EmptyInterface proxy = protectMagicWord(new RemotableClassWithEmptyInterface());
        assertJdkDynamicProxy(proxy);
        registry.scan(proxy);
    }

    /**
     * Returns a JDK dynamic proxy if the target class implements any interfaces,
     * or a CGLib proxy if it does not.
     * <p>
     * The proxy intercepts calls to any method named "getMagicWord".
     */
    @SuppressWarnings("unchecked")
    private <T> T protectMagicWord(Object target) {
        ProxyFactory factory = new ProxyFactory(target);
        factory.addAdvice((MethodInterceptor) invocation ->
                invocation.getMethod().getName().equals("getMagicWord")
                        ? "Not so fast, mortal!" : invocation.proceed());
        return (T) factory.getProxy();
    }

    private void assertInvocationViaProxy(Object proxy) throws IOException {
        registry.scan(proxy);
        check("grimoire.getMagicWord", "[]", "'Not so fast, mortal!'");
    }

    private void assertInvocationBypassesProxy(Object proxy) throws IOException {
        registry.scan(proxy);
        check("grimoire.getMagicWord", "[]", "'xyzzy'");
    }

    private static void assertJdkDynamicProxy(Object o) {
        assertTrue(Proxy.isProxyClass(o.getClass()));
        assertFalse(o instanceof Factory);
    }

    private static void assertCglibProxy(Object o) {
        assertFalse(Proxy.isProxyClass(o.getClass()));
        assertTrue(o instanceof Factory);
    }
}
