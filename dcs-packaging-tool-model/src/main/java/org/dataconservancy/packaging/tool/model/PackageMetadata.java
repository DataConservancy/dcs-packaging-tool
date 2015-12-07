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

import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class captures information necessary to the package tool to know what and how prompt user for specific package
 * metadata.
 */
@XmlRootElement(name="packageMetadata")
public class PackageMetadata {

    @XmlEnum
    public enum Requiredness {
        REQUIRED,
        RECOMMENDED,
        OPTIONAL
    }

    private String name;
    private String label;
    private String defaultValue;
    private PropertyValueHint validationType;
    private String helpText;
    @XmlElement(name = "editable")
    private boolean isEditable;
    @XmlElement(name = "repeatable")
    private boolean isRepeatable;
    @XmlElement(name = "visible")
    private boolean isVisible;
    private Requiredness requiredness;

    /**
     * Indicates whether the field is editable.
     * @return {@code true} if the field is editable by user. {@code false} if the field is not editable by user.
     */
    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    /**
     * Returns the type of validation ({@link org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint})
     * that should be performed on the field's value. These types include: {@code NONE}, {@code EMAIL}, {@code PHONE_NUMBER},
     * {@code DATE}, {@code URL}, {@code FILENAME}.
     */
    public PropertyValueHint getValidationType() {
        return validationType;
    }

    public void setValidationType(PropertyValueHint validationType) {
        this.validationType = validationType;
    }

    /**
     * Returns the name of the metadata field.
     * @return the name of the field
     */
    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    /**
     * Returns the (display) label of the metadata field
     * @return label
     */
    public String getLabel() { return label; }

    public void setLabel(String label){ this.label = label;}

    /**
     * Returns the help text associated with the metadata field.
     * @return the help test associated with the metadata field
     */
    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    /**
     * Indicates whether this package metadata field is repeatable.
     * @return bollean idicating whether this package metadata is repeatable
     */
    public boolean isRepeatable() {
        return isRepeatable;
    }

    public void setRepeatable(boolean isRepeatable) {
        this.isRepeatable = isRepeatable;
    }

    /**
     * @return Indication of whether this package metadata field should be visible on the GUI
     */
    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    /**
     * Indicated whether this package metadata field is {@code REQUIRED}, {@code RECOMMENDED} or {@code OPTIONAL}
     */
    public Requiredness getRequiredness() {
        return requiredness;
    }

    public void setRequiredness(Requiredness requiredness) {
        this.requiredness = requiredness;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof PackageMetadata)) return false;

        PackageMetadata that = (PackageMetadata) o;

        if (isEditable != that.isEditable) return false;
        if (isRepeatable != that.isRepeatable) return false;
        if (isVisible != that.isVisible) return false;
        if (helpText != null ? !helpText.equals(that.helpText) : that.helpText != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (requiredness != that.requiredness) return false;
        if (validationType != that.validationType) return false;
        if (defaultValue == null && that.defaultValue != null) {
            return false;
        }
        if (defaultValue != null && !defaultValue.equals(that.defaultValue)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (validationType != null ? validationType.hashCode() : 0);
        result = 31 * result + (helpText != null ? helpText.hashCode() : 0);
        result = 31 * result + (isEditable ? 1 : 0);
        result = 31 * result + (isRepeatable ? 1 : 0);
        result = 31 * result + (isVisible ? 1 : 0);
        result = 31 * result + (requiredness != null ? requiredness.hashCode() : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }
}
