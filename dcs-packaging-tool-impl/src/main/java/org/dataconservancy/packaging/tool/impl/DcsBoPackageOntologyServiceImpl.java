/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.impl;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.http.client.utils.URIBuilder;
import org.dataconservancy.packaging.tool.api.PackageOntologyService;
import org.dataconservancy.packaging.tool.api.support.OntologyPropertyValidator;
import org.dataconservancy.packaging.tool.impl.support.DcsBoOntologyValidatorFactory;
import org.dataconservancy.packaging.tool.model.*;
import org.dataconservancy.packaging.tool.model.PackageOntology.PropertyType;
import org.dataconservancy.packaging.tool.model.PackageOntology.Relationship;
import org.dataconservancy.packaging.tool.model.ontologies.DcsBoPackageOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * <p> This implementation of PackageOntologyService is suitable for ontologies in which hierarchical relationships are
 * essential in determining the graph of related Artifacts.</p>
 *
 *
 */
public class DcsBoPackageOntologyServiceImpl implements PackageOntologyService {

    private PackageOntology ontology;
    private Map<String, Set<RelationshipSourceTargetPair>> relationshipCatalog;

    private Set<String> creatorProperties;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected static final String didfComboType = "DataItem + File";
    private final static String synthesizedArtifactName = "Synthesized artifact";

    private static Random rand = new Random(37);

    /**
     * Set up an instance of DcsBoPackageOntologyService, set ontology to an instance of DcsBoPackageOntology.
     */
    public DcsBoPackageOntologyServiceImpl() {
        ontology = DcsBoPackageOntology.getInstance();
        setUpRelationshipCatalog();
        creatorProperties = new HashSet<>();
    }

    @Override
    public Set<String> getValidTypes()  {
        if (ontology == null) {
            throw new RuntimeException("Ontology and hierarchical relationship names all have to be specified, " +
                    "before service could be used.");
        }
        Set<String> validTypes = new HashSet<>();
        for (String types : ontology.getArtifactTypes()) {
            validTypes.add(types);
        }
        return validTypes;
    }

    /**
     * Gets kids with the ignored status matching the provided status.
     * @param node The node to retrieve the children of
     * @param ignored The ignored status that child nodes should have
     * @return A list of all child nodes that match the passed in ignored status, or an empty list if none are found.
     */
    public List<PackageNode> getKidsWithIgnoredStatus(PackageNode node, boolean ignored) {
        List<PackageNode> result = new ArrayList<>();
        
        for (PackageNode kid : node.getChildrenNodes()) {
            if (kid.getValue().isIgnored() == ignored) {
                result.add(kid);
            }
        }
        
        return result;
    }

    /**
     * Ensure all unignored nodes have a valid type by recursively fixing types.
     * TODO Does this actually work?
     * 
     * @param tree the PackageTree
     * @param node the  PackageNode
     * @param desc the  PackageDescription d
     * @throws PackageOntologyException
     */
    private void fixIgnoredType(PackageTree tree, PackageNode node, PackageDescription desc, File contentRoot) throws PackageOntologyException {
        PackageArtifact artifact = node.getValue();
        
        Set<String> valid_types = getValidTypes(tree, artifact.getId());
         
        if (valid_types.contains(artifact.getType())) {

        } else if (valid_types.isEmpty()) {
            throw new PackageOntologyException("No valid types for: " + artifact);
        } else {
            changeType(desc, tree, artifact, contentRoot,  valid_types.iterator().next());
        }
        
        for (PackageNode kid: node.getChildrenNodes()) {
            if (!kid.getValue().isIgnored()) {
                fixIgnoredType(tree, kid, desc, contentRoot);
            }
        }
     }
     
     /**
      * Ensure that every ignored node has ignored children.
      * TODO What to do with relationships between nodes?
      * 
      * @param node the PackageNode
      */
     private void fixIgnoredFlag(PackageNode node) {
         boolean ignored = node.getValue().isIgnored();
         
         for (PackageNode kid: node.getChildrenNodes()) {
             if (ignored) {
                 kid.getValue().setIgnored(true);
             }
             
             fixIgnoredFlag(kid);
         }
     }

     private String getParentArtifactId(PackageArtifact artifact) throws PackageOntologyException {
         String parent_rel = getAvailableRelationshipToParent(artifact.getAllRelationshipNamesOnArtifact(),
                 DcsBoPackageOntology.relationshipsToParent);
         
         if (parent_rel == null) {
             return null;
         } else {
             PackageRelationship relationship = artifact.getRelationshipByName(parent_rel);
             
             Set<String> parents = relationship.getTargets();
             
             if (parents.size() != 1) {
                 throw new PackageOntologyException("Artifact can only have one parent: " + artifact);
             }

             return parents.iterator().next();
         }
     }     

