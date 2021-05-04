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

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoutesCreator {
    private String oasPathStr;
    private String baseDir;
    private String groupId;

    private final static Logger LOGGER = Logger.getLogger(RoutesCreator.class.getName());

    public RoutesCreator(String oasPathStr, String baseDir, String groupId) {
        this.oasPathStr = oasPathStr;
        this.baseDir = baseDir;
        this.groupId = (groupId).replaceAll("\\.", "/").replaceAll("-", "_");
    }

    void init() throws MojoExecutionException {
        copySpec();
        List<Triple<String, String, Operation>> opInfoList = generateOperationInfoList();

        StringBuffer routesGeneratedCode = generateRoutesGeneratedCode(opInfoList);
        writeRoutesGenerated(routesGeneratedCode);

        StringBuffer routesImplCode = generateRoutesImplCode(opInfoList);
        writeRoutesImplementation(routesImplCode);
    }

    void copySpec() throws MojoExecutionException {
        LOGGER.info("==== Copying OpenAPI spec ====");
        LOGGER.info("API file to copy: " + oasPathStr);

        Path oasFile = Path.of(oasPathStr);
        try {
            String oasPath = baseDir + "/src/generated/api";

            String pathAndName = oasPath + "/" + oasFile.getFileName();

            new File(oasPath).mkdir();
            LOGGER.info("Made directory " + oasPath);

            BufferedWriter writer = new BufferedWriter(new FileWriter(pathAndName));

            ParseOptions options = new ParseOptions();
//            options.setResolveFully(true);
//            options.setResolveCombinators(true);
//            options.setFlatten(true);
            options.setResolve(true);
            OpenAPI openAPI = new OpenAPIV3Parser().read(oasPathStr, null, options);
            YAMLFactory factory = (YAMLFactory) Yaml.mapper().getFactory();
            factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                    .disable(YAMLGenerator.Feature.MINIMIZE_QUOTES);

            String yaml = Yaml.pretty().writeValueAsString(openAPI);

            LOGGER.info("File to write: " + pathAndName);
            writer.write(yaml);
            writer.close();
        } catch (IOException e) {
            throw new MojoExecutionException("There was a problem while copying the spec.");
        }
    }


    List generateOperationInfoList() {
        OpenAPI openAPI = new OpenAPIV3Parser().read(oasPathStr);

        LOGGER.info("==== Parse spec into operation list ====");

        Paths paths = openAPI.getPaths();
        Set<String> pathKeys = paths.keySet();

        List opInfoList = new Vector<Triple>();

        for (String path : pathKeys) {
            PathItem item = paths.get(path);

            item.readOperationsMap().forEach((method, op) ->
                    opInfoList.add(Triple.of(method.toString(), path, op))
            );
        }

        return opInfoList;
    }

    StringBuffer generateRoutesGeneratedCode(List<Triple<String, String, Operation>> opInfoList) {
        LOGGER.log(Level.INFO, "==== Add Endpoints to RoutesGenerated Class (rGen) ====");

        RoutesGeneratedGenerator rGenCode = new RoutesGeneratedGenerator();

        Path oasFile = Path.of(oasPathStr);
        rGenCode.appendRequestValidation(oasFile.getFileName());
        rGenCode.appendStartOfRestDSL();

        for (Triple opInfo : opInfoList) {
            String method = opInfo.getLeft().toString().toLowerCase();
            String path = opInfo.getMiddle().toString();
            Operation operation = (Operation) opInfo.getRight();

            rGenCode.appendDslMethodAndId(method, path);
            rGenCode.appendConsumes(operation);
            rGenCode.appendProduces(operation);
            rGenCode.appendProducer(method, path);
        }

        rGenCode.appendEndColon();

        return rGenCode.getGeneratedCode();
    }

    void writeRoutesGenerated(StringBuffer routesGeneratedCode) throws MojoExecutionException {
        String rGenPath = baseDir + "/src/generated/java/" + groupId + "/RoutesGenerated.java";
        File rGenFile = new File (rGenPath);

        try {
            StringBuffer rGenBuf = new StringBuffer(Files.readString(Path.of(rGenPath)));

            String routesGenToReplace = "// REST DSL routes";

            String rGenCodeStr = rGenBuf.toString()
                    .replace (routesGenToReplace, routesGeneratedCode.toString());

            LOGGER.log(Level.INFO, "File to write: " + rGenFile.getAbsolutePath());

            BufferedWriter writer = new BufferedWriter(new FileWriter(rGenPath));
            writer.write(rGenCodeStr);
            writer.close();
        } catch (IOException e) {
            throw new MojoExecutionException("There was a problem writing RoutesGenerated.");
        }
    }

    StringBuffer generateRoutesImplCode(List<Triple<String, String, Operation>> opInfoList) {
        RoutesImplGenerator routesImplCode = new RoutesImplGenerator();

        routesImplCode.appendStubComment();
        for (Triple opInfo : opInfoList) {
            routesImplCode.appendStub(opInfo);
        }
        return routesImplCode.getGeneratedCode();
    }

    void writeRoutesImplementation(StringBuffer routesImplCode) throws MojoExecutionException {
        LOGGER.log(Level.INFO, "==== Add Endpoints to RoutesImplemented (rImp) Class ====");

        String rImpPath = baseDir + "/src/main/java/" + groupId + "/RoutesImplementation.java";
        File rImpFile = new File (rImpPath);

        try {
            StringBuffer rImpBuf = new StringBuffer(Files.readString(Path.of(rImpPath)));

            String routesImplToReplace = "// Implementation routes";

            String rImpCodeStr = rImpBuf.toString().replace (routesImplToReplace, routesImplCode.toString());
            LOGGER.log(Level.INFO, "File to write: " + rImpFile.getAbsolutePath());

            BufferedWriter writer = new BufferedWriter(new FileWriter(rImpPath));
            writer.write(rImpCodeStr);
            writer.close();
        } catch (IOException e) {
            throw new MojoExecutionException("There was a problem writing RoutesImplementation.");
        }
    }

}
