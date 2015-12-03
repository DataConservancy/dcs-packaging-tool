package org.dataconservancy.packaging.tool.cli;

import org.dataconservancy.packaging.tool.model.PackagingToolReturnInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;


/**
 * Test class for PackageGeneratorApp. Here we mainly test for correct exceptions being thrown in response to
 * user input errors. Exit status of 1 corresponds to a command line parser error. Other return codes are defined
 * in PackagingToolReturnInfo
 */
public class PackageGeneratorAppTest {

    private String contentRootDirectoryPath;
    private String domainProfileFilePath;
    private String badDomainProfileFilePath;
    private String packageGenerationParametersFilePath;
    private String packageMetadataFilePath;


    private PackageGenerationApp underTest = new PackageGenerationApp();

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().muteForSuccessfulTests();

    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        File contentRootDirectory = tmpfolder.newFolder("contentRoot");
        File domainProfileFile =  tmpfolder.newFile("domainProfile.ttl");
        File badDomainProfileFile = tmpfolder.newFile("domainProfile.moo");
        File packageGenerationParametersFile = tmpfolder.newFile("packageGenerationParameters");
        File packageMetadataFile = tmpfolder.newFile("packageMetadata");

        contentRootDirectoryPath = contentRootDirectory.getPath();
        domainProfileFilePath = domainProfileFile.getPath();
        badDomainProfileFilePath = badDomainProfileFile.getPath();
        packageGenerationParametersFilePath = packageGenerationParametersFile.getPath();
        packageMetadataFilePath = packageMetadataFile.getPath();
    }

    @Test
    public void testGoodRequiredFilePathArguments(){
        //expected status here corresponds to not being able to transform the tree to RDF
        //so we know the file name params were correctly specified on the command line
        exit.expectSystemExitWithStatus(PackagingToolReturnInfo.CMD_LINE_CANT_TRANSFORM_TO_RDF.returnCode());
        underTest.main(new String[]{contentRootDirectoryPath, domainProfileFilePath});
    }

        @Test
    public void testGoodRequiredAndOptionalFilePathArgumentsSucceeds() {
        //expected status here corresponds to not being able to transform the tree to RDF
        //so we know the file name params were correctly specified on the command line
        exit.expectSystemExitWithStatus(PackagingToolReturnInfo.CMD_LINE_CANT_TRANSFORM_TO_RDF.returnCode());
        underTest.main(new String[]{contentRootDirectoryPath, domainProfileFilePath, packageMetadataFilePath});
        }

    @Test
    public void testContentRootNotFound(){
        exit.expectSystemExitWithStatus(PackagingToolReturnInfo.CMD_LINE_FILE_NOT_FOUND_EXCEPTION.returnCode());
        underTest.main(new String[]{contentRootDirectoryPath + "moo", domainProfileFilePath});
    }

     @Test
    public void testBadDomainProfileFileExtension(){
        exit.expectSystemExitWithStatus(PackagingToolReturnInfo.CMD_LINE_BAD_DOMAIN_PROFILE_EXTENSION.returnCode());
        underTest.main(new String[]{contentRootDirectoryPath, badDomainProfileFilePath});
    }

    @Test
    public void testDomainProfileNotFound(){
        exit.expectSystemExitWithStatus(PackagingToolReturnInfo.CMD_LINE_FILE_NOT_FOUND_EXCEPTION.returnCode());
        underTest.main(new String[]{contentRootDirectoryPath, domainProfileFilePath + "baa"});
    }

    @Test
    public void testPackageMetadataFileNotFound(){
        exit.expectSystemExitWithStatus(PackagingToolReturnInfo.CMD_LINE_FILE_NOT_FOUND_EXCEPTION.returnCode());
        underTest.main(new String[]{contentRootDirectoryPath, domainProfileFilePath, packageMetadataFilePath + "oink"});
    }

    @Test
    public void testNoArguments() {
        exit.expectSystemExitWithStatus(1);
        underTest.main(new String[]{});
    }

    @Test
    public void testMissingDomainProfile() {
        exit.expectSystemExitWithStatus(1);
        underTest.main(new String[]{contentRootDirectoryPath});
    }

    @Test
    public void testBadPackageGenerationParametersFilePath(){
        exit.expectSystemExitWithStatus(PackagingToolReturnInfo.CMD_LINE_FILE_NOT_FOUND_EXCEPTION.returnCode());
        underTest.main(new String[]{contentRootDirectoryPath, domainProfileFilePath, packageGenerationParametersFilePath, "-g", "moo"});
    }

    @Test
    public void testBadPackageLocationFilePath(){
        exit.expectSystemExitWithStatus(PackagingToolReturnInfo.CMD_LINE_FILE_NOT_FOUND_EXCEPTION.returnCode());
        underTest.main(new String[]{contentRootDirectoryPath, domainProfileFilePath, packageMetadataFilePath, "-l", "moo"});
    }

    @Test
    public void testBadPackageStagingLocationFilePath(){
        exit.expectSystemExitWithStatus(PackagingToolReturnInfo.CMD_LINE_FILE_NOT_FOUND_EXCEPTION.returnCode());
        underTest.main(new String[]{contentRootDirectoryPath, domainProfileFilePath, packageMetadataFilePath, "--stage", "moo"});
    }

    @Test
    public void testMissingArgumentAfterFlag(){
        exit.expectSystemExitWithStatus(1);
        underTest.main(new String[]{contentRootDirectoryPath, domainProfileFilePath, packageMetadataFilePath, "--stage"});
    }

}