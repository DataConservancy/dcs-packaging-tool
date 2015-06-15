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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.dataconservancy.dcs.model.DetectedFormat;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling File Path creation and editing
 */
public class FilePathUtil {

    private static final String FILE_URL_PREFIX = "file:/";

    private static final String CLASSPATH_PREFIX = "classpath:/";

    private static final Pattern FILE_URL_PATTERN = Pattern.compile("^" + FILE_URL_PREFIX + ".*");

    private static final Pattern CLASSPATH_URL_PATTERN = Pattern.compile("^" + CLASSPATH_PREFIX + ".*");

    private static final Pattern ABS_PATH_PATTERN = Pattern.compile("^/.*");

    /**
     * This method has been deprecated use Apache FileNameUtils instead.
     * Takes the file path string and converts all slashes to unix style '/'.
     * @param filePath The original file path to convert slashes on.
     * @return The updated file path will all slashes in the form '/', or null if filePath is null.
     */
    @Deprecated
    public static String convertToUnixSlashes(String filePath) {
        return FilenameUtils.separatorsToUnix(filePath);
    }

    /**
     * This method has been deprecated use Apache FileNameUtils instead.
     * Takes the provided file path and converts all slashes to the platform specific slash.
     * @param filePath The file path to convert the slashes on.
     * @return The file path with slash converted to the platform specific slashes, or the original filepath string if
     * slashes aren't used as the file separator character. Returns null if the file path object is null.
     */
    @Deprecated
    public static String convertToPlatformSpecificSlash(String filePath) {
        return FilenameUtils.separatorsToSystem(filePath);
    }

    /**
     * This method has been deprecated use the Java Path relativize method instead.
     * Strips the base directory out of a file path to make a relative file path. If the file doesn't contain the base path the original path is returned.
     * @param basePath The base directory path to remove from the file path.
     * @param toRelativize The file to be made relative.
     * @return The relative file path created by removing the base directory. Returns null if the file to relative is null.
     */
    @Deprecated
    public static String relativizePath(String basePath, File toRelativize) {

        if (toRelativize == null) {
            return null;
        }

        if (basePath == null
            || !toRelativize.getPath().startsWith(basePath)) {
            return toRelativize.getPath();
        }

        Path path = FileSystems.getDefault().getPath(basePath);
        Path relativePath = path.relativize(toRelativize.toPath());


        return relativePath.toString();
    }

    /**
     * Takes a file path string and prepends the correct file URI prefix.
     * @param filePath The file path to prepend the correct file URI prefix to.
     * @return The file path with the file uri prefix prepended, or null if the file path is null.
     */
    public static String prependFileUriPrefix(String filePath) {
        if (filePath == null) {
            return null;
        }

        if (filePath.startsWith(FILE_URL_PREFIX)) {
            //File path contains drive letter
            if (filePath.matches("file:/*((?i)(?s)[A-Z]):.*")) {
                //Check that it has the triple slash
                if (filePath.charAt(6) != '/') {
                    return filePath.replace("file:/", "file:///");
                }
                if (filePath.charAt(7) != '/') {
                    return filePath.replace("file://", "file:///");
                }
            } 

            //If none of the clauses was caught the file path is valid
            return filePath;
        } else {
            String uriSlash = "";
            //File path contains drive letter
            if (filePath.matches("((?i)(?s)[A-Z]):.*")) {
                uriSlash = "//";
            } else if (!filePath.startsWith("/")) {
                uriSlash = "/";
            }
            return FILE_URL_PREFIX + uriSlash + filePath;
        }
    }

    /**
     * Takes a file path and returns the extension of the file, or an empty string if the file is a directory.
     * If the file has two extensions (i.e. .tar.gz) both will be returned.
     * @param filePath The file path pointing to the file to return the extension for.
     * @return The extension of the file pointed to by the provided file path including the preceding '.', or null if the provided file path is null.
     */
    public static String getFileExtension(String filePath) {
        if (filePath == null) {
            return null;
        }

        String extension = "";

        while (filePath.contains(".")) {
            String potentialExtension = filePath.substring(filePath.lastIndexOf("."));
            //Try to get a list of formats associated with extension minus the period. We use this to determine if the content is period
            List<DetectedFormat> formats = ContentDetectionService.getInstance().getApplicableFormats(potentialExtension.substring(1));
            if (null != formats) {
                extension = potentialExtension + extension;
            } else {
                //Stop at the first none format string
                break;
            }

            filePath = filePath.substring(0, filePath.lastIndexOf("."));
        }
        return extension;
    }

