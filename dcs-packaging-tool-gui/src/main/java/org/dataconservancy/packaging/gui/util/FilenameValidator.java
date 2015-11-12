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

package org.dataconservancy.packaging.gui.util;

import org.dataconservancy.packaging.gui.Configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jrm on 11/12/15.
 */
public class FilenameValidator implements Validator {

    //private Configuration configuration;
    private String windowsReservedNamesRegex = "^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$";
    private Pattern pattern = Pattern.compile(windowsReservedNamesRegex);
    private String blacklist = "<>:\"/\\|?*";

    @Override
    public boolean isValid(String filename){
       return(!isInvalidFileName(filename));
    }

    protected boolean isInvalidFileName(String fileName){
        Matcher matcher = pattern.matcher(fileName);
        return containsAny(fileName, blacklist) || matcher.matches() || fileName.length() > 256;
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

   // private void setConfiguration(Configuration configuration){
  //      this.configuration = configuration;
   // }
}
