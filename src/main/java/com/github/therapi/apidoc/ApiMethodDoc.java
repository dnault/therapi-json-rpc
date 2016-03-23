package com.github.therapi.apidoc;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class ApiMethodDoc {
    private String name;
    private String description;
    private String returns;
    private String returnType;
    private List<ApiParamDoc> params = ImmutableList.of();

    private String requestSchema;

    public String getRequestSchema() {
        return requestSchema;
    }

    public void setRequestSchema(String requestSchema) {
        this.requestSchema = requestSchema;
    }

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

    public List<ApiParamDoc> getParams() {
        return params;
    }

    public void setParams(List<ApiParamDoc> params) {
        this.params = params;
    }
}
