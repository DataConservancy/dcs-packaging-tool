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
package org.dataconservancy.packaging.tool.model;

public class PackageMetadata {

    public enum ValidationType {
        NONE,
        PHONE,
        EMAIL,
        URL,
        DATE,
        FILENAME,
    };

    private String name;
    private ValidationType validationType;
    private String helpText;
    private int minOccurrence;
    private int maxOccurrence;

    public String setName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValidationType(ValidationType validationType) {
        this.validationType = validationType;
    }

    public String getName() {
        return name;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public int getMinOccurrence() {
        return minOccurrence;
    }

    public void setMinOccurrence(int minOccurrence) {
        this.minOccurrence = minOccurrence;
    }

    public int getMaxOccurrence() {
        return maxOccurrence;
    }

    public void setMaxOccurrence(int maxOccurrence) {
        this.maxOccurrence = maxOccurrence;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof PackageMetadata)) return false;

        PackageMetadata that = (PackageMetadata) o;

        if (maxOccurrence != that.maxOccurrence) return false;
        if (minOccurrence != that.minOccurrence) return false;
        if (validationType != that.validationType) return false;
        if (helpText != null ? !helpText.equals(that.helpText) : that.helpText != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (validationType != null ? validationType.hashCode() : 0);
        result = 31 * result + (helpText != null ? helpText.hashCode() : 0);
        result = 31 * result + minOccurrence;
        result = 31 * result + maxOccurrence;
        return result;
    }
}
