package com.ms3.camel;

import com.google.common.io.Resources;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.OpenAPIV3Parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
        Vector<OperationInfo> opInfoList = generateOperationInfoList();

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

    Vector generateOperationInfoList() {
        OpenAPI openAPI = new OpenAPIV3Parser().read(oasPathStr);

        LOGGER.info("==== Parse spec into operation list ====");

        Paths paths = openAPI.getPaths();
        Set<String> pathKeys = paths.keySet();

        Vector opInfoList = new Vector<OperationInfo>();

        for (String path : pathKeys) {
            PathItem item = paths.get(path);

            Operation getOp = item.getGet();
            Operation putOp = item.getPut();
            Operation postOp = item.getPost();
            Operation deleteOp = item.getDelete();
            Operation patchOp = item.getPatch();
            Operation headOp = item.getHead();
            Operation optionsOp = item.getOptions();

            if (getOp != null) {
                OperationInfo opInfo = new OperationInfo("get", getOp, path);
                opInfoList.add(opInfo);
            }
            if (putOp != null) {
                OperationInfo opInfo = new OperationInfo("put", putOp, path);
                opInfoList.add(opInfo);
            }
            if (postOp != null) {
                OperationInfo opInfo = new OperationInfo("post", postOp, path);
                opInfoList.add(opInfo);
            }
            if (deleteOp != null) {
                OperationInfo opInfo = new OperationInfo("delete", deleteOp, path);
                opInfoList.add(opInfo);
            }
            if (patchOp != null) {
                // These are commented until I figure out how to test head and options
//                LOGGER.info("Brackets from a different method");
//                LOGGER.info("patch" + path.replace('/', '-'));

                OperationInfo opInfo = new OperationInfo("patch", patchOp, path);
                opInfoList.add(opInfo);
            }
            if (headOp != null) {
//                LOGGER.info("Confirm the right brackets are being replaced here");
//                LOGGER.info("head" + path.replace('/', '-'));

                OperationInfo opInfo = new OperationInfo("head", headOp, path);
                opInfoList.add(opInfo);
            }
            if (optionsOp != null) {
//                LOGGER.info("Confirm the right brackets are being replaced here");
//                LOGGER.info("options" + path.replace('/', '-'));

                OperationInfo opInfo = new OperationInfo("options", optionsOp, path);
                opInfoList.add(opInfo);
            }
        }

        return opInfoList;
    }

    StringBuffer generateRoutesGeneratedCode(Vector<OperationInfo> opInfoList) {
        Path oasFile = Path.of(oasPathStr);

        LOGGER.log(Level.INFO, "==== Add Endpoints to RoutesGenerated Class (rGen) ====");

        RoutesGeneratedGenerator rGenCode = new RoutesGeneratedGenerator();

        rGenCode.appendRequestValidation(oasFile.getFileName());

        for (OperationInfo opInfo : opInfoList) {
            String method = opInfo.getMethod();
            Operation operation = opInfo.getOperation();
            String path = opInfo.getPath();

            if (method.equals("get")) {
                rGenCode.appendDslMethodAndId(method, path);
                rGenCode.appendProduces(operation);
                rGenCode.appendProducer(method, path);
            } else if (method.equals("put")) {
                rGenCode.appendDslMethodAndId(method, path);
                rGenCode.appendConsumes(operation);
                rGenCode.appendProduces(operation);
                rGenCode.appendProducer(method, path);
            } else if (method.equals("post")) {
                rGenCode.appendDslMethodAndId(method, path);

                if (operation.getRequestBody() != null) {
                    rGenCode.appendConsumes(operation);
                }

                rGenCode.appendProduces(operation);
                rGenCode.appendProducer(method, path);
            } else if (method.equals("delete")) {
                rGenCode.appendDslMethodAndId(method, path);

                if (operation.getRequestBody() != null) {
                    rGenCode.appendConsumes(operation);
                }

                rGenCode.appendProduces(operation);
                rGenCode.appendProducer(method, path);
            } else if (method.equals("patch")) {
                rGenCode.appendDslMethodAndId(method, path);
                rGenCode.appendConsumes(operation);
                rGenCode.appendProduces(operation);
                rGenCode.appendProducer(method, path);
            } else if (method.equals("head")) {
                rGenCode.appendDslMethodAndId(method, path);
                rGenCode.appendProducer(method, path);
            } else if (method.equals("options")) {
                rGenCode.appendDslMethodAndId(method, path);
                rGenCode.appendProduces(operation);
                rGenCode.appendProducer(method, path);
            }
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

            LOGGER.info(rGenCodeStr);
            LOGGER.log(Level.INFO, "File to write: " + rGenFile.getAbsolutePath());

            BufferedWriter writer = new BufferedWriter(new FileWriter(rGenPath));
            writer.write(rGenCodeStr);
            writer.close();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    StringBuffer generateRoutesImplCode(Vector<OperationInfo> opInfoList) {
        RoutesImplGenerator routesImplCode = new RoutesImplGenerator();

        for (OperationInfo opInfo : opInfoList) {
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
        String path = Resources.getResource("gitignore.txt").getPath();
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
