/*
 * Copyright 2020-2026 the original author or authors.
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
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Compiles the generator's <em>output</em> against Camel 4.
 *
 * <p>This exists because the plugin's own source and the source it emits are two
 * different things. Every other test in this project asserts that generation
 * produced the expected characters; none of them asserted that those characters
 * are valid Java against the Camel version the archetype actually targets. A
 * generator can pass its golden-file tests while emitting code that does not
 * compile - which is precisely what happened with the missing
 * DatasonnetExpression import in the archetype template.
 *
 * <p>The emitted route bodies are wrapped in a minimal EndpointRouteBuilder
 * subclass and handed to the in-process javac. The compile classpath is the test
 * classpath, so the Camel artifacts declared test-scoped in the pom are what the
 * generated code is checked against.
 *
 * <p>The request-validation line is excluded deliberately: it references
 * OpenApi4jValidator from camel-rest-extensions, whose Camel 4 build is not yet
 * released. Wiring an unreleased artifact into this test would couple two
 * repositories mid-migration. What is verified here is the REST DSL and
 * DataSonnet surface, which is the part this plugin actually generates.
 */
public class GeneratedCodeCompilesTest {

    private static final String YAML_30 = "target/test-classes/oas-petstore.yaml";
    private static final String YAML_31 = "target/test-classes/oas-31-sample.yaml";

    @Test
    public void generatedRoutesFrom30SpecCompileAgainstCamel4() throws Exception {
        assertCompiles(YAML_30, "Generated30");
    }

    @Test
    public void generatedRoutesFrom31SpecCompileAgainstCamel4() throws Exception {
        assertCompiles(YAML_31, "Generated31");
    }

    private void assertCompiles(String specPath, String className) throws Exception {
        RoutesCreator creator = new RoutesCreator(specPath, null, "com.ms3-inc.tavros");
        List<Triple<String, String, Operation>> ops = creator.generateOperationInfoList();

        String restDsl = stripValidationLine(creator.generateRoutesGeneratedCode(ops).toString());
        String implRoutes = creator.generateRoutesImplCode(ops).toString();

        String source = ""
                + "package generated;\n"
                + "import com.datasonnet.document.MediaTypes;\n"
                + "import org.apache.camel.language.datasonnet.DatasonnetExpression;\n"
                + "import org.apache.camel.builder.endpoint.EndpointRouteBuilder;\n"
                + "public class " + className + " extends EndpointRouteBuilder {\n"
                + "    private final String contextPath = \"/\";\n"
                + "    @Override\n"
                + "    public void configure() throws Exception {\n"
                + restDsl + "\n"
                + implRoutes + "\n"
                + "    }\n"
                + "}\n";

        // Keep the attempted source on disk so a failure can be inspected.
        Path dump = Path.of("target", "generated-compile-check", className + ".java");
        Files.createDirectories(dump.getParent());
        Files.writeString(dump, source);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull("No system Java compiler; tests must run on a JDK, not a JRE", compiler);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        File outputDir = new File("target/generated-compile-check/classes");
        outputDir.mkdirs();

        List<String> options = Arrays.asList(
                "-classpath", System.getProperty("java.class.path"),
                "-d", outputDir.getAbsolutePath());

        boolean ok = compiler.getTask(null, fileManager, diagnostics, options, null,
                List.of(new InMemorySource(className, source))).call();
        fileManager.close();

        String report = diagnostics.getDiagnostics().stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));

        assertTrue("Generated code from " + specPath + " does not compile against Camel 4.\n"
                + "Source written to " + dump.toAbsolutePath() + "\n" + report, ok);
    }

    /**
     * Drops the {@code interceptFrom().process(new OpenApi4jValidator(...))}
     * statement, which depends on camel-rest-extensions. See the class javadoc.
     */
    private static String stripValidationLine(String restDsl) {
        int restStart = restDsl.indexOf("rest()");
        return restStart < 0 ? restDsl : restDsl.substring(restStart);
    }

    private static final class InMemorySource extends SimpleJavaFileObject {
        private final String code;

        InMemorySource(String className, String code) {
            super(URI.create("string:///generated/" + className + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}
