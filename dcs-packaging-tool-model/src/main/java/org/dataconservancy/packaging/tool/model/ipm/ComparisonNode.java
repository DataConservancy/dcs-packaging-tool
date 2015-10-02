package org.dataconservancy.packaging.tool.model.ipm;

import java.net.URI;

/**
 * A node that contains comparison state that's used when two trees are compared.
 */
public class ComparisonNode extends NodeImpl {

    public enum Status {
        ADDED,
        DELETED,
        UPDATED,
        UNCHANGED
    }

    /**
     * The status of the comparison node in relation to the existing tree.
     */
    private Status status;

    /**
     * The URI of the node in the existing tree if one exists(delete, update, unchanged), or null otherwise (add)
     */
    private URI relatedNode;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public URI getRelatedNode() {
        return relatedNode;
    }

    public void setRelatedNode(URI relatedNode) {
        this.relatedNode = relatedNode;
    }

}
