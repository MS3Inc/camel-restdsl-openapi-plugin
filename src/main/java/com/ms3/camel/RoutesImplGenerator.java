package com.ms3.camel;

public class RoutesImplGenerator extends RoutesGenerator {

    protected StringBuffer appendStub(OperationInfo opInfo) {
        Object opId = createOpId(opInfo.getMethod(), opInfo.getPath());

        generatedCode.append(tabs(NO_INDENT) + "from(direct(\"" + opId + "\"))\n");
        generatedCode.append(tabs(ONE_TAB_INDENT) + ".setBody(DatasonnetExpression.builder(\"{opId: '");
        generatedCode.append(opId);
        generatedCode.append("'}, String.class)\n" + tabs(THREE_TAB_INDENT) + ".outputMediaType(MediaTypes.APPLICATION_JSON))\n");
        generatedCode.append(tabs(NO_INDENT)+";\n");

        return generatedCode;
    }

}
