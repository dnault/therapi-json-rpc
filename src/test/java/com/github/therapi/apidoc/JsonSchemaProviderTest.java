package com.github.therapi.apidoc;

import static com.github.therapi.apidoc.JsonSchemaProvider.classNameToHyperlink;
import static com.github.therapi.jackson.ObjectMappers.newLenientObjectMapper;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.therapi.core.MethodDefinition;
import com.github.therapi.core.ParameterDefinition;
import com.github.therapi.core.StandardParameterIntrospector;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Test;

public class JsonSchemaProviderTest {

    private enum Color {
        RED, GREEN, BLUE
    }

    private interface Zoo {
        String getName();

        List<Animal> getAnimals();
    }

    private interface Animal {
        String getSpecies();

        Color getColor();
    }

    private static class NoPublicFields {
        private int x;
    }

    @SuppressWarnings("unused")
    public void exampleMethod(String string,
                              boolean primitiveBoolean,
                              Boolean boxedBoolean,
                              int primitiveInt,
                              Integer boxedInt,
                              long primitiveLong,
                              Long boxedLong,
                              float primitiveFloat,
                              Float boxedFloat,
                              double primitiveDouble,
                              Double boxedDouble,
                              String[] stringArray,
                              List<String> stringList,
                              Zoo zoo) {
    }

    @Test
    public void testGetMethodSchema() throws Exception {
        ObjectMapper objectMapper = newLenientObjectMapper();

        Method method = Arrays.stream(getClass().getMethods())
                .filter(m -> m.getName().equals("exampleMethod"))
                .findAny().get();

        List<ParameterDefinition> paramDefs = new StandardParameterIntrospector(objectMapper).findParameters(method, this);
        MethodDefinition methodDef = new MethodDefinition("test", "exampleMethod", method, this, paramDefs);
        String result = new JsonSchemaProvider().getSchema(objectMapper, methodDef);

        String expected = "{\n" +
                "  \"type\" : \"object\",\n" +
                "  \"id\" : \"urn:jsonschema:com:github:therapi:method:test.exampleMethod\",\n" +
                "  \"properties\" : {\n" +
                "    \"string\" : {\n" +
                "      \"type\" : \"string\"\n" +
                "    },\n" +
                "    \"primitiveBoolean\" : {\n" +
                "      \"type\" : \"boolean\"\n" +
                "    },\n" +
                "    \"boxedBoolean\" : {\n" +
                "      \"type\" : \"boolean\"\n" +
                "    },\n" +
                "    \"primitiveInt\" : {\n" +
                "      \"type\" : \"integer\"\n" +
                "    },\n" +
                "    \"boxedInt\" : {\n" +
                "      \"type\" : \"integer\"\n" +
                "    },\n" +
                "    \"primitiveLong\" : {\n" +
                "      \"type\" : \"integer\"\n" +
                "    },\n" +
                "    \"boxedLong\" : {\n" +
                "      \"type\" : \"integer\"\n" +
                "    },\n" +
                "    \"primitiveFloat\" : {\n" +
                "      \"type\" : \"number\"\n" +
                "    },\n" +
                "    \"boxedFloat\" : {\n" +
                "      \"type\" : \"number\"\n" +
                "    },\n" +
                "    \"primitiveDouble\" : {\n" +
                "      \"type\" : \"number\"\n" +
                "    },\n" +
                "    \"boxedDouble\" : {\n" +
                "      \"type\" : \"number\"\n" +
                "    },\n" +
                "    \"stringArray\" : {\n" +
                "      \"type\" : \"array\",\n" +
                "      \"items\" : {\n" +
                "        \"type\" : \"string\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"stringList\" : {\n" +
                "      \"type\" : \"array\",\n" +
                "      \"items\" : {\n" +
                "        \"type\" : \"string\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"zoo\" : {\n" +
                "      \"type\" : \"object\",\n" +
                "      \"id\" : \"urn:jsonschema:com:github:therapi:apidoc:JsonSchemaProviderTest:Zoo\",\n" +
                "      \"properties\" : {\n" +
                "        \"name\" : {\n" +
                "          \"type\" : \"string\"\n" +
                "        },\n" +
                "        \"animals\" : {\n" +
                "          \"type\" : \"array\",\n" +
                "          \"items\" : {\n" +
                "            \"type\" : \"object\",\n" +
                "            \"id\" : \"urn:jsonschema:com:github:therapi:apidoc:JsonSchemaProviderTest:Animal\",\n" +
                "            \"properties\" : {\n" +
                "              \"color\" : {\n" +
                "                \"type\" : \"string\",\n" +
                "                \"enum\" : [ \"red\", \"green\", \"blue\" ]\n" +
                "              },\n" +
                "              \"species\" : {\n" +
                "                \"type\" : \"string\"\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        assertJsonEquals(expected, result);
    }

    @Test
    public void testGetSchema() throws Exception {
        Optional<String> schema = new JsonSchemaProvider().getSchemaForHtml(new ObjectMapper(), Zoo.class, className -> "LINK:" + className);
        String expected = "{\n" +
                "  \"type\" : \"object\",\n" +
                "  \"id\" : \"com.github.therapi.apidoc.JsonSchemaProviderTest$Zoo\",\n" +
                "  \"properties\" : {\n" +
                "    \"name\" : {\n" +
                "      \"type\" : \"string\"\n" +
                "    },\n" +
                "    \"animals\" : {\n" +
                "      \"type\" : \"array\",\n" +
                "      \"items\" : {\n" +
                "        \"type\" : \"object\",\n" +
                "        \"$ref\" : \"LINK:com.github.therapi.apidoc.JsonSchemaProviderTest$Animal\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String actual = StringEscapeUtils.unescapeHtml3(schema.get());
        assertJsonEquals(expected, actual);
    }

    @Test
    public void noSchema() throws Exception {
        assertFalse(new JsonSchemaProvider()
                .getSchemaForHtml(new ObjectMapper(), NoPublicFields.class, classNameToHyperlink())
                .isPresent());
    }
}