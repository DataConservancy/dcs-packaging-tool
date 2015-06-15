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
package org.dataconservancy.packaging.tool.model;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A preference describes a {@code PackageGenerationParameter}. Each preference specifies a paramater's name,
 * parameter's values and the expected type of the parameter's value. Preference's name reflects the described parameter's
 * name. Preference's valueType specifies the range or domain to which the described parameter's value has to conform.
 * Preference's value specifies the described parameter's default values or collection of possible values.
 * </p>
 * These are the known value types:
 * <ul>
 *     <li>{@code free-text} - parameter's value of this type can have any sort of text.</li>
 *     <li>{@code email} - parameter's value of this type has to be a validly formed email address.</li>
 *     <li>{@code file} - parameter's value of this type has to be a a system file path.</li>
 *     <li>{@code enumerated-single} - parameter's value of this type has be one of the enumerated values</li>
 *     <li>{@code enumerated-multiple} - parameter's value of this type has to be one or more of the enumerated values</li>
 *     <li>{@code immutable} - parameter's value of this type  cannot be changed.
 *     </li>
 * </ul>
 */
public class Preference {
    public enum Type {
        FreeText ("free-text"),
        Email ("email"),
        File ("file"),
        EnumeratedSingle ("enumerated-single"),
        EnumeratedMultiple ("enumberated-multiple"),
        Immutable ("immutable");

        private String value;
        Type (String stringValue) {
            value = stringValue;
        }

        public String stringValue() {
            return value;
        }
    }
    private String name;
    private String valueType;
    private List<String> values;

    public Preference() {}

    public Preference(String name, String valueType, List<String> values) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Preference cannot have null or empty name");
        }
        this.name = name;
        this.valueType = valueType;
        this.values = values;
    }

    public Preference(String name, String valueType, String value) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Preference cannot have null or empty name");
        }
        this.name = name;
        this.valueType = valueType;
        this.values = new ArrayList<>();
        this.values.add(value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Preference cannot have null or empty name");
        }
        this.name = name;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public void addParameterValue(String parameterValue) {
        this.values.add(parameterValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Preference)) return false;

        Preference that = (Preference) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        if (valueType != null ? !valueType.equals(that.valueType) : that.valueType != null)
            return false;
        if (values != null ? !values.equals(that.values) : that.values != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (valueType != null ? valueType.hashCode() : 0);
        result = 31 * result + (values != null ? values.hashCode() : 0);
        return result;
    }
}
