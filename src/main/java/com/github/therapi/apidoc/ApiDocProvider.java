package com.github.therapi.apidoc;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.therapi.core.internal.LangHelper.index;

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
