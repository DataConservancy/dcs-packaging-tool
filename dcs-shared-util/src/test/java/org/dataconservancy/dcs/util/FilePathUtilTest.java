/*
 * Copyright 2012 Johns Hopkins University
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
package org.dataconservancy.dcs.util;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the File Path Util class. Tests success and failure conditions of the different utilities
 */
public class FilePathUtilTest {

    @ClassRule
    public static TemporaryFolder tmpFolder = new TemporaryFolder();

    /**
     * Tests that a file with windows slashes correctly has all slashes flipped to be unix style forward slashes.
     */
    @Test
    public void testConvertWindowsSlashToUnixSlash() {
        final String expectedPath = "/dir/test/file.txt";
        final String resultPath = FilePathUtil.convertToUnixSlashes("\\dir\\test\\file.txt");
        assertTrue("Expected: " + expectedPath + " but was: " + resultPath, expectedPath.equalsIgnoreCase(resultPath));
    }

    /**
     * Tests that if a file path already has unix slashes it's returned unchanged.
     */
    @Test
    public void testConvertUnixSlashToUnixSlashUnchanged() {
        final String expectedPath = "/dir/test/file.txt";
        final String resultPath = FilePathUtil.convertToUnixSlashes("/dir/test/file.txt");
        assertTrue("Expected: " + expectedPath + " but was: " + resultPath, expectedPath.equalsIgnoreCase(resultPath));
    }

    /**
     * Tests that if a path has windows slashes it can be converted to platform specific slashes.
     * If run on windows the strings should be unchanged.
     */
    @Test
    public void testConvertWindowsPathToPlatformSlashes() {
        final String expectedPath = File.separator + "dir" + File.separator + "test" + File.separator + "file.txt";
        final String resultPath = FilePathUtil.convertToPlatformSpecificSlash("\\dir\\test\\file.txt");
        assertTrue("Expected: " + expectedPath + " but was: " + resultPath, expectedPath.equalsIgnoreCase(resultPath));
    }

    /**
     * Tests that if a path has unix style slashes it can be converted to platform specific slashes.
     * If run on unix based platforms the strings should be unchanged.
     */
    @Test
    public void testConvertUnixPathToPlatformSlashes() {
        final String expectedPath = File.separator + "dir" + File.separator + "test" + File.separator + "file.txt";
        final String resultPath = FilePathUtil.convertToPlatformSpecificSlash("/dir/test/file.txt");
        assertTrue("Expected: " + expectedPath + " but was: " + resultPath, expectedPath.equalsIgnoreCase(resultPath));
    }

    /**
     * Tests that if a path has mixed slashes it can be converted to use all platform specific slashes.
     */
    @Test
    public void testMixedSlashPathToPlatformSlashes() {
        final String expectedPath = File.separator + "dir" + File.separator + "test" + File.separator + "file.txt";
        final String resultPath = FilePathUtil.convertToPlatformSpecificSlash("\\dir\\test/file.txt");
        assertTrue("Expected: " + expectedPath + " but was: " + resultPath, expectedPath.equalsIgnoreCase(resultPath));
    }

    /**
     * Tests that if a file is already the relative it's unchanged by the relativize method.
     */
    @Test
    public void testRelativizeAlreadyRelativeFile() throws IOException {
        final String expectedPath = File.separator + "test" + File.separator + "file.txt";
        final File relativeFile = new File("/test/file.txt");
        final File directory = tmpFolder.newFolder(UUID.randomUUID().toString());
        final String resultPath = FilePathUtil.relativizePath(directory.getPath(), relativeFile);

        assertTrue("Expected: " + expectedPath + " but was: " + resultPath, expectedPath.equalsIgnoreCase(resultPath));
    }

    /**
     * Tests that if a file is absolute it's made relative.
     */
    @Test
    public void testRelativizeAbsoluteFile() throws IOException {
        final String expectedPath = "relative" + File.separator + "file.txt";
        final File directory = tmpFolder.newFolder(UUID.randomUUID().toString());

        final File relativeFile = new File(directory, "/relative/file.txt");
        final String resultPath = FilePathUtil.relativizePath(directory.getPath(), relativeFile);

        assertTrue("Expected: " + expectedPath + " but was: " + resultPath, expectedPath.equalsIgnoreCase(resultPath));
    }

