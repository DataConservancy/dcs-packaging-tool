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
package org.dataconservancy.dcs.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates Format information detected from a bytestream
 */
public class DetectedFormat {

    private String id;
    private String name;
    private String version;
    private String mimeType;
    private List<String> possibleExtensions;

    public DetectedFormat() {
        possibleExtensions = new ArrayList<>();
    }

    /**
     * Returns {@code String} - Pronom Id of the bytestream format.
     * @return  the Pronom Id of the bytestream format
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns {@code String} - version information of the format
     * <p>
     * Returns {@code null} - when version information is not available.
     * @return   version information of the format, or {@code null} when the version information is not available.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns a list of {@code String} values for possible file extensions for this format
     * @return a list of {@code String} values for possible file extensions for this format
     */
    public List<String> getPossibleExtensions() {
        return possibleExtensions;
    }

    public void setPossibleExtensions(List<String> possibleExtensions) {
        this.possibleExtensions = possibleExtensions;
    }

    /**
     *
     * Returns {@code String} - text name of the format
     * @return the  text name of the format
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * Returns {@code String} - INANA mimetype of the format
     * @return the INANA mimetype of the format
     */
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DetectedFormat format = (DetectedFormat) o;

        if (!id.equals(format.id)) return false;
        if (!mimeType.equals(format.mimeType)) return false;
        if (!name.equals(format.name)) return false;
        if (possibleExtensions != null ? !possibleExtensions.equals(format.possibleExtensions) : format.possibleExtensions != null)
            return false;
        if (!version.equals(format.version)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (possibleExtensions != null ? possibleExtensions.hashCode() : 0);
        return result;
    }
}
