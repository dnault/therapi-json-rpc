package com.github.dnault.therapi.apidoc;

import com.github.dnault.therapi.core.internal.MethodDefinition;
import com.github.dnault.therapi.runtimejavadoc.ClassJavadoc;
import com.github.dnault.therapi.runtimejavadoc.Comment;
import com.github.dnault.therapi.runtimejavadoc.MethodJavadoc;
import com.github.dnault.therapi.runtimejavadoc.RuntimeJavadocReader;

import java.io.IOException;
import java.util.Optional;

public class ApiDocProvider {
    private CommentRenderer commentRenderer = new CommentRendererImpl();
    private RuntimeJavadocReader javadocReader = new RuntimeJavadocReader();

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
        return commentRenderer.render(comment);
    }
}
