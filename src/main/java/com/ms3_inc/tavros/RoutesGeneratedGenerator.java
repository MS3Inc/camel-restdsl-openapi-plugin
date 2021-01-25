package com.ms3_inc.tavros;

import io.swagger.v3.oas.models.Operation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RoutesGeneratedGenerator extends RoutesGenerator {

    protected StringBuffer appendDslMethodAndId(String method, String path) {
        generatedCode.append(tabs(ONE_TAB_INDENT)).append(".").append(method).append("(\"").append(path).append("\")\n");
        generatedCode.append(tabs(TWO_TAB_INDENT)).append(".id(\"").append(createOpId(method, path)).append("\")\n");
        return generatedCode;
    }

    protected StringBuffer appendConsumes(Operation operation) {
        List<String> consumes = new ArrayList<>();
        operation.getRequestBody().getContent().forEach((mediaType, mediaTypeObj) -> {
            consumes.add(mediaType);
        });
        if (consumes.size() > 0) {
            generatedCode.append(tabs(TWO_TAB_INDENT)).append(".consumes(\"").append(String.join(",", consumes)).append("\")\n");
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
            generatedCode.append(tabs(TWO_TAB_INDENT)).append(".produces(\"").append(String.join(",", produces)).append("\")\n");
        }
        return generatedCode;
    }

    protected StringBuffer appendProducer(String method, String path) {
        generatedCode.append(tabs(TWO_TAB_INDENT)).append(".to(direct(\"").append(createOpId(method, path)).append("\").getUri())\n");

        return generatedCode;
    }

    protected StringBuffer appendRequestValidation(Path fileName) {
        generatedCode.append(tabs(NO_INDENT)).append("interceptFrom(\"rest*\")\n");
        generatedCode.append(tabs(ONE_TAB_INDENT)).append(".process(new OpenApi4jValidator(\"").append(fileName).append("\", contextPath));\n\n");
        generatedCode.append(tabs(NO_INDENT)).append("rest()\n");
        return generatedCode;
    }

    protected StringBuffer appendEndColon() {
        generatedCode.append(tabs(NO_INDENT)).append(';');
        return generatedCode;
    }
}

