package com.ms3_inc.tavros;


import io.swagger.v3.oas.models.Operation;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OpenApiMojoTest
{
    private final static Logger LOGGER = Logger.getLogger(OpenApiMojoTest.class.getName());
    private final String GROUP_ID = "com.ms3-inc.tavros";
    private final String YAML = "target/test-classes/oas-petstore.yaml";

    @Rule
    public MojoRule rule = new MojoRule()
    {
        @Override
        protected void before() throws Throwable 
        {
        }

        @Override
        protected void after()
        {
        }
    };

    /**
     * @throws Exception if any
     */
    @Test
    public void testGoal()
            throws Exception
    {
        File pom = new File( "target/test-classes/project-to-test/" );

        OpenApiMojo openApiMojo = (OpenApiMojo) rule.lookupConfiguredMojo( pom, "generate" );
        assertNotNull(openApiMojo);
    }

    // TODO: Add verify tests
    // TODO: Add more unit tests

    @Test
    public void testOperationVectorList() throws IOException {
        RoutesCreator routesCreator = new RoutesCreator(YAML, null, GROUP_ID);
        List<Triple<String, String, Operation>> vector = routesCreator.generateOperationInfoList();
        assertEquals(vector.size(),19);
    }

    @Test
    public void testRoutesGeneratedCode() throws IOException {
        RoutesCreator routesCreator = new RoutesCreator(YAML, null, GROUP_ID);
        List<Triple<String, String, Operation>> vector = routesCreator.generateOperationInfoList();
        StringBuffer routesGeneratedCodeUnderTest = routesCreator.generateRoutesGeneratedCode(vector);

        String pathToExpectedRoutesGenerated = "target/test-classes/expectedRoutesGenerated.txt";
        StringBuffer expectedRoutesGenerated = new StringBuffer(Files.readString(Path.of(pathToExpectedRoutesGenerated)));

        String routesGeneratedCodeWithoutSpaces = routesGeneratedCodeUnderTest.toString().replaceAll("\\s","");
        String buffer = expectedRoutesGenerated.toString().replaceAll("\\s","");

        assertEquals(routesGeneratedCodeWithoutSpaces, buffer);
    }

    @Test
    public void testRoutesImplementationCode() throws IOException {
        RoutesCreator routesCreator = new RoutesCreator(YAML, null, GROUP_ID);
        List<Triple<String, String, Operation>> vector = routesCreator.generateOperationInfoList();
        StringBuffer routesImplCodeUnderTest = routesCreator.generateRoutesImplCode(vector);

        String pathToExpectedRoutesImpl = "target/test-classes/expectedRoutesImpl.txt";
        StringBuffer expectedRoutesImpl = new StringBuffer(Files.readString(Path.of(pathToExpectedRoutesImpl)));

        String routesImplCodeWithoutSpaces = routesImplCodeUnderTest.toString().replaceAll("\\s","");
        String buffer = expectedRoutesImpl.toString().replaceAll("\\s","");

        assertEquals(routesImplCodeWithoutSpaces, buffer);
    }

    @Test
    public void testReadGitignore() throws IOException {
        RoutesCreator routesCreator = new RoutesCreator(YAML, null, GROUP_ID);
        String s = routesCreator.readGitignore();
        assertNotNull(s);
    }
}

