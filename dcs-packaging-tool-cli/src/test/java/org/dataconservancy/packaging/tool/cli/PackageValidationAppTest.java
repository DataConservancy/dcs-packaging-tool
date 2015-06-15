/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.packaging.tool.model.PackageRelationship;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.StandardErrorStreamLog;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.junit.contrib.java.lang.system.TextFromStandardInputStream;
import org.junit.rules.TemporaryFolder;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageDescriptionBuilder;
import org.dataconservancy.packaging.tool.model.builder.json.JSONPackageDescriptionBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 */
public class PackageValidationAppTest {

    private static final String LINE_SEPARATOR = System
            .getProperty("line.separator");

    private PackageDescriptionBuilder builder =
            new JSONPackageDescriptionBuilder();

    private AtomicInteger idSource = new AtomicInteger();

    @Rule
    public final StandardErrorStreamLog stderr = new StandardErrorStreamLog();

    @Rule
    public final StandardOutputStreamLog stdout = new StandardOutputStreamLog();

    @Rule
    public final TextFromStandardInputStream stdin =
            TextFromStandardInputStream.emptyStandardInputStream();

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    
    @Rule
    public final TemporaryFolder tmpfolder = new TemporaryFolder();

    /* Verify that valid package descriptions are summarized to stdout */
    @Test
    public void validFilesTest() {
        final int stdoutLineCount = 3;

        ArrayList<String> args = new ArrayList<String>();

        /*
         * Create as many valid PackageDescriptions as success output statements
         * we expect
         */
        for (int i = 0; i < stdoutLineCount; i++) {
            args.add(createValidPackageDescription().getPath());
        }

        /* We expect return status of 0 */
        exit.expectSystemExitWithStatus(0);

        /* After validator calls System.exit(0), check the output it gave */
        exit.checkAssertionAfterwards(new Assertion() {

            @Override
            public void checkAssertion() throws Exception {
                String stdoutText = stdout.getLog();
                assertEquals(stdoutLineCount,
                             stdoutText.split(LINE_SEPARATOR).length);

                for (String line : stdoutText.split(LINE_SEPARATOR)) {
                    assertTrue(line.startsWith("Valid:"));
                }

                String stderrText = stderr.getLog();
                for (String line : stderrText.split(LINE_SEPARATOR)) {
                    if (line.startsWith("Failed")) {
                        fail("Did not expect a failed validation");
                    }
                }
            }
        });

        PackageValidationApp.main(args.toArray(new String[0]));

    }

    /*
     * Verify that invalid package descriptions are written to stderr, while the
     * valid ones are written to stdout, and that the program terminates with a
     * nonzero status code
     */
    @Test
    public void invalidFilesTest() {
        final int stdoutLineCount = 3;
        final int stderrLineCount = 4;

        ArrayList<String> args = new ArrayList<String>();

        /*
         * Create as many valid PackageDescriptions as success output statements
         * we expect
         */
        for (int i = 0; i < stdoutLineCount; i++) {
            args.add(createValidPackageDescription().getPath());
        }

        /*
         * Likewise, create as many invalid PackageDescriptions as failure
         * statements
         */
        for (int i = 0; i < stderrLineCount; i++) {
            args.add(createInvalidPackageDescription().getPath());
        }

        /* We expect return status of the number of failed validations */
        exit.expectSystemExitWithStatus(stderrLineCount);

        /* After validator calls System.exit(N), check the output it gave */
        exit.checkAssertionAfterwards(new Assertion() {

            @Override
            public void checkAssertion() throws Exception {

                int encounteredFailCount = 0;

                String stdoutText = stdout.getLog();
                assertEquals(stdoutLineCount,
                             stdoutText.split(LINE_SEPARATOR).length);

                String stderrText = stderr.getLog();
                for (String line : stderrText.split(LINE_SEPARATOR)) {
                    if (line.startsWith("Failed")) {
                        encounteredFailCount++;
                    }
                }

                assertEquals(stderrLineCount, encounteredFailCount);
            }
        });

        PackageValidationApp.main(args.toArray(new String[0]));

    }

