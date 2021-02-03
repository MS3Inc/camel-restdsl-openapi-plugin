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

public class RoutesGenerator {
    public final int NO_INDENT = 2;
    public final int ONE_TAB_INDENT = 3;
    public final int TWO_TAB_INDENT = 4;
    public final int THREE_TAB_INDENT = 5;

    public final StringBuffer generatedCode;
    public RoutesGenerator() {
        this.generatedCode = new StringBuffer();
    }

    public StringBuffer getGeneratedCode() {
        return generatedCode;
    }

    public String tabs(int level) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<level; i++) {
            sb.append('\t');
        }
        return sb.toString();
    }

    public String createOpId(String method, String path) {
        String opId = method + path.replace('/', '-');
        return opId.replace("{", "").replace("}", "");
    }
}
