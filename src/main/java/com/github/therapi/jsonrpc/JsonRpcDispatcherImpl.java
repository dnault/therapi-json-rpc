package com.github.therapi.jsonrpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.therapi.core.MethodDefinition;
import com.github.therapi.core.MethodRegistry;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.github.therapi.core.internal.JacksonHelper.isLikeNull;
import static com.google.common.util.concurrent.MoreExecutors.newDirectExecutorService;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.indexOfAny;

public class JsonRpcDispatcherImpl implements JsonRpcDispatcher {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcDispatcherImpl.class);

    protected final MethodRegistry methodRegistry;
    protected final ExecutorService executorService;
    protected final ExceptionTranslator exceptionTranslator;
    protected final JsonRpcLogger jsonRpcLogger;

    public JsonRpcDispatcherImpl(MethodRegistry registry, ExceptionTranslator translator) {
        this(registry, translator, newDirectExecutorService(), new DefaultJsonRpcLogger());
    }

    public JsonRpcDispatcherImpl(MethodRegistry registry,
                                 ExceptionTranslator translator,
                                 ExecutorService executorService,
                                 JsonRpcLogger jsonRpcLogger) {
        this.methodRegistry = requireNonNull(registry);
        this.executorService = requireNonNull(executorService);
        this.exceptionTranslator = requireNonNull(translator);
        this.jsonRpcLogger = requireNonNull(jsonRpcLogger);
    }

    protected ObjectMapper getObjectMapper() {
        return methodRegistry.getObjectMapper();
    }

    protected JsonNode parseNode(String json) {
        try {
            return getObjectMapper().readTree(json);
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    protected JsonNode parseNode(InputStream json) {
        try {
            return getObjectMapper().readTree(json);
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    protected ArrayNode newArray() {
        return getObjectMapper().createArrayNode();
    }

    @Override
    public Optional<JsonNode> invoke(InputStream jsonRpcRequest) {
        try {
            JsonNode requestNode = parseNode(jsonRpcRequest);
            return invoke(requestNode);

        } catch (Throwable t) {
            jsonRpcLogger.logException(t);
            JsonRpcError jsonRpcError = exceptionTranslator.translate(t);
            return Optional.of(buildErrorResponse(jsonRpcError, null));
        }
    }

    protected Optional<JsonNode> invoke(JsonNode requestNode) {
        try {
            if (requestNode.isArray()) {
                ArrayNode batchResult = invokeBatch((ArrayNode) requestNode);
                return batchResult.size() == 0 ? Optional.empty() : Optional.of(batchResult);
            }
            if (requestNode.isObject()) {
                JsonNode soloResponse = invokeSolo(requestNode);
                if (isLikeNull(requestNode.get("id")) && isValidSoloRequest(requestNode)) {
                    log.debug("suppressing notification response because request had null 'id': {}", soloResponse);
                    return Optional.empty();
                }
                return Optional.of(soloResponse);
            }

            throw new InvalidRequestException("expected json-rpc request node to be ARRAY or OBJECT but found " + requestNode.getNodeType());

        } catch (Throwable t) {
            jsonRpcLogger.logException(t);

            JsonRpcError jsonRpcError = exceptionTranslator.translate(t);
            return Optional.of(buildErrorResponse(jsonRpcError, null));
        }
    }

    @Override
    public Optional<JsonNode> invoke(String jsonRpcRequest) {
        jsonRpcRequest = expandShorthand(jsonRpcRequest);

        try {
            JsonNode requestNode = parseNode(jsonRpcRequest);
            return invoke(requestNode);

        } catch (Throwable t) {
            jsonRpcLogger.logException(t);
            JsonRpcError jsonRpcError = exceptionTranslator.translate(t);
            return Optional.of(buildErrorResponse(jsonRpcError, null));
        }
    }

    /**
     * @param request a valid JSON RPC 2.0 request, or a string of the form
     *                {@code method[arg1, arg2, ...]} or {@code method{arg1Name:arg1, arg2Name,arg2, ...}}
     */
    protected String expandShorthand(String request) {
        if (request.startsWith("{") || request.startsWith("[")) {
            return request;
        }

        int paramStartIndex = indexOfAny(request, '{', '[');
        final String method;
        final String params;

        if (paramStartIndex == -1) {
            method = request.trim();
            params = "{}";
        } else {
            method = request.substring(0, paramStartIndex).trim();
            params = request.substring(paramStartIndex);
        }

        return "{"
                + "\"jsonrpc\":\"2.0\","
                + "\"id\":\"\","
                + "\"method\":\"" + method + "\","
                + "\"params\":" + params
                + "}";
    }

    protected boolean isValidSoloRequest(JsonNode soloRequest) {
        try {
            validateSoloRequest(soloRequest);
            return true;
        } catch (InvalidRequestException e) {
            return false;
        }
    }

    protected Request validateSoloRequest(JsonNode soloRequest) throws InvalidRequestException {
        if (!soloRequest.isObject()) {
            throw new InvalidRequestException("expected request node to be OBJECT but found " + soloRequest.getNodeType());
        }

        JsonNode methodName = soloRequest.get("method");
        if (isLikeNull(methodName)) {
            throw new InvalidRequestException("missing non-null 'method' field");
        }
        if (!methodName.isTextual()) {
            throw new InvalidRequestException("expected 'method' to be a STRING but found " + methodName.getNodeType());
        }

        JsonNode idNode = soloRequest.get("id");
        if (!isValidId(idNode)) {
            throw new InvalidRequestException("expected 'id' to be NULL or STRING or NUMBER but found " + idNode.getNodeType());
        }

        JsonNode versionNode = soloRequest.get("jsonrpc");
        if (versionNode != null) {
            if (!versionNode.isTextual()) {
                throw new InvalidRequestException("expected 'jsonrpc' to be a STRING but found " + versionNode.getNodeType());
            }

            if (!versionNode.asText().equals("2.0")) {
                throw new InvalidRequestException("expected 'jsonrpc' to be '2.0' but found '" + versionNode.asText() + "'");
            }
        }

        JsonNode params = soloRequest.get("params");
        if (isLikeNull(params)) {
            params = getObjectMapper().createObjectNode();
        } else {
            if (!params.isArray() && !params.isObject()) {
                throw new IllegalArgumentException("expected 'params' to be ARRAY or OBJECT but found " + params.getNodeType());
            }
        }

        return new Request(methodName.asText(), idNode, params);
    }

    protected static class Request {
        private final String methodName;
        private final JsonNode id;
        private final JsonNode params;

        public Request(String methodName, JsonNode id, JsonNode params) {
            this.methodName = methodName;
            this.id = id;
            this.params = params;
        }

        public String getMethodName() {
            return methodName;
        }

        public JsonNode getId() {
            return id;
        }

        public JsonNode getParams() {
            return params;
        }
    }

    protected ObjectNode invokeSolo(Request validRequest) {
        final Stopwatch timer = Stopwatch.createStarted();
        final String methodName = validRequest.getMethodName();
        final JsonNode arguments = validRequest.getParams();
        final MethodDefinition methodDef = methodRegistry.getMethod(methodName).orElse(null);

        final JsonRpcLogger.RequestInfo requestInfo = newRequestInfo(methodName, methodDef, arguments);

        try {
            jsonRpcLogger.logRequest(requestInfo);

            JsonNode result = methodRegistry.invoke(methodName, arguments);

            Map<String, Object> resultMap = new LinkedHashMap<>();
            resultMap.put("jsonrpc", "2.0");
            resultMap.put("id", validRequest.getId());
            resultMap.put("result", result);

            ObjectNode response = getObjectMapper().convertValue(resultMap, ObjectNode.class);
            jsonRpcLogger.logSuccessResponse(requestInfo, newResponseInfo(response, timer));
            return response;
        } catch (Throwable t) {
            jsonRpcLogger.logException(t);

            JsonRpcError jsonRpcError = exceptionTranslator.translate(t);
            ObjectNode response = buildErrorResponse(jsonRpcError, validRequest.id);
            jsonRpcLogger.logErrorResponse(requestInfo, newResponseInfo(response, timer));
            return response;
        }
    }

    private static JsonRpcLogger.RequestInfo newRequestInfo(String methodName,
                                                            @Nullable MethodDefinition methodDef,
                                                            JsonNode arguments) {
        return new JsonRpcLogger.RequestInfo() {
            @Override
            public String getMethodName() {
                return methodName;
            }

            @Override
            public Optional<MethodDefinition> getMethodDefinition() {
                return Optional.ofNullable(methodDef);
            }

            @Override
            public JsonNode getArguments() {
                return arguments;
            }
        };
    }

    private static JsonRpcLogger.ResponseInfo newResponseInfo(ObjectNode response, Stopwatch timer) {
        return new JsonRpcLogger.ResponseInfo() {
            @Override
            public ObjectNode getResponse() {
                return response;
            }

            @Override
            public Stopwatch getExecutionTimer() {
                return timer;
            }
        };
    }

    protected ObjectNode invokeSolo(JsonNode soloRequest) {
        try {
            return invokeSolo(validateSoloRequest(soloRequest));
        } catch (Throwable t) {
            jsonRpcLogger.logException(t);

            JsonRpcError jsonRpcError = exceptionTranslator.translate(t);
            return buildErrorResponse(jsonRpcError, soloRequest.get("id"));
        }
    }

    protected boolean isValidId(@Nullable JsonNode id) {
        return isLikeNull(id) || id.isNumber() || id.isTextual();
    }

    protected ObjectNode buildErrorResponse(JsonRpcError jsonRpcError, @Nullable JsonNode id) {
        if (!isValidId(id)) {
            id = null;
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("error", jsonRpcError);
        return getObjectMapper().convertValue(response, ObjectNode.class);
    }

    protected ArrayNode invokeBatch(ArrayNode batchRequest) {
        if (batchRequest.size() == 0) {
            throw new InvalidRequestException("batch must not be empty");
        }

        List<Future<JsonNode>> futureResults = new ArrayList<>();
        for (JsonNode soloRequest : batchRequest) {

            boolean isNotification = isLikeNull(soloRequest.get("id")) && isValidSoloRequest(soloRequest);
            Future<JsonNode> future = executorService.submit(() -> invokeSolo(soloRequest));

            if (!isNotification) {
                futureResults.add(future);
            }
        }

        ArrayNode response = newArray();
        for (Future<JsonNode> future : futureResults) {
            try {
                response.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new InternalErrorException(e);
            }
        }

        // xxx if the response is empty (batch was all notifications) the server is
        // supposed to return nothing -- NOT an empty array
        return response;
    }
}
