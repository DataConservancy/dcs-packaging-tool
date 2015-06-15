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

import java.io.InputStream;

import java.util.List;
import java.util.Map;

import org.dataconservancy.packaging.tool.cli.util.ContentLoader;
import org.dataconservancy.packaging.tool.impl.PackageDescriptionValidator;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageDescriptionBuilder;
import org.dataconservancy.packaging.tool.model.PackageToolException;
import org.dataconservancy.packaging.validation.PackageValidationException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Application for validating package descriptions
 * <p>
 * There are three levels of validation that this application is intended to
 * perform:
 * <dl>
 * <dt>Well-formedness</dt>
 * <dd>Verifies that the basic PackageDescription structure is free of error</dd>
 * <dt>Ontology</dt>
 * <dd>Verifies that the description document complies with the ontology
 * constraints set forth in the specification identified by the package
 * specification id</dd>
 * <dt>Resolution</dt>
 * <dd>Verifies that package artifact sources are resolvable to content, where
 * applicable</dd>
 * </dl>
 * <p>
 * TODO Right now, only well-formedness is complete.
 * </p>
 * <p>
 * When validation is run, there will be one line of text output to STDOUT for
 * every successfully validated file, and one line of text output for every file
 * that fails validation. The return code will be nonzero if any file has failed
 * validation.
 * </p>
 */
public class PackageValidationApp {

    private ClassPathXmlApplicationContext appContext;

    /*
     * Arguments
     */
    @Argument(multiValued = true, index = 0, metaVar = "[infiles]", usage = "infiles: package description file(s), '-' for stdin")
    private List<String> locations;

    /*
     * General Options
     */
    /** Request for help/usage documentation */
    @Option(name = "-h", aliases = {"-help", "--help"}, usage = "print help message")
    public boolean help = false;

    /** Requests the current version number of the cli application. */
    @Option(name = "-v", aliases = {"-version", "--version"}, usage = "print version information")
    public boolean version = false;

    public static void main(String[] args) {

        final PackageValidationApp application = new PackageValidationApp();

        CmdLineParser parser = new CmdLineParser(application);
        parser.setUsageWidth(80);

        try {
            parser.parseArgument(args);

            /* Handle general options such as help, version */
            if (application.help) {
                parser.printUsage(System.err);
                System.err.println();
                System.exit(0);
            } else if (application.version) {
                System.out.println(PackageValidationApp.class.getPackage()
                        .getImplementationVersion());
                System.exit(0);
            }

            /* Run the package generation application proper */
            System.exit(application.run());

        } catch (CmdLineException e) {
            /*
             * This is an error in command line args, just print out usage data
             * and description of the error.
             */
            System.out.println(e.getMessage());
            parser.printUsage(System.err);
            System.err.println();
            System.exit(1);
        }
    }

    public PackageValidationApp() {
        appContext =
                new ClassPathXmlApplicationContext(new String[] {"classpath*:org/dataconservancy/cli/config/applicationContext.xml"});
    }

    private int run() {
        PackageDescriptionBuilder builder =
                appContext.getBean("packageDescriptionBuilder",
                                   PackageDescriptionBuilder.class);

        int errorCount = 0;

        PackageDescriptionValidator validator =
                new PackageDescriptionValidator();

        for (Map.Entry<String, InputStream> content : ContentLoader
                .loadContentFrom(locations).entrySet()) {
            try {
                PackageDescription desc =
                        builder.deserialize(content.getValue());
                validator.validate(desc);
                System.out.println("Valid: " + content.getKey());
            } catch (PackageValidationException v) {
                System.err.println(String.format("Failed validation: %s, %s",
                                                 content.getKey(),
                                                 v.getMessage()));
                errorCount++;
            } catch (PackageToolException t) {
                System.err.println(String
                        .format("Failed deserialization: %s, %s",
                                content.getKey(),
                                t.getMessage()));
                errorCount++;
            }
        }

        return errorCount;
    }
}
