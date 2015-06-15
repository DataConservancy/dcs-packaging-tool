/*
 * Copyright 2014 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.packaging.tool.model;


import java.util.HashMap;
import java.util.Map;

/**
 * Represents a hierarchy of PackageArtifacts arranged as nodes in a tree structure.
 * 
 * Holds a reference to the root node and a mapping from strings to nodes. The keys of the map should be ids of PackageArtifacts 
 * and the values should be the corresponding nodes holding those PackageArtifacts. The nodes must be nodes in the tree.
 */
public class PackageTree {
    private PackageNode root;
    private Map<String, PackageNode> nodesMap;

    public PackageTree() {
        this.nodesMap = new HashMap<String, PackageNode>();
    }

    /**
     * @return root of tree
     */
    public PackageNode getRoot() {
        return root;
    }

    public void setRoot(PackageNode root) {
        this.root = root;
    }

    /**
     * @return Mapping from artifact ids to nodes in this tree
     */
    public Map<String, PackageNode> getNodesMap() {
        return nodesMap;
    }

    /**
     * Set mapping from artifact ids to nodes in this tree.
     * 
     * @param nodesMap the mapping from artifact ids to nodes in this tree
     */
    public void setNodesMap(Map<String, PackageNode> nodesMap) {
        this.nodesMap = nodesMap;
    }

    @Override
    public String toString() {
        return "PackageTree{" +
                "root=" + root +
                "\n nodeMap=" + nodesMap+ "}";
    }
}
