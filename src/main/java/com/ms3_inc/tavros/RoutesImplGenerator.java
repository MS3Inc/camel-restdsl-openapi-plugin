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

import org.apache.commons.lang3.tuple.Triple;

public class RoutesImplGenerator extends RoutesGenerator {

    protected StringBuffer appendStubComment() {
        generatedCode.append(indents(NONE)).append("// TODO: Replace stubs for each endpoint with real implementation. Implementation defaults to a simple response with operation Id.\n");
        return generatedCode;
    }

    protected StringBuffer appendStub(Triple opInfo) {
        String method = opInfo.getLeft().toString().toLowerCase();
        String path = opInfo.getMiddle().toString();
        Object opId = createOpId(method, path);

        generatedCode.append(indents(TWO)).append("from(direct(\"").append(opId).append("\"))\n");
        generatedCode.append(indents(THREE)).append(".setBody(DatasonnetExpression.builder(\"{opId: '");
        generatedCode.append(opId);
        generatedCode.append("'}\", String.class)\n").append(indents(FIVE)).append(".outputMediaType(MediaTypes.APPLICATION_JSON))\n");
        generatedCode.append(indents(TWO)).append(";\n");

        return generatedCode;
    }

}
