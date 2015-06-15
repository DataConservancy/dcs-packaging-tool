/*
 * Copyright 2015 Johns Hopkins University
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

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.dataconservancy.packaging.tool.api.*;
import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.model.*;
import org.dataconservancy.packaging.tool.model.builder.json.JSONPackageDescriptionBuilder;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.net.URI;
import java.util.*;

/**
 * Application for generating a well formed package from a folder structure. The bags are only meant to be used in
 * performance testing.
 * <p>
 * The only input this app needs is a directory location. It has hard coded in package generation parameters, and builds
 * package solely on DCS Business Object ontology.
 * </p>
 *
 */
public class PseudoPackageGeneratorApp {
    private ClassPathXmlApplicationContext appContext;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static String bagItProfileId = "http://dataconservancy.org/formats/data-conservancy-pkg-0.9";

    private static String compressionFormat = CompressorStreamFactory.GZIP;
    private static String archivingFormat = ArchiveStreamFactory.TAR;
    /*
       *
       * Arguments
       */
    @Argument(multiValued = true, index = 0, metaVar = "[[infile] [outfile] [projectid]]", usage = "infile: content location - outfile: location of result package - projectid: id of target project")
    private List<String> args;

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

    public PseudoPackageGeneratorApp() {
        appContext = new ClassPathXmlApplicationContext(
                new String[] { "classpath*:org/dataconservancy/config/applicationContext.xml" });
    }

    public static void main(String[] args) {

        final PseudoPackageGeneratorApp application = new PseudoPackageGeneratorApp();

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
                System.out.println(PackageGenerationApp.class.getPackage()
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
            System.out.println(e.getMessage());
            parser.printUsage(System.err);
            System.err.println();
            System.exit(1);
        } catch (PackageToolException e){
            System.out.println(e.getMessage());
            //TODO: log stack trace if we decide to do logging here
            System.exit(e.getCode());
        }
    }

    private void run() throws PackageToolException {
        File contentRootDir = new File(args.get(0));
        File packageLocation = new File(args.get(1));
        String packageLocationName = packageLocation.getAbsolutePath();
        PackageGenerationParameters packageParams = new PackageGenerationParameters();
        packageParams.addParam(GeneralParameterNames.PACKAGE_FORMAT_ID, PackagingFormat.BOREM.toString());
        packageParams.addParam(GeneralParameterNames.PACKAGE_NAME, contentRootDir.getName());
        packageParams.addParam(GeneralParameterNames.PACKAGE_LOCATION, packageLocationName);
        packageParams.addParam(BagItParameterNames.BAGIT_PROFILE_ID, bagItProfileId);
        packageParams.addParam(BagItParameterNames.CONTACT_NAME, "Test bag creator");
        packageParams.addParam(BagItParameterNames.CONTACT_EMAIL, "Test bag creator email");
        packageParams.addParam(BagItParameterNames.CONTACT_PHONE, "Test bag creator phone");
        packageParams.addParam(GeneralParameterNames.CHECKSUM_ALGORITHMS, "md5");
        packageParams.addParam(BagItParameterNames.COMPRESSION_FORMAT, compressionFormat);
        packageParams.addParam(BagItParameterNames.EXTERNAL_IDENTIFIER, "External:id");
        packageParams.addParam(BagItParameterNames.BAG_COUNT, "1 of 1");
        packageParams.addParam(BagItParameterNames.BAG_GROUP_ID, "bag:group:id:1");
        packageParams.addParam(BagItParameterNames.PKG_BAG_DIR, contentRootDir.getName());

        PackageGenerationService generationService = appContext.getBean(
                "packageGenerationService", PackageGenerationService.class);

        JSONPackageDescriptionBuilder builder = appContext.getBean(
                "packageDescriptionBuilder", JSONPackageDescriptionBuilder.class);

        PackageDescription pd = new PackageDescription();
        pd.setPackageOntologyIdentifier(OntologyIdentifiers.DCSBO.toString());
        //has to be an existing project
        //String
        String projectId = args.get(2);
        if (projectId == null) {
            projectId = "http://bamboo.mse.jhu.edu:80/dcs-ui/project/4456451";
            System.out.println("Since no project id was provided, the package was produce to target project" +
                    "with id " + projectId);
        }
        //creating place folder project
        PackageArtifact project = createProject(projectId, contentRootDir.getName(),  null);
        pd.getPackageArtifacts().add(project);
        setUpPackages(pd, contentRootDir, projectId, true);
        builder.serialize(pd, System.out);
        Package pkg = generationService.generatePackage(pd, packageParams);
    }
    /* build package from the ground up */
    private void setUpPackages(PackageDescription pd, File file, String parentId, boolean isTopLevel) {

        //if a directory drill down recursively
        if (file.isDirectory()) {
            //creating relationship for containing data item.
            Set<String> relatedArtifacts = new HashSet<String>();
            relatedArtifacts.add(parentId);
            List<PackageRelationship> relationships = new ArrayList<PackageRelationship>();
            if (!isTopLevel) {
                relationships.add(new PackageRelationship(DcsPackageDescriptionSpec.Relationship.COLLECTION_IS_PART_OF_COLLECTION.toString(), true,
                        relatedArtifacts));
            } else {
                relationships.add(new PackageRelationship(DcsPackageDescriptionSpec.Relationship.COLLECTION_AGGREGATED_BY_PROJECT.toString(), true,
                        relatedArtifacts));
            }
            String collectionId = UUID.randomUUID().toString();
            PackageArtifact collection = createCollection(collectionId, file.getName(),  relationships);
            pd.getPackageArtifacts().add(collection);
            File [] lowerLevelFiles = file.listFiles();
            for (File lowerLevelFile : lowerLevelFiles) {
                setUpPackages(pd, lowerLevelFile, collectionId, false);
            }
        } else { //if a file then make a data item containing the file
            //creating relationship for containing data item.
            Set<String> relatedArtifacts = new HashSet<String>();
            relatedArtifacts.add(parentId);

            List<PackageRelationship> relationships = new ArrayList<PackageRelationship>();
            relationships.add(new PackageRelationship(DcsPackageDescriptionSpec.Relationship.DATA_ITEM_IS_MEMBER_OF_COLLECTION.toString(), true,
                    relatedArtifacts));

            //create containing data-item
            String dataItemId = UUID.randomUUID().toString();
            PackageArtifact dataItem = createDataItem(dataItemId, file.getName(), relationships);
            pd.getPackageArtifacts().add(dataItem);

            //creating relationship for data file
            relatedArtifacts = new HashSet<String>();
            relatedArtifacts.add(dataItemId);
            relationships = new ArrayList<PackageRelationship>();
            relationships.add(new PackageRelationship(DcsPackageDescriptionSpec.Relationship.DATA_FILE_IS_PART_OF_DATA_ITEM.toString(), true,
                    relatedArtifacts));

            PackageArtifact artifact = createDataFile("datafile:id:" + UUID.randomUUID().toString(), file.getPath(),  relationships);
            pd.getPackageArtifacts().add(artifact);
        }
    }