    /**
     * Gets the available types for the given artifact, based on it's contextual location in the tree.
     * 
     * If called on ignored node, take into account ignored children, otherwise do not take into account ignored children.
     *
     * @param tree The tree representing the {@link org.dataconservancy.packaging.tool.model.PackageDescription}
     * @param currentArtifactId The id of the artifact to retrieve the types for.
     * @return A set of all the available relationships for the artifact.
     */
    @Override
    public Set<String> getValidTypes(PackageTree tree, String currentArtifactId) throws PackageOntologyException {
        if (ontology == null) {
            throw new RuntimeException("Ontology and hierarchical relationship names all have to be specified, " +
                    "before service could be used.");
        }

        if (tree == null || currentArtifactId == null) {
            throw new PackageOntologyException("Package tree and currentArifactId cannot be null");
        }

        //retrieve current Node from nodeMaps
        PackageNode currentNode = tree.getNodesMap().get(currentArtifactId);

        //filter out valid types for artifact's filesystem types (ie. directories cannot be files and bytestreams have
        //to be files
        Set<String> validTypes = getValidTypesPerFileSystemType(currentNode.getValue().isByteStream());

        Set<String> validTypesDictatedByParent;

        validTypesDictatedByParent = new HashSet<>();
        PackageNode parentNode = currentNode.getParentNode();

        //if a parent node of with the same artifactId exists in the NodeMap
        //calculate the valid types based on parent's type
        if (parentNode != null) {
            //if node exists, check for its PackageArtifact's type
            String parentNodeType = parentNode.getValue().getType();
            //look up the source target pairs by relationship name:
            for (String relationshipToParent : DcsBoPackageOntology.relationshipsToParent) {
                Set<RelationshipSourceTargetPair> sourceTargetPairs = relationshipCatalog.get(relationshipToParent);
                if (sourceTargetPairs != null) {
                    //For every combination of source-target for the relationship
                    for (RelationshipSourceTargetPair pair : sourceTargetPairs) {
                        if (pair.getTargetType().equals(parentNodeType)) {
                            //add up all valid types determined by this relationship to parent
                            validTypesDictatedByParent.add(pair.getSourceType());
                        }
                    }
                }
            }
            //By this point, should have a set of types dictated by the parent of the relationship
            //intersect with all types to get an intermediate set of valid types.
            //Only intersect the set if validByParent set is not empty
            if (validTypesDictatedByParent.size() > 0) {
                validTypes.retainAll(validTypesDictatedByParent);
            }
        }

        // If node is ignored, consider all kids.
        // If node is not ignored, only consider kids that are not ignored.
        
        List<PackageNode> kids;
        
        if (currentNode.getValue().isIgnored()) {
            kids = currentNode.getChildrenNodes();
        } else {
            kids = getKidsWithIgnoredStatus(currentNode, false);
        }
        
        if (kids.size() > 0) {
            Set<String> validTypesDictatedByChildren;
            
            for (PackageNode childNode : kids) {
                //if node exists, check for its PackageArtifact's type
                String childNodeType = childNode.getValue().getType();
                //If child is not a File type
                if (!childNodeType.contains("File")) {
                    for (String relationshipToParent : DcsBoPackageOntology.relationshipsToParent) {
                        validTypesDictatedByChildren = new HashSet<>();
                        //look up the source target pairs by relationship name:
                        Set<RelationshipSourceTargetPair> sourceTargetPairs = relationshipCatalog.get(relationshipToParent);
                        if (sourceTargetPairs != null) {
                            //For every combination of source of the relationship
                            for (RelationshipSourceTargetPair pair : sourceTargetPairs) {
                                //get valid relationship's target
                                if (pair.getSourceType().equals(childNodeType)) {
                                    validTypesDictatedByChildren.add(pair.getTargetType());
                                }
                            }
                        }
                        //If there are some type restriction by the children of this relationship
                        if (validTypesDictatedByChildren.size() > 0) {
                            //intersect the set of types dictate by children with the main validTypes set.
                            validTypes.retainAll(validTypesDictatedByChildren);
                        }
                    }
                } else { // if child is a file Type
                    //to deal with the isMetadataFor and isMemberOf being interchangeable in the case of Files.
                    validTypesDictatedByChildren = new HashSet<>();
                    for (String relationshipToParent : DcsBoPackageOntology.relationshipsToParent) {
                        //look up the source target pairs by relationship name:
                        Set<RelationshipSourceTargetPair> sourceTargetPairs = relationshipCatalog.get(relationshipToParent);
                        if (sourceTargetPairs != null) {
                            for (RelationshipSourceTargetPair pair : sourceTargetPairs) {
                                if (pair.getSourceType().contains("File")) {
                                    validTypesDictatedByChildren.add(pair.getTargetType());
                                }
                            }
                        }
                    }
                    //If there are some type restriction by the children of this relationship
                    if (validTypesDictatedByChildren.size() > 0) {
                        //intersect the set of types dictate by children with the main validTypes set.
                        validTypes.retainAll(validTypesDictatedByChildren);
                    }
                }
            }
        } else if (canBeDataItemFile(tree, currentArtifactId)) {
            validTypes.add(didfComboType);
        }

        return validTypes;
    }

    /**
     * Determines whether an Artifact representing a bytestream could be represented by a DataFile encapsulated by an
     * auto-generated DataItem.
     * @param tree the PackageTree
     * @param currentArtifactId the ID of the current artifact
     * @return  boolean
     */
    protected boolean canBeDataItemFile(PackageTree tree, String currentArtifactId) {
        if (ontology == null) {
            throw new RuntimeException("Ontology and hierarchical relationship names all have to be specified, " +
                    "before service could be used.");
        }

        if (tree == null || currentArtifactId == null) {
            throw new IllegalArgumentException("Package tree and currentArifactId cannot be null");
        }
        //get current node
        PackageNode node = tree.getNodesMap().get(currentArtifactId);
        if (node == null) {
            throw new IllegalArgumentException("Provided artifact id " + currentArtifactId + " does not identify any" +
                    " package artifact in the package.");
        }

        //if current node is a bytestream
        if (node.getValue().isByteStream()) {
            //if the bytestream artifact is under a collection currently
            if (node.getParentNode().getValue().getType().equals(DcsBoPackageOntology.COLLECTION)) {
                //then it can be wrapped in a data item
                return true;
            } else {
                //if not, then it cannot be wrapped in a data item, since Collection is the only Artifact type that could
                //be parent to a DataItem
                return false;
            }
        } else {
            //if not a bytestream, can't be wrapped in DataItem return false
            return false;
        }
    }

