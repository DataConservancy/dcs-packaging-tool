package org.dataconservancy.packaging.tool.model.dprofile;

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
     * @param min
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
     * @param max
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
     * @param other 
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
