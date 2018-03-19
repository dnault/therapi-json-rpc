package com.github.therapi.core;

import org.springframework.aop.framework.AopProxyUtils;

/**
 * Inspector for proxies created by the Spring AOP Framework.
 */
public class SpringAopProxyIntrospector implements ProxyIntrospector {
    @Override
    public Class<?> getProxyTargetClass(Object object) {
        return AopProxyUtils.ultimateTargetClass(object);
    }
}