    /**
     * Get the last file extension from a file. If the file has multiple extensions (i.e. .tar.gz) on the last (.gz) will be returned.
     * If the string provided has no extensions an empty string will be returned.
     * @param filePath The file path to get the last extension for.
     * @return The last extension of the file pointed to by the provided file path including the preceding '.', or null if the provided file path is null.
     */
    public static String getLastFileExtension(String filePath) {
        if (filePath == null) {
            return null;
        }

        String extension = "";

        if (filePath.contains(".")) {
            String potentialExtension = filePath.substring(filePath.lastIndexOf("."));
            //Try to get a list of formats associated with extension minus the period. We use this to determine if the content is period
            List<DetectedFormat> formats = ContentDetectionService.getInstance().getApplicableFormats(potentialExtension.substring(1));
            if (null != formats) {
                extension = potentialExtension + extension;
            }
        }
        return extension;
    }

    /**
     * Takes a file path, or file name and removes the extension from the file. If the file has two extensions (i.e. .tar.gz) both will be removed.
     * @param filePath The file path pointing to the file to remove the extension from.
     * @return The original file path with the extension removed, or null if the orignal file path was null.
     */
    public static String removeFileExtension(String filePath) {
        if (filePath == null) {
            return null;
        }

        String extension = getFileExtension(filePath);
        return filePath.substring(0, filePath.length() - extension.length());
    }

    /**
     * Removes just the last extension from a file. If the file has multiple extensions (i.e. .tar.gz) on the first (.gz) will be removed. If the string
     * passed in contains no extensions it will be returned unchanged.
     * @param filePath The file path pointing to the file to remove the extension from.
     * @return The original file path with th extension removed, or null if the original file path was null.
     */
    public static String removeLastFileExtension(String filePath) {
        if (filePath == null) {
           return null;
        }

        String extension = getLastFileExtension(filePath);
        return filePath.substring(0, filePath.length() - extension.length());
    }

    /**
     * This method has been deprecated you should now use Java's Path.resolve method.
     * Takes a relative file path and makes it absolute by prepending the base path.
     * If the provided file path is already absolute it's returned without modification.
     * @param baseFile The base file that will be used to make the file path absolute.
     * @param toAbsolutize The file to make absolute by adding it to the file base
     * @return A java File object pointing to the absolute path to the file, or null if the file object, or base file is null.
     */
    @Deprecated
    public static File absolutize(File baseFile, File toAbsolutize) {
        if (toAbsolutize == null || baseFile == null) {
            return null;
        }

        // If the file is already absolute, there's nothing for us to do here.
        if (toAbsolutize.isAbsolute()) {
            return toAbsolutize;
        }

        Path basePath = baseFile.toPath().resolve(toAbsolutize.toPath());


        return basePath.toFile();
    }

    /**
     * Transforms the file path into a classpath resource urls.
     * <p>
     * If the {@code filePath} starts with a forward slash or if it starts with 'file:/' URL, the text up to the
     * first occurrence of {@code classpathBase} is stripped.  The text "classpath:" is prefixed to {@code filePath},
     * and returned.  If {@code filePath} already starts with "classpath:", then no transformation occurs.
     *
     * @param filePath the file path string
     * @param classpathBase the classpath base to preserve
     * @return the transformed source string, or null if the file path is null.
     */
    public static String convertToClasspathUrl(String filePath, String classpathBase) {
        if (filePath == null) {
            return null;
        }

        final Matcher startsWithFile = FILE_URL_PATTERN.matcher(filePath);
        final Matcher startsWithClasspath = CLASSPATH_URL_PATTERN.matcher(filePath);

        if (startsWithClasspath.matches()) {
            // no need to transform
            return filePath;
        }

        if (startsWithFile.matches()) {
            filePath = filePath.substring(FILE_URL_PREFIX.length());
        }

        if (classpathBase != null) {
            int baseIndex = filePath.indexOf(classpathBase);
            if (baseIndex > 0) {
                filePath = filePath.substring(baseIndex);
            }
        }

        final Matcher startsWithSlash = ABS_PATH_PATTERN.matcher(filePath);
        //Handle windows file paths
        if (!startsWithSlash.matches()) {
            filePath = "/" + filePath;
        }

        return "classpath:" + filePath;
    }

    /**
     * This method checks whether a file path contains any illegal characters.
     * @param file the file to check
     * @return boolean true if the path is OK, false otherwise
     */
    public static boolean hasValidFilePath(File file){
        Path path = file.toPath();
        for(int i=0; i<path.getNameCount(); i++){
            if (StringUtils.containsAny(path.getName(i).toString(), getCharacterBlackList())){
                return false;
            }
        }
        return true;
    }

    /**
     * This method provides an error string for illegal characters in file names
     * @param file the file with the bad path
     * @return a String to be used in constructing an error message
     */
    public static String getInvalidFilnameErrorString(File file){
        StringBuilder sb = new StringBuilder();
        sb.append("Path element in ");
        sb.append(file.getPath());
        sb.append(" contains an invalid character: one or more of ");
        sb.append(getCharacterBlackList());
        sb.append("\n");
        return sb.toString();
    }

    /**
     * This method provides a String containing invalid characters for path elements. The
     * bagit specification wants us to avoid these for Windows file names: section 7.2.2
     * @return a string containing the illegal characters
     */
    public static String getCharacterBlackList() {
        return "<>:\"/|?*";
    }

}