    /**
     * Tests that if a file doesn't contain the base path then the original path is returned.
     */
    @Test
    public void testRelativizeFileNotUnderBasePath() throws IOException {
        final File testFile = new File("foo", "test.file");
        final String expectedPath = testFile.getPath();
        final File directory = tmpFolder.newFolder(UUID.randomUUID().toString());

        final String resultPath = FilePathUtil.relativizePath(directory.getPath(), testFile);

        assertTrue("Expected: " + expectedPath + " but was: " + resultPath, expectedPath.equalsIgnoreCase(resultPath));
    }

    /**
     * Tests that if null is passed in as the file to relativize null is returned.
     */
    @Test
    public void testRelativizeNullFile() {
        assertNull(FilePathUtil.relativizePath("foo", null));
    }

    /**
     * Test that if the base file is null the original file is returned.
     */
    @Test
    public void testRelativizeNullBaseFile() throws IOException {
        final String expectedPath = "relative" + File.separator + "file.txt";

        final File relativeFile = new File("relative/file.txt");
        final String resultPath = FilePathUtil.relativizePath(null, relativeFile);

        assertTrue("Expected: " + expectedPath + " but was: " +
                       resultPath, expectedPath.equalsIgnoreCase(resultPath));
    }

    /**
     * Tests that the prepend file uri prefix method will not change a string that already has the prefix.
     * Also tests that if a file prefix doesn't have the correct number of slashes the correct string will be returned.
     */
    @Test
    public void testFileAlreadyContainingFilePrefixHasCorrectSlashes() {
        final String correctFileURI = "file://dir/test/file";
        final String correctWindowsURI = "file:///c:/dir/test/file";

        final String twoSlashURI = "file://dir/test/file";
        //This is really the reason for this class behavior this is an invalid URI that needs to be have an extra slash
        final String twoSlashWindowsURI = "file://c:/dir/test/file";

        final String singleSlashWindowsURI = "file:/c:/dir/test/file";
        final String singleSlashURI = "file:/dir/test/file";

        String returnedFileURI = FilePathUtil.prependFileUriPrefix(correctFileURI);
        assertTrue("Expected: " + correctFileURI + " but was: " + returnedFileURI, returnedFileURI.equalsIgnoreCase(correctFileURI));

        returnedFileURI = FilePathUtil.prependFileUriPrefix(correctWindowsURI);
        assertTrue("Expected: " + correctWindowsURI + " but was: " + returnedFileURI, returnedFileURI.equalsIgnoreCase(correctWindowsURI));

        returnedFileURI = FilePathUtil.prependFileUriPrefix(twoSlashURI);
        assertTrue("Expected: " + correctFileURI + " but was: " + returnedFileURI, returnedFileURI.equalsIgnoreCase(correctFileURI));

        returnedFileURI = FilePathUtil.prependFileUriPrefix(twoSlashWindowsURI);
        assertTrue("Expected: " + correctWindowsURI + " but was: " + returnedFileURI, returnedFileURI.equalsIgnoreCase(correctWindowsURI));

        returnedFileURI = FilePathUtil.prependFileUriPrefix(singleSlashWindowsURI);
        assertTrue("Expected: " + correctWindowsURI + " but was: " + returnedFileURI, returnedFileURI.equalsIgnoreCase(correctWindowsURI));

        //A single slash uri on unix should be unchanged.
        returnedFileURI = FilePathUtil.prependFileUriPrefix(singleSlashURI);
        assertTrue("Expected: " + correctFileURI + " but was: " + returnedFileURI, returnedFileURI.equalsIgnoreCase(singleSlashURI));
    }

