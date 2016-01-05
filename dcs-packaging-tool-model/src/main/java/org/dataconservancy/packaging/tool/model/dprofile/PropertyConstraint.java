package org.dataconservancy.packaging.tool.model.dprofile;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


/**
 * A PropertyConstraint represents restrictions on the cardinality and type of a
 * property in a node.
 */
public class PropertyConstraint extends CardinalityConstraint {
    private PropertyType type;

    /**
     * @return Required property type
     */
    public PropertyType getPropertyType() {
        return type;
    }

    public void setPropertyType(PropertyType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /**
     * @param other The other object to check if it can be equal to PropertyConstraint
     * @return Whether or not this object may be equal to the other
     */
    public boolean canEqual(Object other) {
        return (other instanceof PropertyConstraint);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof PropertyConstraint))
            return false;
        PropertyConstraint other = (PropertyConstraint) obj;
        
        if (!other.canEqual(this))
            return false;

        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PropertyConstraint [type=" + type + "] {" + super.toString() + "}";
    }
}
