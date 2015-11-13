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
package org.dataconservancy.packaging.gui.presenter;

import javafx.scene.control.TreeItem;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.api.PropertyFormatService;
import org.dataconservancy.packaging.tool.api.support.NodeComparison;
import org.dataconservancy.packaging.tool.impl.DomainProfileObjectStore;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This presenter controls the display of the package description view. 
 * It also handles validating a package description, before a package is allowed to be created. *
 */
public interface EditPackageContentsPresenter extends Presenter {

    /**
     * Sets the ipm service that's used for manipulating the tree.
     * @param ipmService the ipmService
     */
    void setIpmService(IPMService ipmService);

    /**
     * Sets the service that is used to format property values.
     * @param propertyFormatService The property format service.
     */
    void setPropertyFormatService(PropertyFormatService propertyFormatService);

    /**
     * Changes the type of the provided node to the provided type.
     * @param node The node to change the type of.
     * @param transform The transform to perform on the node.
     */
    void changeType(Node node, NodeTransform transform);

    /**
     * Rerun the ontology service on the PackageDescription and redisplay the resulting PackageTree.
     */
    void rebuildTreeView();

    /**
     * Refresh the display of a new PackageTree.
     */
    void displayPackageTree();
    
    /**
     * Return the TreeItem containing the given Node or null if none found.
     * 
     * @param node the PackageArtifact
     * @return matching TreeItem
     */
    TreeItem<Node> findItem(Node node);

    /**
     * Saves the artifact that's currently being displayed in the properties window.
     */
    void saveCurrentNode();

    /**
     * Gets a list of possible child node types for the given node. This is used to determine if a file or folder picker should be present for the add.
     * @param node The node to get the possible child types for.
     */
    List<NodeType> getPossibleChildTypes(Node node);

    /**
     * Creates a tree from the given path and adds it to the tree at the given node.
     * @param parent The node that should be the parent of the new code.
     * @param contentToAdd The path of the content to add to the tree.
     */
    void addToTree(Node parent, Path contentToAdd);

    /**
     * Refreshes the underlying file system under the given node, and returns the result to be applied or rejected.
     * @param node The node that everything should be refreshed under.
     * @return The map containing the results of the comparison of the tree to the file system.
     */
    Map<Node, NodeComparison> refreshTreeContent(Node node);
}