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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class captures information necessary to the package tool to know what and how prompt user for specific package
 * metadata.
 */
@XmlRootElement(name="packageMetadata")
public class PackageMetadata {

    @XmlEnum
    public enum ValidationType {
        NONE,
        PHONE,
        EMAIL,
        URL,
        DATE,
        FILENAME,
    };

    @XmlEnum
    public enum Requiredness {
        REQUIRED,
        RECOMMENDED,
        OPTIONAL
    }

    private String name;
    private ValidationType validationType;
    private String helpText;
    @XmlAttribute(name = "editable")
    private boolean isEditable;
    @XmlAttribute(name = "repeatable")
    private boolean isRepeatable;
    private Requiredness requiredness;

    /**
     * Indicates whether the field is editable.
     * @return {@code true} if the field is editable by user.
     * @return {@code false} if the field is not editable by user.
     */
    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    /**
     * Returns the type of validation ({@link org.dataconservancy.packaging.tool.model.PackageMetadata.ValidationType})
     * that should be performed on the field's value. These types include: {@code NONE}, {@code EMAIL}, {@code PHONE},
     * {@code DATE}, {@code URL}, {@code FILENAME}.
     */
    public ValidationType getValidationType() {
        return validationType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValidationType(ValidationType validationType) {
        this.validationType = validationType;
    }

    /**
     * Returns the name of the metadata field.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the help text associated with the metadata field.
     */
    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    /**
     * Indicate whether this package metadata field is repeatable.
     * @return
     */
    public boolean isRepeatable() {
        return isRepeatable;
    }

    public void setRepeatable(boolean isRepeatable) {
        this.isRepeatable = isRepeatable;
    }

    /**
     * Indicated whether this package metadata field is REQUIRED, RECOMMENDED or OPTIONAL
     */
    public Requiredness getRequiredness() {
        return requiredness;
    }

    public void setRequiredness(Requiredness requiredness) {
        this.requiredness = requiredness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof PackageMetadata)) return false;

        PackageMetadata that = (PackageMetadata) o;

        if (isEditable != that.isEditable) return false;
        if (isRepeatable != that.isRepeatable) return false;
        if (helpText != null ? !helpText.equals(that.helpText) : that.helpText != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (requiredness != that.requiredness) return false;
        if (validationType != that.validationType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (validationType != null ? validationType.hashCode() : 0);
        result = 31 * result + (helpText != null ? helpText.hashCode() : 0);
        result = 31 * result + (isEditable ? 1 : 0);
        result = 31 * result + (isRepeatable ? 1 : 0);
        result = 31 * result + (requiredness != null ? requiredness.hashCode() : 0);
        return result;
    }
}
