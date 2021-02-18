/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ms3_inc.tavros;

import io.swagger.v3.oas.models.Operation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RoutesGeneratedGenerator extends RoutesGenerator {

    protected StringBuffer appendDslMethodAndId(String method, String path) {
        generatedCode.append(tabs(THREE_TABS)).append(".").append(method).append("(\"").append(path).append("\")\n");
        generatedCode.append(tabs(FOUR_TABS)).append(".id(\"").append(createOpId(method, path)).append("\")\n");
        return generatedCode;
    }

    protected StringBuffer appendConsumes(Operation operation) {
        List<String> consumes = new ArrayList<>();
        if (operation.getRequestBody() != null) {
            operation.getRequestBody().getContent().forEach((mediaType, mediaTypeObj) -> {
                    consumes.add(mediaType);
            });
        }

        if (consumes.size() > 0) {
            generatedCode.append(tabs(FOUR_TABS)).append(".consumes(\"").append(String.join(",", consumes)).append("\")\n");
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
            generatedCode.append(tabs(FOUR_TABS)).append(".produces(\"").append(String.join(",", produces)).append("\")\n");
        }
        return generatedCode;
    }

    protected StringBuffer appendProducer(String method, String path) {
        generatedCode.append(tabs(FOUR_TABS)).append(".to(direct(\"").append(createOpId(method, path)).append("\").getUri())\n");

        return generatedCode;
    }

    protected StringBuffer appendRequestValidation(Path fileName) {
        generatedCode.append(tabs(ZERO_TABS)).append("interceptFrom()\n");
        generatedCode.append(tabs(THREE_TABS)).append(".process(new OpenApi4jValidator(\"").append(fileName).append("\", contextPath));\n\n");
        generatedCode.append(tabs(TWO_TABS)).append("rest()\n");
        return generatedCode;
    }

    protected StringBuffer appendEndColon() {
        generatedCode.append(tabs(TWO_TABS)).append(';');
        return generatedCode;
    }
}

