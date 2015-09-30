package org.dataconservancy.packaging.tool.model.dprofile;

import java.util.List;

/**
 * A node constraint represents requirements on a node.
 */
public interface NodeConstraint {
    /**
     * @return Whether or not the constraint matches any node.
     */
    boolean matchesAny();

    /**
     * @return Whether or not the constraint matches no node.
     */
    boolean matchesNone();

    /**
     * Only used if matchesAny and matchesNone are false.
     * 
     * @return The constrained node must have one of these types.
     */
    List<NodeType> getNodeTypes();

    /**
     * Only used if matchesAny and matchesNone are false.
     * 
     * @return The constrained node must be in at least one of these
     *         relationships.
     */
    List<StructuralRelation> getStructuralRelations();
}
