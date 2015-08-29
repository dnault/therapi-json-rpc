package com.github.dnault.therapi.jsonrpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dnault.therapi.core.MethodRegistry;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.github.dnault.therapi.core.internal.JacksonHelper.isLikeNull;
import static java.util.Objects.requireNonNull;

public class JsonRpcDispatcher {
    private static final Logger log = LoggerFactory.getLogger(JsonRpcDispatcher.class);

    protected final MethodRegistry methodRegistry;
    protected final ExecutorService executorService;
    protected ExceptionTranslator exceptionTranslator = new ExceptionTranslatorImpl();

    public JsonRpcDispatcher(MethodRegistry methodRegistry) {
        this(methodRegistry, MoreExecutors.newDirectExecutorService());
    }

    public void setExceptionTranslator(ExceptionTranslator exceptionTranslator) {
        this.exceptionTranslator = requireNonNull(exceptionTranslator);
    }

    public JsonRpcDispatcher(MethodRegistry methodRegistry, ExecutorService executorService) {
        this.methodRegistry = requireNonNull(methodRegistry);
        this.executorService = requireNonNull(executorService);
    }

    protected ObjectMapper getObjectMapper() {
        return methodRegistry.getObjectMapper();
    }

    protected JsonNode parseNode(String s) {
        try {
            return getObjectMapper().readTree(s);
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    protected JsonNode parseNode(InputStream is) {
        try {
            return getObjectMapper().readTree(is);
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    protected ArrayNode newArray() {
        return getObjectMapper().createArrayNode();
    }

    public JsonNode invoke(InputStream jsonRpcRequest) {
        try {
            JsonNode requestNode = parseNode(jsonRpcRequest);
            return invoke(requestNode);

        } catch (Throwable t) {
            log.error("exception raised during json-rpc invocation", t);
            JsonRpcError jsonRpcError = exceptionTranslator.translate(t);
            return buildErrorResponse(jsonRpcError, null);
        }
    }

    public JsonNode invoke(JsonNode requestNode) {
        try {
            if (requestNode.isArray()) {
                return invokeBatch((ArrayNode) requestNode);
            }
            if (requestNode.isObject()) {
                return invokeSolo(requestNode);
            }

            throw new InvalidRequestException("expected json-rpc request node to be ARRAY or OBJECT but found " + requestNode.getNodeType());

        } catch (Throwable t) {
            log.error("exception raised during json-rpc invocation", t);

            JsonRpcError jsonRpcError = exceptionTranslator.translate(t);
            return buildErrorResponse(jsonRpcError, null);
        }
    }

    public JsonNode invoke(String jsonRpcRequest) {
        try {
            JsonNode requestNode = parseNode(jsonRpcRequest);
            return invoke(requestNode);

        } catch (Throwable t) {
            log.error("exception raised during json-rpc invocation", t);

            JsonRpcError jsonRpcError = exceptionTranslator.translate(t);
            return buildErrorResponse(jsonRpcError, null);
        }
    }

    public ObjectNode invokeSolo(JsonNode soloRequest) {
        try {
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

            JsonNode result = methodRegistry.invoke(methodName.asText(), params);

            Map<String, Object> resultMap = new LinkedHashMap<>();
            if (versionNode != null) {
                resultMap.put("jsonrpc", "2.0");
                resultMap.put("id", idNode);
                resultMap.put("result", result);
            }

            return getObjectMapper().convertValue(resultMap, ObjectNode.class);
        } catch (Throwable t) {
            log.error("exception raised during json-rpc invocation", t);

            JsonRpcError jsonRpcError = exceptionTranslator.translate(t);
            return buildErrorResponse(jsonRpcError, soloRequest.get("id"));
        }
    }

    private boolean isValidId(@Nullable JsonNode id) {
        return isLikeNull(id) || id.isNumber() || id.isTextual();
    }

    private ObjectNode buildErrorResponse(JsonRpcError jsonRpcError, @Nullable JsonNode id) {
        if (!isValidId(id)) {
            id = null;
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("error", jsonRpcError);
        return getObjectMapper().convertValue(response, ObjectNode.class);
    }

    public ArrayNode invokeBatch(ArrayNode batchRequest) {
        if (batchRequest.size() == 0) {
            throw new InvalidRequestException("batch must not be empty");
        }

        List<Future<JsonNode>> futureResults = new ArrayList<>();
        for (JsonNode soloRequest : batchRequest) {
            boolean isNotification = isLikeNull(soloRequest.get("id"));
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
