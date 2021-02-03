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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.commons.lang3.tuple.Triple;

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

    void init() {
        copySpec();
        List<Triple<String, String, Operation>> opInfoList = generateOperationInfoList();

        StringBuffer routesGeneratedCode = generateRoutesGeneratedCode(opInfoList);
        writeRoutesGenerated(routesGeneratedCode);

        StringBuffer routesImplCode = generateRoutesImplCode(opInfoList);
        writeRoutesImplementation(routesImplCode);

        String gitignoreContents = readGitignore();
        writeGitignore(gitignoreContents);
    }

    void copySpec() {
        LOGGER.info("==== Copying OpenAPI spec ====");
        LOGGER.info("API file to copy: " + oasPathStr);
        Path oasFile = Path.of(oasPathStr);

        String specText = null;
        try {
            specText = Files.readString(oasFile);

            String oasPath = baseDir + "/src/generated/api";

            new File(oasPath).mkdir();
            LOGGER.info("Made directory " + oasPath);

            String pathAndName = oasPath + "/" + oasFile.getFileName();

            BufferedWriter writer = new BufferedWriter(new FileWriter(pathAndName));

            LOGGER.info("File to write: " + pathAndName);
            writer.write(specText);
            writer.close();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
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
        Path oasFile = Path.of(oasPathStr);

        LOGGER.log(Level.INFO, "==== Add Endpoints to RoutesGenerated Class (rGen) ====");

        RoutesGeneratedGenerator rGenCode = new RoutesGeneratedGenerator();

        rGenCode.appendRequestValidation(oasFile.getFileName());

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

    void writeRoutesGenerated(StringBuffer routesGeneratedCode) {
        String rGenPath = baseDir + "/src/generated/java/" + groupId + "/RoutesGenerated.java";
        File rGenFile = new File (rGenPath);
        try {
            StringBuffer rGenBuf = new StringBuffer(Files.readString(Path.of(rGenPath)));

            String rGenCodeStr = rGenBuf.toString().replace ("[generated-restdsl]", routesGeneratedCode.toString());

            LOGGER.log(Level.INFO, "File to write: " + rGenFile.getAbsolutePath());

            BufferedWriter writer = new BufferedWriter(new FileWriter(rGenPath));
            writer.write(rGenCodeStr);
            writer.close();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    StringBuffer generateRoutesImplCode(List<Triple<String, String, Operation>> opInfoList) {
        RoutesImplGenerator routesImplCode = new RoutesImplGenerator();

        for (Triple opInfo : opInfoList) {
            routesImplCode.appendStub(opInfo);
        }
        return routesImplCode.getGeneratedCode();
    }

    void writeRoutesImplementation(StringBuffer routesImplCode) {
        LOGGER.log(Level.INFO, "==== Add Endpoints to RoutesImplemented (rImp) Class ====");

        String rImpPath = baseDir + "/src/main/java/" + groupId + "/RoutesImplementation.java";
        File rImpFile = new File (rImpPath);

        try {
            StringBuffer rImpBuf = new StringBuffer(Files.readString(Path.of(rImpPath)));

            String rImpCodeStr = rImpBuf.toString().replace ("[generated-routes]", routesImplCode.toString());
            LOGGER.log(Level.INFO, "File to write: " + rImpFile.getAbsolutePath());

            BufferedWriter writer = new BufferedWriter(new FileWriter(rImpPath));
            writer.write(rImpCodeStr);
            writer.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    String readGitignore() {
        StringBuffer gitIgnoreBuf = new StringBuffer();
        String path = "target/classes/gitignore.txt";
        try {
            gitIgnoreBuf = new StringBuffer(Files.readString(Path.of(path)));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        return gitIgnoreBuf.toString();
    }

    void writeGitignore(String gitignoreStr) {
        try {
            String gitignorePath = baseDir + "/.gitignore";
            File gitignoreFile = new File (gitignorePath);

            LOGGER.log(Level.INFO, "File to write: " + gitignoreFile.getAbsolutePath());

            BufferedWriter writer = new BufferedWriter(new FileWriter(gitignorePath));
            writer.write(gitignoreStr);
            writer.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

    }

}
