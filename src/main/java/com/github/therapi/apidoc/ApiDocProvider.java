package com.github.therapi.apidoc;

import static com.github.therapi.apidoc.JsonSchemaProvider.classNameToHyperlink;
import static com.github.therapi.core.internal.LangHelper.index;
import static com.github.therapi.core.internal.TypesHelper.getClassNames;
import static com.github.therapi.core.internal.TypesHelper.getSimpleName;
import static com.github.therapi.core.internal.LangHelper.propagate;
import static com.google.common.html.HtmlEscapers.htmlEscaper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.therapi.core.MethodDefinition;
import com.github.therapi.core.MethodRegistry;
import com.github.therapi.core.ParameterDefinition;
import com.github.therapi.core.internal.TypesHelper;
import com.github.therapi.runtimejavadoc.ClassJavadoc;
import com.github.therapi.runtimejavadoc.Comment;
import com.github.therapi.runtimejavadoc.CommentFormatter;
import com.github.therapi.runtimejavadoc.MethodJavadoc;
import com.github.therapi.runtimejavadoc.ParamJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

public class ApiDocProvider {
    private final CommentFormatter commentFormatter = new CommentFormatter();
    private final JsonSchemaProvider schemaProvider = new JsonSchemaProvider();

    public List<ApiNamespaceDoc> getDocumentation(MethodRegistry registry) throws IOException {
        final ObjectWriter prettyWriter = registry.getObjectMapper().writerWithDefaultPrettyPrinter();

        final List<ApiNamespaceDoc> namespaces = new ArrayList<>();

        final SortedSetMultimap<String, MethodDefinition> methodDefinitionsByNamespace = TreeMultimap.create(
                Comparator.<String>naturalOrder(), new Comparator<MethodDefinition>() {
                    @Override
                    public int compare(MethodDefinition o1, MethodDefinition o2) {
                        return o1.getUnqualifiedName().compareTo(o2.getUnqualifiedName());
                    }
                }
        );

        for (MethodDefinition mdef : registry.getMethods()) {
            methodDefinitionsByNamespace.put(mdef.getNamespace().orElse(""), mdef);
        }

        for (String namespaceName : methodDefinitionsByNamespace.keySet()) {
            final ApiNamespaceDoc nsDoc = new ApiNamespaceDoc();
            nsDoc.setName(namespaceName);

            final List<ApiMethodDoc> methods = new ArrayList<>();
            for (MethodDefinition mdef : methodDefinitionsByNamespace.get(namespaceName)) {

                final ApiMethodDoc mdoc = new ApiMethodDoc();
                mdoc.setName(mdef.getUnqualifiedName());
                mdoc.setReturnType(toJsonType(mdef.getReturnTypeRef()));
                mdoc.setRequestSchema(schemaProvider.getSchema(registry.getObjectMapper(), mdef));

                final Optional<MethodJavadoc> methodJavadocOptional = getJavadoc(mdef);
                final Map<String, ParamJavadoc> javadocsByParamName = methodJavadocOptional.isPresent()
                        ? index(methodJavadocOptional.get().getParams(), ParamJavadoc::getName)
                        : ImmutableMap.<String, ParamJavadoc>of();

                if (methodJavadocOptional.isPresent()) {
                    mdoc.setDescription(render(methodJavadocOptional.get().getComment()));
                    mdoc.setReturns(render(methodJavadocOptional.get().getReturns()));
                } else {
                    mdoc.setDescription("");
                    mdoc.setReturns("");
                }

                final List<ApiParamDoc> paramDocs = new ArrayList<>();
                for (ParameterDefinition pdef : mdef.getParameters()) {
                    final ApiParamDoc pdoc = new ApiParamDoc();
                    pdoc.setName(pdef.getName());
                    pdoc.setType(toJsonType(pdef.getType()));

                    Optional<Supplier<?>> defaultSupplier = pdef.getDefaultValueSupplier();
                    if (defaultSupplier.isPresent()) {
                        pdoc.setDefaultValue(prettyWriter.writeValueAsString(defaultSupplier.get().get()));
                    }

                    ParamJavadoc paramJavadoc = javadocsByParamName.get(pdef.getName());
                    if (paramJavadoc != null) {
                        pdoc.setDescription(render(paramJavadoc.getComment()));
                    }
                    paramDocs.add(pdoc);
                }
                mdoc.setParams(paramDocs);
                methods.add(mdoc);

            }

            nsDoc.setMethods(methods);
            namespaces.add(nsDoc);
        }

        return namespaces;
    }

