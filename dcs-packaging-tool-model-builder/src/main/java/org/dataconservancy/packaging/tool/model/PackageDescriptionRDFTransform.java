/*
  * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.packaging.tool.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dataconservancy.packaging.tool.model.PackageArtifact.PropertyValueGroup;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

public class PackageDescriptionRDFTransform implements PackageResourceMapConstants {
    
    public static Model transformToRDF(PackageDescription description) throws RDFTransformException {
        //Create the basic model that will hold the RDF graph
        Model descriptionModel = ModelFactory.createDefaultModel();
        
        //Create a resource for the package description object
        Resource descriptionResource = descriptionModel.createResource();
        descriptionResource.addProperty(RDF.type, PACKAGE_DESCRIPTION_TYPE);
        descriptionResource.addProperty(HAS_SPECIFICATION_ID, description.getPackageOntologyIdentifier());
        
        Resource rootPropertyResource = descriptionModel.createResource();
        rootPropertyResource.addProperty(RDF.type, PROPERTY_TYPE);
        rootPropertyResource.addProperty(HAS_NAME, "root");
        rootPropertyResource.addProperty(HAS_VALUE, description.getRootArtifactRef().getRefString());
        
        descriptionResource.addProperty(HAS_PROPERTY, rootPropertyResource);
        
        //Loop through artifacts and create a resource for each one. 
        for(PackageArtifact descriptionArtifact : description.getPackageArtifacts()) {
            Resource artifactResource = descriptionModel.createResource();
            artifactResource.addProperty(RDF.type, ARTIFACT_TYPE);
            artifactResource.addProperty(HAS_TYPE, descriptionArtifact.getType());
            artifactResource.addProperty(HAS_ID, descriptionArtifact.getId());
            artifactResource.addProperty(HAS_REF, descriptionArtifact.getArtifactRef().getRefString());
            artifactResource.addProperty(IS_BYTE_STREAM, Boolean.toString(descriptionArtifact.isByteStream()));
            artifactResource.addProperty(IS_IGNORED, Boolean.toString(descriptionArtifact.isIgnored()));

            for (String key : descriptionArtifact.getPropertyNames()) {
                

                if (descriptionArtifact.getSimplePropertyValues(key) != null) {
                    Resource propertyResource = descriptionModel.createResource();
                    propertyResource.addProperty(RDF.type, PROPERTY_TYPE);
                    propertyResource.addProperty(HAS_NAME, key);
                    
                    for (String value : descriptionArtifact.getSimplePropertyValues(key)) {
                        propertyResource.addProperty(HAS_VALUE, value);
                    }      
                    
                    artifactResource.addProperty(HAS_PROPERTY, propertyResource);

                } else if (descriptionArtifact.getPropertyValueGroups(key) != null) {
                    for (PropertyValueGroup group : descriptionArtifact.getPropertyValueGroups(key)) {
                        Resource propertyResource = descriptionModel.createResource();
                        propertyResource.addProperty(RDF.type, PROPERTY_TYPE);
                        propertyResource.addProperty(HAS_NAME, key);
                        
                        for (String subPropertyName : group.getSubPropertyNames()) {
                            Resource subPropertyResource = descriptionModel.createResource();
                            subPropertyResource.addProperty(RDF.type, PROPERTY_TYPE);
                            subPropertyResource.addProperty(HAS_NAME, subPropertyName);
                            
                            if (group.getSubPropertyValues(subPropertyName) != null) {
                                for (String value : group.getSubPropertyValues(subPropertyName)) {
                                    subPropertyResource.addProperty(HAS_VALUE, value);
                                }
                            }
                            
                            propertyResource.addProperty(HAS_PROPERTY, subPropertyResource);
                        }
                        
                        artifactResource.addProperty(HAS_PROPERTY, propertyResource);
                    }
                } else {
                    throw new RDFTransformException("Property: " + key + "'s values could not be found");
                }
            }
            
            //Loop through relationships 
            for (PackageRelationship relationship : descriptionArtifact.getRelationships()) {
                Resource relationshipResource = descriptionModel.createResource();
                relationshipResource.addProperty(RDF.type, RELATIONSHIP_TYPE);
                relationshipResource.addProperty(HAS_TYPE, relationship.getName());
                
                //Loop through all of the targets
                for (String target : relationship.getTargets()) {
                    relationshipResource.addProperty(HAS_TARGET, target);
                }

                relationshipResource.addProperty(REQUIRES_URI, String.valueOf(relationship.requiresUriTargets()));
                artifactResource.addProperty(HAS_RELATIONSHIP, relationshipResource);
            }
            
            //Add the artifact to the package description resource
            descriptionResource.addProperty(HAS_ARTIFACT, artifactResource);
            
        }
        return descriptionModel;
    }
    
    private static String getLiteral(Resource res, Property p) throws RDFTransformException {
        if (!res.hasProperty(p)) {
            throw new RDFTransformException("Expected node " + res + " to have property " + p);
        }

        RDFNode value = res.getProperty(p).getObject();
        
        if (!value.isLiteral()) {
            throw new RDFTransformException("Expected node " + res + " property " + p
                    + " to be a literal");
        }

        return value.asLiteral().getString();
    }
    
    /**
     * Transform a package description encoded as RDF into a PackageDescription object
     * 
     * @param model the RDF model to transform
     * @return The package description represented by the model.
     * @throws RDFTransformException If the model is not a well formed PackageDescription object.
     */
    public static PackageDescription transformToPackageDescription(Model model) throws RDFTransformException {
        PackageDescription pkg = new PackageDescription();
        List<Resource> pkg_resources = model.listResourcesWithProperty(RDF.type, PACKAGE_DESCRIPTION_TYPE).toList();
        List<PackageArtifact> artifacts = new ArrayList<>();

        if (pkg_resources.size() != 1) {
            throw new RDFTransformException("Expected one node with Rdf type: " + PACKAGE_DESCRIPTION_TYPE);
        }

        Resource pkg_resource = pkg_resources.get(0);

        if (pkg_resource.hasProperty(HAS_SPECIFICATION_ID)) {
            pkg.setPackageOntologyIdentifier(getLiteral(pkg_resource, HAS_SPECIFICATION_ID));
        }
        
        for (RDFNode property_node: model.listObjectsOfProperty(pkg_resource, HAS_PROPERTY).toList()) {
            if (!property_node.isResource()) {
                throw new RDFTransformException("Expected node " + property_node + " to be resource");
            }
                            
            Resource property_resource = property_node.asResource();
            
            if (!property_resource.hasProperty(RDF.type, PROPERTY_TYPE)) {
                throw new RDFTransformException("Expected node " + property_node + " to be type " + PROPERTY_TYPE);
            }

            String name = getLiteral(property_resource, HAS_NAME);
            
            if (name.equalsIgnoreCase("root")) {
                if (property_resource.listProperties(PackageResourceMapConstants.HAS_VALUE).toList().size() > 0) {
                    StmtIterator it = property_resource.listProperties(HAS_VALUE);
                    if (it.hasNext()) {
                        Statement stmt = it.nextStatement();
                        String value = stmt.getLiteral().getString();
                        pkg.setRootArtifactRef(value);
                    }
                }                
            }
        }
        
        // Extract all linked artifacts
        for (RDFNode artifact_node: model.listObjectsOfProperty(pkg_resource, HAS_ARTIFACT).toList()) {
            if (!artifact_node.isResource()) {
                throw new RDFTransformException("Expected node " + artifact_node + " to be resource.");
            }
            
            Resource artifact_resource = artifact_node.asResource();
            
            if (!artifact_resource.hasProperty(RDF.type, ARTIFACT_TYPE)) {
                throw new RDFTransformException("Expected object of " + HAS_ARTIFACT + " property to have RDF type " + ARTIFACT_TYPE);
            }
            
            PackageArtifact artifact = new PackageArtifact();
            artifacts.add(artifact);
            
            artifact.setArtifactRef(getLiteral(artifact_resource, HAS_REF));
            artifact.setId(getLiteral(artifact_resource, HAS_ID));
            final String type_str = getLiteral(artifact_resource, HAS_TYPE);

            artifact.setType(type_str);
            artifact.setByteStream(Boolean.valueOf(getLiteral(artifact_resource, IS_BYTE_STREAM)));
            artifact.setIgnored(Boolean.valueOf(getLiteral(artifact_resource, IS_IGNORED)));
            
            // Extract artifact properties
            
            for (RDFNode property_node: model.listObjectsOfProperty(artifact_resource, HAS_PROPERTY).toList()) {
                if (!property_node.isResource()) {
                    throw new RDFTransformException("Expected node " + property_node + " to be resource");
                }
                                
                Resource property_resource = property_node.asResource();
                
                if (!property_resource.hasProperty(RDF.type, PROPERTY_TYPE)) {
                    throw new RDFTransformException("Expected node " + property_node + " to be type " + PROPERTY_TYPE);
                }

                String name = getLiteral(property_resource, HAS_NAME);
                
                if (property_resource.listProperties(PackageResourceMapConstants.HAS_VALUE).toList().size() > 0) {
                    StmtIterator it = property_resource.listProperties(HAS_VALUE);
                    while (it.hasNext()) {
                        Statement stmt = it.nextStatement();
                        String value = stmt.getLiteral().getString();
                        artifact.addSimplePropertyValue(name, value);
                    }
                } else {
                    PropertyValueGroup group = new PropertyValueGroup();

                    for (RDFNode subProperty_node : model.listObjectsOfProperty(property_resource, HAS_PROPERTY).toList()) {
                        if (!subProperty_node.isResource()) {
                            throw new RDFTransformException("Expected node " + subProperty_node + " to be resource");
                        }
                        
                        Resource subproperty_resource = subProperty_node.asResource();
                        
                        if (!subproperty_resource.hasProperty(RDF.type, PROPERTY_TYPE)) {
                            throw new RDFTransformException("Expected node " + subProperty_node + " to be type " + PROPERTY_TYPE);
                        }
                        
                        String subPropertyName = getLiteral(subproperty_resource, HAS_NAME);
                        
                        StmtIterator subPropertyIter = subproperty_resource.listProperties(HAS_VALUE);
                        while (subPropertyIter.hasNext()) {
                            Statement stmt = subPropertyIter.nextStatement();
                            String value = stmt.getLiteral().getString();
                            group.addSubPropertyValue(subPropertyName, value);
                        }                        
                    }
                    artifact.addPropertyValueGroup(name, group);
                }
            }

            // Extract artifact relationships
            
            List<PackageRelationship> relationships = new ArrayList<>();

            for (RDFNode rel_node: model.listObjectsOfProperty(artifact_resource, HAS_RELATIONSHIP).toList()) {
                if (!rel_node.isResource()) {
                    throw new RDFTransformException("Expected node " + rel_node + " to be resource.");
                }
                
                Resource rel_resource = rel_node.asResource();
                
                if (!rel_resource.hasProperty(RDF.type, RELATIONSHIP_TYPE)) {
                    throw new RDFTransformException("Expected resource " + rel_resource + " to be type " + RELATIONSHIP_TYPE);
                }
                
                String predicate = getLiteral(rel_resource, HAS_TYPE);
                Set<String> targets = new HashSet<>();
                
                for (RDFNode target_object: model.listObjectsOfProperty(rel_resource, HAS_TARGET).toList()) {
                    if (!target_object.isLiteral()) {
                        throw new RDFTransformException("Expected node " + target_object + " to be literal.");
                    }
                    
                    targets.add(target_object.asLiteral().getString());
                }

                boolean requiresUri = Boolean.parseBoolean(getLiteral(rel_resource, REQUIRES_URI));

                PackageRelationship relationship = new PackageRelationship(predicate, requiresUri, targets);

                relationships.add(relationship);
            }
            
            artifact.setRelationships(relationships);
        }
        
        pkg.setPackageArtifacts(new HashSet<>(artifacts));
        
        return pkg;
    }
}