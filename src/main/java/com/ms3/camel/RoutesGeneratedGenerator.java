package com.ms3.camel;

import io.swagger.v3.oas.models.Operation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RoutesGeneratedGenerator extends RoutesGenerator {

    protected StringBuffer appendDslMethodAndId(String method, String path) {
        generatedCode.append(tabs(ONE_TAB_INDENT) + "." + method + "(\"" + path + "\")\n");
        generatedCode.append(tabs(TWO_TAB_INDENT) + ".id(\"" + createOpId(method, path) + "\")\n");

        return generatedCode;
    }

    protected StringBuffer appendConsumes(Operation operation) {
        List<String> consumes = new ArrayList<>();
        operation.getRequestBody().getContent().forEach((mediaType, mediaTypeObj) -> {
            consumes.add(mediaType);
        });
        if (consumes.size() > 0) {
            generatedCode.append(tabs(TWO_TAB_INDENT) + ".consumes(\"" + String.join(",", consumes) + "\")\n");
        }
        return generatedCode;
    }

    protected StringBuffer appendProduces(Operation operation) {
        List<String> produces = new ArrayList<>();
        operation.getResponses().forEach((status, resp) -> {
            if (resp.getContent() != null) {
                resp.getContent().forEach((mediaType, mediaTypeObj) -> {
                    if (!produces.contains(mediaType)) {
                        produces.add(mediaType);
                    }
                });
            }
        });
        if (produces.size() > 0) {
            generatedCode.append(tabs(TWO_TAB_INDENT) + ".produces(\"" + String.join(",", produces) + "\")\n");
        }

        return generatedCode;
    }

    protected StringBuffer appendProducer(String method, String path) {
        int indent = TWO_TAB_INDENT;
        generatedCode.append(tabs(indent) + ".to(direct(\"" + createOpId(method, path) + "\").getUri())\n");
        return generatedCode;
    }

    protected StringBuffer appendRequestValidation(Path fileName) {
        int indent = NO_INDENT;
        generatedCode.append(tabs(indent) + "interceptFrom()\n");
        indent = ONE_TAB_INDENT;
        generatedCode.append(tabs(indent) + ".process(new OpenApi4jValidator(\"" + fileName + "\", contextPath));\n\n");

        indent = NO_INDENT;
        generatedCode.append(tabs(indent)+"rest()\n");

        return generatedCode;
    }

    protected StringBuffer appendEndColon() {
        int indent = NO_INDENT;
        generatedCode.append(tabs(indent)+';');

        return generatedCode;
    }
}

