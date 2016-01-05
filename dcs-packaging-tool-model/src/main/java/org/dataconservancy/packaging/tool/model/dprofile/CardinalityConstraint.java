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
 * Represents a constraint based on the cardinality of some item.
 */
public class CardinalityConstraint {
    private int min;
    private int max;

    /**
     * @return Minimum number of required occurrences.
     */
    public int getMinimum() {
        return min;
    }

    /**
     * @param min The minimum bound of the cardinality constraint.
     */
    public void setMin(int min) {
        this.min = min;
    }

    /**
     * @return Maximum number of required occurrences or -1 for unbounded.
     */
    public int getMaximum() {
        return max;
    }

    /**
     * @param max The maximum bound of the cardinality constraint.
     */
    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + max;
        result = prime * result + min;
        return result;
    }
    
    /**
     * @param other The other object to be checked if it can equal this CardinalityConstraint.
     * @return Whether or not this object may be equal to the other
     */
    public boolean canEqual(Object other) {
        return (other instanceof CardinalityConstraint);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof CardinalityConstraint))
            return false;
        CardinalityConstraint other = (CardinalityConstraint) obj;
        
        if (!other.canEqual(this))
            return false;

        if (max != other.max)
            return false;
        if (min != other.min)
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "CardinalityConstraint [min=" + min + ", max=" + max + "]";
    }
}