    /**
     * Change the current artifact to DataFile. Add a DataItem to contain it in the proper spot in the PackageTree
     * @param packageDesc the PackageDescription
     * @param tree  the  PackageTree
     * @param packageArtifact  the current artifact
     * @param contentRoot the root directory for the package content
     */
    protected void makeDataItemFileCombo(PackageDescription packageDesc, PackageTree tree, PackageArtifact packageArtifact, File contentRoot) {
        String currentArtifactId = packageArtifact.getId();
        if (ontology == null) {
            throw new RuntimeException("Ontology and hierarchical relationship names all have to be specified, " +
                    "before service could be used.");
        }

        if (tree == null || currentArtifactId == null) {
            throw new IllegalArgumentException("Package tree and currentArifactId cannot be null");
        }
        //get current fileNode
        PackageNode fileNode = tree.getNodesMap().get(currentArtifactId);
        if (fileNode == null) {
            throw new IllegalArgumentException("Provided artifact id " + currentArtifactId + " does not identify any" +
                    " package artifact in the package.");
        }


        //Create a new URIBuilder to help manipulate file uris to be used for new DI and DF ids and ArtifactReferences
        URIBuilder fileURIBuilder;
            fileURIBuilder = new URIBuilder(packageArtifact.getArtifactRef().getRefURI(contentRoot));
        //obtain the containing Collection node as a starting place
        PackageNode containingCollectionNode = tree.getNodesMap().get(fileNode.getParentNode().getValue().getId());

        //unhook fileNode from tree/containing collection node
        //set appropriate node linkage
        fileNode.setParentNode(null);
        fileNode.setChildrenNodes();
        containingCollectionNode.getChildrenNodes().remove(fileNode);
        //remove relevant relationships
        fileNode.getValue().removeRelationship(DcsBoPackageOntology.IS_METADATA_FOR, containingCollectionNode.getValue().getId());
        containingCollectionNode.getValue().removeRelationship(DcsBoPackageOntology.HAS_METADATA, fileNode.getValue().getId());
        //remove file node with the old id from nodes map
        tree.getNodesMap().remove(fileNode.getValue().getId());

        //Set up package artifact for the new DI
        PackageArtifact diArtifact = new PackageArtifact();
        //set new DI's id and artifact ref
        //TODO: do we want to use a different ID?
        diArtifact.setId((fileURIBuilder.setFragment(Integer.toString(rand.nextInt(Integer.MAX_VALUE)))).toString());

        try {
            URI absoluteURI = fileURIBuilder.build();
            URI relativeURI = contentRoot.getParentFile().toURI().relativize(absoluteURI);
            diArtifact.setArtifactRef(relativeURI.toString());
        } catch (URISyntaxException e){
           throw new IllegalArgumentException("Invalid URI, cannot create ArtifactReference",e) ;
        }
        diArtifact.setIgnored(containingCollectionNode.getValue().isIgnored());

        //set package artifact type
        diArtifact.setType(DcsBoPackageOntology.DATAITEM);
        //loop through the available properties in the file artifact
        for (String propertyName : fileNode.getValue().getPropertyNames()) {
            // if the property is applicable to the DI then copy it to the DI.
            if (getProperties(DcsBoPackageOntology.DATAITEM).containsKey(propertyName)) {
                if (isPropertyComplex(propertyName)) {
                    diArtifact.setPropertyValueGroups(propertyName,
                            new HashSet<>(fileNode.getValue().getPropertyValueGroups(propertyName)));
                } else {
                    diArtifact.setSimplePropertyValues(propertyName,
                            new HashSet<>(fileNode.getValue().getSimplePropertyValues(propertyName)));
                }
            }
        }

        //set DI artifact's name
        Iterator<String> nameIter = fileNode.getValue().getSimplePropertyValues(DcsBoPackageOntology.FILE_NAME).iterator();
        if (nameIter.hasNext()) {
            diArtifact.addSimplePropertyValue(DcsBoPackageOntology.NAME, synthesizedArtifactName + " for " + nameIter.next());
        } else {
            diArtifact.addSimplePropertyValue(DcsBoPackageOntology.NAME, synthesizedArtifactName);
        }

        //copy over the fileNode's relationship, ***AFTER*** fileNode's hierarchical relationship has been removed.
        diArtifact.setRelationships(new ArrayList<>(fileNode.getValue().getRelationships()));
        //set hierarchical relationships
        diArtifact.getRelationships().add(
                new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, containingCollectionNode.getValue().getId()));
        //Add diArtifact to package description
        packageDesc.getPackageArtifacts().add(diArtifact);

        //Set up Package node for the new DI, give it the value
        PackageNode diNode = new PackageNode(diArtifact);
        //Hook the DI node onto the tree and add it to the Nodes map
        containingCollectionNode.getChildrenNodes().add(diNode);
        diNode.setParentNode(containingCollectionNode);
        diNode.setChildrenNodes(fileNode);
        //make DI node the parent of the fileNode
        tree.getNodesMap().put(diArtifact.getId(), diNode);

        //file node will keep id

