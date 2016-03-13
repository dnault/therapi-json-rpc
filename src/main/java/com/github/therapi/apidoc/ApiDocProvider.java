package com.github.therapi.apidoc;

import static com.github.therapi.core.internal.LangHelper.index;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.substringBetween;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.therapi.core.MethodRegistry;
import com.github.therapi.core.internal.MethodDefinition;
import com.github.therapi.core.internal.ParameterDefinition;
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
    private CommentFormatter commentFormatter = new CommentFormatter();
    private RuntimeJavadocReader javadocReader = new RuntimeJavadocReader();

    public List<TherapiNamespaceDoc> getDocumentation(MethodRegistry registry) throws IOException {
        List<TherapiNamespaceDoc> namespaces = new ArrayList<>();

        SortedSetMultimap<String, MethodDefinition> methodDefinitionsByNamespace = TreeMultimap.create(
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
            TherapiNamespaceDoc nsDoc = new TherapiNamespaceDoc();
            nsDoc.setName(namespaceName);

            List<TherapiMethodDoc> methods = new ArrayList<>();
            for (MethodDefinition mdef : methodDefinitionsByNamespace.get(namespaceName)) {

                TherapiMethodDoc mdoc = new TherapiMethodDoc();
                mdoc.setName(mdef.getUnqualifiedName());

                Optional<MethodJavadoc> methodJavadocOptional = getJavadoc(mdef);
                Map<String, ParamJavadoc> javadocsByParamName = methodJavadocOptional.isPresent()
                        ? index(methodJavadocOptional.get().getParams(), ParamJavadoc::getName)
                        : ImmutableMap.<String, ParamJavadoc>of();

                if (methodJavadocOptional.isPresent()) {
                    mdoc.setDescription(render(methodJavadocOptional.get().getComment()));
                    mdoc.setReturns(render(methodJavadocOptional.get().getReturns()));
                }

                List<TherapiParamDoc> paramDocs = new ArrayList<>();
                for (ParameterDefinition pdef : mdef.getParameters()) {
                    TherapiParamDoc pdoc = new TherapiParamDoc();
                    pdoc.setName(pdef.getName());
                    pdoc.setType(toJsonType(pdef.getType()));

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
        String typeName = typeRef.getType().toString();

        typeName = removeStart(typeName, "class ");
        typeName = removeStart(typeName, "interface ");

        if (typeName.equals("int") || typeName.equals("long")) {
            return "integer";
        }

        if (typeName.equals("float") || typeName.equals("double")) {
            return "number";
        }

        typeName = typeName.replace("java.lang.Object", "any");

        typeName = typeName.replace("java.lang.String", "string");

        typeName = typeName.replace("java.lang.Integer", "integer");
        typeName = typeName.replace("java.lang.Long", "integer");

        typeName = typeName.replace("java.lang.Float", "number");
        typeName = typeName.replace("java.lang.Double", "number");

        typeName = typeName.replace("java.util.Set", "array");
        typeName = typeName.replace("java.util.List", "array");
        typeName = typeName.replace("java.util.Collection", "array");

        typeName = typeName.replace("java.util.Map", "map");

        typeName = typeName.replace("java.util.Optional", "optional");
        typeName = typeName.replace("com.google.common.base.Optional", "optional");

        if (typeName.startsWith("com.google.common.collect.Multimap")) {
            String params = substringBetween(typeName, "<", ">");
            String keyType = substringBefore(params, ",").trim();
            String valueType = substringAfter(params, ",").trim();
            typeName = "map<" + keyType + ", array<" + valueType + ">>";
        }

        return typeName;
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
}
