package com.github.therapi.jsonrpc.client;

import static com.github.therapi.jackson.ObjectMappers.newLenientObjectMapper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.therapi.core.annotation.Remotable;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class ServiceFactoryTest {

    private final ObjectMapper objectMapper = newLenientObjectMapper();

    @Remotable("test")
    private interface TestService {
        List<String> zeroArgs();

        void returnsVoid();

        List<String> echo(List<String> args);
    }

    @Test
    public void canCallZeroArgMethod() throws Exception {
        JsonRpcHttpClient client = (objectMapper1, jsonRpcRequest) ->
                objectMapper1.readValue("{'result': ['hello', 'world']}", JsonNode.class);

        ServiceFactory factory = new ServiceFactory(objectMapper, client);
        TestService service = factory.createService(TestService.class);

        assertEquals(ImmutableList.of("hello", "world"), service.zeroArgs());
    }

    @Test
    public void canCallVoidMethod() throws Exception {
        AtomicBoolean invokedMethod = new AtomicBoolean();

        JsonRpcHttpClient client = (objectMapper1, jsonRpcRequest) -> {
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

        JsonRpcHttpClient client = (objectMapper1, jsonRpcRequest) -> {
            ObjectNode response = objectMapper1.createObjectNode();
            ObjectNode requestNode = objectMapper1.convertValue(jsonRpcRequest, ObjectNode.class);
            response.set("result", requestNode.get("params").get(0));
            return response;
        };

        ServiceFactory factory = new ServiceFactory(objectMapper, client);
        TestService service = factory.createService(TestService.class);

        assertEquals(ImmutableList.of("a", "b"), service.echo(ImmutableList.of("a", "b")));
    }
}