        //Change type to Data File
        fileNode.getValue().setType(DcsBoPackageOntology.DATAFILE);
        //clear out fileNode's relationships as they have been moved to the containing DI
        fileNode.getValue().setRelationships();
        //Update the artifact's relationship to make it a member of the Di artifact
        fileNode.getValue().setRelationships(
                new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, diArtifact.getId()));

        //hook childNode to the tree as child of the DI node
        fileNode.setParentNode(diNode);
        //add new mapping: new id to file node on nodes maps
        tree.getNodesMap().put(fileNode.getValue().getId(), fileNode);

    }

    @Override
    public boolean canCollapseParentArtifact(PackageTree tree, String fileId) throws PackageOntologyException {

        PackageNode fileNode = tree.getNodesMap().get(fileId);
        //answer is always false for root artifact and 2 level artifact
        if (fileNode.getParentNode() == null || fileNode.getParentNode().getParentNode() == null) {
            return false;
        }

        if (!fileNode.getValue().getType().equals(DcsBoPackageOntology.METADATAFILE)) {
            return false;
        }
        PackageNode parentNode = tree.getNodesMap().get(fileNode.getParentNode().getValue().getId());
        if (!parentNode.getValue().getType().equals(DcsBoPackageOntology.DATAITEM) || parentNode.getChildrenNodes().size() != 1) {
            return false;
        }

        File tempRoot = new File("/tmp");//this is all we need to check, dont need actual absolute URI
        URI parentURI= parentNode.getValue().getArtifactRef().getRefURI(tempRoot);
        URI fileURI = fileNode.getValue().getArtifactRef().getRefURI(tempRoot);
        return parentURI.getScheme().equals(fileURI.getScheme()) &&
            parentURI.getSchemeSpecificPart().equals(fileURI.getSchemeSpecificPart()) &&
            parentURI.getFragment() != null &&
            !parentURI.getFragment().isEmpty();
    }

    @Override
    public String collapseParentArtifact(PackageDescription packageDesc, PackageTree tree, String fileArtifactId) throws PackageOntologyException {
        if (ontology == null) {
            throw new RuntimeException("Ontology and hierarchical relationship names all have to be specified, " +
                    "before service could be used.");
        }

        if (tree == null || fileArtifactId == null) {
            throw new IllegalArgumentException("Package tree and the ids provided cannot be null");
        }
        //get fileNode
        PackageNode fileNode = tree.getNodesMap().get(fileArtifactId);
        if (fileNode == null) {
            throw new IllegalArgumentException("Provided artifact id " + fileArtifactId + " does not identify any" +
                    " package artifact in the package.");
        }

        if (!canCollapseParentArtifact(tree, fileArtifactId)) {
            throw new IllegalArgumentException("The parent of artifact referenced cannot " +
                    "be removed.");
        }

        //get diNode
        PackageNode parentNode = tree.getNodesMap().get(fileNode.getParentNode().getValue().getId());
        if (parentNode == null) {
            throw new IllegalArgumentException("Provided artifact id " + fileNode.getParentNode().getValue().getId()
                    + " does not identify any package artifact in the package.");
        }

        PackageNode grandparentNode = parentNode.getParentNode();
        if (grandparentNode == null) {
            throw new PackageOntologyException("Expected grandparent node of the file not to be null.");
        }

        //unhook diNode from containingCollectionNode
        grandparentNode.getChildrenNodes().remove(parentNode);
        grandparentNode.getValue().removeRelationship(DcsBoPackageOntology.HAS_MEMBER, parentNode.getValue().getId());
        //remove diNode from nodes map
        tree.getNodesMap().remove(parentNode.getValue().getId());

        //remove parentNode's artifact from package description
        packageDesc.getPackageArtifacts().remove(parentNode.getValue());

        //unhook file artifact from DataItem artifact and hook it on to the collection
        fileNode.getValue().removeRelationship(DcsBoPackageOntology.IS_METADATA_FOR, parentNode.getValue().getId());
        fileNode.getValue().setRelationships(
                new PackageRelationship(DcsBoPackageOntology.IS_METADATA_FOR, true, grandparentNode.getValue().getId()));
        fileNode.setParentNode(grandparentNode);

        //remove hierarchical relationship from diNode
        parentNode.getValue().removeRelationshipByName(DcsBoPackageOntology.IS_MEMBER_OF);
        parentNode.getValue().removeRelationshipByName(DcsBoPackageOntology.HAS_MEMBER);

        //merging relationships
        for (PackageRelationship relationship : parentNode.getValue().getRelationships()) {
            fileNode.getValue().getRelationships().add(relationship);
        }

        Set<String> validPropertiesForMdF = getProperties(fileNode.getValue()).keySet();
        //merging properties with preferences to DI's properties.
        for  (String propertyName : parentNode.getValue().getPropertyNames()) {
            if (validPropertiesForMdF.contains(propertyName)) {
                if (isPropertyComplex(propertyName)) {
                    fileNode.getValue().
                            setPropertyValueGroups(propertyName,
                                    new HashSet<>(parentNode.getValue().getPropertyValueGroups(propertyName)));
                } else {
                    fileNode.getValue().
                            setSimplePropertyValues(propertyName,
                                    new HashSet<>(parentNode.getValue().getSimplePropertyValues(propertyName)));
                }
            }
        }

        //hook file up to containing collection
        grandparentNode.getChildrenNodes().add(fileNode);
        fileNode.setParentNode(grandparentNode);
        fileNode.getValue().getRelationships().add(new PackageRelationship(DcsBoPackageOntology.IS_MEMBER_OF, true, grandparentNode.getValue().getId()));

        return parentNode.getValue().getId();
    }

    @Override
    public Map<String, String> getProperties(PackageArtifact currentArtifact) {
        return getProperties(currentArtifact.getType());
    }

    @Override
    public Map<String, String> getProperties(String typeName) {
        Map<String, String> result = new HashMap<>();

        if (typeName == null || typeName.isEmpty()) {
            return result;
        }
        Set<PackageOntology.Property> properties = new HashSet<>();
        if (typeName.equals(didfComboType)) {
            properties.addAll(ontology.getProperties(DcsBoPackageOntology.DATAITEM));
            properties.addAll(ontology.getProperties(DcsBoPackageOntology.DATAFILE));
        } else {
            properties.addAll(ontology.getProperties(typeName));
        }

        for (PackageOntology.Property property : properties) {
            result.put(property.getName(), property.getValueType());
        }
        return result;
    }

    @Override
    public Set<String> getCreatorProperties(PackageArtifact currentArtifact) {
        Set<PackageOntology.Property> properties = ontology.getProperties(currentArtifact.getType());
        Set<String> propertyNames = new HashSet<>();

        for (PackageOntology.Property property : properties) {

            if (creatorProperties.contains(property.getName())) {
                propertyNames.add(property.getName());
            }

        }
        return propertyNames;
    }

    @Override
    public PackageTree buildPackageTree(PackageDescription packageDescription, File contentRoot) throws PackageOntologyException {
        PackageTree tree = new PackageTree();
        Map<String, PackageNode> nodemap = tree.getNodesMap();
        
        // Create node for each PackageArtifact
        
        for (PackageArtifact artifact: packageDescription.getPackageArtifacts()) {
            nodemap.put(artifact.getId(), new PackageNode(artifact));
        }
                
        // Set node parents and children
        
        for (PackageArtifact artifact: packageDescription.getPackageArtifacts()) {
            String parent_id = getParentArtifactId(artifact);
            
            if (parent_id == null) {
                if (tree.getRoot() != null) {
                    throw new PackageOntologyException("Only one root artifact allowed: " + artifact);
                }
        
                tree.setRoot(nodemap.get(artifact.getId()));
            } else {
                PackageNode parent = nodemap.get(parent_id);
            
                if (parent == null) {
                    throw new PackageOntologyException("Could not find parent: " + parent_id);
                }
            
                PackageNode node = nodemap.get(artifact.getId());            
                node.setParentNode(parent);
                parent.getChildrenNodes().add(node);
            }
        }

        // Modify tree to handle any ignored nodes
        
        fixIgnoredFlag(tree.getRoot());
        fixIgnoredType(tree, tree.getRoot(), packageDescription, contentRoot);
        
        return tree;
    }

    private void setUpRelationshipCatalog() {
        Set<String> validTypes = ontology.getArtifactTypes();
        //Building relationship catalog
        relationshipCatalog = new HashMap<>();
        for (String typeName : validTypes) {
            Set<PackageOntology.Relationship> allowedRelationships = ontology.getRelationships(typeName);
            for (PackageOntology.Relationship allowedRelationship : allowedRelationships) {

                if (relationshipCatalog.get(allowedRelationship.getName()) == null) {
                    relationshipCatalog.put(allowedRelationship.getName(), new HashSet<>());
                }
                relationshipCatalog.get(allowedRelationship.getName())
                        .add(new RelationshipSourceTargetPair(typeName, allowedRelationship.getRelatedArtifactType()));
            }
        }
    }

    public void setCreatorPropertiesByString(String creatorProperties) {
        String [] propertyNames =  creatorProperties.split(",");
        for (String name : propertyNames) {
            this.creatorProperties.add(name.trim());
        }
    }

    public void setCreatorProperties(Set<String> creatorProperties) {
        this.creatorProperties = creatorProperties;
    }

     @Override
    public void validateProperties(PackageArtifact artifact) throws PackageOntologyException{
        String artifactType = artifact.getType();

        List<String> missingRequiredProperties = new ArrayList<>();
        Set<PackageOntology.Property> associatedProperties = ontology.getProperties(artifactType);
        for (PackageOntology.Property property : associatedProperties) {
            // if min occurrence s greater than 0, then the property is required
            if (property.getMinOccurrence() > 0) {
                if (!artifact.getPropertyNames().contains(property.getName())) {
                    missingRequiredProperties.add(property.getName());
                }
            }
        }

        if (missingRequiredProperties.size() > 0) {
            throw new PackageOntologyException("Fields required for PackageArtifacts of " + artifactType + "  type, " +
                    "but are missing: " + missingRequiredProperties);
        }
    }

    /**
     * <p>
     * In this implementation, changing from DataFile to MetadataFile and vice versa affects the relationship of the
     * current artifact to its parent (isMetadataFor vs isMemberOf). Additionally, changing a DataItem to a Collection,
     * when valid, affects the children of the DataItems and their relationship to the DataItem (DataFiles become.
     * MetadataFiles)
     * </p>
     *
     * <p>
     * These mappings are being done by this method in addition to change the {@code type} property on the current
     * artifact.
     * </p>
     *
     * This implementation also allows the transformation of a collection's MetadataFile into a combination of DataItem
     * and DataTime and vice versa.
     *
     * @param packageDesc the PackageDescription
     * @param tree The tree representing the {@link org.dataconservancy.packaging.tool.model.PackageDescription} the artifact is a member of
     * @param packageArtifact the artifact whose type is to be change
     * @param contentRoot the root artifact for the package content
     * @param newTypeName The type the artifact should be changed to.
     * @throws PackageOntologyException if the new type is not valid
     */
    @Override
    public void changeType(PackageDescription packageDesc, PackageTree tree, PackageArtifact packageArtifact, File contentRoot, String newTypeName) throws PackageOntologyException {
        PackageNode currentNode = tree.getNodesMap().get(packageArtifact.getId());
        PackageArtifact currentArtifact = currentNode.getValue();

        if (currentArtifact.getType().equals(newTypeName)) {
            return;
        }
        
        Set<String> validTypes =  getValidTypes(tree, packageArtifact.getId());
        if (!validTypes.contains(newTypeName)) {
            throw new PackageOntologyException("The provided new type was not a valid one. Valid types for this " +
                    "currentNode (based on the package tree include: " + validTypes +
                    ", provided type: [" + newTypeName + "]");
        }

        PackageArtifact parentArtifact = null;
        if (currentNode.getParentNode() != null) {
            parentArtifact = currentNode.getParentNode().getValue();
        }

        String originalTypeName = currentArtifact.getType();
        currentArtifact.setType(newTypeName);

        //*********************************************************************************************************
        // If new type is the DI-F combo, call helper method to do so and call it done.
        //*********************************************************************************************************
        if (newTypeName.equals(didfComboType)) {
            makeDataItemFileCombo(packageDesc, tree, packageArtifact, contentRoot);
            return;
        }
        //*********************************************************************************************************
        //*** To deal with the interchangeable nature of DataFile and MetadataFile's relationship to their parents
        //*** and the interchangeable nature of DataItem and Collection in some cases
        //*********************************************************************************************************
        //Change a DataFile to a MetadataFile
        if (originalTypeName.equals(DcsBoPackageOntology.DATAFILE) && newTypeName.equals(DcsBoPackageOntology.METADATAFILE) ) {
            updateRelationships(currentArtifact, parentArtifact, DcsBoPackageOntology.IS_MEMBER_OF, DcsBoPackageOntology.IS_METADATA_FOR, DcsBoPackageOntology.HAS_MEMBER, DcsBoPackageOntology.HAS_METADATA);
        } else if (originalTypeName.equals(DcsBoPackageOntology.METADATAFILE) && newTypeName.equals(DcsBoPackageOntology.DATAFILE)) {
            //Change a MetadataFile to a Data file
            updateRelationships(currentArtifact, parentArtifact, DcsBoPackageOntology.IS_METADATA_FOR, DcsBoPackageOntology.IS_MEMBER_OF, DcsBoPackageOntology.HAS_METADATA, DcsBoPackageOntology.HAS_MEMBER);
        } else if (originalTypeName.equals(DcsBoPackageOntology.DATAITEM) && newTypeName.equals(DcsBoPackageOntology.COLLECTION)) {
            //Change DataItem to Collection
            updateRelationships(currentArtifact, parentArtifact, DcsBoPackageOntology.HAS_MEMBER, DcsBoPackageOntology.HAS_METADATA, "", "");
            //Update the currentNode's parent's relationships to the currentNode itself.
            for (PackageNode childNode : currentNode.getChildrenNodes()) {
                PackageArtifact child = childNode.getValue();
                if (child.isByteStream() && !child.isIgnored()) {
                    changeType(packageDesc, tree, child, contentRoot, DcsBoPackageOntology.METADATAFILE);
                }
            }

        } else if (originalTypeName.equals(DcsBoPackageOntology.COLLECTION) && newTypeName.equals(DcsBoPackageOntology.DATAITEM)) {
            //Change DataItem to Collection
            updateRelationships(currentArtifact, parentArtifact, DcsBoPackageOntology.HAS_METADATA, DcsBoPackageOntology.HAS_MEMBER, "", "");
            //Update the currentNode's parent's relationships to the currentNode itself.
            for (PackageNode childNode : currentNode.getChildrenNodes()) {
                PackageArtifact child = childNode.getValue();
                
                if (!child.isIgnored()) {
                    changeType(packageDesc, tree, child, contentRoot, DcsBoPackageOntology.DATAFILE);
                }
            }

        }
    }

    private void updateRelationships(PackageArtifact currentArtifact, PackageArtifact parentArtifact, String oldRelationshipName, String newRelationshipName,
                                     String oldParentRelationshipName, String newParentRelationshipName) {
        //Update the currentNode's relationship to its parent
        PackageRelationship oldRelationship = null;
        if (parentArtifact != null) {
            oldRelationship = currentArtifact.findRelationship(oldRelationshipName, parentArtifact.getId());
        }

        if (oldRelationship != null) {
            Set<String> artifactsRelatedByOldRel = oldRelationship.getTargets();
            if (artifactsRelatedByOldRel != null && artifactsRelatedByOldRel.size() > 0) {
                currentArtifact.removeRelationshipByName(oldRelationshipName);
                currentArtifact.getRelationships().add(new PackageRelationship(newRelationshipName, true, artifactsRelatedByOldRel));
            }
        }

        if (!oldParentRelationshipName.isEmpty() && !newParentRelationshipName.isEmpty()) {
            //Update the parent artifact's relationship to the current artifact
            if (parentArtifact != null && parentArtifact.getRelationshipByName(oldParentRelationshipName) != null) {
                PackageRelationship oldParentRelationship = parentArtifact.findRelationship(oldParentRelationshipName, currentArtifact.getId());
                if (oldParentRelationship != null) {
                    oldParentRelationship.getTargets().remove(currentArtifact.getId());
                }

                PackageRelationship newParentRelationship = parentArtifact.getRelationshipByName(newParentRelationshipName);
                if (newParentRelationship == null) {
                    newParentRelationship = new PackageRelationship(newParentRelationshipName, true, new HashSet<>());
                    parentArtifact.getRelationships().add(newParentRelationship);
                }
                newParentRelationship.getTargets().add(currentArtifact.getId());
            }
        }
    }

    private void buildNode(PackageDescription packageDescription, PackageTree tree) {
        for (PackageArtifact artifact : packageDescription.getPackageArtifacts()) {
            if(tree.getNodesMap().get(artifact.getId()) == null) {
                addNode(packageDescription, artifact, tree);
            }
        }
    }


    private PackageNode addNode(PackageDescription packageDescription, PackageArtifact artifact, PackageTree tree) {
        PackageNode newNode = null;
        if (artifact != null) {
            String availableRelToParent = getAvailableRelationshipToParent(artifact.getAllRelationshipNamesOnArtifact(),
                    DcsBoPackageOntology.relationshipsToParent);
            //If the artifact is a member of other artifact, add the new node to the parent node's children list.
            if (availableRelToParent != null) {
                PackageRelationship relationship = artifact.getRelationshipByName(availableRelToParent);
                if (relationship != null) {
                    Set<String> relationshipRefs = relationship.getTargets();
                    if (relationshipRefs != null && relationshipRefs.iterator().hasNext()) {
                        String parentRef = relationshipRefs.iterator().next();
                        newNode = addArtifactToParent(packageDescription, parentRef, tree, artifact);
                    }
                }
            } else {
                newNode = new PackageNode(artifact);
                if (tree.getRoot() != null) {
                    //If the collection isn't part of a project or collection add it as a child under the root.
                    tree.getRoot().getChildrenNodes().add(newNode);
                } else {
                    tree.setRoot(newNode);
                }
            }
            //if the artifact was not null and a new node was created for it, add the new node to the nodesMap on the
            //tree
            if (newNode != null && newNode.getValue() != null) {
                tree.getNodesMap().put(newNode.getValue().getId(), newNode);
            }
        }
        return newNode;
    }

    /**
     * If the set of the available relationships contains any of the relationships which represent parental relationship,
     * then return that parental relationship. If none of the parental relationships are available, return null.
     *
     * <p/>
     *
     * ***IMPORTANT NOTE*** It is assumed that an artifact only has ONE parent; therefore only one relationship to
     * parent is expected.
     * @param availableRelationships Set of relationships, in which parental relationship will be searched for.
     * @param relationshipsToParent Set of relationships, which has been specified to indicate parental relationship by the
     *                              set up of this PackageOntologyService.
     * @return  the parental relationship
     */
    private String getAvailableRelationshipToParent(Set<String> availableRelationships, Set<String> relationshipsToParent) {
        for (String relationshipToParent : relationshipsToParent) {
            if (availableRelationships.contains(relationshipToParent)) {
                return relationshipToParent;
            }
        }
        return null;
    }

    /**
     * Finds a parent PackageArtifact based on the artifactRef value
     * @param artifactRef The ref of the PackageArtifact to find.
     * @return The PackageArtifact matching the artifact ref, or null if none exists.
     */
    private PackageArtifact findArtifactParent(PackageDescription pd, String artifactRef) {
        for (PackageArtifact artifact : pd.getPackageArtifacts()) {
            if(artifact.getArtifactRef().equals(artifactRef)){
                return artifact;
            }
        }
        return null;
    }

    private PackageNode addArtifactToParent(PackageDescription packageDescription, String parentRef, PackageTree tree, PackageArtifact artifact) {
        PackageNode newNode = null;
        if (parentRef != null && !parentRef.isEmpty()) {
            PackageNode parent = tree.getNodesMap().get(parentRef);
            if ( parent != null) {
                newNode = new PackageNode(artifact);
                parent.getChildrenNodes().add(newNode);
                newNode.setParentNode(parent);
            } else {
                PackageArtifact parentArtifact = findArtifactParent(packageDescription, parentRef);
                parent = addNode(packageDescription, parentArtifact , tree);
                if (parent != null) {
                    newNode = new PackageNode(artifact);
                    parent.getChildrenNodes().add(newNode);
                    newNode.setParentNode(parent);
                }
            }
        }
        return newNode;
    }

    /**
     * Gets all valid types (in ontology) for  file system bytestreams if isByteStream is {@code true}. Gets all valid
     * types for file system's directories if isByteStreams is {@code false}
     * @return  all valid types
     */
    private Set<String> getValidTypesPerFileSystemType(boolean isByteStream) {
        Set<String> validTypesForByteStreams = new HashSet<>();
        if (isByteStream) {
            for (String types : getValidTypes()){
                if (types.contains("File")) {
                    validTypesForByteStreams.add(types);
                }
            }
        } else {
            for (String types : getValidTypes()){
                if (!types.contains("File") && !types.equals("ContactInfo") && !types.equals("Creator")) {
                    validTypesForByteStreams.add(types);
                }
            }
        }
        return validTypesForByteStreams;
    }

    private class RelationshipSourceTargetPair {
        private String sourceType;
        private String targetType;

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        public String getTargetType() {
            return targetType;
        }


        public void setTargetType(String targetType) {
            this.targetType = targetType;
        }

        public RelationshipSourceTargetPair() {

        }
        public RelationshipSourceTargetPair(String sourceType, String targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }
    }

    @Override
    public int getPropertyMaxOccurrences(PackageArtifact artifact, String propertyName, String typeName) {
        int maxOccurs = -1;

        if (typeName != null && !typeName.isEmpty()) {
            Set<PropertyType> types = ontology.getCustomPropertyTypes();
            if (types != null && !types.isEmpty()) {
                for (PropertyType type : types) {
                    if (type.getName().equals(typeName)) {
                        if (type.getTypeFieldsMap().containsKey(propertyName)) {
                            maxOccurs = type.getTypeFieldsMap().get(propertyName).getMaxOccurrence();
                        }
                        break;
                    }
                }
            }
        }

        //If we didn't find it in the type or if type wasn't supplied check the artifact properties.
        if (maxOccurs == -1 && artifact != null) {
            Set<PackageOntology.Property> artifactProperties = ontology.getProperties(artifact.getType());
            if (artifactProperties != null && !artifactProperties.isEmpty()) {
                for (PackageOntology.Property property : artifactProperties) {
                    if (property.getName().equals(propertyName)) {
                        maxOccurs = property.getMaxOccurrence();
                        break;
                    }
                }
            }
        }
        return maxOccurs;
    }

    @Override
    public Set<String> getGroupPropertyNames(String propertyType) {
        Set<String> propertyNames = new HashSet<>();

        Set<PropertyType> types = ontology.getCustomPropertyTypes();
        if (types != null && !types.isEmpty()) {
            for (PropertyType type : types) {
                if (type.getName().equals(propertyType)) {
                    propertyNames = type.getFieldNames();
                    break;
                }
            }
        }
        return propertyNames;
    }

    @Override
    public boolean isPropertyComplex(String propertyType) {
        boolean isComplex = false;

        Set<PropertyType> types = ontology.getCustomPropertyTypes();
        if (types != null && !types.isEmpty()) {
            for (PropertyType type : types) {
                if (type.getName().equals(propertyType)) {
                    isComplex = true;
                    break;
                }
            }
        }

        return isComplex;
    }

    @Override
    public String getComplexPropertySubPropertyType(String complexPropertyType,
                                                    String subPropertyName) {
        String propertyType = "";
        Set<PropertyType> types = ontology.getCustomPropertyTypes();
        if (types != null && !types.isEmpty()) {
            for (PropertyType type : types) {
                if (type.getName().equals(complexPropertyType)) {
                    if (type.getFieldNames().contains(subPropertyName)) {
                        propertyType = type.getTypeFieldsMap().get(subPropertyName).getValueType();
                    }
                    break;
                }
            }
        }
        return propertyType;
    }

    @Override
    public boolean isRelationshipHierarchical(PackageArtifact artifact, String relationshipName) {
        boolean hierarchical = false;
        if (artifact != null && ontology.getRelationships(artifact.getType()) != null) {
            for (Relationship relationship : ontology.getRelationships(artifact.getType())) {
                if (relationship.getName().equals(relationshipName)) {
                    hierarchical = relationship.isHierarchical();
                    break;
                }
            }
        }

        return hierarchical;
    }

    @Override
    public int getPropertyMinOccurrences(PackageArtifact artifact,
                                        String propertyName,
                                        String typeName) {
        int minOccurs = -1;

        if (typeName != null && !typeName.isEmpty()) {
            Set<PropertyType> types = ontology.getCustomPropertyTypes();
            if (types != null && !types.isEmpty()) {
                for (PropertyType type : types) {
                    if (type.getName().equals(typeName)) {
                        if (type.getTypeFieldsMap().containsKey(propertyName)) {
                            minOccurs = type.getTypeFieldsMap().get(propertyName).getMinOccurrence();
                        }
                        break;
                    }
                }
            }
        }

        //If we didn't find it in the type or if type wasn't supplied check the artifact properties.
        if (minOccurs == -1 && artifact != null) {
            Set<PackageOntology.Property> artifactProperties = ontology.getProperties(artifact.getType());
            if (artifactProperties != null && !artifactProperties.isEmpty()) {
                for (PackageOntology.Property property : artifactProperties) {
                    if (property.getName().equals(propertyName)) {
                        minOccurs = property.getMinOccurrence();
                        break;
                    }
                }
            }
        }
        return minOccurs;
    }


    @Override
    public boolean isSystemSuppliedProperty(PackageArtifact artifact, String propertyName) {
        boolean isSystemSupplied = false;
        if (artifact != null && ontology.getProperties(artifact.getType()) != null) {
            for (PackageOntology.Property property : ontology.getProperties(artifact.getType())) {
                if (property.getName().equals(propertyName)) {
                    isSystemSupplied = property.isSystemSupplied();
                    break;
                }
            }
        }

        return isSystemSupplied;
    }

    @Override
    public boolean isInheritableProperty(PackageArtifact artifact, String propertyName) {
        boolean isInheritable = false;
        if (artifact != null && ontology.getProperties(artifact.getType()) != null) {
            for (PackageOntology.Property property : ontology.getProperties(artifact.getType())) {
                if (property.getName().equals(propertyName)) {
                    isInheritable = property.isInheritable();
                    break;
                }
            }
        }

        return isInheritable;
    }

    @Override
    public Set<String> getKnownRelationshipNames() {
        Set<String> allKnownTypes = getValidTypes();
        Set<String> knownRelationshipNames = new HashSet<>();
        for ( String type : allKnownTypes) {
            Set<Relationship> knownRelationships = ontology.getRelationships(type);
            for (Relationship relationship : knownRelationships) {
                knownRelationshipNames.add(relationship.getName());
            }
        }
        return knownRelationshipNames;
    }

    @Override
    public Set<String> getArtifactTypesContainProperty(String propertyName) {
        Set<String> typesContainingProperty = new HashSet<>();
        for (String artifactType : ontology.getArtifactTypes()) {
            for (PackageOntology.Property property : ontology.getProperties(artifactType)) {
                if ( property.getName().equals(propertyName)) {
                    typesContainingProperty.add(artifactType);
                    break;
                }
            }
        }
        return typesContainingProperty;
    }

    @Override
    public Set<String> getValidChildrenTypes(String parentType) {
        Set<String> validChildrenType = new HashSet<>();
        Set<String> artifactTypes = ontology.getArtifactTypes();
        for (String artifactType : artifactTypes) {
            Set<Relationship> relationships = ontology.getRelationships(artifactType);
            for (Relationship relationship : relationships) {
                if (DcsBoPackageOntology.relationshipsToParent.contains(relationship.getName())
                        && relationship.getRelatedArtifactType().equals(parentType)) {
                    validChildrenType.add(artifactType);
                }
            }
        }
        return validChildrenType;
    }

    @Override
    public Set<String> getValidDescendantTypes(String parentType) {
        Set<String> validDescendantTypes = new HashSet<>();
        populateValidDescendants(validDescendantTypes, parentType);
        return validDescendantTypes;
    }

    @Override
    public boolean isDateProperty(PackageArtifact artifact, String propertyName) {
        //Simple implmentation with the current ontology returns true if the name matches on of the three date properties
        return !(propertyName == null || propertyName.isEmpty()) &&
            (propertyName.equals(DcsBoPackageOntology.CREATE_DATE) ||
                 propertyName.equals(DcsBoPackageOntology.MODIFIED_DATE) ||
                 propertyName.equals(DcsBoPackageOntology.PUBLICATION_DATE));

    }

    @Override
    public boolean isSizeProperty(PackageArtifact artifact, String propertyName) {
        //Simple implementation for now returns true if the name matches size
        return !(propertyName == null || propertyName.isEmpty()) &&
            propertyName.equals(DcsBoPackageOntology.SIZE);
    }

    @Override
    public boolean isDisciplineProperty(PackageArtifact artifact, String propertyName) {
        //Simple implementation for now returns true if the name matches discipline
        return !(propertyName == null || propertyName.isEmpty()) &&
            propertyName.equals(DcsBoPackageOntology.DISCIPLINE);
    }

    @Override
    public String getUnFormattedProperty(PackageArtifact artifact, String parentPropertyName, String propertyName, String propertyValue) {
        String unFormattedValue = propertyValue;

        String propertyType;

        propertyType = ontology.getPropertyType(artifact.getType(), parentPropertyName, propertyName);

        if (propertyType != null) {
            switch (propertyType) {
                case DcsBoPackageOntology.PHONE_NUMBER_TYPE:
                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    try {
                        Phonenumber.PhoneNumber number = phoneUtil.parseAndKeepRawInput(propertyValue, "US");
                        String regionCode = phoneUtil.getRegionCodeForNumber(number);
                        unFormattedValue = phoneUtil.formatInOriginalFormat(number, regionCode);
                    } catch (NumberParseException e) {
                        log.warn("Phone number wasn't properly formatted uri, using provided value as is: " + unFormattedValue);
                    }
                    break;
                case DcsBoPackageOntology.EMAIL_TYPE:
                    if (propertyValue.startsWith("mailto:")) {
                        unFormattedValue = propertyValue.substring(7);
                    }

                    break;
            }
        }
        return unFormattedValue;
    }

    @Override
    public String getFormattedProperty(PackageArtifact artifact, String parentPropertyName, String propertyName, String propertyValue) {
        String formattedValue = propertyValue;

        String propertyType = ontology.getPropertyType(artifact.getType(), parentPropertyName, propertyName);
        if (propertyType != null) {
            switch (propertyType) {
                case DcsBoPackageOntology.PHONE_NUMBER_TYPE:
                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    try {
                        formattedValue = phoneUtil.format(phoneUtil.parseAndKeepRawInput(propertyValue, "US"), PhoneNumberUtil.PhoneNumberFormat.RFC3966);
                    } catch (NumberParseException e) {
                        log.error("Phone number wasn't properly formed, unable to generate uri: " + formattedValue);
                    }
                    break;
                case DcsBoPackageOntology.EMAIL_TYPE:
                    if (!propertyValue.startsWith("mailto:")) {
                        formattedValue = "mailto:" + propertyValue;
                    }
                    break;
            }
        }
        return formattedValue;
    }

    @Override
    public PropertyValidationResult validateProperty(PackageArtifact artifact, String parentPropertyName, String propertyName, String propertyValue) {
        PropertyValidationResult result = new PropertyValidationResult(true, PropertyValidationResult.VALIDATION_HINT.NONE);

        String propertyType = ontology.getPropertyType(artifact.getType(), parentPropertyName, propertyName);
        if (propertyType != null) {
            OntologyPropertyValidator validator = DcsBoOntologyValidatorFactory.getValidator(propertyType);
            if (validator != null) {
                result = validator.validate(propertyValue);
            }
        }

        return result;
    }

    private  void populateValidDescendants(Set<String> validDescendantTypes, String parentTypes) {
        if (validDescendantTypes == null) {
            throw new IllegalArgumentException("validDescendantTypes argument cannot be null. ");
        }
        Set<String> validChildrenTypes = this.getValidChildrenTypes(parentTypes);

        for (String children : validChildrenTypes) {
            validDescendantTypes.add(children);
            if (children.equals(parentTypes)) {
                continue;
            }
            validDescendantTypes.addAll(this.getValidChildrenTypes(children));
            populateValidDescendants(validDescendantTypes, children);
        }
    }

    @Override
    public boolean propertySupportsMultipleLines(PackageArtifact artifact, String propertyName) {
        //Simple implementation for now returns true if the name matches description
        return !(propertyName == null || propertyName.isEmpty()) &&
            propertyName.equals(DcsBoPackageOntology.DESCRIPTION);
    }
}