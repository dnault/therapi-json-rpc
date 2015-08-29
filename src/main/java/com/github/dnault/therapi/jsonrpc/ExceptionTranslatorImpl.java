package com.github.dnault.therapi.jsonrpc;

import com.github.dnault.therapi.core.MethodNotFoundException;
import com.github.dnault.therapi.core.ParameterBindingException;

import static java.util.Collections.singletonMap;

public class ExceptionTranslatorImpl implements ExceptionTranslator {
    @Override
    public JsonRpcError translate(Throwable t) {
        if (t instanceof InvalidRequestException) {
            JsonRpcError e = new JsonRpcError(ErrorCodes.INVALID_REQUEST, "Invalid request");
            e.setData(singletonMap("detail", t.getMessage()));
            return e;
        }

        if (t instanceof ParseException) {
            JsonRpcError e = new JsonRpcError(ErrorCodes.PARSE_ERROR, "Parse error");
            e.setData(singletonMap("detail", t.getMessage()));
            return e;
        }

        if (t instanceof MethodNotFoundException) {
            JsonRpcError e = new JsonRpcError(ErrorCodes.METHOD_NOT_FOUND, "Method '" + ((MethodNotFoundException) t).getMethod() + "' not found");
            e.setData(singletonMap("suggestions", ((MethodNotFoundException) t).getSuggestions()));
            return e;
        }

        if (t instanceof InternalErrorException) {
            JsonRpcError e = new JsonRpcError(ErrorCodes.INTERNAL_ERROR, "Internal error");
            e.setData(singletonMap("detail", t.getMessage()));
            return e;
        }

        if (t instanceof ParameterBindingException) {
            JsonRpcError e = new JsonRpcError(ErrorCodes.INVALID_PARAMS, "Invalid params");
            e.setData(singletonMap("detail", t.getMessage()));
            return e;
        }

        return translateCustom(t);
    }

    /**
     * Subclasses may override to implement custom translation schemes.
     */
    protected JsonRpcError translateCustom(Throwable t) {
        return new JsonRpcError(ErrorCodes.INTERNAL_ERROR, "Internal error");
    }
}
