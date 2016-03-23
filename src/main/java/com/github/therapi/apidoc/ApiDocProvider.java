package com.github.therapi.apidoc;

import static com.github.therapi.core.internal.LangHelper.index;
import static com.github.therapi.core.internal.TypesHelper.getClassNames;
import static com.github.therapi.core.internal.TypesHelper.getSimpleName;
import static com.google.common.html.HtmlEscapers.htmlEscaper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.therapi.core.MethodRegistry;
import com.github.therapi.core.MethodDefinition;
import com.github.therapi.core.ParameterDefinition;
import com.github.therapi.core.internal.TypesHelper;
import com.github.therapi.runtimejavadoc.ClassJavadoc;
import com.github.therapi.runtimejavadoc.Comment;
import com.github.therapi.runtimejavadoc.CommentFormatter;
import com.github.therapi.runtimejavadoc.MethodJavadoc;
import com.github.therapi.runtimejavadoc.ParamJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadocReader;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

public class ApiDocProvider {
    private final CommentFormatter commentFormatter = new CommentFormatter();
    private final RuntimeJavadocReader javadocReader = new RuntimeJavadocReader();
    private final JsonSchemaProvider schemaProvider = new JsonSchemaProvider();

    public List<TherapiNamespaceDoc> getDocumentation(MethodRegistry registry) throws IOException {
        final ObjectWriter prettyWriter = registry.getObjectMapper().writerWithDefaultPrettyPrinter();

        final List<TherapiNamespaceDoc> namespaces = new ArrayList<>();

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
            final TherapiNamespaceDoc nsDoc = new TherapiNamespaceDoc();
            nsDoc.setName(namespaceName);

            final List<TherapiMethodDoc> methods = new ArrayList<>();
            for (MethodDefinition mdef : methodDefinitionsByNamespace.get(namespaceName)) {

                final TherapiMethodDoc mdoc = new TherapiMethodDoc();
                mdoc.setName(mdef.getUnqualifiedName());
                mdoc.setRequestSchema(schemaProvider.getSchema(registry.getObjectMapper(), mdef));

                final Optional<MethodJavadoc> methodJavadocOptional = getJavadoc(mdef);
                final Map<String, ParamJavadoc> javadocsByParamName = methodJavadocOptional.isPresent()
                        ? index(methodJavadocOptional.get().getParams(), ParamJavadoc::getName)
                        : ImmutableMap.<String, ParamJavadoc>of();

                if (methodJavadocOptional.isPresent()) {
                    mdoc.setDescription(render(methodJavadocOptional.get().getComment()));
                    mdoc.setReturns(render(methodJavadocOptional.get().getReturns()));
                    mdoc.setReturnType(toJsonType(mdef.getReturnTypeRef()));
                }

                final List<TherapiParamDoc> paramDocs = new ArrayList<>();
                for (ParameterDefinition pdef : mdef.getParameters()) {
                    final TherapiParamDoc pdoc = new TherapiParamDoc();
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
        ClassJavadoc classJavadoc = javadocReader.getDocumentation(m.getMethod().getDeclaringClass().getName());
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

    public Optional<TherapiMethodDoc> getMethodDoc(MethodDefinition m) throws IOException {
        ClassJavadoc classJavadoc = javadocReader.getDocumentation(m.getMethod().getDeclaringClass().getName());
        if (classJavadoc == null) {
            return Optional.empty();
        }

        for (MethodJavadoc methodJavadoc : classJavadoc.getMethods()) {
            if (methodJavadoc.getName().equals(m.getMethod().getName())) {
                TherapiMethodDoc doc = new TherapiMethodDoc();
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
}
