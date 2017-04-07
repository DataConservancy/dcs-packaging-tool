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

package org.dataconservancy.packaging.tool.impl.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This validator checks for validity of file path components as defined by the Data Conservancy BagIt Profile Version 1.0
 * To check a path for validity, a caller should apply this to each component in the path.
 *
 */
public class FilenameValidator implements Validator {

    private String windowsReservedNamesRegex = "^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])($|\\..*$)";
    private Pattern pattern = Pattern.compile(windowsReservedNamesRegex);
    private String blacklist = "<>:\"/\\|?*~";

    private int maxNameLength = 255;

    @Override
    public ValidatorResult isValid(String pathComponent) {
        Matcher matcher = pattern.matcher(pathComponent);
        ValidatorResult vr = new ValidatorResult();
        int v;

        if(pathComponent.equals(".")){
            vr.setResult(false);
            vr.setMessage(" path component may not be \".\"");
        } else if(pathComponent.equals("..")){
            vr.setResult(false);
            vr.setMessage(" path component may not be \"..\"");
        } else if(containsAny(pathComponent,blacklist)){
            vr.setResult(false);
            vr.setMessage(" path component contains an illegal character: one or more of " + blacklist);
        } else if(matcher.matches()){
            vr.setResult(false);
            vr.setMessage(String.format("'%s' is a reserved path component", pathComponent));
        } else if (pathComponent.length() > 255){
            vr.setResult(false);
            vr.setMessage(" path component is too long, may not exceed " + maxNameLength +" characters");
        } else if((v=containsIllegalUnicode(pathComponent)) >= 0){
            vr.setResult(false);
            vr.setMessage(String.format(" path component '%s' contains an illegal unicode character at index %s; " +
                    "this character may not be visible", pathComponent, v));
        } else {
            vr.setResult(true);
        }
        return vr;
    }

    private static boolean containsAny(String fileName, String blacklist) {
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            for (int j = 0; j < blacklist.length(); j++) {
                if (blacklist.charAt(j) == c) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int containsIllegalUnicode(String fileName) {
        int position = -1;
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            int j = (int) c;
            if (((Integer.parseInt("00", 16) <= j) && (j <= Integer.parseInt("1f", 16))) ||
                    (j >= Integer.parseInt("7f", 16))) { //0x7f is Delete
                position = i;
                break;
            }
        }
        return position;
    }
}
