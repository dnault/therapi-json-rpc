package com.github.therapi.jsonrpc;

public interface ErrorCodes {
    int PARSE_ERROR = -32700; // Invalid JSON was received by the server. An error occurred on the server while parsing the JSON text.
    int INVALID_REQUEST = -32600; // The JSON sent is not a valid Request object.
    int METHOD_NOT_FOUND = -32601; // The method does not exist / is not available.
    int INVALID_PARAMS = -32602; // Invalid method parameter(s).
    int INTERNAL_ERROR = -32603; // Internal JSON-RPC error.
}
