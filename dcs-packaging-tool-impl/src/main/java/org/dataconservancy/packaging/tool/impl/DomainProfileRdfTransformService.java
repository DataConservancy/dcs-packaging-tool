package org.dataconservancy.packaging.tool.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dataconservancy.packaging.tool.model.PackageResourceMapConstants;
import org.dataconservancy.packaging.tool.model.RDFTransformException;
import org.dataconservancy.packaging.tool.model.dprofile.CardinalityConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.FileAssociation;
import org.dataconservancy.packaging.tool.model.dprofile.NodeConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyCategory;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.dprofile.StructuralRelation;
import org.dataconservancy.packaging.tool.model.dprofile.SuppliedProperty;
import org.joda.time.DateTime;

/**
 * Transforms DomainProfile and related classes to RDF and back.
 */
public class DomainProfileRdfTransformService implements PackageResourceMapConstants {
    public static final String DC_DP_NS_URI = "http://www.dataconservancy.org/ptg-prof/";
    public static final Resource DP_TYPE = ResourceFactory.createResource(
        DC_DP_NS_URI + "DomainProfile");
    public static final Resource NODE_TYPE_TYPE = ResourceFactory.createResource(
        DC_DP_NS_URI + "NodeType");
    public static final Resource PROPERTY_TYPE_TYPE = ResourceFactory.createResource(
        DC_DP_NS_URI + "PropertyType");
    public static final Resource PROPERTY_CATEGORY_TYPE = ResourceFactory.createResource(
        DC_DP_NS_URI + "PropertyCategory");
    public static final Resource NODE_TRANSFORM_TYPE = ResourceFactory.createResource(
        DC_DP_NS_URI + "NodeTransform");
    public static final Property HAS_DOMAIN_ID = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasDomainId");
    public static final Property HAS_DOMAIN_PROFILE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasDomainProfile");
    public static final Property HAS_DOMAIN_TYPE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasDomainType");
    public static final Property HAS_PARENT_CONSTRAINT = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasParentConstraint");
    public static final Resource NODE_CONSTRAINT_TYPE = ResourceFactory.createResource(
        DC_DP_NS_URI + "NodeConstraint");
    public static final Property HAS_PROPERTY_CONSTRAINT = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasPropertyConstraint");
    public static final Resource PROPERTY_CONSTRAINT_TYPE = ResourceFactory.createResource(
        DC_DP_NS_URI + "PropertyConstraint");
    public static final Property HAS_INHERITABLE_PROPERTY = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasInheritableProperty");
    public static final Property HAS_DEFAULT_PROPERTY_VALUE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasDefaultPropertyValue");
    public static final Resource PROPERTY_VALUE_TYPE = ResourceFactory.createResource(
        DC_DP_NS_URI + "PropertyValue");
    public static final Property HAS_SUPPLIED_PROPERTY = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasSuppliedProperty");
    public static final Property HAS_FILE_ASSOCIATION = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasFileAssociation");
    public static final Resource CARDINALITY_CONSTRAINT_TYPE = ResourceFactory.createResource(
        DC_DP_NS_URI + "CardinalityConstraint");
    public static final Property HAS_CHILD_CONSTRAINT = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasChildConstraint");
    public static final Property HAS_DOMAIN_PREDICATE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasDomainPredicate");
    public static final Property HAS_PROPERTY_VALUE_HINT = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasPropertyValueHint");
    public static final Property HAS_ALLOWED_VALUE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasAllowedValue");
    public static final Property HAS_PROPERTY_CATEGORY = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasPropertyCategory");
    public static final Property IS_READ_ONLY = ResourceFactory.createProperty(
        DC_DP_NS_URI, "isReadOnly");
    public static final Property HAS_PROPERTY_TYPE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasPropertyType");
    public static final Property HAS_SOURCE_TYPE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasSourceType");
    public static final Property HAS_SOURCE_PARENT_CONSTRAINT = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasSourceParentConstraint");
    public static final Property HAS_SOURCE_GRANDPARENT_CONSTRAINT = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasSourceGrandparentConstraint");
    public static final Property HAS_SOURCE_CHILD_CONSTRAINT = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasSourceChildConstraint");
    public static final Property HAS_RESULT_NODE_TYPE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasResultNodeType");
    public static final Property HAS_RESULT_PARENT_CONSTRAINT = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasResultParentConstraint");
    public static final Property INSERT_PARENT = ResourceFactory.createProperty(
        DC_DP_NS_URI, "insertParent");
    public static final Property MOVE_RESULT_GRANDPARENT = ResourceFactory.createProperty(
        DC_DP_NS_URI, "moveResultGrandparent");
    public static final Property REMOVE_EMPTY_PARENT = ResourceFactory.createProperty(
        DC_DP_NS_URI, "removeEmptyParent");
    public static final Property HAS_NODE_TYPE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasNodeType");
    public static final Property MATCHES_ANY = ResourceFactory.createProperty(
        DC_DP_NS_URI, "matchesAny");
    public static final Property MATCHES_NONE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "matchesNone");
    public static final Property HAS_STRUCTURAL_RELATION = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasStructuralRelation");
    public static final Resource STRUCTURAL_RELATION_TYPE = ResourceFactory.createResource(
        DC_DP_NS_URI + "StructuralRelation");
    public static final Property HAS_PARENT_PREDICATE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasParentPredicate");
    public static final Property HAS_CHILD_PREDICATE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasChildPredicate");
    public static final Property HAS_MIN = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasMin");
    public static final Property HAS_MAX = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasMax");
    public static final Property HAS_CARDINALITY_CONSTRAINT = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasCardinalityConstraint");
    public static final Property HAS_PROPERTY_VALUE_TYPE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasPropertyValueType");
    public static final Resource SUPPLIED_PROPERTY_TYPE = ResourceFactory.createResource(
        DC_DP_NS_URI + "SuppliedProperty");
    public static final Property HAS_SUPPLIED_PROPERTY_VALUE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasSuppliedPropertyValue");
    public static final Property HAS_NODE_TRANSFORM = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasNodeTransform");
    public static final Property HAS_STRING_VALUE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasStringValue");
    public static final Property HAS_LONG_VALUE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasLongValue");
    public static final Property HAS_DATE_TIME_VALUE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasDateTimeValue");
    public static final Property HAS_COMPLEX_VALUE = ResourceFactory.createProperty(
        DC_DP_NS_URI, "hasComplexValue");

    private Map<URI, NodeType> transformedNodeTypes;

    /**
     * Transforms a DomainProfile into a Jena RDF Model.  The profile will be a blank node.
     * @param profile The DomainProfile to transform.
     * @return The Jena model containing the RDF representation of the DomainProfile
     * @throws RDFTransformException
     */
    public Model transformToRdf(DomainProfile profile) throws RDFTransformException {
        return transformToRdf(profile, null);
    }
    
    /**
     * Transforms a DomainProfile into a Jena RDF Model
     * @param profile The DomainProfile to transform.
     * @param profileResourceURI If defined, the Profile resource will have this URI. 
     *        If null, it will be a blank node.
     * @return The Jena model containing the RDF representation of the DomainProfile
     * @throws RDFTransformException
     */
    public Model transformToRdf(DomainProfile profile, String profileResourceURI)
        throws RDFTransformException {
        //Create the basic model that will hold the RDF graph
        Model profileModel = ModelFactory.createDefaultModel();

        Resource profileResource = profileModel.createResource(profileResourceURI);
        
        profileResource.addProperty(RDF.type, DP_TYPE);

        if (profile.getLabel() != null) {
            profileResource.addProperty(RDFS.label, profile.getLabel());
        }

        if (profile.getDescription() != null) {
            profileResource.addProperty(RDFS.comment, profile.getDescription());
        }

        if (profile.getIdentifier() != null) {
            profileResource.addProperty(HAS_ID, profile.getIdentifier().toString());
        }

        if (profile.getDomainIdentifier() != null) {
            profileResource.addProperty(HAS_DOMAIN_ID, profile.getDomainIdentifier().toString());
        }

        if (profile.getNodeTypes() != null && !profile.getNodeTypes().isEmpty()) {
            for (NodeType nodeType : profile.getNodeTypes()) {
                profileResource.addProperty(HAS_NODE_TYPE, transformToRdf(profileModel, nodeType, profileResource));
            }
        }

        if (profile.getPropertyTypes() != null && !profile.getPropertyTypes().isEmpty()) {
            for (PropertyType propertyType : profile.getPropertyTypes()) {
                profileResource.addProperty(HAS_PROPERTY_TYPE, transformToRdf(profileModel, propertyType));
            }
        }

        if (profile.getPropertyCategories() != null && !profile.getPropertyCategories().isEmpty()) {
            for (PropertyCategory category : profile.getPropertyCategories()) {
                profileResource.addProperty(HAS_PROPERTY_CATEGORY, transformToRdf(profileModel, category));
            }
        }

        if (profile.getNodeTransforms() != null && !profile.getNodeTransforms().isEmpty()) {
            for (NodeTransform transform : profile.getNodeTransforms()) {
                profileResource.addProperty(HAS_NODE_TRANSFORM, transformToRdf(profileModel, transform, profileResource));
            }
        }

        return profileModel;

    }

    private Resource transformToRdf(Model model, NodeType nodeType, Resource domainProfileResource)
        throws RDFTransformException {
        Resource nodeTypeResource;
        List<Resource> nodeTypeList = model.listResourcesWithProperty(HAS_ID, nodeType.getIdentifier().toString()).toList();
        //If the node type has already been created return the resource otherwise, create a new one
        if (nodeTypeList != null && !nodeTypeList.isEmpty()) {
            nodeTypeResource = nodeTypeList.get(0);
        } else {
            nodeTypeResource = model.createResource();
            nodeTypeResource.addProperty(RDF.type, NODE_TYPE_TYPE);

            if (nodeType.getLabel() != null) {
                nodeTypeResource.addProperty(RDFS.label, nodeType.getLabel());
            }

            if (nodeType.getDescription() != null) {
                nodeTypeResource.addProperty(RDFS.comment, nodeType.getDescription());
            }

            if (nodeType.getIdentifier() != null) {
                nodeTypeResource.addProperty(HAS_ID, nodeType.getIdentifier().toString());
            }

            nodeTypeResource.addProperty(HAS_DOMAIN_PROFILE, domainProfileResource);

            if (nodeType.getDomainTypes() != null && !nodeType.getDomainTypes().isEmpty()) {
                for (URI domainType : nodeType.getDomainTypes()) {
                    nodeTypeResource.addProperty(HAS_DOMAIN_TYPE, model.createResource(domainType.toString()));
                }
            }

            if (nodeType.getParentConstraints() != null && !nodeType.getParentConstraints().isEmpty()) {
                for (NodeConstraint parentConstraint : nodeType.getParentConstraints()) {
                    if (parentConstraint.getNodeType() != null && parentConstraint.getNodeType().equals(nodeType)) {
                        nodeTypeResource.addProperty(HAS_PARENT_CONSTRAINT, transformToRdf(model, parentConstraint, domainProfileResource, nodeTypeResource));
                    } else {
                        nodeTypeResource.addProperty(HAS_PARENT_CONSTRAINT, transformToRdf(model, parentConstraint, domainProfileResource, null));
                    }
                }
            }

            if (nodeType.getPropertyConstraints() != null && !nodeType.getPropertyConstraints().isEmpty()) {
                for (PropertyConstraint propertyConstraint : nodeType.getPropertyConstraints()) {
                    nodeTypeResource.addProperty(HAS_PROPERTY_CONSTRAINT, transformToRdf(model, propertyConstraint));
                }
            }

            if (nodeType.getInheritableProperties() != null && !nodeType.getInheritableProperties().isEmpty()) {
                for (PropertyType inheritableProperty : nodeType.getInheritableProperties()) {
                    nodeTypeResource.addProperty(HAS_INHERITABLE_PROPERTY, transformToRdf(model, inheritableProperty));
                }
            }

            if (nodeType.getDefaultPropertyValues() != null && !nodeType.getDefaultPropertyValues().isEmpty()) {
                for (org.dataconservancy.packaging.tool.model.dprofile.Property defaultValue : nodeType.getDefaultPropertyValues()) {
                    nodeTypeResource.addProperty(HAS_DEFAULT_PROPERTY_VALUE, transformToRdf(model, defaultValue));
                }
            }

            if (nodeType.getSuppliedProperties() != null && !nodeType.getSuppliedProperties().isEmpty()) {
                transformSuppliedPropertiesToRdf(model, nodeType.getSuppliedProperties(), nodeTypeResource);
            }

            if (nodeType.getFileAssociation() != null) {
                nodeTypeResource.addProperty(HAS_FILE_ASSOCIATION, nodeType.getFileAssociation().toString());
            }

            if (nodeType.getPreferredCountOfChildrenWithFiles() != null) {
                nodeTypeResource.addProperty(HAS_CHILD_CONSTRAINT, transformCardinalityConstraintToRdf(model, nodeType.getPreferredCountOfChildrenWithFiles()));
            }
        }
        return nodeTypeResource;
    }

    private Resource transformToRdf(Model model, NodeConstraint constraint, Resource domainProfileResource, Resource nodeTypeResource)
        throws RDFTransformException {
        Resource constraintResource = model.createResource();
        constraintResource.addProperty(RDF.type, NODE_CONSTRAINT_TYPE);
        constraintResource.addLiteral(MATCHES_ANY, constraint.matchesAny());
        constraintResource.addLiteral(MATCHES_NONE, constraint.matchesNone());

        if (constraint.getNodeType() != null) {
            if (nodeTypeResource != null) {
                constraintResource.addProperty(HAS_NODE_TYPE, nodeTypeResource);
            } else {
                constraintResource.addProperty(HAS_NODE_TYPE, transformToRdf(model, constraint.getNodeType(), domainProfileResource));
            }
        }

        if (constraint.getStructuralRelation() != null) {
            constraintResource.addProperty(HAS_STRUCTURAL_RELATION, transformToRdf(model, constraint.getStructuralRelation()));
        }

        return constraintResource;
    }

    private Resource transformToRdf(Model model, StructuralRelation relation) {
        Resource structuralRelationResource = model.createResource();
        structuralRelationResource.addProperty(RDF.type, STRUCTURAL_RELATION_TYPE);
        if (relation.getHasParentPredicate() != null) {
            structuralRelationResource.addProperty(HAS_PARENT_PREDICATE, relation.getHasParentPredicate().toString());
        }

        if (relation.getHasChildPredicate() != null) {
            structuralRelationResource.addProperty(HAS_CHILD_PREDICATE, relation.getHasChildPredicate().toString());
        }

        return structuralRelationResource;
    }

    private Resource transformToRdf(Model model, PropertyConstraint constraint)
        throws RDFTransformException {
        Resource constraintResource = model.createResource();
        constraintResource.addProperty(RDF.type, PROPERTY_CONSTRAINT_TYPE);
        constraintResource.addProperty(HAS_CARDINALITY_CONSTRAINT, transformCardinalityConstraintToRdf(model, constraint));
        if (constraint.getPropertyType() != null) {
            constraintResource.addProperty(HAS_PROPERTY_TYPE, transformToRdf(model, constraint.getPropertyType()));
        }

        return constraintResource;
    }

    private Resource transformCardinalityConstraintToRdf(Model model, CardinalityConstraint constraint) {
        Resource constraintResource = model.createResource();
        constraintResource.hasProperty(RDF.type, CARDINALITY_CONSTRAINT_TYPE);
        constraintResource.addLiteral(HAS_MIN, constraint.getMinimum());
        constraintResource.addLiteral(HAS_MAX, constraint.getMaximum());
        return constraintResource;
    }

    private Resource transformToRdf(Model model, PropertyType type)
        throws RDFTransformException {
        Resource typeResource;

        List<Resource> typeList = model.listResourcesWithProperty(HAS_DOMAIN_PREDICATE, type.getDomainPredicate().toString()).toList();

        //If we've already created a resource for the property type return it otherwise create a new one
        if (typeList != null && !typeList.isEmpty()) {
            typeResource = typeList.get(0);
        } else {
            typeResource = model.createResource();
            typeResource.addProperty(RDF.type, PROPERTY_TYPE_TYPE);
            if (type.getLabel() != null) {
                typeResource.addProperty(RDFS.label, type.getLabel());
            }

            if (type.getDescription() != null) {
                typeResource.addProperty(RDFS.comment, type.getDescription());
            }

            if (type.getDomainPredicate() != null) {
                typeResource.addProperty(HAS_DOMAIN_PREDICATE, type.getDomainPredicate().toString());
            }

            typeResource.addLiteral(IS_READ_ONLY, type.isReadOnly());

            if (type.getPropertyValueType() != null) {
                typeResource.addProperty(HAS_PROPERTY_VALUE_TYPE, type.getPropertyValueType().toString());
            }

            if (type.getPropertyValueHint() != null) {
                typeResource.addProperty(HAS_PROPERTY_VALUE_HINT, type.getPropertyValueHint().toString());
            }

            if (type.getAllowedPropertyValues() != null && !type.getAllowedPropertyValues().isEmpty()) {
                for (org.dataconservancy.packaging.tool.model.dprofile.Property value : type.getAllowedPropertyValues()) {
                    typeResource.addProperty(HAS_ALLOWED_VALUE, transformToRdf(model, value));
                }
            }

            if (type.getComplexPropertyConstraints() != null && !type.getComplexPropertyConstraints().isEmpty()) {
                for (PropertyConstraint pc : type.getComplexPropertyConstraints()) {
                    typeResource.addProperty(HAS_PROPERTY_CONSTRAINT, transformToRdf(model, pc));
                }
            }
            
            if (type.getComplexDomainTypes() != null && !type.getComplexDomainTypes().isEmpty()) {
                for (URI dt: type.getComplexDomainTypes()) {
                    typeResource.addProperty(HAS_DOMAIN_TYPE, model.createResource(dt.toString()));
                }
            }

            if (type.getPropertyCategory() != null) {
                typeResource.addProperty(HAS_PROPERTY_CATEGORY, transformToRdf(model, type.getPropertyCategory()));
            }
        }
        return typeResource;
    }

    private Resource transformToRdf(Model model, org.dataconservancy.packaging.tool.model.dprofile.Property value)
        throws RDFTransformException {
        Resource valueResource = model.createResource();
        valueResource.addProperty(RDF.type, PROPERTY_VALUE_TYPE);
        valueResource.addProperty(HAS_PROPERTY_TYPE, transformToRdf(model, value.getPropertyType()));

        switch (value.getPropertyType().getPropertyValueType()) {
            case STRING:
                valueResource.addLiteral(HAS_STRING_VALUE, value.getStringValue());
                break;
            case LONG:
                valueResource.addLiteral(HAS_LONG_VALUE, value.getLongValue());
                break;
            case DATE_TIME:
                valueResource.addLiteral(HAS_DATE_TIME_VALUE, value.getDateTimeValue().getMillis());
                break;
            case COMPLEX:
                if (value.getComplexValue() != null) {
                    for (org.dataconservancy.packaging.tool.model.dprofile.Property subValue : value.getComplexValue()) {
                        valueResource.addProperty(HAS_COMPLEX_VALUE, transformToRdf(model, subValue));
                    }
                }
                
                break;
            default:
                throw new RDFTransformException("Property Value has an unknown value type");
        }

        return valueResource;
    }

    private Resource transformToRdf(Model model, PropertyCategory category)
        throws RDFTransformException {
        Resource categoryResource = model.createResource();
        categoryResource.addProperty(RDF.type, PROPERTY_CATEGORY_TYPE);

        if (category.getLabel() != null) {
            categoryResource.addProperty(RDFS.label, category.getLabel());
        }

        if (category.getDescription() != null) {
            categoryResource.addProperty(RDFS.comment, category.getDescription());
        }

        if (category.getPropertyTypes() != null && !category.getPropertyTypes().isEmpty()) {
            for (PropertyType type : category.getPropertyTypes()) {
                categoryResource.addProperty(HAS_PROPERTY_TYPE, transformToRdf(model, type));
            }
        }
        return categoryResource;
    }

    private void transformSuppliedPropertiesToRdf(Model model, Map<PropertyType, SuppliedProperty> suppliedPropertyMap, Resource nodeTypeResource)
        throws RDFTransformException {

        for (PropertyType type : suppliedPropertyMap.keySet()) {
            Resource suppliedPropertyResource = model.createResource();
            suppliedPropertyResource.addProperty(RDF.type, SUPPLIED_PROPERTY_TYPE);
            suppliedPropertyResource.addProperty(HAS_PROPERTY_TYPE, transformToRdf(model, type));
            suppliedPropertyResource.addProperty(HAS_SUPPLIED_PROPERTY_VALUE, suppliedPropertyMap.get(type).toString());
            nodeTypeResource.addProperty(HAS_SUPPLIED_PROPERTY, suppliedPropertyResource);
        }
    }

    private Resource transformToRdf(Model model, NodeTransform transform, Resource domainProfileResource)
        throws RDFTransformException {
        Resource transformResource = model.createResource();
        transformResource.addProperty(RDF.type, NODE_TRANSFORM_TYPE);

        if (transform.getLabel() != null) {
            transformResource.addProperty(RDFS.label, transform.getLabel());
        }

        if (transform.getDescription() != null) {
            transformResource.addProperty(RDFS.comment, transform.getDescription());
        }

        if (transform.getSourceNodeType() != null) {
            transformResource.addProperty(HAS_SOURCE_TYPE, transformToRdf(model, transform.getSourceNodeType(), domainProfileResource));
        }

        if (transform.getSourceParentConstraint() != null) {
            transformResource.addProperty(HAS_SOURCE_PARENT_CONSTRAINT, transformToRdf(model, transform.getSourceParentConstraint(), domainProfileResource, null));
        }

        if (transform.getSourceGrandParentConstraint() != null) {
            transformResource.addProperty(HAS_SOURCE_GRANDPARENT_CONSTRAINT, transformToRdf(model, transform.getSourceGrandParentConstraint(), domainProfileResource, null));
        }

        if (transform.getSourceChildConstraint() != null) {
            transformResource.addProperty(HAS_SOURCE_CHILD_CONSTRAINT, transformToRdf(model, transform.getSourceChildConstraint(), domainProfileResource, null));
        }

        if (transform.getResultNodeType() != null) {
            transformResource.addProperty(HAS_RESULT_NODE_TYPE, transformToRdf(model, transform.getResultNodeType(), domainProfileResource));
        }

        if (transform.getResultParentConstraint() != null) {
            transformResource.addProperty(HAS_RESULT_PARENT_CONSTRAINT, transformToRdf(model, transform.getResultParentConstraint(), domainProfileResource, null));
        }

        transformResource.addLiteral(INSERT_PARENT, transform.insertParent());
        transformResource.addLiteral(MOVE_RESULT_GRANDPARENT, transform.moveResultToGrandParent());
        transformResource.addLiteral(REMOVE_EMPTY_PARENT, transform.removeEmptyParent());

        return transformResource;
    }

    /**
     * Transforms a Jena model representing a DomainProfile in RDF into a DomainProfile object.
     * @param model The model containing the DomainProfile
     * @return The DomainProfile object that corresponds to the RDF
     * @throws RDFTransformException
     */
    public DomainProfile transformToProfile(Model model) throws RDFTransformException {
        transformedNodeTypes = new HashMap<>();

        List<Resource> domainProfiles = model.listResourcesWithProperty(RDF.type, DP_TYPE).toList();

        if (domainProfiles.size() != 1) {
            throw new RDFTransformException("Expected one node with Rdf type: " + DP_TYPE);
        }

        Resource profileResource = domainProfiles.get(0);

        return transformToDomainProfile(profileResource, model);
    }

    public DomainProfile transformToDomainProfile(Resource profileResource, Model model)
        throws RDFTransformException {
        transformedNodeTypes = new HashMap<>();
        DomainProfile profile = new DomainProfile();

        if (profileResource.hasProperty(RDFS.label)) {
            profile.setLabel(getLiteral(profileResource, RDFS.label).getString());
        }

        if (profileResource.hasProperty(RDFS.comment)) {
            profile.setDescription(getLiteral(profileResource, RDFS.comment).getString());
        }

        if (profileResource.hasProperty(HAS_ID)) {
            try {
                profile.setIdentifier(new URI(getLiteral(profileResource, HAS_ID).getString()));
            } catch (URISyntaxException e) {
                throw new RDFTransformException("Expected id to be a URI.");
            }
        }

        if (profileResource.hasProperty(HAS_DOMAIN_ID)) {
            try {
                profile.setDomainIdentifier(new URI(getLiteral(profileResource, HAS_DOMAIN_ID).getString()));
            } catch (URISyntaxException e) {
                throw new RDFTransformException("Expected domain id to be a URI.");
            }
        }

        List<NodeType> nodeTypes = new ArrayList<>();
        for (RDFNode nodeTypeNode : model.listObjectsOfProperty(profileResource, HAS_NODE_TYPE).toList()) {
            if (!nodeTypeNode.isResource()) {
                throw new RDFTransformException(
                    "Expected node " + nodeTypeNode + " to be resource");
            }

            nodeTypes.add(transformToNodeType(nodeTypeNode.asResource(), profile, model));
        }
        profile.setNodeTypes(nodeTypes);

        List<PropertyType> propertyTypes = new ArrayList<>();
        for (RDFNode propertyTypeNode : model.listObjectsOfProperty(profileResource, HAS_PROPERTY_TYPE).toList()) {
            if (!propertyTypeNode.isResource()) {
                throw new RDFTransformException(
                    "Expected node " + propertyTypeNode + " to be resource");
            }

            propertyTypes.add(transformToPropertyType(propertyTypeNode.asResource(), profile, model));
        }
        profile.setPropertyTypes(propertyTypes);

        List<PropertyCategory> propertyCategories = new ArrayList<>();
        for (RDFNode propertyCategoryNode : model.listObjectsOfProperty(profileResource, HAS_PROPERTY_CATEGORY).toList()) {
            if (!propertyCategoryNode.isResource()) {
                throw new RDFTransformException("Expected node " + propertyCategoryNode + " to be a resource");
            }

            propertyCategories.add(transformToPropertyCategory(propertyCategoryNode.asResource(), model, profile));
        }
        profile.setPropertyCategories(propertyCategories);

        List<NodeTransform> nodeTransforms = new ArrayList<>();
        for (RDFNode nodeTransformNode : model.listObjectsOfProperty(profileResource, HAS_NODE_TRANSFORM).toList()) {
            if (!nodeTransformNode.isResource()) {
                throw new RDFTransformException("Expected node " + nodeTransformNode + " to be a resource");
            }

            nodeTransforms.add(transformToNodeTransform(nodeTransformNode.asResource(), profile, model));
        }
        profile.setNodeTransforms(nodeTransforms);

        return profile;
    }

    public NodeType transformToNodeType(Resource resource, DomainProfile profile, Model model)
        throws RDFTransformException {

        URI identifier = null;
        if (resource.hasProperty(HAS_ID)) {
            try {
                identifier = new URI(getLiteral(resource, HAS_ID).getString());
            } catch (URISyntaxException e) {
                throw new RDFTransformException("Expected identifier to be a URI");
            }
        }

        if (transformedNodeTypes != null && transformedNodeTypes.get(identifier) != null) {
            return transformedNodeTypes.get(identifier);
        }

        NodeType nodeType = new NodeType();
        nodeType.setDomainProfile(profile);
        nodeType.setIdentifier(identifier);

        if (resource.hasProperty(RDFS.label)) {
            nodeType.setLabel(getLiteral(resource, RDFS.label).getString());
        }

        if (resource.hasProperty(RDFS.comment)) {
            nodeType.setDescription(getLiteral(resource, RDFS.comment).getString());
        }

        
        List<URI> domainTypes = new ArrayList<>();
        
        for (RDFNode dt : model.listObjectsOfProperty(resource, HAS_DOMAIN_TYPE).toList()) {
            if (!dt.isURIResource()) {
                throw new RDFTransformException(
                        "Expected node " + dt + " to be a uri resource");
            }
            domainTypes.add(URI.create(dt.asResource().getURI()));
        }

        nodeType.setDomainTypes(domainTypes);

        //We add this to the map here so that if parent constraints refer to this node type it's loaded instead of trying to be deserialized
        if (transformedNodeTypes != null) {
            transformedNodeTypes.put(nodeType.getIdentifier(), nodeType);
        }

        List<NodeConstraint> parentConstraints = new ArrayList<>();
        for (RDFNode parentConstraintNode : model.listObjectsOfProperty(resource, HAS_PARENT_CONSTRAINT).toList()) {
            if (!parentConstraintNode.isResource()) {
                throw new RDFTransformException(
                    "Expected node " + parentConstraintNode + " to be resource");
            }

            parentConstraints.add(transformToNodeConstraint(parentConstraintNode.asResource(), profile, model));
        }
        nodeType.setParentConstraints(parentConstraints);

        List<PropertyConstraint> propertyConstraints = new ArrayList<>();
        for (RDFNode propertyConstraintNode : model.listObjectsOfProperty(resource, HAS_PROPERTY_CONSTRAINT).toList()) {
            if (!propertyConstraintNode.isResource()) {
                throw new RDFTransformException(
                    "Expected node " + propertyConstraintNode + " to be resource");
            }

            propertyConstraints.add(transformToPropertyConstraint(propertyConstraintNode.asResource(), profile, model));
        }
        nodeType.setPropertyConstraints(propertyConstraints);

        if (resource.hasProperty(HAS_INHERITABLE_PROPERTY)) {
            List<PropertyType> inheritableProperties = new ArrayList<>();
            for (RDFNode inheritablePropertyNode : model.listObjectsOfProperty(resource, HAS_INHERITABLE_PROPERTY).toList()) {
                if (!inheritablePropertyNode.isResource()) {
                    throw new RDFTransformException(
                        "Expected node " + inheritablePropertyNode +
                            " to be resource");
                }

                inheritableProperties.add(transformToPropertyType(inheritablePropertyNode.asResource(), profile, model));
            }
            nodeType.setInheritableProperties(inheritableProperties);
        }

        List<org.dataconservancy.packaging.tool.model.dprofile.Property> defaultPropertyValues = new ArrayList<>();
        for (RDFNode defaultPropertyNode : model.listObjectsOfProperty(resource, HAS_DEFAULT_PROPERTY_VALUE).toList()) {
            if (!defaultPropertyNode.isResource()) {
                throw new RDFTransformException(
                    "Expected node " + defaultPropertyNode + " to be resource");
            }

            defaultPropertyValues.add(transformToPropertyValue(defaultPropertyNode.asResource(), profile, model));
        }
        nodeType.setDefaultPropertyValues(defaultPropertyValues);

        if (resource.hasProperty(HAS_SUPPLIED_PROPERTY)) {
            nodeType.setSuppliedProperties(transformToSuppliedProperties(resource, profile, model));
        }

        if (resource.hasProperty(HAS_FILE_ASSOCIATION)) {
            nodeType.setFileAssociation(FileAssociation.valueOf(getLiteral(resource, HAS_FILE_ASSOCIATION).getString()));
        }

        if (resource.hasProperty(HAS_CHILD_CONSTRAINT)) {
            CardinalityConstraint childConstraint = new CardinalityConstraint();
            transformToCardinalityConstraint(childConstraint, resource.getPropertyResourceValue(HAS_CHILD_CONSTRAINT));
            nodeType.setChildFileConstraint(childConstraint);
        }

        return nodeType;
    }

    private PropertyCategory transformToPropertyCategory(Resource resource, Model model, DomainProfile profile)
        throws RDFTransformException {
        PropertyCategory category = new PropertyCategory();
        if (resource.hasProperty(RDFS.label)) {
            category.setLabel(getLiteral(resource, RDFS.label).getString());
        }

        if (resource.hasProperty(RDFS.comment)) {
            category.setDescription(getLiteral(resource, RDFS.comment).getString());
        }

        List<PropertyType> propertyTypes = new ArrayList<>();
        for (RDFNode propertyTypeNode : model.listObjectsOfProperty(resource, HAS_PROPERTY_TYPE).toList()) {
            if (!propertyTypeNode.isResource()) {
                throw new RDFTransformException(
                    "Expected node " + propertyTypeNode + " to be resource");
            }

            propertyTypes.add(transformToPropertyType(propertyTypeNode.asResource(), profile, model));
        }
        category.setPropertyTypes(propertyTypes);
        return category;
    }

    private NodeTransform transformToNodeTransform(Resource resource, DomainProfile profile, Model model)
        throws RDFTransformException {
        NodeTransform nodeTransform = new NodeTransform();
        if (resource.hasProperty(RDFS.label)) {
            nodeTransform.setLabel(getLiteral(resource, RDFS.label).getString());
        }

        if (resource.hasProperty(RDFS.comment)) {
            nodeTransform.setDescription(getLiteral(resource, RDFS.comment).getString());
        }

        if (resource.hasProperty(HAS_SOURCE_TYPE)) {
            nodeTransform.setSourceNodeType(transformToNodeType(resource.getPropertyResourceValue(HAS_SOURCE_TYPE), profile, model));
        }

        if (resource.hasProperty(HAS_SOURCE_PARENT_CONSTRAINT)) {
            nodeTransform.setSourceParentConstraint(transformToNodeConstraint(resource.getPropertyResourceValue(HAS_SOURCE_PARENT_CONSTRAINT), profile, model));
        }

        if (resource.hasProperty(HAS_SOURCE_GRANDPARENT_CONSTRAINT)) {
            nodeTransform.setSourceGrandparentConstraint(transformToNodeConstraint(resource.getPropertyResourceValue(HAS_SOURCE_GRANDPARENT_CONSTRAINT), profile, model));
        }

        if (resource.hasProperty(HAS_SOURCE_CHILD_CONSTRAINT)) {
            nodeTransform.setSourceChildConstraint(transformToNodeConstraint(resource.getPropertyResourceValue(HAS_SOURCE_CHILD_CONSTRAINT), profile, model));
        }

        if (resource.hasProperty(HAS_RESULT_NODE_TYPE)) {
            nodeTransform.setResultNodeType(transformToNodeType(resource.getPropertyResourceValue(HAS_RESULT_NODE_TYPE), profile, model));
        }

        if (resource.hasProperty(HAS_RESULT_PARENT_CONSTRAINT)) {
            nodeTransform.setResultParentConstraint(transformToNodeConstraint(resource.getPropertyResourceValue(HAS_RESULT_PARENT_CONSTRAINT), profile, model));
        }

        if (resource.hasProperty(INSERT_PARENT)) {
            nodeTransform.setInsertParent(getLiteral(resource, INSERT_PARENT).getBoolean());
        }

        if (resource.hasProperty(MOVE_RESULT_GRANDPARENT)) {
            nodeTransform.setMoveResultToGrandParent(getLiteral(resource, MOVE_RESULT_GRANDPARENT).getBoolean());
        }

        if (resource.hasProperty(REMOVE_EMPTY_PARENT)) {
            nodeTransform.setRemoveEmptyParent(getLiteral(resource, REMOVE_EMPTY_PARENT).getBoolean());
        }
        return nodeTransform;
    }

    private NodeConstraint transformToNodeConstraint(Resource resource, DomainProfile profile, Model model)
        throws RDFTransformException {
        NodeConstraint constraint = new NodeConstraint();

        if (resource.hasProperty(MATCHES_ANY)) {
            constraint.setMatchesAny(getLiteral(resource, MATCHES_ANY).getBoolean());
        }

        if (resource.hasProperty(MATCHES_NONE)) {
            constraint.setMatchesNone(getLiteral(resource, MATCHES_NONE).getBoolean());
        }

        if (resource.hasProperty(HAS_NODE_TYPE)) {
            constraint.setNodeType(transformToNodeType(resource.getPropertyResourceValue(HAS_NODE_TYPE), profile, model));
        }

        if (resource.hasProperty(HAS_STRUCTURAL_RELATION)) {
            constraint.setStructuralRelation(transformToStructuralRelation(resource.getPropertyResourceValue(HAS_STRUCTURAL_RELATION)));
        }

        return constraint;
    }

    public PropertyConstraint transformToPropertyConstraint(Resource resource, DomainProfile profile, Model model)
        throws RDFTransformException {
        PropertyConstraint constraint = new PropertyConstraint();

        if (resource.hasProperty(HAS_CARDINALITY_CONSTRAINT)) {
            transformToCardinalityConstraint(constraint, resource.getPropertyResourceValue(HAS_CARDINALITY_CONSTRAINT));
        }

        if (resource.hasProperty(HAS_PROPERTY_TYPE)) {
            constraint.setPropertyType(transformToPropertyType(resource.getPropertyResourceValue(HAS_PROPERTY_TYPE), profile, model));
        }
        return constraint;
    }

    public PropertyType transformToPropertyType(Resource resource, DomainProfile profile, Model model)
        throws RDFTransformException {
        PropertyType propertyType = new PropertyType();

        if (resource.hasProperty(RDFS.label)) {
            propertyType.setLabel(getLiteral(resource, RDFS.label).getString());
        }

        if (resource.hasProperty(RDFS.comment)) {
            propertyType.setDescription(getLiteral(resource, RDFS.comment).getString());
        }

        if (resource.hasProperty(HAS_DOMAIN_PREDICATE)) {
            try {
                propertyType.setDomainPredicate(new URI(getLiteral(resource, HAS_DOMAIN_PREDICATE).getString()));
            } catch (URISyntaxException e) {
                throw new RDFTransformException("Expected domain predicate to be a uri");
            }
        }

        if (resource.hasProperty(IS_READ_ONLY)) {
            propertyType.setReadOnly(getLiteral(resource, IS_READ_ONLY).getBoolean());
        }

        if (resource.hasProperty(HAS_PROPERTY_VALUE_TYPE)) {
            propertyType.setPropertyValueType(PropertyValueType.valueOf(getLiteral(resource, HAS_PROPERTY_VALUE_TYPE).getString()));
        }

        if (resource.hasProperty(HAS_PROPERTY_VALUE_HINT)) {
            propertyType.setPropertyValueHint(PropertyValueHint.valueOf(getLiteral(resource, HAS_PROPERTY_VALUE_HINT).getString()));
        }

        if (resource.hasProperty(HAS_ALLOWED_VALUE)) {
            List<org.dataconservancy.packaging.tool.model.dprofile.Property> allowedValues = new ArrayList<>();
            for (RDFNode allowedValueNode : model.listObjectsOfProperty(resource, HAS_ALLOWED_VALUE).toList()) {
                if (!allowedValueNode.isResource()) {
                    throw new RDFTransformException(
                        "Expected node " + allowedValueNode +
                            " to be resource");
                }
                allowedValues.add(transformToPropertyValue(allowedValueNode.asResource(), profile, model));
            }
            propertyType.setAllowedPropertyValues(allowedValues);
        }

        if (resource.hasProperty(HAS_PROPERTY_CONSTRAINT)) {
            List<PropertyConstraint> constraints = new ArrayList<>();
            
            for (RDFNode constraint : model.listObjectsOfProperty(resource, HAS_PROPERTY_CONSTRAINT).toList()) {
                if (!constraint.isResource()) {
                    throw new RDFTransformException(
                        "Expected node " + constraint + " to be resource");
                }
                constraints.add(transformToPropertyConstraint(constraint.asResource(), profile, model));
            }
            propertyType.setComplexPropertyConstraints(constraints);
        }
        
        if (resource.hasProperty(HAS_DOMAIN_TYPE)) {
            List<URI> domain_types = new ArrayList<>();
            
            for (RDFNode dt : model.listObjectsOfProperty(resource, HAS_DOMAIN_TYPE).toList()) {
                if (!dt.isURIResource()) {
                    throw new RDFTransformException(
                        "Expected node " + dt + " to be a uri resource");
                }
                domain_types.add(URI.create(dt.asResource().getURI()));
            }
            
            propertyType.setComplexDomainTypes(domain_types);
        }

        if (resource.hasProperty(HAS_PROPERTY_CATEGORY)) {
            propertyType.setCategory(transformToPropertyCategory(resource.getPropertyResourceValue(HAS_PROPERTY_CATEGORY), model, profile));
        }
        return propertyType;
    }

    private org.dataconservancy.packaging.tool.model.dprofile.Property transformToPropertyValue(Resource resource, DomainProfile profile, Model model)
        throws RDFTransformException {
        org.dataconservancy.packaging.tool.model.dprofile.Property value = null;

        PropertyType type = null;
        if (resource.hasProperty(HAS_PROPERTY_TYPE)) {
            type = transformToPropertyType(resource.getPropertyResourceValue(HAS_PROPERTY_TYPE), profile, model);
        }

        if (type != null) {
            value = new org.dataconservancy.packaging.tool.model.dprofile.Property(type);

            if (resource.hasProperty(HAS_STRING_VALUE)) {
                value.setStringValue(getLiteral(resource, HAS_STRING_VALUE).getString());
            } else if (resource.hasProperty(HAS_LONG_VALUE)) {
                value.setLongValue(getLiteral(resource, HAS_LONG_VALUE).getLong());
            } else if (resource.hasProperty(HAS_DATE_TIME_VALUE)) {
                value.setDateTimeValue(new DateTime(getLiteral(resource, HAS_DATE_TIME_VALUE).getLong()));
            } else {
                List<org.dataconservancy.packaging.tool.model.dprofile.Property> props = new ArrayList<>();
                for (RDFNode node : model.listObjectsOfProperty(resource, HAS_COMPLEX_VALUE).toList()) {
                    if (!node.isResource()) {
                        throw new RDFTransformException(
                            "Expected node " + node + " to be resource");
                    }
                    props.add(transformToPropertyValue(node.asResource(), profile, model));
                }
                
                value.setComplexValue(props);
            }
        }

        return value;
    }

    private Map<PropertyType, SuppliedProperty> transformToSuppliedProperties(Resource resource, DomainProfile profile, Model model)
        throws RDFTransformException {
        Map<PropertyType, SuppliedProperty> suppliedPropertyMap = new HashMap<>();

        for (RDFNode suppliedPropertyNode : model.listObjectsOfProperty(resource, HAS_SUPPLIED_PROPERTY).toList()) {
            if (!suppliedPropertyNode.isResource()) {
                throw new RDFTransformException(
                    "Expected node " + suppliedPropertyNode + " to be resource");
            }

            Resource suppliedPropertyResource = suppliedPropertyNode.asResource();
            if (suppliedPropertyResource.hasProperty(HAS_PROPERTY_TYPE)) {
                PropertyType suppliedType = transformToPropertyType(suppliedPropertyResource.getPropertyResourceValue(HAS_PROPERTY_TYPE), profile, model);
                if (suppliedType != null) {
                    if (suppliedPropertyResource.hasProperty(HAS_SUPPLIED_PROPERTY_VALUE)) {
                        suppliedPropertyMap.put(suppliedType, SuppliedProperty.valueOf(getLiteral(suppliedPropertyResource, HAS_SUPPLIED_PROPERTY_VALUE).getString()));
                    }
                }
            }
        }
        return suppliedPropertyMap;
    }

    private StructuralRelation transformToStructuralRelation(Resource resource)
        throws RDFTransformException {
        StructuralRelation relation = null;

        URI parentPredicate = null;
        if (resource.hasProperty(HAS_PARENT_PREDICATE)) {
            try {
                parentPredicate = new URI(getLiteral(resource, HAS_PARENT_PREDICATE).getString());
            } catch (URISyntaxException e) {
                throw new RDFTransformException("Expected parent predicate to be a URI");
            }
        }

        URI childPredicate = null;
        if (resource.hasProperty(HAS_CHILD_PREDICATE)) {
            try {
                childPredicate = new URI(getLiteral(resource, HAS_CHILD_PREDICATE).getString());
            } catch (URISyntaxException e) {
                throw new RDFTransformException("Expected child predicate to be a URI");
            }
        }

        if (parentPredicate != null && childPredicate != null) {
            relation = new StructuralRelation(parentPredicate, childPredicate);
        }

        return relation;
    }

    private void transformToCardinalityConstraint(CardinalityConstraint constraint, Resource resource)
        throws RDFTransformException {

        if (resource.hasProperty(HAS_MIN)) {
           constraint.setMin(getLiteral(resource, HAS_MIN).getInt());
        }

        if (resource.hasProperty(HAS_MAX)) {
            constraint.setMax(getLiteral(resource, HAS_MAX).getInt());
        }
    }

    private Literal getLiteral(Resource res, Property p) throws RDFTransformException {
        if (!res.hasProperty(p)) {
            throw new RDFTransformException("Expected node " + res + " to have property " + p);
        }

        RDFNode value = res.getProperty(p).getObject();

        if (!value.isLiteral()) {
            throw new RDFTransformException("Expected node " + res + " property " + p
                    + " to be a literal");
        }

        return value.asLiteral();
    }
}