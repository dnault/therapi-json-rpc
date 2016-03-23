package com.github.therapi.apidoc;

import java.util.ArrayList;
import java.util.List;

public class ApiNamespaceDoc {
    private String name;
    private String description;
    private List<ApiMethodDoc> methods = new ArrayList<>();

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

    public List<ApiMethodDoc> getMethods() {
        return methods;
    }

    public void setMethods(List<ApiMethodDoc> methods) {
        this.methods = methods;
    }
}
