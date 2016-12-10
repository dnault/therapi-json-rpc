package com.github.therapi.apidoc;

import static com.google.common.base.Throwables.propagate;
import static java.util.regex.Matcher.quoteReplacement;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.github.therapi.core.MethodDefinition;
import com.github.therapi.core.ParameterDefinition;
import com.github.therapi.core.internal.LangHelper;
import org.apache.commons.lang3.StringEscapeUtils;

public class JsonSchemaProvider {

    public String getSchema(ObjectMapper objectMapper, MethodDefinition methodDef) {
        try {
            ObjectNode methodNode = objectMapper.createObjectNode();
            methodNode.put("type", "object");
            methodNode.put("id", "urn:jsonschema:com:github:therapi:method:" + methodDef.getQualifiedName("."));

            Map<String, JsonNode> properties = new LinkedHashMap<>();
            methodNode.putPOJO("properties", properties);

            for (ParameterDefinition paramDef : methodDef.getParameters()) {
                properties.put(paramDef.getName(), getParamSchema(objectMapper, paramDef));
            }

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(methodNode);

        } catch (JsonProcessingException e) {
            throw propagate(e);
        }
    }

    public Optional<String> getSchemaForHtml(ObjectMapper objectMapper, Class modelClass, Function<String, String> classNameToHyperlink) throws IOException {
        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        visitor.setVisitorContext(new VisitorContextWithoutSchemaInlining() {
            public String javaTypeToUrn(JavaType jt) {
                return jt.toCanonical();
            }
        });

        objectMapper.acceptJsonFormatVisitor(objectMapper.constructType(modelClass), visitor);
        JsonSchema jsonSchema = visitor.finalSchema();

        JsonNode schemaNode = objectMapper.convertValue(jsonSchema, JsonNode.class);
        if (schemaNode.equals(objectMapper.readTree("{\"type\":\"any\"}"))) {
            return Optional.empty();
        }

        String result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaNode);
        result = StringEscapeUtils.escapeHtml3(result);
        result = activateRefLinks(result, classNameToHyperlink);

        return Optional.of(result);
    }

    private static final Pattern REF_PROPERTY_PATTERN = Pattern.compile(
            "(&quot;\\$ref&quot;\\s*:\\s*?&quot;)(.+?)(&quot;)");

    protected String activateRefLinks(String result, Function<String, String> classNameToHyperlink) {
        return LangHelper.replace(result, REF_PROPERTY_PATTERN, (Matcher m) ->
                quoteReplacement(m.group(1) + classNameToHyperlink.apply(m.group(2))) + m.group(3));
    }

    public static Function<String, String> classNameToHyperlink() {
        return className -> "<a href=\"" + className + "\">"
                + StringEscapeUtils.escapeHtml3(className)
                + "</a>";
    }

    /**
     * Treat all schemas as "seen" so that model schemas are never inlined.
     */
    public static class VisitorContextWithoutSchemaInlining extends VisitorContext {
        @Override
        public String addSeenSchemaUri(JavaType aSeenSchema) {
            return getSeenSchemaUri(aSeenSchema);
        }

        @Override
        public String getSeenSchemaUri(JavaType aSeenSchema) {
            return isModel(aSeenSchema) ? javaTypeToUrn(aSeenSchema) : null;
        }

        protected boolean isModel(JavaType type) {
            return type.getRawClass() != String.class
                    && !isBoxedPrimitive(type)
                    && !type.isPrimitive()
                    && !type.isMapLikeType()
                    && !type.isCollectionLikeType();
        }

        protected static boolean isBoxedPrimitive(JavaType type) {
            return type.getRawClass() == Boolean.class
                    || type.getRawClass() == Byte.class
                    || type.getRawClass() == Long.class
                    || type.getRawClass() == Integer.class
                    || type.getRawClass() == Short.class
                    || type.getRawClass() == Float.class
                    || type.getRawClass() == Double.class;
        }
    }

    private JsonNode getParamSchema(ObjectMapper objectMapper, ParameterDefinition paramDef) throws JsonMappingException {
        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        objectMapper.acceptJsonFormatVisitor(objectMapper.constructType(paramDef.getType().getType()), visitor);
        JsonSchema jsonSchema = visitor.finalSchema();
        return objectMapper.convertValue(jsonSchema, JsonNode.class);
    }
}