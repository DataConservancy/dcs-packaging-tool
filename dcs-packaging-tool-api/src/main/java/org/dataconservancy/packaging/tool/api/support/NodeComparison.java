package org.dataconservancy.packaging.tool.api.support;

import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Class for storing the status of node comparisons along with a corresponding node.
 */
public class NodeComparison {

    public enum Status {
        ADDED,
        DELETED,
        UPDATED
    }

    private Status status;
    private Node node;

    /**
     * @param status The status of the comparison.
     * @param node An associated node that will be needed for merging the result of the comparison.
     */
    public NodeComparison(Status status, Node node) {
        this.status = status;
        this.node = node;
    }

    public Status getStatus() {
        return status;
    }

    public Node getNode() {
        return node;
    }

}
