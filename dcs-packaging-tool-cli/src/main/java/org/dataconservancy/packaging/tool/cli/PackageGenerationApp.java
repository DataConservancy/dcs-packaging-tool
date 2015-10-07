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

import java.io.*;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.api.PackagingFormat;
import org.dataconservancy.packaging.tool.model.*;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Application for generating packages from package descriptions.
 * <p>
 * The most specific commandline arguments (i.e. <tt>-f format</tt>) override
 * less specific (format specified in params file indicated by by
 * <tt>-p param_file</tt>).
 * </p>
 * <p>
 * Required arguments are the file/directory containing PackageDescriptions, and
 * output file/directory that will contain the generated package. Ideally, the
 * application will support STDIN/STDOUT in addition to physical files. In an
 * ideal world, it would also support URLs too, at least for input
 * PackageDescriptions.
 * </p>
 * 
 * @author apb18@cornell.edu
 * 
 */
public class PackageGenerationApp {
	private ClassPathXmlApplicationContext appContext;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /*
	 * 
	 * Arguments
	 */
	@Argument(multiValued = true, index = 0, metaVar = "[infile]", usage = "package description file, omit to use stdin")
	private String location = null;

	/*
	 * 
	 * General Options
	 */
	/** Request for help/usage documentation */
	@Option(name = "-h", aliases = { "-help", "--help" }, usage = "print help message")
	public boolean help = false;

	/** Requests the current version number of the cli application. */
	@Option(name = "-v", aliases = { "-version", "--version" }, usage = "print version information")
	public boolean version = false;

    /** Requests for debugging info. */
	@Option(name = "-d", aliases = { "-debug", "--debug" }, usage = "print debug information")
	public boolean debug = false;

    /** Requests for parameter info */
    @Option(name = "-i", aliases = { "-info", "--info"}, usage = "print parameter info")
    public boolean info = false;

	/*
	 * 
	 * Package Generation Options
	 */
	/** Packaging format */
	@Option(name = "-f", aliases = { "--format" }, usage = "packaging format to use")
	public PackagingFormat pkgFormat = PackagingFormat.BOREM;

	/** Package Generation Params location */
	@Option(name = "-p", aliases = { "--generation-params" }, metaVar = "<file>", usage = "package generation params file location")
	public File packageGenerationParamsFile;

    /** Archive format **/
    @Option(name = "-a", aliases = { "--archiving-format"}, metaVar = "tar|zip", usage = "Archive format to use when creating the package.  Defaults to tar")
    public String archiveFormat;

    /** Compression format for tar archives **/
    @Option(name = "-c", aliases = { "--compression-format"}, metaVar = "gz|none", usage = "Compression format, if archive type is tar.  If not specified, no compression is used.  Ignored if non-tar archive is used.")
    public String compressionFormat;

    /** Checksum algorithms **/
    @Option(name = "-s", aliases = { "--checksum"}, metaVar = "md5|sha1", usage = "Checksum algorithms to use.  If none specified, will use md5.  Can be specified multiple times")
    public List<String> checksums;

    /** Package Name **/
    @Option(name = "-n", aliases = { "--name", "--package-name"}, metaVar = "<name>", usage = "The package name, which also determines the output filename.  Will override value in Package Generation Parameters file.")
    public String packageName;

    /** Package output location **/
    @Option(name = "-l", aliases = { "--location", "--package-location"}, metaVar = "<path>", usage = "The directory to which the package file will be written.  Will override value in Package Generation Parameters file.")
    public String packageLocation;

    /** Package staging location **/
    @Option(name = "--stage", aliases = { "--staging", "--staging-location", "--package-staging-location"}, metaVar = "<path>", usage = "The directory to which the package will be staged before building.  Will override value in Package Generation Parameters file.")
    public String packageStagingLocation;

    /** Package External-Id **/
    @Option(name = "--eid", aliases = { "--external-project-id"}, metaVar = "<URI>", usage = "The URI used as the external ID relating to the package.")
    public String externalProjectId;

    /** Force overwrite of target file **/
    @Option(name = "--overwrite", aliases = { "--force" }, usage = "If specified, will overwrite if the destination package file already exists without prompting.")
    public boolean overwriteIfExists = false;

    /** Write to stdout **/
    @Option(name = "--stdout", usage = "Write to stdout, instead of to a file.")
    public boolean stdout = false;


	public PackageGenerationApp() {
		appContext = new ClassPathXmlApplicationContext(
				new String[] { "classpath*:org/dataconservancy/cli/config/applicationContext.xml" });
	}

	public static void main(String[] args) {

		final PackageGenerationApp application = new PackageGenerationApp();

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
				System.err.println(PackageGenerationApp.class.getPackage()
						.getImplementationVersion());
				System.exit(0);
			}

