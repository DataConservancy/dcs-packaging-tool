package org.dataconservancy.packaging.tool.model.dprofile;

/**
 * A node constraint represents requirements on a node.
 */
public class NodeConstraint {
    private boolean matches_any;
    private boolean matches_none;
    private NodeType node_type;
    private StructuralRelation struct_rel;
    
    /**
     * @return Whether or not the constraint matches any node.
     */
    public boolean matchesAny() {
        return matches_any;
    }

    /**
     * @return Whether or not the constraint matches no node.
     */
    public boolean matchesNone() {
        return matches_none;
    }

    /**
     * Only used if matchesAny and matchesNone are false.
     * 
     * @return The constrained node must have one of these types.
     */
    public NodeType getNodeType() {
        return node_type;
    }

    /**
     * Only used if matchesAny and matchesNone are false.
     * 
     * @return The constrained node must be in at least one of these
     *         relationships.
     */
    public StructuralRelation getStructuralRelation() {
        return struct_rel;
    }

    /**
     * @param matches_any The matches any status to set.
     */
    public void setMatchesAny(boolean matches_any) {
        this.matches_any = matches_any;
    }

    /**
     * @param matches_none The matches none status to set
     */
    public void setMatchesNone(boolean matches_none) {
        this.matches_none = matches_none;
    }

    /**
     * @param node_type The node type to set.
     */
    public void setNodeType(NodeType node_type) {
        this.node_type = node_type;
    }

    /**
     * @param struct_rel The structural relationship to set.
     */
    public void setStructuralRelation(StructuralRelation struct_rel) {
        this.struct_rel = struct_rel;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (matches_any ? 1231 : 1237);
        result = prime * result + (matches_none ? 1231 : 1237);
        result = prime * result + ((node_type == null || node_type.getIdentifier() == null) ? 0 : node_type.getIdentifier().hashCode());
        result = prime * result + ((struct_rel == null) ? 0 : struct_rel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof NodeConstraint))
            return false;
        NodeConstraint other = (NodeConstraint) obj;
        if (matches_any != other.matches_any)
            return false;
        if (matches_none != other.matches_none)
            return false;
        if (node_type == null) {
            if (other.node_type != null)
                return false;
        } else if (node_type.getIdentifier() == null) {
            if (other.node_type.getIdentifier() != null)
                return  false;
        } else if (other.node_type == null || other.node_type.getIdentifier() == null || !node_type.getIdentifier().equals(other.node_type.getIdentifier()))
            return false;
        if (struct_rel == null) {
            if (other.struct_rel != null)
                return false;
        } else if (!struct_rel.equals(other.struct_rel))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NodeConstraint [matches_any=" + matches_any + ", matches_none=" + matches_none + ", node_type="
                + (node_type == null ? "" : node_type.getIdentifier()) + ", struct_rels=" + struct_rel + "]";
    }
}
