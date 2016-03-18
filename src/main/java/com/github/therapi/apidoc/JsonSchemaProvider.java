package com.github.therapi.apidoc;

import static com.google.common.base.Throwables.propagate;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.github.therapi.core.internal.MethodDefinition;
import com.github.therapi.core.internal.ParameterDefinition;

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

    private JsonNode getParamSchema(ObjectMapper objectMapper, ParameterDefinition paramDef) throws JsonMappingException {
        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        objectMapper.acceptJsonFormatVisitor(objectMapper.constructType(paramDef.getType().getType()), visitor);
        JsonSchema jsonSchema = visitor.finalSchema();
        return objectMapper.convertValue(jsonSchema, JsonNode.class);
    }

    public Optional<String> getSchema(ObjectMapper objectMapper, Class modelClass) throws IOException {
        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        visitor.setVisitorContext(new VisitorContextWithoutSchemaInlining());

        objectMapper.acceptJsonFormatVisitor(objectMapper.constructType(modelClass), visitor);
        JsonSchema jsonSchema = visitor.finalSchema();

        JsonNode schemaNode = objectMapper.convertValue(jsonSchema, JsonNode.class);
        if (schemaNode.equals(objectMapper.readTree("{\"type\":\"any\"}"))) {
            return Optional.empty();
        }

        return Optional.of(
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaNode));
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
}