package com.github.dnault.therapi.jsonrpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.dnault.therapi.core.MethodRegistry;
import com.github.dnault.therapi.core.annotation.Remotable;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.dnault.therapi.core.internal.JacksonHelper.newLenientObjectMapper;
import static org.junit.Assert.assertEquals;

public class JsonRpcDispatcherImplTest {
    private JsonRpcDispatcherImpl dispatcher;

    @Before
    public void setup() {
        MethodRegistry registry = new MethodRegistry(newLenientObjectMapper());
        registry.scan(new ExampleServiceImpl());
        dispatcher = new JsonRpcDispatcherImpl(registry);
        dispatcher.setExceptionTranslator(new ExceptionTranslatorImpl().excludeDetails());
    }

    @Remotable("")
    private interface ExampleService {
        int subtract(int minuend, int subtrahend);

        int sum(int a, int b, int c);

        List<Object> get_data();

        void notify_hello(int x);
    }

    private static class ExampleServiceImpl implements ExampleService {
        @Override
        public int subtract(int minuend, int subtrahend) {
            return minuend - subtrahend;
        }

        @Override
        public int sum(int a, int b, int c) {
            return a + b + c;
        }

        @Override
        public List<Object> get_data() {
            return Arrays.asList("hello", 5);
        }

        @Override
        public void notify_hello(int x) {
        }
    }

    @Test
    public void rpc_call_with_positional_parameters() throws Exception {
        check("{jsonrpc: '2.0', method: 'subtract', params: [42, 23], id: 1}",
                "{jsonrpc: '2.0', result: 19, id: 1}");

        check("{jsonrpc: '2.0', method: 'subtract', params: [23, 42], id: 2}",
                "{jsonrpc: '2.0', result: -19, id: 2}");
    }

    @Test
    public void rpc_call_with_named_parameters() throws Exception {
        check("{jsonrpc: '2.0', method: 'subtract', params: {subtrahend: 23, minuend: 42}, id: 3}",
                "{jsonrpc: '2.0', result: 19, id: 3}");

        check("{jsonrpc: '2.0', method: 'subtract', params: {minuend: 42, subtrahend: 23}, id: 4}",
                "{jsonrpc: '2.0', result: 19, id: 4}");
    }

    @Test
    public void notification() throws Exception {
        check("{'jsonrpc': '2.0', 'method': 'update', 'params': [1,2,3,4,5]}",
                "");

        check("{'jsonrpc': '2.0', 'method': 'foobar'}",
                "");
    }

    @Test
    public void rpc_call_of_non_existent_method() throws Exception {
        check("{jsonrpc: '2.0', method: 'foobar', id: '1'}",
                "{jsonrpc: '2.0', id:'1', error: {code: -32601, message: 'Method \\'foobar\\' not found'}}");
    }

    @Test
    public void rpc_call_with_invalid_json() throws Exception {
        check("{'jsonrpc': '2.0', 'method': 'foobar, 'params': 'bar', 'baz]",
                "{jsonrpc: '2.0', error: {code: -32700, message: 'Parse error'}, id: null}");
    }

    @Test
    public void rpc_call_with_invalid_request_object() throws Exception {
        check("{jsonrpc: '2.0', method: 1, params: 'bar'}",
                "{jsonrpc: '2.0', error: {code: -32600, message: 'Invalid request'}, id: null}");
    }

    @Test
    public void rpc_call_batch_invalid_json() throws Exception {
        check("[\n" +
                        "  {'jsonrpc': '2.0', 'method': 'sum', 'params': [1,2,4], 'id': '1'},\n" +
                        "  {'jsonrpc': '2.0', 'method'\n" +
                        "]",
                "{jsonrpc: '2.0', error: {code: -32700, message: 'Parse error'}, id: null}");
    }

    @Test
    public void rpc_call_with_an_empty_array() throws Exception {
        check("[]",
                "{'jsonrpc': '2.0', 'error': {'code': -32600, 'message': 'Invalid request'}, 'id': null}");
    }

    @Test
    public void rpc_call_with_an_invalid_batch_but_not_empty() throws Exception {
        check("[1]",
                "[{'jsonrpc': '2.0', 'error': {'code': -32600, 'message': 'Invalid request'}, 'id': null}]");
    }

    @Test
    public void rpc_call_with_an_invalid_batch() throws Exception {
        check("[1,2,3]", "[" +
                "{'jsonrpc': '2.0', 'error': {'code': -32600, 'message': 'Invalid request'}, 'id': null}," +
                "{'jsonrpc': '2.0', 'error': {'code': -32600, 'message': 'Invalid request'}, 'id': null}," +
                "{'jsonrpc': '2.0', 'error': {'code': -32600, 'message': 'Invalid request'}, 'id': null}" +
                "]");
    }

    @Test
    public void rpc_call_batch() throws Exception {
        check("[\n" +
                        "        {'jsonrpc': '2.0', 'method': 'sum', 'params': [1,2,4], 'id': '1'},\n" +
                        "        {'jsonrpc': '2.0', 'method': 'notify_hello', 'params': [7]},\n" +
                        "        {'jsonrpc': '2.0', 'method': 'subtract', 'params': [42,23], 'id': '2'},\n" +
                        "        {'foo': 'boo'},\n" +
                        "        {'jsonrpc': '2.0', 'method': 'foo.get', 'params': {'name': 'myself'}, 'id': '5'},\n" +
                        "        {'jsonrpc': '2.0', 'method': 'get_data', 'id': '9'} \n" +
                        "    ]",
                "[\n" +
                        "        {'jsonrpc': '2.0', 'result': 7, 'id': '1'},\n" +
                        "        {'jsonrpc': '2.0', 'result': 19, 'id': '2'},\n" +
                        "        {'jsonrpc': '2.0', 'error': {'code': -32600, 'message': 'Invalid request'}, 'id': null},\n" +
                        "        {'jsonrpc': '2.0', 'error': {'code': -32601, 'message': 'Method \\'foo.get\\' not found'}, 'id': '5'},\n" +
                        "        {'jsonrpc': '2.0', 'result': ['hello', 5], 'id': '9'}\n" +
                        "    ]");
    }

    @Test
    public void rpc_call_batch_all_notifications() throws Exception {
        check("[\n" +
                        "        {'jsonrpc': '2.0', 'method': 'notify_sum', 'params': [1,2,4]},\n" +
                        "        {'jsonrpc': '2.0', 'method': 'notify_hello', 'params': [7]}\n" +
                        "    ]",
                "");
    }

    private void check(String jsonRpcRequest, String expectedResponse) throws IOException {
        Optional<JsonNode> actualResponse = dispatcher.invoke(jsonRpcRequest);
        if (expectedResponse.isEmpty()) {
            assertEquals(Optional.empty(), actualResponse);
        } else {
            assertJsonEquals(expectedResponse, actualResponse.get().toString());
        }
    }

    private void assertJsonEquals(String expected, String actual) throws IOException {
        JsonNode expectedNode = dispatcher.getObjectMapper().readTree(expected);
        JsonNode actualNode = dispatcher.getObjectMapper().readTree(actual);
        assertEquals(expectedNode, actualNode);
    }
}