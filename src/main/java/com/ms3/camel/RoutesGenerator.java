package com.ms3.camel;

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