    /**
     * Tests that a file can be updated to contain the file uri prefix.
     */
    @Test
    public void testFilePrefixCorrectlyAdded() {
        final String correctFileURI = "file://dir/test/file";
        final String correctWindowsURI = "file:///c:/dir/test/file";

        final String filePathStartingWithSlash = "/dir/test/file";
        final String filePathNoStartingSlash = "dir/test/file";
        final String windowsFilePath = "c:/dir/test/file";

        String returnedFileURI = FilePathUtil.prependFileUriPrefix(filePathStartingWithSlash);
        assertTrue("Expected: " + correctFileURI + " but was: " + returnedFileURI, returnedFileURI.equalsIgnoreCase(correctFileURI));

        returnedFileURI = FilePathUtil.prependFileUriPrefix(filePathNoStartingSlash);
        assertTrue("Expected: " + correctFileURI + " but was: " + returnedFileURI, returnedFileURI.equalsIgnoreCase(correctFileURI));

        returnedFileURI = FilePathUtil.prependFileUriPrefix(windowsFilePath);
        assertTrue("Expected: " + correctWindowsURI + " but was: " + returnedFileURI, returnedFileURI.equalsIgnoreCase(correctWindowsURI));
    }

    /**
     * Tests that prefixing a null string returns null
     */
    @Test
    public void testPrefixNullFile() {
        assertNull(FilePathUtil.prependFileUriPrefix(null));
    }

    /**
     * Tests that trying to get the file extension of a directory returns an empty string
     */
    @Test
    public void testGetFileExtensionOfDirectory() {
        String unixPath = "//foo/bar/baz";
        String windowsPath = "C:/foo/bar/baz";

        String extension = FilePathUtil.getFileExtension(unixPath);
        assertTrue("Expected empty string but was: " + extension, extension.isEmpty());

        extension = FilePathUtil.getFileExtension(windowsPath);
        assertTrue(
            "Expected empty string but was: " + extension, extension.isEmpty());
    }

    /**
     * Tests that trying to get the file extension of a non path string returns an empty string
     */
    @Test
    public void testGetFileExtensionOfNonPathString() {
        String nonPath = "How now brown cow";
        String nonPathWithPeriod = "The brown cow jumps over the yellow moon. And moos. Alot";

        String extension = FilePathUtil.getFileExtension(nonPath);
        assertTrue("Expected empty string but was: " + extension, extension.isEmpty());

        extension = FilePathUtil.getFileExtension(nonPathWithPeriod);
        assertTrue("Expected empty string but was: " + extension, extension.isEmpty());
    }

    /**
     * Tests that getFile extension returns the correct file extension
     */
    @Test
    public void testGetFileExtension() {
        String justFileName = "foo.txt";
        String pathWithFileName = "foo/bar/baz.jpg";

        String extension = FilePathUtil.getFileExtension(justFileName);
        assertTrue("Expected .txt but was: " + extension, extension.equalsIgnoreCase(".txt"));

        extension = FilePathUtil.getFileExtension(pathWithFileName);
        assertTrue("Expected .jpg but was: " + extension, extension.equalsIgnoreCase(".jpg"));
    }

    /**
     * Tests that files with multiple extensions have both returned
     */
    @Test
    public void testGetMultipleFileExtensions() {
        String justFileName = "foo.txt.bak";
        String pathWithFileName = "foo/bar/baz.tar.gz";

        String extension = FilePathUtil.getFileExtension(justFileName);
        assertTrue("Expected .txt.bak but was: " + extension, extension.equalsIgnoreCase(".txt.bak"));

        extension = FilePathUtil.getFileExtension(pathWithFileName);
        assertTrue("Expected .tar.gz but was: " + extension, extension.equalsIgnoreCase(".tar.gz"));
    }

    /**
     * Tests that the service stops looking for extensions after the first failure
     */
    @Test
    public void testGetExtensionStopsAtNotUnderstoodExtension() {
        String justFileName = "foo.txt.foo";

        String extension = FilePathUtil.getFileExtension(justFileName);
        assertTrue("Expected empty string: " + extension, extension.isEmpty());
    }

