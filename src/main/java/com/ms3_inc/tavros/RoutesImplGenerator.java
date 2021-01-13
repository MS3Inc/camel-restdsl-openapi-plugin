package com.ms3_inc.tavros;

import org.apache.commons.lang3.tuple.Triple;

public class RoutesImplGenerator extends RoutesGenerator {

    protected StringBuffer appendStub(Triple opInfo) {
        String method = opInfo.getLeft().toString().toLowerCase();
        String path = opInfo.getMiddle().toString();
        Object opId = createOpId(method, path);

        generatedCode.append(tabs(NO_INDENT)).append("from(direct(\"").append(opId).append("\"))\n");
        generatedCode.append(tabs(ONE_TAB_INDENT)).append(".setBody(DatasonnetExpression.builder(\"{opId: '");
        generatedCode.append(opId);
        generatedCode.append("'}\", String.class)\n").append(tabs(THREE_TAB_INDENT)).append(".outputMediaType(MediaTypes.APPLICATION_JSON))\n");
        generatedCode.append(tabs(NO_INDENT)).append(";\n");

        return generatedCode;
    }

}
