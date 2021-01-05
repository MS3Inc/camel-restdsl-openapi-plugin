package com.ms3.camel;

import io.swagger.v3.oas.models.Operation;

public class OperationInfo {
    private String method;
    private Operation operation;
    private String path;

    public OperationInfo(String method, Operation operation, String path) {
        this.method = method;
        this.operation = operation;
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getPath() {
        return path;
    }
}
