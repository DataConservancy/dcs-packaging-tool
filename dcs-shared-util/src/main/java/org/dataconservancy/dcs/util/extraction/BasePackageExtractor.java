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
package org.dataconservancy.dcs.util.extraction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;
import org.dataconservancy.dcs.util.FilePathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePackageExtractor implements PackageExtractor {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected String extractDirectory;

    protected StringBuilder invalidFilePathErrorStringBuilder = new StringBuilder();

    @Override
    public void setExtractDirectory(String dir) {
        extractDirectory = dir;
    }

    @Override
    public List<File> getFilesFromPackageFile(String baseDir, File file)
            throws UnpackException {

        String packageDir = createParentDir(baseDir, extractDirectory);

        List<File> fileList;
        try {
            fileList = unpackFilesFromArchive(file, packageDir);
        } catch (Exception e) {
            final String message = "Error unpacking provided file [" + file.getName() + "]. It was " +
                    "either empty, or a not a known package file type";
            UnpackException ue = new UnpackException(message, e);
            ue.setError(message);
            ue.setFilename(file.getName());
            throw ue;
        }

        if(invalidFilePathErrorStringBuilder.length() == 0) {
            return fileList;
        } else {//we have invalid file names in the package
         UnpackException ue = new UnpackException(invalidFilePathErrorStringBuilder.toString());
            ue.setFilename(file.getName());
            throw ue;
        }
    }

    @Override
    public List<File> getFilesFromPackageStream(String baseDir, String fileName, InputStream packageStream) throws UnpackException {
        String packageDir = createParentDir(baseDir, extractDirectory);

        List<File> fileList;

        if (packageStream != null) {

            try {
                fileList = unpackFilesFromStream(packageStream, packageDir, fileName);
            } catch (Exception e) {
                final String message = "Error unpacking package file " + fileName + " was either empty, or a not " +
                        "a known package file type";
                UnpackException ue = new UnpackException(message, e);
                ue.setError(message);
                ue.setFilename(fileName);
                throw ue;
            }
        } else {
            final String message = "Error unpacking archive file " + fileName + " was null";
            UnpackException ue = new UnpackException(message);
            ue.setError(message);
            ue.setFilename(fileName);
            throw ue;
        }

        try {
            packageStream.close();
        } catch (IOException e) {
            final String message = "Error unpacking package file " + fileName + " was either empty, or a not " +
                    "a a known package file type";
            UnpackException ue = new UnpackException(message, e);
            ue.setError(message);
            ue.setFilename(fileName);
            throw ue;
        }

        if(invalidFilePathErrorStringBuilder.length() == 0) {
            return fileList;
        } else { //we have invalid file names in the package
            UnpackException ue = new UnpackException(invalidFilePathErrorStringBuilder.toString());
            ue.setFilename(fileName);
            throw ue;
        }
    }

    public String createParentDir(String baseDir, String dir) {
        String packageDir = dir;
        if (dir != null && !dir.isEmpty()) {
            if (!dir.endsWith("/") && !dir.endsWith("\\")) {
                dir += '/';
            }

            if (baseDir != null && !baseDir.isEmpty()) {
                packageDir = dir + "/" + baseDir;
            }

            File tempDir = new File(dir);
            tempDir.mkdirs();
        } else {
            if (baseDir != null && !baseDir.isEmpty()) {
                packageDir = "./" + baseDir;
            } else {
                packageDir = "./";
            }
        }

        File extractDir = new File(packageDir);
        extractDir.mkdirs();

        return packageDir;
    }

    /**
     * Extracts a list of files from the archive file
     *
     * @param archive the archive File
     * @param packageDir a string representing the package directory
     * @return the list of archived files
     * @throws Exception if an exception occurs during unpacking
     */
    protected abstract List<File> unpackFilesFromArchive(File archive, String packageDir) throws Exception;

    protected String truncatePackageExtension(String fileName) {
        return FilePathUtil.removeFileExtension(fileName);
    }

    protected List<File> saveExtractedFile(File extractedFile, InputStream extractedContent) throws IOException {
        List<File> files = new ArrayList<>();

        if(FilePathUtil.hasValidFilePath(extractedFile)) {
            if (extractedFile.getParent() != null && !extractedFile.getParent().isEmpty()) {
                //Create a temp directory of the parent for copying files
                File tempDir = new File(extractedFile.getParent());
                if (!tempDir.exists()) {
                    createParentDirectory(tempDir, files);
                }
            }

            FileOutputStream fileOut = new FileOutputStream(extractedFile);

            IOUtils.copy(extractedContent, fileOut);
            fileOut.close();
            files.add(extractedFile);
        } else {
            invalidFilePathErrorStringBuilder.append(FilePathUtil.getInvalidFilnameErrorString(extractedFile));
        }

        return files;
    }

    private void createParentDirectory(File dir, List<File> files) {
        if(FilePathUtil.hasValidFilePath(dir)) {
            File parentFile = dir.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                createParentDirectory(parentFile, files);
            }
            dir.mkdirs();
            files.add(dir);
        } else {
            invalidFilePathErrorStringBuilder.append(FilePathUtil.getInvalidFilnameErrorString(dir));
        }
    }

    @Override
    public void cleanUpExtractedPackage(File dir) {
        if (dir != null && dir.exists()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    cleanUpExtractedPackage(file);
                } else {
                    file.delete();
                }
            }

            dir.delete();
        }
        invalidFilePathErrorStringBuilder.setLength(0);//clear string builder for next package use
    }

    @Override
    public String getExtractDirectory() {
        return extractDirectory;
    }

}