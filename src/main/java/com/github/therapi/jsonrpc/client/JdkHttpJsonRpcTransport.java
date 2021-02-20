package com.github.therapi.jsonrpc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class JdkHttpJsonRpcTransport implements JsonRpcTransport {

    private final URL endpoint;
    private int connectionTimeoutMillis;
    private int readTimeoutMillis;

    public JdkHttpJsonRpcTransport(String endpoint) throws MalformedURLException {
        this.endpoint = new URL(endpoint);
        setConnectionTimeout(30, SECONDS);
        setReadTimeout(30, SECONDS);
    }

    public void setConnectionTimeout(long duration, TimeUnit unit) {
        this.connectionTimeoutMillis = (int) unit.toMillis(duration);
    }

    public void setReadTimeout(long duration, TimeUnit unit) {
        this.readTimeoutMillis = (int) unit.toMillis(duration);
    }

    @Override public JsonNode execute(ObjectMapper objectMapper, Object jsonRpcRequest) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
        try {
            connection.setConnectTimeout(connectionTimeoutMillis);
            connection.setReadTimeout(readTimeoutMillis);
            connection.setDoOutput(true); // Triggers POST.
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream output = connection.getOutputStream()) {
                objectMapper.writeValue(output, jsonRpcRequest);
            }

            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                throw new IOException("Server returned HTTP status code " + statusCode);
            }

            try (InputStream response = connection.getInputStream()) {
                return objectMapper.readTree(response);
            }
        } finally {
            connection.disconnect();
        }
    }
}