    /* Verify that a garbage file simply registers as an invalid description */
    @Test
    public void garbageFileTest() throws Exception {
        File descFile = createValidPackageDescription();
        FileOutputStream out = new FileOutputStream(descFile);

        /* corrupt the package description file */
        IOUtils.write("moo{]'", out);
        out.close();

        /* Exit code 1 (1 failure) */
        exit.expectSystemExitWithStatus(1);

        exit.checkAssertionAfterwards(new Assertion() {

            @Override
            public void checkAssertion() throws Exception {
                int encounteredFailCount = 0;

                /* There should be no stdout */
                String stdoutText = stdout.getLog();
                assertEquals("", stdoutText);

                /* Stderr should have one line */
                String stderrText = stderr.getLog();
                for (String line : stderrText.split(LINE_SEPARATOR)) {
                    if (line.startsWith("Failed")) {
                        encounteredFailCount++;
                    }
                }
                assertEquals(1, encounteredFailCount);
            }
        });

        PackageValidationApp.main(new String[] {descFile.getPath()});
    }

    @Test
    public void stdinTest() throws Exception {
        File descFile = createValidPackageDescription();
        stdin.provideText(IOUtils.toString(new FileInputStream(descFile)));
        
        /* Expect no error */
        exit.expectSystemExitWithStatus(0);
        
        /* Call with stdin */
        PackageValidationApp.main(new String[0]);
    }

    private File createValidPackageDescription() {
        final String TYPE = "type";
        try {
            File valid = tmpfolder.newFile("valid" + UUID.randomUUID());
                    
            OutputStream writer = new FileOutputStream(valid);

            PackageDescription desc = new PackageDescription();
            desc.setPackageOntologyIdentifier("valid");
            desc.setPackageArtifacts(new HashSet<PackageArtifact>());
            desc.setRootArtifactRef("root");
            PackageArtifact artifact1 = newArtifact(TYPE);
            PackageArtifact artifact2 = newArtifact(TYPE);
            addRel("rel", artifact2, artifact1);

            desc.getPackageArtifacts().add(artifact1);
            desc.getPackageArtifacts().add(artifact2);

            builder.serialize(desc, writer);

            writer.close();
            return valid;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private File createInvalidPackageDescription() {
        final String TYPE = "type";
        try {
            File invalid = tmpfolder.newFile("invalid" + UUID.randomUUID());
                    
            OutputStream writer = new FileOutputStream(invalid);

            PackageDescription desc = new PackageDescription();
            desc.setPackageOntologyIdentifier("invalid");
            desc.setRootArtifactRef("root");
            desc.setPackageArtifacts(new HashSet<PackageArtifact>());

            PackageArtifact artifact1 = newArtifact(TYPE);
            PackageArtifact artifact2 = newArtifact(TYPE);
            // empty property name makes this invalid
            artifact1.addSimplePropertyValue("", "TestValue");
            addRel("rel", artifact2, artifact1);

            desc.getPackageArtifacts().add(artifact1);
            desc.getPackageArtifacts().add(artifact2);

            builder.serialize(desc, writer);

            writer.close();
            return invalid;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* Helper to create new, identified artifacts. */
    public PackageArtifact newArtifact(final String type) {
        PackageArtifact artifact = new PackageArtifact();

        File contentFile = null;
        try {
            contentFile = tmpfolder.newFile("content" + UUID.randomUUID());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        artifact.setId(new Integer(idSource.incrementAndGet()).toString());

        if (type != null) {
            artifact.setType(type);
        }

        artifact.setArtifactRef(contentFile.toURI().toString());

        return artifact;
    }

    /* Safely add a relationship from one artifact to another. */
    private void addRel(String rel, PackageArtifact to, PackageArtifact from) {
        PackageRelationship relationship = from.getRelationshipByName(rel.toString());

        if (relationship == null) {
            relationship = new PackageRelationship(rel.toString(), true, new HashSet<String>());
            from.getRelationships().add(relationship);
        }

        relationship.getTargets().add(to.getId());
    }
}
