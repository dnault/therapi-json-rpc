package com.github.therapi.jackson.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LowerCamelCaseEnumModuleTest {
    private enum Color {
        RED,
        BRIGHT_GREEN,
        yellow
    }

    @Test
    public void testEnumSerialization() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new LowerCamelCaseEnumModule());

        assertEquals("\"red\"", objectMapper.writeValueAsString(Color.RED));
        assertEquals("\"brightGreen\"", objectMapper.writeValueAsString(Color.BRIGHT_GREEN));
        assertEquals("\"yellow\"", objectMapper.writeValueAsString(Color.yellow));
    }

    @Test
    public void testEnumDeserialization() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new LowerCamelCaseEnumModule());

        assertEquals(Color.RED, objectMapper.readValue("\"red\"", Color.class));
        assertEquals(Color.BRIGHT_GREEN, objectMapper.readValue("\"brightGreen\"", Color.class));
        assertEquals(Color.yellow, objectMapper.readValue("\"yellow\"", Color.class));
    }
}