    /**
     * Tests that if file name contains a period it's not included as the extension
     */
    @Test
    public void testGetExtensionWithPeriodsInName() {
        String nameWithPeriods = "test.pd.name.txt.jpg.tar";
        String extension = FilePathUtil.getFileExtension(nameWithPeriods);
        assertTrue("Expected .txt.bak but was: " + extension, extension.equalsIgnoreCase(".txt.jpg.tar"));
    }

    /**
     * Tests that getting the extension of a null string returns null.
     */
    @Test
    public void testGetExtensionOfNullString() {
        assertNull(FilePathUtil.getFileExtension(null));
    }

    /**
     * Tests that trying to get the file extension of a directory returns an empty string
     */
    @Test
    public void testGetLastFileExtensionOfDirectory() {
        String unixPath = "//foo/bar/baz";
        String windowsPath = "C:/foo/bar/baz";

        String extension = FilePathUtil.getLastFileExtension(unixPath);
        assertTrue("Expected empty string but was: " + extension, extension.isEmpty());

        extension = FilePathUtil.getLastFileExtension(windowsPath);
        assertTrue(
            "Expected empty string but was: " + extension, extension.isEmpty());
    }

    /**
     * Tests that trying to get the file extension of a non path string returns an empty string
     */
    @Test
    public void testGetLastFileExtensionOfNonPathString() {
        String nonPath = "How now brown cow";
        String nonPathWithPeriod = "The brown cow jumps over the yellow moon. And moos. Alot";

        String extension = FilePathUtil.getLastFileExtension(nonPath);
        assertTrue("Expected empty string but was: " + extension, extension.isEmpty());

        extension = FilePathUtil.getLastFileExtension(nonPathWithPeriod);
        assertTrue("Expected empty string but was: " + extension, extension.isEmpty());
    }

    /**
     * Tests that getFile extension returns the correct file extension
     */
    @Test
    public void testGetLastFileExtension() {
        String justFileName = "foo.txt";
        String pathWithFileName = "foo/bar/baz.jpg";

        String extension = FilePathUtil.getLastFileExtension(justFileName);
        assertTrue("Expected .txt but was: " + extension, extension.equalsIgnoreCase(".txt"));

        extension = FilePathUtil.getLastFileExtension(pathWithFileName);
        assertTrue("Expected .jpg but was: " + extension, extension.equalsIgnoreCase(".jpg"));
    }

    /**
     * Tests that files with multiple extensions have both returned
     */
    @Test
    public void testMultipleFileExtensionsGetLast() {
        String justFileName = "foo.txt.bak";
        String pathWithFileName = "foo/bar/baz.tar.gz";

        String extension = FilePathUtil.getLastFileExtension(justFileName);
        assertTrue("Expected .txt.bak but was: " + extension, extension.equalsIgnoreCase(".bak"));

        extension = FilePathUtil.getLastFileExtension(pathWithFileName);
        assertTrue("Expected .tar.gz but was: " + extension, extension.equalsIgnoreCase(".gz"));
    }

    /**
     * Tests that if file name contains a period it's not included as the extension
     */
    @Test
    public void testGetLastExtensionWithPeriodsInName() {
        String nameWithPeriods = "test.pd.name.txt.jpg.tar";
        String extension = FilePathUtil.getLastFileExtension(nameWithPeriods);
        assertTrue("Expected .tar but was: " + extension, extension.equalsIgnoreCase(".tar"));
    }

    /**
     * Tests that getting the extension of a null string returns null.
     */
    @Test
    public void testGetLastExtensionOfNullString() {
        assertNull(FilePathUtil.getLastFileExtension(null));
    }

    /**
     * Tests that removing the file extension from a directory returns an unmodified string.
     */
    @Test
    public void testRemoveExtensionFromDirectory() {
        String unixPath = "//foo/bar/baz";
        String windowsPath = "C:/foo/bar/baz";

        String filePath = FilePathUtil.removeFileExtension(unixPath);
        assertTrue("Expected:" + unixPath + " but was: " + filePath, unixPath.equalsIgnoreCase(filePath));

        filePath = FilePathUtil.removeFileExtension(windowsPath);
        assertTrue("Expected:" + windowsPath + " but was: " +
                       filePath, windowsPath.equalsIgnoreCase(filePath));
    }

