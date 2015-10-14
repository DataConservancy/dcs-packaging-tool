package org.dataconservancy.packaging.tool.impl;

import java.net.URI;
import java.util.Arrays;

import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValue;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.dprofile.Requirement;

/**
 * Domain profile for testing.
 * 
 */
public class FarmDomainProfile extends DomainProfile {
    private NodeType cow_node_type;
    private NodeType farm_node_type;
    private NodeType barn_node_type;

    public FarmDomainProfile() {
        farm_node_type = new NodeType();
        barn_node_type = new NodeType();
        cow_node_type = new NodeType();
        
        PropertyType weight = new PropertyType();
        weight.setPropertyValueType(PropertyValueType.LONG);
        weight.setPropertyValueHint(PropertyValueHint.NUMBER);
        weight.setDomainPredicate(URI.create("farm:weight"));
        
        PropertyType breed = new PropertyType();
        breed.setPropertyValueType(PropertyValueType.STRING);
        breed.setDomainPredicate(URI.create("farm:breed"));
        
        PropertyType title = new PropertyType();
        title.setPropertyValueType(PropertyValueType.STRING);
        title.setDomainPredicate(URI.create("dc:title"));
        
        PropertyType species = new PropertyType();
        species.setPropertyValueType(PropertyValueType.STRING);
        species.setDomainPredicate(URI.create("farm:species"));
        
        PropertyConstraint weight_constraint = new PropertyConstraint();
        weight_constraint.setPropertyType(weight);
        weight_constraint.setMin(1);
        weight_constraint.setMax(1);
        
        PropertyConstraint breed_constraint = new PropertyConstraint();
        breed_constraint.setPropertyType(breed);
        breed_constraint.setMin(0);
        breed_constraint.setMax(1);
        
        PropertyConstraint species_constraint = new PropertyConstraint();
        species_constraint.setPropertyType(species);
        species_constraint.setMin(1);
        species_constraint.setMax(1);
        
        PropertyConstraint title_constraint = new PropertyConstraint();
        title_constraint.setPropertyType(title);
        title_constraint.setMin(1);
        title_constraint.setMax(-1);
        
        PropertyValue cow_species = new PropertyValue(species);
        cow_species.setStringValue("Bos taurus");
        
        farm_node_type.setIdentifier(URI.create("fdp:farm"));
        farm_node_type.setLabel("farm");
        farm_node_type.setDescription("The domain of a benevolent dictator.");
        farm_node_type.setDomainTypes(Arrays.asList(URI.create("farm:Farm"), URI.create("org:Organization")));
        farm_node_type.setPropertyConstraints(Arrays.asList());
        farm_node_type.setDefaultPropertyValues(Arrays.asList());
        farm_node_type.setParentConstraints(Arrays.asList());
        farm_node_type.setDomainProfile(this);

        barn_node_type.setIdentifier(URI.create("fdp:barn"));
        barn_node_type.setLabel("Barn");
        barn_node_type.setDescription("A place of rest and relaxation.");
        barn_node_type.setDomainTypes(Arrays.asList(URI.create("farm:Barn")));
        farm_node_type.setPropertyConstraints(Arrays.asList());
        farm_node_type.setDefaultPropertyValues(Arrays.asList());
        barn_node_type.setParentConstraints(Arrays.asList());
        barn_node_type.setDomainProfile(this);

        cow_node_type.setIdentifier(URI.create("fdp:cow"));
        cow_node_type.setLabel("Cow");
        cow_node_type.setDescription("A cow is a tasty and noble creature.");
        cow_node_type.setDomainTypes(Arrays.asList(URI.create("farm:Cow")));
        cow_node_type.setPropertyConstraints(Arrays.asList(species_constraint, weight_constraint, breed_constraint, title_constraint));
        cow_node_type.setDefaultPropertyValues(Arrays.asList(cow_species));
        cow_node_type.setParentConstraints(Arrays.asList());
        cow_node_type.setFileAssocationRequirement(Requirement.MUST);
        cow_node_type.setDomainProfile(this);

        setIdentifier(URI.create("http://example.com/farm"));
        setLabel("Farm");
        setDescription("Vocabulary for describing a farm");
        setNodeTypes(Arrays.asList(farm_node_type, cow_node_type, barn_node_type));
        setPropertyTypes(Arrays.asList(species, weight, title, breed));
        setPropertyCategories(Arrays.asList());
        setNodeTransforms(Arrays.asList());
    }

    public NodeType getFarmNodeType() {
        return farm_node_type;
    }

    public NodeType getBarnNodeType() {
        return barn_node_type;
    }

    public NodeType getCowNodeType() {
        return cow_node_type;
    }

}
