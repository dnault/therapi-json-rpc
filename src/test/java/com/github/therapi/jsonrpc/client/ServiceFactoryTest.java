package com.github.therapi.jsonrpc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.therapi.core.MethodRegistry;
import com.github.therapi.core.annotation.Remotable;
import com.github.therapi.jsonrpc.JsonRpcDispatcher;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.therapi.jackson.ObjectMappers.newLenientObjectMapper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServiceFactoryTest {

    private final ObjectMapper objectMapper = newLenientObjectMapper();

    private final MethodRegistry registry = new MethodRegistry();

    {
        registry.scan(new TestServiceImpl());
    }

    private final JsonRpcDispatcher dispatcher = JsonRpcDispatcher.builder(registry).build();

    private final JsonRpcTransport transport = (objectMapper, jsonRpcRequest) -> {
        String request = objectMapper.writeValueAsString(jsonRpcRequest);
        return dispatcher.invoke(request).orElse(null);
    };

    private final ServiceFactory factory = new ServiceFactory(objectMapper, transport);

    private final TestService service = factory.createService(TestService.class);

    @Remotable
    private interface TestService {
        List<String> zeroArgs();

        void returnsVoid();

        List<String> echo(List<String> args);
    }

    private static class TestServiceImpl implements TestService {
        @Override
        public List<String> zeroArgs() {
            return Arrays.asList("hello", "world");
        }

        @Override
        public void returnsVoid() {
        }

        @Override
        public List<String> echo(List<String> args) {
            return args;
        }
    }

    @Test
    public void canCallZeroArgMethod() throws Exception {
        assertEquals(ImmutableList.of("hello", "world"), service.zeroArgs());
    }

    @Test
    public void canCallVoidMethod() throws Exception {
        AtomicBoolean invokedMethod = new AtomicBoolean();

        JsonRpcTransport client = (objectMapper1, jsonRpcRequest) -> {
            invokedMethod.set(true);
            return objectMapper1.readValue("{'result': {}}", JsonNode.class);
        };

        ServiceFactory factory = new ServiceFactory(objectMapper, client);
        TestService service = factory.createService(TestService.class);

        service.returnsVoid();
        assertTrue(invokedMethod.get());
    }

    @Test
    public void canEchoParams() throws Exception {
        assertEquals(ImmutableList.of("a", "b"), service.echo(ImmutableList.of("a", "b")));
    }

    @Test
    public void methodNameAndNamespaceAreCorrect() throws Exception {
        JsonRpcTransport client = (objectMapper1, jsonRpcRequest) -> {
            ObjectNode request = objectMapper1.convertValue(jsonRpcRequest, ObjectNode.class);
            assertEquals("test.zeroArgs", request.path("method").textValue());
            ObjectNode response = objectMapper1.createObjectNode();
            response.set("result", NullNode.getInstance()); // whatever
            return response;
        };

        ServiceFactory factory = new ServiceFactory(objectMapper, client);
        TestService service = factory.createService(TestService.class);

        service.zeroArgs();
    }

    @Test
    public void canEchoParamsWithNamedArguments() throws Exception {
        JsonRpcTransport client = (objectMapper1, jsonRpcRequest) -> {
            ObjectNode response = objectMapper1.createObjectNode();
            ObjectNode requestNode = objectMapper1.convertValue(jsonRpcRequest, ObjectNode.class);
            response.set("result", requestNode.get("params").get("args"));
            return response;
        };

        ServiceFactory factory = new ServiceFactory(objectMapper, client);
        factory.setUseNamedArguments(true);
        TestService service = factory.createService(TestService.class);

        assertEquals(ImmutableList.of("a", "b"), service.echo(ImmutableList.of("a", "b")));
    }
}