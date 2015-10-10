package com.github.therapi.apidoc;

import java.util.ArrayList;
import java.util.List;

public class TherapiNamespaceDoc {
    private String name;
    private String description;
    private List<TherapiMethodDoc> methods = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TherapiMethodDoc> getMethods() {
        return methods;
    }

    public void setMethods(List<TherapiMethodDoc> methods) {
        this.methods = methods;
    }
}
