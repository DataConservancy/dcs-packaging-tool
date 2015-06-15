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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * A {@code PackageNode} object contains a PackageArtifact which it represents, as well as references to other
 * {@code PackageNodes} to which it is related in a tree-like structure.
 * </p>
 * <p>
 * Each PackageNode is a tree whose top node is {@code node} itself and has references to it's parent and child {@code PackageNode}s.
 * </p>
 * <p>
 * If a {@code PackageNode} is the root node of a tree, then its {@code parentNode} is {@code null}.
 * </p>
 * If a {@code PackageNode} is the lowest level node, its {@code childenNodes} will be empty.
 * 
 * Equality is defined by comparing the PackageArtifact value alone, not by the structure of the tree.
 */
public class PackageNode {
    private PackageArtifact value;
    private PackageNode parentNode;
    private List<PackageNode> childrenNodes;

    public PackageNode(PackageArtifact value) {
        this.value = value;
        this.childrenNodes = new ArrayList<>();
    }

    /**
     * Set the parent as given and add this node as a child to the parent.
     * 
     * @param value PackageArtifactthe
     * @param parent the parent PackageNode
     */
    public PackageNode(PackageArtifact value, PackageNode parent) {
        this.value = value;
        this.parentNode = parent;
        this.childrenNodes = new ArrayList<>();
        
        parent.childrenNodes.add(this);
    }

    public PackageArtifact getValue() {
        return value;
    }

    public void setValue(PackageArtifact value) {
        this.value = value;
    }

    public PackageNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(PackageNode parentNode) {
        this.parentNode = parentNode;
    }

    /**
     * @return the instance used to store child nodes
     */
    public List<PackageNode> getChildrenNodes() {
        return childrenNodes;
    }

    /**
     * Set children to the instance given.
     * 
     * @param childrenNodes the List of children PackageNodes
     */
    public void setChildrenNodes(List<PackageNode> childrenNodes) {
        this.childrenNodes = childrenNodes;
    }

    /**
     * Set children to a list containing the given nodes.
     * 
     * @param childrenNodes the children PackageNodes
     */
    public void setChildrenNodes(PackageNode ... childrenNodes) {
        this.childrenNodes = new ArrayList<>(Arrays.asList(childrenNodes));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PackageNode)) return false;

        PackageNode that = (PackageNode) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PackageNode{ \n" +
                "\tvalue=" + value +
                ", \tchildrenNodes= \n\t" + childrenNodes +
                "}\t";
    }
}
