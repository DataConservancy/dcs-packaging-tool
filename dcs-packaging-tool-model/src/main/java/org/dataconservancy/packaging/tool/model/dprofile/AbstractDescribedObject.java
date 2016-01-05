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


public abstract class AbstractDescribedObject implements HasDescription {
    private String label;
    private String description;
    
    /**
     * @return The label
     */
    public String getLabel() {
        return label;
    }
    /**
     * @param label The label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }
    /**
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        return result;
    }

    /**
     * @param other The other AbstractDescribedObject to compare
     * @return Whether or not this object may be equal to the other
     */
    public boolean canEqual(Object other) {
        return (other instanceof AbstractDescribedObject);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AbstractDescribedObject))
            return false;
        AbstractDescribedObject other = (AbstractDescribedObject) obj;
        
        if (!other.canEqual(this))
            return false;
        
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "AbstractDescribedObject [label=" + label + ", description=" + description + "]";
    }
}
