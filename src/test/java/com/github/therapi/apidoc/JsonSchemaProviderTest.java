package com.github.therapi.apidoc;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class JsonSchemaProviderTest {

    private static class Zoo {
        public String name;
        public List<Animal> animals;
    }

    private static class Animal {
        public String species;
    }

    private static class NoPublicFields {
        private int x;
    }

    @Test
    public void testGetSchema() throws Exception {
        Optional<String> schema = new JsonSchemaProvider().getSchema(new ObjectMapper(), Zoo.class);
        String expected = "{\n" +
                "  \"type\" : \"object\",\n" +
                "  \"id\" : \"urn:jsonschema:com:github:therapi:apidoc:JsonSchemaProviderTest:Zoo\",\n" +
                "  \"properties\" : {\n" +
                "    \"name\" : {\n" +
                "      \"type\" : \"string\"\n" +
                "    },\n" +
                "    \"animals\" : {\n" +
                "      \"type\" : \"array\",\n" +
                "      \"items\" : {\n" +
                "        \"$ref\" : \"urn:jsonschema:com:github:therapi:apidoc:JsonSchemaProviderTest:Animal\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        assertJsonEquals(expected, schema.get());
    }

    @Test
    public void noSchema() throws Exception {
        assertFalse(new JsonSchemaProvider().getSchema(new ObjectMapper(), NoPublicFields.class).isPresent());
    }
}