    protected String toJsonType(TypeReference typeRef) {
        return TypesHelper.toJsonType(typeRef.getType());
    }

    public Optional<MethodJavadoc> getJavadoc(MethodDefinition m) throws IOException {
        ClassJavadoc classJavadoc = RuntimeJavadoc.getJavadoc(m.getMethod().getDeclaringClass().getName()).orElse(null);
        if (classJavadoc == null) {
            return Optional.empty();
        }

        for (MethodJavadoc methodJavadoc : classJavadoc.getMethods()) {
            if (methodJavadoc.getName().equals(m.getMethod().getName())) {
                return Optional.of(methodJavadoc);
            }

        }

        return Optional.empty();
    }

    public Optional<ApiMethodDoc> getMethodDoc(MethodDefinition m) throws IOException {
        ClassJavadoc classJavadoc = RuntimeJavadoc.getJavadoc(m.getMethod().getDeclaringClass().getName()).orElse(null);
        if (classJavadoc == null) {
            return Optional.empty();
        }

        for (MethodJavadoc methodJavadoc : classJavadoc.getMethods()) {
            if (methodJavadoc.getName().equals(m.getMethod().getName())) {
                ApiMethodDoc doc = new ApiMethodDoc();
                doc.setName(m.getQualifiedName("."));
                doc.setDescription(render(methodJavadoc.getComment()));

                return Optional.of(doc);
            }

        }
        return Optional.empty();
    }

    protected String render(Comment comment) {
        return commentFormatter.format(comment);
    }

    public static String activateModelLinks(String typeName) {
        String result = htmlEscaper().escape(typeName);
        for (String className : getClassNames(typeName)) {
            String link = "<a href=\"modeldoc/" + className + "\">" + getSimpleName(className) + "</a>";
            result = result.replace(className, link);
        }

        return result;
    }

    public Optional<ApiModelDoc> getModelDocumentation(MethodRegistry registry, String modelClassName) throws IOException {
        Class modelClass = TypesHelper.findClass(modelClassName).orElse(null);

        if (modelClass == null) {
            return Optional.empty();
        }

        String commentHtml = null;
        ClassJavadoc classDoc = RuntimeJavadoc.getJavadoc(modelClassName).orElse(null);
        if (classDoc != null && classDoc.getComment() != null) {
            commentHtml = commentFormatter.format(classDoc.getComment());
        }

        String schemaHtml = new JsonSchemaProvider()
                .getSchemaForHtml(registry.getObjectMapper(), modelClass, classNameToHyperlink())
                .orElse(null);

        List<ApiExampleModelDoc> examples = getExamples(registry, modelClass);

        return Optional.of(new ApiModelDoc(modelClass.getSimpleName(), modelClass.getName(),
                commentHtml, schemaHtml, examples));
    }

    public List<ApiExampleModelDoc> getExamples(MethodRegistry registry, Class modelClass) {
        List<ApiExampleModelDoc> results = new ArrayList<>();

        for (Method factoryMethod : registry.getExampleFactoryMethods(modelClass)) {
            try {
                Object example = factoryMethod.invoke(null);

                MethodJavadoc methodDoc = RuntimeJavadoc.getJavadoc(factoryMethod).orElse(null);
                String commentHtml = methodDoc == null ? null
                        : commentFormatter.format(methodDoc.getComment());

                String exampleJson = registry.getObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(example);

                results.add(new ApiExampleModelDoc(commentHtml, exampleJson));

            } catch (IllegalAccessException | InvocationTargetException | JsonProcessingException e) {
                throw propagate(e);
            }
        }

        return results;
    }
}
