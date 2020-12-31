package com.ms3.camel;


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

public class RoutesWriter {
    private String oasPathStr;
    private String baseDir;
    private String groupId;

    private Vector opIdList;
    private int startIndent = 2;

    private final static Logger LOGGER = Logger.getLogger(RoutesWriter.class.getName());

    public RoutesWriter(String oasPathStr, String baseDir, String groupId) {
        this.oasPathStr = oasPathStr;
        this.baseDir = baseDir;
        this.groupId = groupId;

        this.groupId = (groupId).replaceAll("\\.", "/").replaceAll("-", "_");
        this.opIdList = new Vector<String>();
    }

    void init() {
        copySpec();
        generateAndWriteRoutesGenerated();
        writeRoutesImplementation();
        writeGitignore();
    }

    // TODO: move gitignore code out to example gitignore and read it instead
    // TODO: write unit tests for all
    // TODO: use try with resources instead

    StringBuffer generateRoutesGeneratedString() {
        Path oasFile = Path.of(oasPathStr);

        OpenAPI openAPI = new OpenAPIV3Parser().read(oasPathStr);

        LOGGER.log(Level.INFO, "==== Add Endpoints to RoutesGenerated Class (rGen) ====");

        StringBuffer rGenCode = new StringBuffer();
        int indent = startIndent;

        //	Add support for request validation.
        rGenCode.append(tabs(indent) + "interceptFrom()\n");
        indent = 3;
        rGenCode.append(tabs(indent) + ".process(new OpenApi4jValidator(\"" + oasFile.getFileName() + "\", contextPath));\n\n");

        indent = 2;
        rGenCode.append(tabs(indent)+"rest()\n");

        Paths paths = openAPI.getPaths();
        Set<String> pathKeys = paths.keySet();

        for (String path : pathKeys) {
            PathItem item = paths.get((Object) path);

            Map<PathItem.HttpMethod, Operation> ops = item.readOperationsMap();

            Operation getOp = item.getGet();
            Operation putOp = item.getPut();
            Operation postOp = item.getPost();
            Operation deleteOp = item.getDelete();
            Operation patchOp = item.getPatch();
            Operation headOp = item.getHead();
            Operation optionsOp = item.getOptions();

            if (getOp != null) {
                indent = 3;
                String opId = "get" + path.replace('/', '-');
                opId = opId.replace("{", "").replace("}", "");
                opIdList.add(opId);
                String desc = getOp.getDescription();
                rGenCode.append(tabs(indent)+".get(\"" + path + "\")\n");
                indent = 4;
                rGenCode.append(tabs(indent)+".id(\"" + opId + "\")\n");

                List<String> produces = new ArrayList<>();
                getOp.getResponses().forEach((status, resp) -> {
                    if (resp.getContent() != null) {
                        resp.getContent().forEach((mediaType, mediaTypeObj) -> {
                            if (!produces.contains(mediaType)) {
                                produces.add(mediaType);
                            }
                        });
                    }
                });
                if (produces.size() > 0) {
                    rGenCode.append(tabs(indent) + ".produces(\"" + String.join(",", produces) + "\")\n");
                }

                rGenCode.append(tabs(indent)+".to(direct(\"" + opId + "\").getUri())\n");
            }
            if (putOp != null) {
                indent = 3;
                String opId = "put" + path.replace("/", "-");
                opId = opId.replace("{", "").replace("}", "");
                opIdList.add(opId);
                String desc = putOp.getDescription();
                rGenCode.append(tabs(indent)+".put(\"" + path + "\")\n");
                indent = 4;
                rGenCode.append(tabs(indent)+".id(\"" + opId + "\")\n");

                List<String> consumes = new ArrayList<>();

                putOp.getRequestBody().getContent().forEach((mediaType, mediaTypeObj) -> {
                    consumes.add(mediaType);
                });
                if (consumes.size() > 0) {
                    rGenCode.append(tabs(indent) +".consumes(\"" + String.join(",", consumes) + "\")\n");
                }

                List<String> produces = new ArrayList<>();
                putOp.getResponses().forEach((status, resp) -> {
                    if (resp.getContent() != null) {
                        resp.getContent().forEach((mediaType, mediaTypeObj) -> {
                            if (!produces.contains(mediaType)) {
                                produces.add(mediaType);
                            }
                        });
                    }
                });
                if (produces.size() > 0) {
                    rGenCode.append(tabs(indent) + ".produces(\"" + String.join(",", produces) + "\")\n");
                }

                rGenCode.append(tabs(indent) + ".to(direct(\"" + opId + "\").getUri())\n");
            }
            if (postOp != null) {
                indent = 3;
                String opId = "post" + path.replace('/', '-');
                opId = opId.replace("{", "").replace("}", "");
                opIdList.add(opId);
                String desc = postOp.getDescription();
                rGenCode.append(tabs(indent) + ".post(\"" + path + "\")\n");
                indent = 4;
                rGenCode.append(tabs(indent) + ".id(\"" + opId + "\")\n");

                if (postOp.getRequestBody() != null) {
                    List<String> consumes = new ArrayList<>();
                    postOp.getRequestBody().getContent().forEach((mediaType, mediaTypeObj) -> {
                        consumes.add(mediaType);
                    });
                    if (consumes.size() > 0) {
                        rGenCode.append(tabs(indent) + ".consumes(\"" + String.join(",", consumes) + "\")\n");
                    }
                }

                List<String> produces = new ArrayList<>();
                postOp.getResponses().forEach((status, resp) -> {
                    if (resp.getContent() != null) {
                        resp.getContent().forEach((mediaType, mediaTypeObj) -> {
                            if (!produces.contains(mediaType)) {
                                produces.add(mediaType);
                            }
                        });
                    }
                });

                if (produces.size() > 0) {
                    rGenCode.append(tabs(indent) + ".produces(\"" + String.join(",", produces) + "\")\n");
                }

                rGenCode.append(tabs(indent) + ".to(direct(\"" + opId + "\").getUri())\n");
            }
            if (deleteOp != null) {
                indent = 3;
                String opId = "delete" + path.replace('/', '-');
                opId = opId.replace("{", "").replace("}", "");
                opIdList.add(opId);
                String desc = deleteOp.getDescription();
                rGenCode.append(tabs(indent) + ".delete(\"" + path + "\")\n");
                indent = 4;
                rGenCode.append(tabs(indent) + ".id(\"" + opId + "\")\n");

                if (deleteOp.getRequestBody() != null) {
                    List<String> consumes = new ArrayList<>();

                    deleteOp.getRequestBody().getContent().forEach((mediaType, mediaTypeObj) -> {
                        consumes.add(mediaType);
                    });
                    if (consumes.size() > 0) {
                        rGenCode.append(tabs(indent) + ".consumes(\"" + String.join(",", consumes) + "\")\n");
                    }
                }

                List<String> produces = new ArrayList<>();
                deleteOp.getResponses().forEach((status, resp) -> {
                    if (resp.getContent() != null) {
                        resp.getContent().forEach((mediaType, mediaTypeObj) -> {
                            if (!produces.contains(mediaType)) {
                                produces.add(mediaType);
                            }
                        });
                    }
                });
                if (produces.size() > 0) {
                    rGenCode.append(tabs(indent) + ".produces(\"" + String.join(",", produces) + "\")\n");
                }

                rGenCode.append(tabs(indent) + ".to(direct(\"" +opId + "\").getUri())\n");
            }
            if (patchOp != null) {
                indent = 3;
                String opId = "patch" + path.replace('/', '-');
                opId = opId.replace("{", "").replace("}", "");
                opIdList.add(opId);
                String desc = patchOp.getDescription();
                rGenCode.append(tabs(indent) + ".patch(\"" + path + "\")\n");
                indent = 4;
                rGenCode.append(tabs(indent) + ".id(\"" + opId + "\")\n");

                List<String> consumes = new ArrayList<>();
                patchOp.getRequestBody().getContent().forEach((mediaType, mediaTypeObj) -> {
                    consumes.add(mediaType);
                });
                if (consumes.size() > 0) {
                    rGenCode.append(tabs(indent) + ".consumes(\"" + String.join(",", consumes) + "\")\n");
                }

                List<String> produces = new ArrayList<>();
                patchOp.getResponses().forEach((status, resp) -> {
                    if (resp.getContent() != null) {
                        resp.getContent().forEach((mediaType, mediaTypeObj) -> {
                            if (!produces.contains(mediaType)) {
                                produces.add(mediaType);
                            }
                        });
                    }
                });
                if (produces.size() > 0) {
                    rGenCode.append(tabs(indent) + ".produces(\"" + String.join(",", produces) + "\")\n");
                }

                rGenCode.append(tabs(indent) + ".to(direct(\"" + opId + "\").getUri())\n");
            }
            if (headOp != null) {
                indent = 3;
                String opId = "head" + path.replace('/', '-');
                opId = opId.replace("{", "").replace("{", "");
                opIdList.add(opId);
                String desc = headOp.getDescription();
                rGenCode.append(tabs(indent) + ".head(\"" + path + "\")\n");
                indent = 4;
                rGenCode.append(tabs(indent) + ".id(\"" + opId + "\")\n");
                rGenCode.append(tabs(indent) + ".to(direct(\"" + opId + "\").getUri())\n");
            }
            if (optionsOp != null) {
                indent = 3;
                String opId = "options" + path.replace('/', '-');
                opId = opId.replace("{", "").replace("{", "");
                opIdList.add(opId);
                String desc = optionsOp.getDescription();
                rGenCode.append(tabs(indent) + ".options(\"" + path + "\")\n");
                indent = 4;
                rGenCode.append(tabs(indent) + ".id(\"" + opId + "\")\n");

                List<String> produces = new ArrayList<>();
                getOp.getResponses().forEach((status, resp) -> {
                    if (resp.getContent() != null) {
                        resp.getContent().forEach((mediaType, mediaTypeObj) -> {
                            if (!produces.contains(mediaType)) {
                                produces.add(mediaType);
                            }
                        });
                    }
                });
                if (produces.size() > 0) {
                    rGenCode.append(tabs(indent) + ".produces(\"" + String.join(",", produces) + "\")\n");
                }

                rGenCode.append(tabs(indent) + ".to(direct(\"" + opId + "\").getUri())\n");
            }
        }

        indent = 2;
        rGenCode.append(tabs(indent)+';');

        return rGenCode;
    }

