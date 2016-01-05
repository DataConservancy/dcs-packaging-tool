package org.dataconservancy.packaging.tool.api.support;

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
