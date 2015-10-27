package org.dataconservancy.packaging.gui.services;

import org.dataconservancy.packaging.gui.Configuration;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This service is responsible for checking all filenames in a filesystem tree which is being
 * considered for processing into a package, and flagging all filenames which violate naming conventions
 * which have been implemented in the interest of cross-platform compatibility.
 */
public class FilenameValidatorService {

    public FilenameValidatorService(Configuration configuration) {
        this.blacklist = configuration.getPackageFilenameIllegalCharacters();
    }

    private String blacklist;
    private List<String> invalidFilenames;

    /**
     *
     * @param rootDirectoryPath the root of the filesystem tree to be checked for invalid file names
     * @return a List of invalid file names, empty if all names are valid. Each entry in the list will have an invalid
     *  character in the final path component. There will be one entry for each error to be fixed.
     * @throws IOException if the file at rootDirectoryPath cannot be found
     * @throws InterruptedException if the walk is interrupted
     */
    public final List<String> findInvalidFilenames(String rootDirectoryPath) throws IOException, InterruptedException {
        invalidFilenames = new ArrayList<>();
        Files.walkFileTree(Paths.get(rootDirectoryPath), new SimpleFileVisitor<Path>() {

            @Override
             public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs)
                 throws IOException{
                boolean matches = containsAny(path.getFileName().toString(), blacklist);
                if (matches) {
                    invalidFilenames.add(path.toString());
                }
                return FileVisitResult.CONTINUE;
             }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes mainAtts)
                    throws IOException {
                boolean matches = containsAny(path.getFileName().toString(), blacklist);
                if (matches) {
                    invalidFilenames.add(path.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return invalidFilenames;
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