			/* Run the package generation application proper */
			application.run();

		} catch (CmdLineException e) {
			/*
			 * This is an error in command line args, just print out usage data
			 * and description of the error.
			 */
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			System.err.println();
			System.exit(1);
		} catch (PackageToolException e){
            System.err.println(e.getMessage());
            System.exit(e.getCode());
        }
	}

	private void run() throws PackageToolException {
        boolean useDefaults = true;

		// Prepare parameter builder
        PackageGenerationParametersBuilder parametersBuilder = appContext.getBean("packageGenerationParametersBuilder",
                PackageGenerationParametersBuilder.class);

        // Load parameters first from default, then override with home directory .packageToolParams, then with
        // specified params file (if given).
        PackageGenerationParameters packageParams;

        try {
            packageParams = parametersBuilder.buildParameters(getClass().getResourceAsStream("/org/dataconservancy/cli/config/default.properties"));
            updateCompression(packageParams);
        } catch (ParametersBuildException e) {
            throw new PackageToolException(PackagingToolReturnInfo.CMD_LINE_PARAM_BUILD_EXCEPTION, e);
        }

        File userParamsFile = new File(System.getProperty("user.home") + File.pathSeparator + ".dataconservancy", "packageGenerationParameters");
        if (userParamsFile.exists()) {
            try {
                PackageGenerationParameters homeParams =
                        parametersBuilder.buildParameters(new FileInputStream(userParamsFile));

                System.err.println("Overriding generation parameters with values from standard 'packageGenerationParameters'");
                useDefaults = false;
                updateCompression(homeParams);
                packageParams.overrideParams(homeParams);
            } catch (FileNotFoundException e) {
                // Do nothing, it's ok to not have this file
            } catch (ParametersBuildException e) {
                throw new PackageToolException(PackagingToolReturnInfo.CMD_LINE_PARAM_BUILD_EXCEPTION, e);
            }
        }

        if (this.packageGenerationParamsFile != null) {

            try {
                PackageGenerationParameters fileParams = parametersBuilder.
                        buildParameters(new FileInputStream(this.packageGenerationParamsFile));

                System.err.println("Overriding generation parameters with values from " + this.packageGenerationParamsFile + " specified on command line");
                useDefaults = false;
                updateCompression(fileParams);
                packageParams.overrideParams(fileParams);
            } catch (ParametersBuildException e) {
                throw new PackageToolException(PackagingToolReturnInfo.CMD_LINE_PARAM_BUILD_EXCEPTION, e);
            } catch (FileNotFoundException e) {
                throw new PackageToolException(PackagingToolReturnInfo.CMD_LINE_FILE_NOT_FOUND_EXCEPTION, e);
            }
            if (debug) {
                log.debug("Parameters resulted from parsing file "
                        + this.packageGenerationParamsFile.getAbsoluteFile() + ": \n" + packageParams.toString());
            }
        }

        if (this.pkgFormat != null) {
            packageParams.addParam(GeneralParameterNames.PACKAGE_FORMAT_ID, this.pkgFormat.name());
        }

        // Finally, override with command line options
        // If any options overridden, this will cause useDefaults to become false, if it wasn't already
        PackageGenerationParameters flagParams = createCommandLinePrefs();
        if (!flagParams.getKeys().isEmpty()) {
            useDefaults = false;
            System.err.println("Overriding generation parameters using command line flags");
            updateCompression(flagParams);
            packageParams.overrideParams(flagParams);
        }

        // If nothing else overrode the defaults, say so
        if (useDefaults) {
            System.err.println("Using default values for all parameters");
        }

        // Get and load the Package Descirption file, use it to get the content root location
        // This will be overridden always as it needs to match what's on disk, so shouldn't really
        // be provided in the params files anyway
        PackageDescription desc = getPackageDescription();
        packageParams.removeParam(GeneralParameterNames.CONTENT_ROOT_LOCATION);
        packageParams.addParam(GeneralParameterNames.CONTENT_ROOT_LOCATION, desc.getRootArtifactRef().getRefString());

        // Print package generation parameters, if desired
        if (info) {
            for (String key : packageParams.getKeys()) {
                List<String> values = packageParams.getParam(key);
                System.err.println(key + ":  " + StringUtils.join(values, ", "));
            }
        }

        File outFile = null;

        // Generate the package
        PackageGenerationService generationService = appContext.getBean(
                "packageGenerationService", PackageGenerationService.class);

        Package pkg = generationService
                .generatePackage(desc, packageParams);

        // Write to the destination. do not write a package file if we have an exploded package
        if(!packageParams.getParam(GeneralParameterNames.ARCHIVING_FORMAT, 0).equals("exploded")) {
            if (pkg != null) {
                try {
                    if (pkg.isAvailable()) {
                        OutputStream os;
                        if (stdout) {
                            os = System.out;
                        } else {
                            outFile = getOutputFile(packageParams, pkg);

                            if (outFile == null) {
                                // This will be null if the user opted to not overwrite
                                System.err.println("Package Generation aborted...");
                                return;
                            }

                            System.err.println("Writing to file : " + outFile);
                            os = new FileOutputStream(outFile);
                        }
                        InputStream pkgStream = pkg.serialize();
                        IOUtils.copy(pkgStream, os);
                        os.close();
                        pkgStream.close();
                        pkg.cleanupPackage();
                    }
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new PackageToolException(PackagingToolReturnInfo.PKG_IO_EXCEPTION);
                }
            } else {
                throw new PackageToolException(PackagingToolReturnInfo.CMD_LINE_PACKAGE_NOT_GENERATED, "No package generated.");
            }
        }
    }

	/*
	 * Fetch the PackageDescription(s) based on the location specified in the
	 * user. This will be the first value in the this.locations list (well,
	 * technically "all except the last value", if multiple input
	 * PackageDescription files are specified. It would be nice to handle STDIN
	 * too (through the value '-' rather than file path).
	 */
	private PackageDescription getPackageDescription() {
        PackageDescription packageDescription;

		PackageDescriptionBuilder descriptionBuilder = appContext.getBean("packageDescriptionBuilder",
                PackageDescriptionBuilder.class);

        if (location == null || location.isEmpty()) {
            try {
                System.err.println("Reading Package Description from STDIN...");
                if (debug) {
                    log.debug("Loading description file from stdin.");
                }
                packageDescription = descriptionBuilder.deserialize(System.in);
            } catch (Exception e) {
                throw new PackageToolException(PackagingToolReturnInfo.CMD_LINE_INPUT_ERROR, e);
            }
        } else {
           try {
                if (debug) {
                    log.debug("Loading description file: " + location);
                }
                FileInputStream fis = new FileInputStream(location);
                packageDescription = descriptionBuilder.deserialize(fis);
           } catch (FileNotFoundException e) {
                throw new PackageToolException(PackagingToolReturnInfo.CMD_LINE_FILE_NOT_FOUND_EXCEPTION, e);
           }
        }

		return packageDescription;
	}


    /**
     * Create a PackageGenerationParameter for command line flags
     * @return a PackageGenerationParameter object with any command line overrides
     */
    private PackageGenerationParameters createCommandLinePrefs() {
        PackageGenerationParameters params = new PackageGenerationParameters();

        if (archiveFormat != null) {params.addParam(GeneralParameterNames.ARCHIVING_FORMAT, archiveFormat);}
        if (compressionFormat != null) {params.addParam(GeneralParameterNames.COMPRESSION_FORMAT, compressionFormat);}
        if (packageName != null) {
            params.addParam(GeneralParameterNames.PACKAGE_NAME, packageName);
            params.addParam(BagItParameterNames.PKG_BAG_DIR, packageName);
        }
        if (packageLocation != null) {params.addParam(GeneralParameterNames.PACKAGE_LOCATION, packageLocation);}
        if (packageStagingLocation != null) {params.addParam(GeneralParameterNames.PACKAGE_STAGING_LOCATION, packageStagingLocation);}
        if (externalProjectId != null) {params.addParam(GeneralParameterNames.EXTERNAL_PROJECT_ID, externalProjectId);}

        if (checksums != null && !checksums.isEmpty()) {
            params.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, checksums);
        }

        return params;
    }


    private File getOutputFile(PackageGenerationParameters params, Package pkg) {
        String name = pkg.getPackageName();
        String loc = params.getParam(GeneralParameterNames.PACKAGE_LOCATION, 0);

        File outfile = new File(loc, name);

        if (outfile.exists() && !overwriteIfExists) {
            Console c = System.console();
            String answer = "";

            if (c != null) {
                answer = c.readLine("File %s exists.  Do you wish to overwrite? (y/N) ", outfile);
            }

            // if they don't want to overwrite the output, find a file to write to by appending
            // a number to it
            if (answer.isEmpty() || !answer.toLowerCase().startsWith("y")) {
                int lastDot = name.lastIndexOf(".");
                int secondDot = name.lastIndexOf(".", lastDot-1);

                String namePart;
                String extPart;

                if (secondDot != -1 && lastDot - secondDot <= 4) {
                    namePart = name.substring(0, secondDot);
                    extPart = name.substring(secondDot);
                } else {
                    namePart = name.substring(0, lastDot);
                    extPart = name.substring(lastDot);
                }

                int i = 1;
                do {
                    outfile = new File(loc, namePart + "(" + i + ")" + extPart);
                    i += 1;
                } while (outfile.exists());
            }
        }

        return outfile;
    }


    /**
     * Update the compression format for the parameters, if necessary.
     * Basically, if the archive format is "zip", it should set the compression
     * format to "none" unless another format is explicitly set.
     * @param params The package generation params, used to get the file needed
     */
    private void updateCompression(PackageGenerationParameters params) {
        String archive = params.getParam(GeneralParameterNames.ARCHIVING_FORMAT, 0);
        String compress = params.getParam(GeneralParameterNames.COMPRESSION_FORMAT, 0);

        //manually set the compression to none if archive is ZIP and no compression
        // is specifically set in this object, or if archive is exploded
        if (archive != null && ((archive.equals(ArchiveStreamFactory.ZIP) && compress == null) || archive.equals("exploded"))) {
            params.addParam(GeneralParameterNames.COMPRESSION_FORMAT, "none");
        }
    }


}
