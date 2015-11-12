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

package org.dataconservancy.packaging.gui.services;

import org.dataconservancy.packaging.gui.util.FilenameValidator;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


/**
 * This service is responsible for checking all filenames in a filesystem tree which is being
 * considered for processing into a package, and flagging all filenames which violate naming conventions
 * which have been implemented in the interest of cross-platform compatibility.
 * This validator checks for validity of file paths as defined by the Data Conservancy BagIt Provile Version 1.0
 */
public class FilenameValidatorService {

    public FilenameValidatorService() {
        filenameValidator = new FilenameValidator();
    }

    private FilenameValidator filenameValidator;

    /**
     * This method takes a root path and checks for invalid names in the tree headed by the given path
     *
     * @param rootDirectoryPath the root path of the filesystem tree to be checked for invalid file names
     * @return a List of invalid file names, empty if all names are valid. Each entry in the list will have an invalid
     * character or reserved filename in the final path component. There will be one entry for each error to be fixed.
     * @throws IOException if the file at rootDirectoryPath cannot be found
     * @throws InterruptedException if the walk is interrupted
     */
    public final List<String> findInvalidFilenames(Path rootDirectoryPath) throws IOException, InterruptedException {
        List<String> invalidFilenames = new ArrayList<>();

        Files.walkFileTree(rootDirectoryPath, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs)
                    throws IOException {
                if (!filenameValidator.isValid(path.getFileName().toString()) || path.toString().length() > 1024) {
                    invalidFilenames.add(path.toString());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes mainAtts)
                    throws IOException {
                if (!filenameValidator.isValid(path.getFileName().toString()) || path.toString().length() > 1024) {
                    invalidFilenames.add(path.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return invalidFilenames;
    }
}