    /**
     * Tests that removing a file extension from a non file string returns the unmodified string.
     */
    @Test
    public void testRemoveExtensionNonFileString() {
        String nonPath = "How now brown cow";
        String nonPathWithPeriod = "The brown cow jumps over the yellow moon. And moos. Alot";

        String path = FilePathUtil.removeFileExtension(nonPath);
        assertTrue("Expected: " + nonPath + " but was: " + path, path.equalsIgnoreCase(nonPath));

        path = FilePathUtil.removeFileExtension(nonPathWithPeriod);
        assertTrue("Expected: " + nonPathWithPeriod + " but was: " + path, path.equalsIgnoreCase(nonPathWithPeriod));
    }

    /**
     * Tests that single file extensions are correctly removed.
     */
    @Test
    public void testRemoveFileExtension() {
        String justFileName = "foo.txt";
        String pathWithFileName = "foo/bar/baz.jpg";

        String name = FilePathUtil.removeFileExtension(justFileName);
        assertTrue("Expected foo but was: " + name, name.equalsIgnoreCase("foo"));

        name = FilePathUtil.removeFileExtension(pathWithFileName);
        assertTrue("Expected foo/bar/baz but was: " + name, name.equalsIgnoreCase("foo/bar/baz"));
    }

    /**
     * Tests that multiple file extensions are all removed.
     */
    @Test
    public void testRemoveAllFileExtensions() {
        String justFileName = "foo.txt.bak";
        String pathWithFileName = "foo/bar/baz.tar.gz";

        String name = FilePathUtil.removeFileExtension(justFileName);
        assertTrue("Expected foo but was: " + name, name.equalsIgnoreCase("foo"));

        name = FilePathUtil.removeFileExtension(pathWithFileName);
        assertTrue("Expected foo/bar/baz but was: " + name, name.equalsIgnoreCase("foo/bar/baz"));
    }

    /**
     * Tests that removing the extension from a null file is null.
     */
    @Test
    public void testRemoveNullExtensionReturnsNull() {
        assertNull(FilePathUtil.removeFileExtension(null));
    }

    /**
     * Tests that if file name contains a period it's not included as the extension
     */
    @Test
    public void testRemoveExtensionWithPeriodsInName() {
        String nameWithPeriods = "test.pd.name.txt.jpg.tar";
        String name = FilePathUtil.removeFileExtension(nameWithPeriods);
        assertTrue("Expected test.pd.name but was: " +
                       name, name.equalsIgnoreCase("test.pd.name"));
    }

    /**
      * Tests that removing the file extension from a directory returns an unmodified string.
      */
     @Test
     public void testRemoveLastExtensionFromDirectory() {
         String unixPath = "//foo/bar/baz";
         String windowsPath = "C:/foo/bar/baz";

         String filePath = FilePathUtil.removeLastFileExtension(unixPath);
         assertTrue("Expected:" + unixPath + " but was: " + filePath, unixPath.equalsIgnoreCase(filePath));

         filePath = FilePathUtil.removeLastFileExtension(windowsPath);
         assertTrue("Expected:" + windowsPath + " but was: " + filePath, windowsPath.equalsIgnoreCase(filePath));
     }

     /**
      * Tests that removing a file extension from a non file string returns the unmodified string.
      */
     @Test
     public void testRemoveLastExtensionNonFileString() {
         String nonPath = "How now brown cow";
         String nonPathWithPeriod = "The brown cow jumps over the yellow moon. And moos. Alot";

         String path = FilePathUtil.removeLastFileExtension(nonPath);
         assertTrue("Expected: " + nonPath + " but was: " + path, path.equalsIgnoreCase(nonPath));

         path = FilePathUtil.removeLastFileExtension(nonPathWithPeriod);
         assertTrue("Expected: " + nonPathWithPeriod + " but was: " + path, path.equalsIgnoreCase(nonPathWithPeriod));
     }

