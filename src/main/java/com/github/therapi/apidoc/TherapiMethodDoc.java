package com.github.therapi.apidoc;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class TherapiMethodDoc {
    private String name;
    private String description;
    private String returns;
    private String returnType;

    private List<TherapiParamDoc> params = ImmutableList.of();

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

    public String getReturns() {
        return returns;
    }

    public void setReturns(String returns) {
        this.returns = returns;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<TherapiParamDoc> getParams() {
        return params;
    }

    public void setParams(List<TherapiParamDoc> params) {
        this.params = params;
    }
}