    void generateAndWriteRoutesGenerated() {
        StringBuffer rGenCode = generateRoutesGeneratedString();
        String rGenPath = baseDir + "/src/generated/java/" + groupId + "/RoutesGenerated.java";
        File rGenFile = new File (rGenPath);
        try {
            StringBuffer rGenBuf = new StringBuffer(Files.readString(Path.of(rGenPath)));

            //	Write the RoutesGenerated document.
            String rGenCodeStr = rGenBuf.toString().replace ("[generated-restdsl]", rGenCode.toString());

            LOGGER.log(Level.INFO, "File to write: " + rGenFile.getAbsolutePath());

            BufferedWriter writer = new BufferedWriter(new FileWriter(rGenPath));
            writer.write(rGenCodeStr);
            writer.close();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    void writeRoutesImplementation() {
        LOGGER.log(Level.INFO, "==== Add Endpoints to RoutesImplemented (rImp) Class ====");

        // Read the RoutesImplemented placeholder file.
        String rImpPath = baseDir + "/src/main/java/" + groupId + "/RoutesImplementation.java";
        File rImpFile = new File (rImpPath);

        try {
            StringBuffer rImpBuf = new StringBuffer(Files.readString(Path.of(rImpPath)));

            //  ------------------------------------
            //  Generate code using the opIdList.
            //  ------------------------------------
            StringBuffer rGenCode = new StringBuffer();
            int indent = startIndent;

            for (Object opId : opIdList) {
                indent = 2;
                rGenCode.append(tabs(indent) + "from(direct(\"" + opId + "\"))\n");
                indent = 3;
                rGenCode.append(tabs(indent) + ".setBody(DatasonnetExpression.builder(\"{opId: '");
                rGenCode.append(opId);
                indent = 5;
                rGenCode.append("'}, String.class)\n" + tabs(indent) + ".outputMediaType(MediaTypes.APPLICATION_JSON))\n");

                indent = 2;
                rGenCode.append(tabs(indent)+";\n");
            }

            //	Write the RoutesImplemented document.
            String rImpCodeStr = rImpBuf.toString().replace ("[generated-routes]", rGenCode.toString());
            LOGGER.log(Level.INFO, "File to write: " + rImpFile.getAbsolutePath());

            BufferedWriter writer = new BufferedWriter(new FileWriter(rImpPath));
            writer.write(rImpCodeStr);
            writer.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

    }

    void writeGitignore() {
        try {
            String gitignorePath = baseDir + "/.gitignore";
            File gitignoreFile = new File (gitignorePath);

            StringBuffer rGenCode = new StringBuffer();
            rGenCode.append("HELP.md\n" +
                    "target/\n" +
                    "!.mvn/wrapper/maven-wrapper.jar\n" +
                    "!**/src/main/**/target/\n" +
                    "!**/src/test/**/target/\n" +
                    "**/META-INF/*\n" +
                    "\n" +
                    "### STS ###\n" +
                    ".apt_generated\n" +
                    ".classpath\n" +
                    ".factorypath\n" +
                    ".project\n" +
                    ".settings\n" +
                    ".springBeans\n" +
                    ".sts4-cache\n" +
                    "\n" +
                    "### IntelliJ IDEA ###\n" +
                    ".idea/*\n" +
                    "*.iws\n" +
                    "*.iml\n" +
                    "*.ipr\n" +
                    "\n" +
                    "### NetBeans ###\n" +
                    "/nbproject/private/\n" +
                    "/nbbuild/\n" +
                    "/dist/\n" +
                    "/nbdist/\n" +
                    "/.nb-gradle/\n" +
                    "build/\n" +
                    "!**/src/main/**/build/\n" +
                    "!**/src/test/**/build/\n" +
                    "\n" +
                    "### VS Code ###\n" +
                    ".vscode/");

            String gitignoreStr = rGenCode.toString();
            LOGGER.log(Level.INFO, "File to write: " + gitignoreFile.getAbsolutePath());

            BufferedWriter writer = new BufferedWriter(new FileWriter(gitignorePath));
            writer.write(gitignoreStr);
            writer.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

    }

    void copySpec() {
        LOGGER.log(Level.INFO, "==== Copying OpenAPI spec ====");
        LOGGER.log(Level.INFO, "API file to copy: " + oasPathStr);
        Path oasFile = Path.of(oasPathStr);

        String specText = null;
        try {
            specText = Files.readString(oasFile);

            String oasPath = baseDir + "/src/generated/api";

            new File(oasPath).mkdir();
            LOGGER.log(Level.INFO, "Made directory " + oasPath);

            String pathAndName = oasPath + "/" + oasFile.getFileName();

            BufferedWriter writer = new BufferedWriter(new FileWriter(pathAndName));

            LOGGER.log(Level.INFO, "File to write: " + pathAndName);
            writer.write(specText);
            writer.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    private String tabs(int level) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<level; i++) {
            sb.append('\t');
        }
        return sb.toString();
    }

}