     /**
      * Tests that single file extensions are correctly removed.
      */
     @Test
     public void testRemoveLastFileExtension() {
         String justFileName = "foo.txt";
         String pathWithFileName = "foo/bar/baz.jpg";

         String name = FilePathUtil.removeLastFileExtension(justFileName);
         assertTrue("Expected foo but was: " + name, name.equalsIgnoreCase("foo"));

         name = FilePathUtil.removeLastFileExtension(pathWithFileName);
         assertTrue("Expected foo/bar/baz but was: " + name, name.equalsIgnoreCase("foo/bar/baz"));
     }

     /**
      * Tests that multiple file extensions are all removed.
      */
     @Test
     public void testRemoveLastFileExtensionMultipleExtensions() {
         String justFileName = "foo.txt.bak";
         String pathWithFileName = "foo/bar/baz.tar.gz";

         String name = FilePathUtil.removeLastFileExtension(justFileName);
         assertTrue("Expected foo.txt but was: " + name, name.equalsIgnoreCase("foo.txt"));

         name = FilePathUtil.removeLastFileExtension(pathWithFileName);
         assertTrue("Expected foo/bar/baz.tar but was: " + name, name.equalsIgnoreCase("foo/bar/baz.tar"));
     }

     /**
      * Tests that removing the extension from a null file is null.
      */
     @Test
     public void testRemoveLastExtensionNullReturnsNull() {
         assertNull(FilePathUtil.removeLastFileExtension(null));
     }

    /**
     * Tests that null is returned if a null base file is passed to absolutize
     */
    @Test
    public void testAbsolutizeNullBaseFile() throws IOException {
        File testFile = tmpFolder.newFile(UUID.randomUUID().toString());
        assertNull(FilePathUtil.absolutize(null, testFile));
    }

    /**
     * Tests that null is returned if the file to absolutize is null
     */
    @Test
    public void testAbsolutizeNullFile() throws IOException {
        File testFolder = tmpFolder.newFolder(UUID.randomUUID().toString());
        assertNull(FilePathUtil.absolutize(testFolder, null));
    }

    /**
     * Tests that if the file is already absolute absolutize returns the same file.
     */
    @Test
    public void testAbsolutizeAlreadyAbsoluteFile() throws IOException {
        final File testDir = tmpFolder.newFolder(UUID.randomUUID().toString());
        final File testFile = new File(testDir, "testFile");

        File returnedFile = FilePathUtil.absolutize(testDir, testFile);

        assertTrue("Expected: " + testFile.getPath() + " but was: " +
                       returnedFile.getPath(), testFile.getPath().equalsIgnoreCase(returnedFile.getPath()));
    }

    /**
     * Tests that a file is correctly made absolute by prepending the base directory to it.
     */
    @Test
    public void testAbsolutizeFile() throws IOException {
        final File testDir = tmpFolder.newFolder(UUID.randomUUID().toString());
        final File testFile = new File("testFile");
        final String expectedPath = testDir.getPath() + File.separator + testFile.getPath();
        File returnedFile = FilePathUtil.absolutize(testDir, testFile);

        assertTrue("Expected: " + expectedPath + " but was: " + returnedFile.getPath(), expectedPath.equalsIgnoreCase(returnedFile.getPath()));
    }

    /**
     * Tests that if the file path is null convert to Classpath URL returns null
     */
    @Test
    public void testConvertNull() {
        assertNull(FilePathUtil.convertToClasspathUrl(null, "classpath:/test/file"));
    }

    /**
     * Tests that if the classpath base is null, the string is still properly converted.
     */
    @Test
    public void testConvertNullBaseClasspath() {
        final String expectedPath = "classpath:/dir/test/file";
        final String filePath = "/dir/test/file";

        String returnedPath = FilePathUtil.convertToClasspathUrl(filePath, null);

        assertTrue("Expected: " + expectedPath + " but was: " +
                       returnedPath, expectedPath.equalsIgnoreCase(returnedPath));
    }

