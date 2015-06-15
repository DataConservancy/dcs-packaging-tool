/*
 * Copyright 2014 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.impl.rules.operations;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.dataconservancy.dcs.model.DetectedFormat;
import org.dataconservancy.dcs.util.ContentDetectionService;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.packaging.tool.impl.rules.FileContext;
import org.dataconservancy.packaging.tool.impl.rules.FileOperation;
import org.dataconservancy.packaging.tool.impl.rules.ValueOperation;

/**
 * Get a value from file metadata
 * <p>
 * This will return file metadata for any file operands, or the current file
 * context if no operands are provided.
 * </p>
 */
public class Value_FileMetadata
        implements ValueOperation {

    private FileAttribute attr = FileAttribute.name;

    private FileOperation[] operands = new FileOperation[0];

    /** Determine the metadata attribute to extract */
    @Override
    public void setSpecifier(String attrSpec) {

        this.attr = FileAttribute.valueOf(attrSpec);
    }

    @Override
    public void setConstraints(FileOperation... constraints) {
        this.operands = constraints;
    }

    @Override
    public String[] operate(FileContext fileContext) {
        if (fileContext.getFile() == null || !fileContext.getFile().exists() || !fileContext.getFile().canRead()) {
            throw new OperationException("Pathname " + fileContext.getFile().getPath()
                    + " denotes a file that does not exist or cannot be read.");
        }
        ArrayList<String> values = new ArrayList<>();

        if (operands.length > 0) {
            for (FileOperation operand : operands) {
                for (File file : operand.operate(fileContext)) {
                    values.addAll(getAttr(file));
                }
            }
        } else {
            values.addAll(getAttr(fileContext.getFile()));
        }

        return values.toArray(new String[values.size()]);
    }

    private List<String> getAttr(File file) {
        BasicFileAttributes fileMetadata;
        try {
            fileMetadata =
                    Files.getFileAttributeView(file.toPath(),
                                               BasicFileAttributeView.class)
                            .readAttributes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<String> valuesList = new ArrayList<>();
        switch (attr) {
            case fileType:
                if (file.isDirectory()) {
                    valuesList.add("Directory");
                    return valuesList;
                } else if (file.isFile()) {
                    valuesList.add("File");
                    return valuesList;
                } else {
                    valuesList.add("Other");
                    return valuesList;
                }
            case size:
                valuesList.add(Long.toString(file.length()));
                return valuesList;
            case name:
                valuesList.add(file.getName());
                return valuesList;
            case createDate:
                valuesList.add(DateUtility.toIso8601_DateTimeNoMillis(new Date(fileMetadata.creationTime()
                        .toMillis())));
                return valuesList;
            case modifiedDate:
                valuesList.add(DateUtility.toIso8601_DateTimeNoMillis(new Date(fileMetadata.lastModifiedTime()
                        .toMillis())));
                return valuesList;
            case format:
                List<DetectedFormat> fileFormats = ContentDetectionService.getInstance().detectFormats(file);
                for (DetectedFormat format : fileFormats) {
                    valuesList.add(createFormatURIString(format));
                }
                return valuesList;
            default:
                throw new RuntimeException("Unexpected file attribute " + attr);
        }
    }

    public enum FileAttribute {
        /** File type: 'Directory', 'File' or 'Other' */
        fileType,

        /** File size in bytes */
        size,

        /** File name */
        name,

        /** Creation date */
        createDate,

        /** Last modified date */
        modifiedDate,

        /** File Format **/
        format
    }

    public enum FileType {
        Directory, File, Other
    }

    /**
     * Converts format id from the DcsFormat objects into formatURI string with qualifying namespace. Only applicable
     * to pronom format identifier at this point.
     * @param format the DetectedFormat object
     * @return a formatURI string with qualifying namespace
     */
    private String createFormatURIString(DetectedFormat format) {
        String formatString = "";
        if (format.getId() != null && !format.getId().isEmpty()) {
            formatString = "info:pronom/" + format.getId();
        } else if (format.getMimeType() != null && !format.getMimeType().isEmpty()) {
            formatString = format.getMimeType();
        }

        return formatString;
    }

}