    private PackageArtifact createProject(String id, String projectName, List<PackageRelationship> relationships) {
        //Project artifact
        PackageArtifact artifact = new PackageArtifact();
        artifact.setId(id);
        artifact.setType(DcsPackageArtifactType.Project.name());
        File pathToProject = new File(projectName);
        String artifactRef = pathToProject.toURI().toString();
        artifact.setArtifactRef(artifactRef);
        artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.PROJECT_IDENTIFIER.toString(), id);
        addRelationships(artifact, relationships);
        return artifact;
    }

    private PackageArtifact createCollection(String id, String name, List<PackageRelationship> relationships) {
        PackageArtifact artifact = new PackageArtifact();
        artifact.setId(id);
        artifact.setType(DcsPackageArtifactType.Collection.name());
        File pathToCollection = new File("Collection");
        artifact.setArtifactRef(pathToCollection.toURI().toString());
        artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.COLLECTION_TITLE.toString(), "Test collection named " + name);
        artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.COLLECTION_CREATOR_NAME.toString(), "Sergent Yoda");
        //artifact.addProperty(DcsPackageDescriptionSpec.Property.COLLECTION_ID.toString(), id);
        artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.COLLECTION_SUMMARY.toString(), "Collection used in testing borem package generator");
        addRelationships(artifact, relationships);
        return artifact;
    }

    private PackageArtifact createDataItem(String id,  String name, List<PackageRelationship> relationships) {
        PackageArtifact artifact = new PackageArtifact();
        artifact.setId(id);
        artifact.setType(DcsPackageArtifactType.DataItem.name());
        File pathToDataItem = new File("DataItem");
        artifact.setArtifactRef(pathToDataItem.toURI().toString());
        artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.DATA_ITEM_NAME.toString(), "Test DataItem  for file " + name);
        //artifact.addProperty(DcsPackageDescriptionSpec.Property.DATA_ITEM_ID.toString(), id);
        artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.DATA_ITEM_DESCRIPTION.toString(), "Description for Test DataItem");

        addRelationships(artifact, relationships);
        return artifact;
    }

    private PackageArtifact createDataFile(String id, String path, List<PackageRelationship> relationships) {
        PackageArtifact artifact = new PackageArtifact();
        artifact.setId(id);
        artifact.setType(DcsPackageArtifactType.DataFile.name());
        File file = new File(path);
        URI test = file.toURI() ;
        artifact.setArtifactRef(test.toString());
        artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.FILE_NAME.toString(), path);
        artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.FILE_PATH.toString(), test.getPath());
        artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.FILE_FORMAT.toString(), "application/text");


        addRelationships(artifact, relationships);

        return artifact;
    }

    private PackageArtifact createMetadataFile(String id, String path, List<PackageRelationship> relationships) {
        PackageArtifact artifact = new PackageArtifact();
        artifact.setId(id);
        artifact.setType(DcsPackageArtifactType.MetadataFile.name());
        File file = new File(path);
        URI test = file.toURI() ;
        artifact.setArtifactRef(test.toString());
        artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.FILE_NAME.toString(), path);
        artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.FILE_PATH.toString(), test.getPath());
        artifact.addSimplePropertyValue(DcsPackageDescriptionSpec.Property.FILE_FORMAT.toString(), "application/text");

        addRelationships(artifact, relationships);

        return artifact;
    }

    private void addRelationships (PackageArtifact artifact, List<PackageRelationship> relationships) {
        if (relationships != null) {
            artifact.setRelationships(relationships);
        }
    }
}
