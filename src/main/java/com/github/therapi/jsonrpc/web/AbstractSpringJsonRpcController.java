package com.github.therapi.jsonrpc.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.therapi.apidoc.ApiDocProvider;
import com.github.therapi.apidoc.ApiDocWriter;
import com.github.therapi.apidoc.ApiModelDoc;
import com.github.therapi.apidoc.ModelDocWriter;
import com.github.therapi.core.MethodRegistry;
import com.github.therapi.core.annotation.Remotable;
import com.github.therapi.jsonrpc.DefaultExceptionTranslator;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Base class for a Spring controller that acts as a JSON-RPC 2.0 endpoint and
 * automatically scans the Spring application context for remotable services.
 * Subclasses should be annotated with {@code @Controller} and must override the
 * abstract {@link #getObjectMapper} method.
 * <p>To override the default request mapping of '/jsonrpc',
 * annotate the subclass with @RequestMapping("/myCustomPath").
 * <p>If you wish to use this base class, make sure the "spring-web" JAR and its dependencies
 * are on the classpath.
 */
@RequestMapping("/jsonrpc")
public abstract class AbstractSpringJsonRpcController implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger log = LoggerFactory.getLogger(AbstractSpringJsonRpcController.class);

    protected JsonRpcServletHandler handler;

    // By listening for context refresh events, bean factory scanning can be deferred
    // until *after* any BeanPostProcessors have executed. This is important because
    // we want to register any generated proxies (not the proxy targets) so that
    // AOP interceptors are invoked when this controller executes methods.
    public void onApplicationEvent(ContextRefreshedEvent event) {
        MethodRegistry registry = newMethodRegistry();
        registerRemotableBeans(registry, event.getApplicationContext());
        postProcessRegistry(registry);
        handler = new JsonRpcServletHandler(registry, new DefaultExceptionTranslator());
    }

    @RequestMapping(method = RequestMethod.GET)
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handler.handleGet(req, resp);
    }

    @RequestMapping(method = RequestMethod.POST)
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handler.handlePost(req, resp);
    }

    @RequestMapping(path = "/apidoc", method = RequestMethod.GET)
    public void sendApiDoc(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        ApiDocProvider provider = new ApiDocProvider();

        resp.setContentType("text/html;charset=UTF-8");
        ApiDocWriter.writeTo(provider.getDocumentation(handler.getRegistry()), resp.getWriter());
    }

    @RequestMapping(path = "/modeldoc/{modelClassName:.+}", method = RequestMethod.GET)
    public void sendModelDoc(HttpServletRequest req, HttpServletResponse resp, @PathVariable String modelClassName) throws IOException, ServletException {
        ApiDocProvider provider = new ApiDocProvider();
        ApiModelDoc modelDoc = provider.getModelDocumentation(handler.getRegistry(), modelClassName)
                .orElse(null);

        if (modelDoc == null) {
            resp.sendError(404, "Model class not found: " + modelClassName);
            return;
        }

        resp.setContentType("text/html;charset=UTF-8");
        ModelDocWriter.writeTo(modelDoc, resp.getWriter());
    }

    /**
     * @return the ObjectMapper to use when creating the MethodRegistry managed by this controller.
     */
    protected abstract ObjectMapper getObjectMapper();

    protected MethodRegistry newMethodRegistry() {
        return new MethodRegistry(getObjectMapper());
    }

    protected void postProcessRegistry(MethodRegistry registry) {
    }

    protected void registerRemotableBeans(MethodRegistry registry, ListableBeanFactory beanFactory) {
        log.info("Scanning bean factory for remotable services");

        Stopwatch timer = Stopwatch.createStarted();
        for (Map.Entry<String, ?> entry : beanFactory.getBeansWithAnnotation(Remotable.class).entrySet()) {
            String beanName = entry.getKey();
            Object remotableBean = entry.getValue();
            List<String> methodNames = registry.scan(remotableBean);
            log.info("Registering remotable service bean '{}'; found methods: {}", beanName, methodNames);

        }

        log.info("Scanned bean factory for remotable services in {}", timer);
    }
}
