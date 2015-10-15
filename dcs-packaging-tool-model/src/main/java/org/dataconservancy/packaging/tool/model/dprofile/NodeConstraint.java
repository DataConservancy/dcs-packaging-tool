package org.dataconservancy.packaging.tool.model.dprofile;

import java.util.List;

/**
 * A node constraint represents requirements on a node.
 */
public class NodeConstraint {
    private boolean matches_any;
    private boolean matches_none;
    private List<NodeType> node_types;
    private List<StructuralRelation> struct_rels;
    
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
    public List<NodeType> getNodeTypes() {
        return node_types;
    }

    /**
     * Only used if matchesAny and matchesNone are false.
     * 
     * @return The constrained node must be in at least one of these
     *         relationships.
     */
    public List<StructuralRelation> getStructuralRelations() {
        return struct_rels;
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
     * @param node_types The node_types to set.
     */
    public void setNodeTypes(List<NodeType> node_types) {
        this.node_types = node_types;
    }

    /**
     * @param struct_rels The structural relationships to set
     */
    public void setStructuralRelations(List<StructuralRelation> struct_rels) {
        this.struct_rels = struct_rels;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (matches_any ? 1231 : 1237);
        result = prime * result + (matches_none ? 1231 : 1237);
        result = prime * result + ((node_types == null) ? 0 : node_types.hashCode());
        result = prime * result + ((struct_rels == null) ? 0 : struct_rels.hashCode());
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
        if (node_types == null) {
            if (other.node_types != null)
                return false;
        } else if (!node_types.equals(other.node_types))
            return false;
        if (struct_rels == null) {
            if (other.struct_rels != null)
                return false;
        } else if (!struct_rels.equals(other.struct_rels))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NodeConstraint [matches_any=" + matches_any + ", matches_none=" + matches_none + ", node_types="
                + node_types + ", struct_rels=" + struct_rels + "]";
    }
}