    /**
     * Tests that if the classpath base is empty, the string is still properly converted.
     */
    @Test
    public void testConvertEmptyBaseClasspath() {
        final String expectedPath = "classpath:/dir/test/file";
        final String filePath = "/dir/test/file";

        String returnedPath = FilePathUtil.convertToClasspathUrl(filePath, "");

        assertTrue("Expected: " + expectedPath + " but was: " + returnedPath, expectedPath.equalsIgnoreCase(returnedPath));
    }

    /**
     * Tests that if the classpath base isn't in the file path the file string is still properly converted.
     */
    @Test
    public void testConvertNotFoundBaseClasspath() {
        final String expectedPath = "classpath:/dir/test/file";
        final String filePath = "/dir/test/file";
        final String classpathBase = "/foo/bar/";
        String returnedPath = FilePathUtil.convertToClasspathUrl(filePath, classpathBase);

        assertTrue("Expected: " + expectedPath + " but was: " + returnedPath, expectedPath.equalsIgnoreCase(returnedPath));
    }

    /**
     * Tests that if the file starts with a file uri prefix it's correctly converted.
     */
    @Test
    public void testConvertPathStartingWithFilePrefix() {
        final String expectedPath = "classpath:/foo/bar/dir/test/file";
        final String filePath = "file://foo/bar/dir/test/file";
        final String classpathBase = "/foo/bar/";
        String returnedPath = FilePathUtil.convertToClasspathUrl(filePath, classpathBase);

        assertTrue("Expected: " + expectedPath + " but was: " + returnedPath, expectedPath.equalsIgnoreCase(returnedPath));
    }

    /**
     * Tests that if the file already starts with the classpath prefix it's returned unedited.
     */
    @Test
    public void testConvertClasspathFile() {
        final String expectedPath = "classpath:/dir/test/file";
        final String filePath = "classpath:/dir/test/file";
        final String classpathBase = "/foo/bar/";
        String returnedPath = FilePathUtil.convertToClasspathUrl(filePath, classpathBase);

        assertTrue("Expected: " + expectedPath + " but was: " + returnedPath, expectedPath.equalsIgnoreCase(returnedPath));
    }

    /**
     * Tests that a standard file path is correctly converted.
     */
    @Test
    public void testConvertClasspath() {
        final String expectedPath = "classpath:/foo/bar/dir/test/file";
        final String filePath = "/foo/bar/dir/test/file";
        final String classpathBase = "/foo/bar/";
        String returnedPath = FilePathUtil.convertToClasspathUrl(filePath, classpathBase);

        assertTrue("Expected: " + expectedPath + " but was: " + returnedPath, expectedPath.equalsIgnoreCase(returnedPath));

        final String expectedWindowsPath = "classpath:/C:/foo/bar/dir/test/file";
        final String windowsFilePath = "C:/foo/bar/dir/test/file";
        final String windowsClasspathBase = "C:/foo/bar/";
        returnedPath = FilePathUtil.convertToClasspathUrl(windowsFilePath, windowsClasspathBase);

        assertTrue("Expected: " + expectedWindowsPath + " but was: " + returnedPath, expectedWindowsPath.equalsIgnoreCase(returnedPath));
    }

    @Test
    public void testIllegalCharacterDetectedInPath() throws IOException {
        final File testFile = tmpFolder.newFolder("badFileName:oops");
        Assert.assertFalse(FilePathUtil.hasValidFilePath(testFile));
    }

    /**
     * Expect paths to be returned as is if no illegal characters are found
     */
    @Test
    public void testSanitizeCleanPaths() {
        assertTrue("abc".equalsIgnoreCase(FilePathUtil.sanitizePath("abc")));
        assertTrue("a/b/c".equalsIgnoreCase(FilePathUtil.sanitizePath("a/b/c")));
    }

    /**
     * Expect bad characters to removed from paths
     */
    @Test
    public void testSanitizeDirtyPaths() {
        assertTrue("C/".equalsIgnoreCase(FilePathUtil.sanitizePath("C:/")));
        assertTrue("Foo/Bar".equalsIgnoreCase(FilePathUtil.sanitizePath("Fo:?o/B?*a\"r")));

    }


}
