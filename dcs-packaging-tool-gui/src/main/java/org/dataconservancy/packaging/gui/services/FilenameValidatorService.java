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

import org.dataconservancy.packaging.gui.Configuration;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This service is responsible for checking all filenames in a filesystem tree which is being
 * considered for processing into a package, and flagging all filenames which violate naming conventions
 * which have been implemented in the interest of cross-platform compatibility.  This service as currently implemented
 * will test for conformance with the Bagit version 0.97 specification section 7.2.2. The windows filenames are hard coded in
 * this class, but we will allow other characters to differ from the default set &lt; &gt; : " / | ? *  via configuration.
 */
public class FilenameValidatorService {

    public FilenameValidatorService(Configuration configuration) {
        this.blacklist = configuration.getPackageFilenameIllegalCharacters();
    }

    private String blacklist;
    private String windowsReservedNamesRegex = "^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$";
    private Pattern pattern = Pattern.compile(windowsReservedNamesRegex);

    /**
     * This method takes a root path and checks for invalid names in the tree headed by the given path
     *
     * @param rootDirectoryPath the root path of the filesystem tree to be checked for invalid file names
     * @return a List of invalid file names, empty if all names are valid. Each entry in the list will have an invalid
     *  character or reserved filename in the final path component. There will be one entry for each error to be fixed.
     * @throws IOException if the file at rootDirectoryPath cannot be found
     * @throws InterruptedException if the walk is interrupted
     */
    public final List<String> findInvalidFilenames(Path rootDirectoryPath) throws IOException, InterruptedException {
        List<String> invalidFilenames = new ArrayList<>();

        Files.walkFileTree(rootDirectoryPath, new SimpleFileVisitor<Path>() {

            @Override
             public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs)
                 throws IOException{
                if (isInvalidPathComponent(path.getFileName().toString())) {
                    invalidFilenames.add(path.toString());
                }
                return FileVisitResult.CONTINUE;
             }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes mainAtts)
                    throws IOException {
                if (isInvalidPathComponent(path.getFileName().toString())) {
                    invalidFilenames.add(path.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return invalidFilenames;
    }

    protected boolean isInvalidPathComponent(String fileName){
        Matcher matcher = pattern.matcher(fileName);
        return containsAny(fileName, blacklist) || matcher.matches();
    }

    private static boolean containsAny(String fileName, String blacklist) {
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            for (int j = 0; j < blacklist.length(); j++) {
                if ( blacklist.charAt(j) == c) {
                    return true;
                }
            }
        }
        return false;
    }
}
