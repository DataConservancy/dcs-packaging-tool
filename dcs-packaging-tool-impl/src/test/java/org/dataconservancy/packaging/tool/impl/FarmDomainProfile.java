package org.dataconservancy.packaging.tool.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.FileAssociation;
import org.dataconservancy.packaging.tool.model.dprofile.NodeConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyCategory;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.dprofile.StructuralRelation;
import org.dataconservancy.packaging.tool.model.dprofile.SuppliedProperty;

/**
 * Domain profile for testing.
 * 
 * Farm has_part Barns, Troughs, and Farms.
 * Barn has_occupant Cows and has_part Stockpiles.
 * Troughs and Stockpiles has_part Feed.
 *
 * Media can describe Farms, Barns, and Cows with has_data.
 * 
 * TODO Switch to more real looking uris.
 */
public class FarmDomainProfile extends DomainProfile {
    private NodeType cow_node_type;
    private NodeType farm_node_type;
    private NodeType barn_node_type;
    private NodeType trough_node_type;
    private NodeType feed_node_type;
    private NodeType stockpile_node_type;
    private NodeType media_node_type;
    private PropertyType title_property_type;
    private PropertyType size_property_type;
    private PropertyType created_property_type;
    private PropertyType farmer_property_type;
    private PropertyType name_property_type;
    private PropertyType mbox_property_type;
    private PropertyType weight_property_type;
    private PropertyType species_property_type;
    private StructuralRelation has_part_rel;
    private StructuralRelation has_occupant_rel;
    private StructuralRelation has_data_rel;
    private NodeTransform cowToStockpileTransform;
    private NodeTransform barnNoChildToFarmTransform;
    private NodeTransform barnMediaChildToFarmTransform;
    private NodeTransform troughToCowTransform;
    private NodeTransform moveMediaFromCowToBarnTransform;
    
    public FarmDomainProfile() {
        farm_node_type = new NodeType();
        barn_node_type = new NodeType();
        cow_node_type = new NodeType();
        media_node_type = new NodeType();
        trough_node_type = new NodeType();
        feed_node_type = new NodeType();
        stockpile_node_type = new NodeType();

        weight_property_type = new PropertyType();
        weight_property_type.setPropertyValueType(PropertyValueType.LONG);
        weight_property_type.setPropertyValueHint(PropertyValueHint.NUMBER);
        weight_property_type.setDomainPredicate(URI.create("farm:weight"));

        PropertyType breed = new PropertyType();
        breed.setPropertyValueType(PropertyValueType.STRING);
        breed.setDomainPredicate(URI.create("farm:breed"));

        title_property_type = new PropertyType();
        title_property_type.setPropertyValueType(PropertyValueType.STRING);
        title_property_type.setDomainPredicate(URI.create("dc:title"));

        size_property_type = new PropertyType();
        size_property_type.setPropertyValueType(PropertyValueType.LONG);
        size_property_type.setDomainPredicate(URI.create("premis:fileSize"));
        
        created_property_type = new PropertyType();
        created_property_type.setPropertyValueType(PropertyValueType.DATE_TIME);
        created_property_type.setDomainPredicate(URI.create("dcterms:created"));
        
        name_property_type = new PropertyType();
        name_property_type.setPropertyValueType(PropertyValueType.STRING);
        name_property_type.setDomainPredicate(URI.create("foaf:name"));
        
        mbox_property_type = new PropertyType();
        mbox_property_type.setPropertyValueType(PropertyValueType.STRING);
        mbox_property_type.setDomainPredicate(URI.create("foaf:mbox"));
        
        PropertyConstraint mbox_constraint = new PropertyConstraint();
        mbox_constraint.setPropertyType(mbox_property_type);
        mbox_constraint.setMin(1);
        mbox_constraint.setMax(1);
        
        PropertyConstraint name_constraint = new PropertyConstraint();
        name_constraint.setPropertyType(name_property_type);
        name_constraint.setMin(1);
        name_constraint.setMax(1);
        
        farmer_property_type = new PropertyType();
        farmer_property_type.setDomainPredicate(URI.create("farm:hasFarmer"));
        farmer_property_type.setPropertyValueType(PropertyValueType.COMPLEX);
        farmer_property_type.setComplexDomainTypes(Arrays.asList(URI.create("foaf:person")));
        farmer_property_type.setComplexPropertyConstraints(Arrays.asList(name_constraint, mbox_constraint));        

        species_property_type = new PropertyType();
        species_property_type.setPropertyValueType(PropertyValueType.STRING);
        species_property_type.setDomainPredicate(URI.create("farm:species"));

        PropertyConstraint weight_constraint = new PropertyConstraint();
        weight_constraint.setPropertyType(weight_property_type);
        weight_constraint.setMin(1);
        weight_constraint.setMax(1);

        PropertyConstraint breed_constraint = new PropertyConstraint();
        breed_constraint.setPropertyType(breed);
        breed_constraint.setMin(0);
        breed_constraint.setMax(-1);
        
        PropertyConstraint person_constraint = new PropertyConstraint();
        person_constraint.setPropertyType(farmer_property_type);
        person_constraint.setMin(1);
        person_constraint.setMax(-1);

        PropertyConstraint species_constraint = new PropertyConstraint();
        species_constraint.setPropertyType(species_property_type);
        species_constraint.setMin(1);
        species_constraint.setMax(1);
        
        PropertyConstraint size_constraint = new PropertyConstraint();
        size_constraint.setPropertyType(size_property_type);
        size_constraint.setMin(1);
        size_constraint.setMax(1);

        PropertyConstraint title_constraint = new PropertyConstraint();
        title_constraint.setPropertyType(title_property_type);
        title_constraint.setMin(1);
        title_constraint.setMax(2);

        Property cow_species = new Property(species_property_type);
        cow_species.setStringValue("Bos taurus");

        has_part_rel = new StructuralRelation(URI.create("dcterms:isPartOf"), URI.create("dcterms:hasPart"));
        has_occupant_rel= new StructuralRelation(URI.create("farm:isOccupantOf"), URI.create("farm:hasOccupant"));
        has_data_rel= new StructuralRelation(URI.create("farm:isDataFor"), URI.create("farm:hasData"));
        
        NodeConstraint farm_parent_constraint = new NodeConstraint();
        farm_parent_constraint.setNodeType(farm_node_type);
        farm_parent_constraint.setStructuralRelation(has_part_rel);
        
        NodeConstraint no_parent_constraint = new NodeConstraint();
        no_parent_constraint.setMatchesNone(true);
        
        farm_node_type.setIdentifier(URI.create("fdp:farm"));
        farm_node_type.setLabel("Farm");
        farm_node_type.setDescription("The domain of a benevolent dictator.");
        farm_node_type.setDomainTypes(Arrays.asList(URI.create("farm:Farm"), URI.create("org:Organization")));
        farm_node_type.setPropertyConstraints(Arrays.asList(title_constraint, person_constraint));
        farm_node_type.setDefaultPropertyValues(Arrays.asList());
        farm_node_type.setParentConstraints(Arrays.asList(no_parent_constraint, farm_parent_constraint));
        farm_node_type.setFileAssociation(FileAssociation.DIRECTORY);
        farm_node_type.setDomainProfile(this);

        barn_node_type.setIdentifier(URI.create("fdp:barn"));
        barn_node_type.setLabel("Barn");
        barn_node_type.setDescription("A place of rest and relaxation.");
        barn_node_type.setDomainTypes(Arrays.asList(URI.create("farm:Barn")));
        barn_node_type.setPropertyConstraints(Arrays.asList());
        barn_node_type.setDefaultPropertyValues(Arrays.asList());
        barn_node_type.setParentConstraints(Arrays.asList(farm_parent_constraint));
        barn_node_type.setFileAssociation(FileAssociation.DIRECTORY);
        barn_node_type.setDomainProfile(this);

        trough_node_type.setIdentifier(URI.create("fdp:trough"));
        trough_node_type.setLabel("Trough");
        trough_node_type.setDescription("A place of gluttony and enjoyment");
        trough_node_type.setDomainTypes(Collections.singletonList(URI.create("farm:trough")));
        trough_node_type.setPropertyConstraints(Collections.emptyList());
        trough_node_type.setDefaultPropertyValues(Collections.emptyList());
        trough_node_type.setParentConstraints(Collections.singletonList(farm_parent_constraint));
        trough_node_type.setDomainProfile(this);

        NodeConstraint stockpile_parent_constraint = new NodeConstraint();
        stockpile_parent_constraint.setNodeType(stockpile_node_type);
        stockpile_parent_constraint.setStructuralRelation(has_part_rel);
        
        NodeConstraint trough_parent_constraint = new NodeConstraint();
        trough_parent_constraint.setNodeType(trough_node_type);
        trough_parent_constraint.setStructuralRelation(has_part_rel);

        feed_node_type.setIdentifier(URI.create("fdp:feed"));
        feed_node_type.setLabel("Feed");
        feed_node_type.setDescription("The sustenance of life");
        feed_node_type.setDomainTypes(Collections.singletonList(URI.create("farm:feed")));
        feed_node_type.setPropertyConstraints(Collections.singletonList(weight_constraint));
        feed_node_type.setDefaultPropertyValues(Collections.emptyList());
        feed_node_type.setParentConstraints(Arrays.asList(trough_parent_constraint, stockpile_parent_constraint));
        feed_node_type.setDomainProfile(this);

        NodeConstraint barn_parent_constraint = new NodeConstraint();
        barn_parent_constraint.setNodeType(barn_node_type);
        barn_parent_constraint.setStructuralRelation(has_part_rel);

        stockpile_node_type.setIdentifier(URI.create("fdp:stockpile"));
        stockpile_node_type.setLabel("Stockpile");
        stockpile_node_type.setDescription("Excess goodness");
        stockpile_node_type.setDomainTypes(Collections.singletonList(URI.create("farm:stockpile")));
        stockpile_node_type.setPropertyConstraints(Collections.singletonList(weight_constraint));
        stockpile_node_type.setDefaultPropertyValues(Collections.emptyList());
        stockpile_node_type.setParentConstraints(Collections.singletonList(barn_parent_constraint));
        stockpile_node_type.setDomainProfile(this);

        NodeConstraint barn_occ_parent_constraint = new NodeConstraint();
        barn_occ_parent_constraint.setNodeType(barn_node_type);
        barn_occ_parent_constraint.setStructuralRelation(has_occupant_rel);

        cow_node_type.setIdentifier(URI.create("fdp:cow"));
        cow_node_type.setLabel("Cow");
        cow_node_type.setDescription("A cow is a tasty and noble creature.");
        cow_node_type.setDomainTypes(Arrays.asList(URI.create("farm:Cow")));
        cow_node_type.setPropertyConstraints(
                Arrays.asList(species_constraint, weight_constraint, breed_constraint, title_constraint));
        cow_node_type.setDefaultPropertyValues(Arrays.asList(cow_species));
        cow_node_type.setParentConstraints(Arrays.asList(barn_occ_parent_constraint));
        cow_node_type.setDomainProfile(this);

        Map<PropertyType, SuppliedProperty> supplied_media_properties = new HashMap<>();
        supplied_media_properties.put(size_property_type, SuppliedProperty.FILE_SIZE);

        NodeConstraint cow_data_parent_constraint = new NodeConstraint();
        cow_data_parent_constraint.setNodeType(cow_node_type);
        cow_data_parent_constraint.setStructuralRelation(has_data_rel);
        
        NodeConstraint farm_data_parent_constraint = new NodeConstraint();
        farm_data_parent_constraint.setNodeType(farm_node_type);
        farm_data_parent_constraint.setStructuralRelation(has_data_rel);
        
        NodeConstraint barn_data_parent_constraint = new NodeConstraint();
        barn_data_parent_constraint.setNodeType(barn_node_type);
        barn_data_parent_constraint.setStructuralRelation(has_data_rel);

        media_node_type.setIdentifier(URI.create("fdp:media"));
        media_node_type.setLabel("Media");
        media_node_type.setDescription("Commemorative media of best tasting animals.");
        media_node_type.setDomainTypes(Arrays.asList(URI.create("farm:Media")));
        media_node_type.setPropertyConstraints(Arrays.asList(title_constraint, size_constraint));
        media_node_type.setDefaultPropertyValues(Arrays.asList());
        media_node_type.setParentConstraints(Arrays.asList(cow_data_parent_constraint, farm_data_parent_constraint, barn_data_parent_constraint));
        media_node_type.setSuppliedProperties(supplied_media_properties);
        media_node_type.setFileAssociation(FileAssociation.REGULAR_FILE);
        media_node_type.setDomainProfile(this);

        PropertyCategory saleCategory = new PropertyCategory();
        saleCategory.setPropertyTypes(Arrays.asList(weight_property_type, breed));
        saleCategory.setLabel("Sale Properties");
        saleCategory.setDescription("Properties relevant to the sale of animals.");
        setPropertyCategories(Collections.singletonList(saleCategory));

        NodeConstraint no_child_constraint = new NodeConstraint();
        no_child_constraint.setMatchesNone(true);
        
        NodeConstraint media_constraint = new NodeConstraint();
        media_constraint.setNodeType(media_node_type);
        media_constraint.setStructuralRelation(has_data_rel);
        
        // Transform cow to stockpile
        cowToStockpileTransform = new NodeTransform();
        cowToStockpileTransform.setLabel("Cow -> Stockpile");
        cowToStockpileTransform.setDescription("Tranform a cow into a stockpile.");
        cowToStockpileTransform.setSourceNodeType(cow_node_type);
        // Cannot transform cow with a Media child
        cowToStockpileTransform.setSourceChildConstraint(no_child_constraint);
        cowToStockpileTransform.setResultNodeType(stockpile_node_type);
        //cowToStockpileTransform.setres
        

        // Transform barn to farm
        
        // No child
        barnNoChildToFarmTransform = new NodeTransform();
        barnNoChildToFarmTransform.setLabel("Barn -> Farm (no media)");
        barnNoChildToFarmTransform.setDescription("Transform an empty barn into a farm.");
        barnNoChildToFarmTransform.setSourceNodeType(barn_node_type);
        barnNoChildToFarmTransform.setSourceChildConstraint(no_child_constraint);
        barnNoChildToFarmTransform.setResultNodeType(farm_node_type);

        // Media child
        barnMediaChildToFarmTransform = new NodeTransform();
        barnMediaChildToFarmTransform.setLabel("Barn -> Farm (with media)");
        barnMediaChildToFarmTransform.setDescription("Transform a barn with media into a farm.");
        barnMediaChildToFarmTransform.setSourceNodeType(barn_node_type);
        barnMediaChildToFarmTransform.setSourceChildConstraint(media_constraint);
        barnMediaChildToFarmTransform.setResultNodeType(farm_node_type);
        
        // Transform trough with now child to cow in barn.
        
        troughToCowTransform = new NodeTransform();
        troughToCowTransform.setLabel("Trough -> Cow (in barn)");
        troughToCowTransform.setDescription("Transform a trough to a cow in a barn.");
        troughToCowTransform.setSourceNodeType(trough_node_type);
        troughToCowTransform.setSourceChildConstraint(no_child_constraint);
        troughToCowTransform.setResultNodeType(cow_node_type);
        troughToCowTransform.setResultParentNodeType(barn_node_type);
        troughToCowTransform.setInsertParent(true);

        moveMediaFromCowToBarnTransform = new NodeTransform();
        moveMediaFromCowToBarnTransform.setLabel("Media moves from Cow to Barn.");
        moveMediaFromCowToBarnTransform.setDescription("Move media file from Cow to Barn grandparent and remove the original cow parent if it has no media.");
        moveMediaFromCowToBarnTransform.setSourceParentConstraint(cow_data_parent_constraint);
        moveMediaFromCowToBarnTransform.setResultParentNodeType(farm_node_type);
        moveMediaFromCowToBarnTransform.setMoveResultToGrandParent(true);
        moveMediaFromCowToBarnTransform.setRemoveEmptyParent(true);
        
        setIdentifier(URI.create("http://example.com/farm"));
        setLabel("Farm");
        setDescription("Vocabulary for describing a farm");
        setNodeTypes(Arrays.asList(trough_node_type, media_node_type, cow_node_type, barn_node_type, feed_node_type, stockpile_node_type, farm_node_type));
        setPropertyTypes(Arrays.asList(species_property_type, weight_property_type, title_property_type, breed));
        setNodeTransforms(Arrays.asList(cowToStockpileTransform, barnNoChildToFarmTransform, barnMediaChildToFarmTransform));
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

    public NodeType getMediaNodeType() {
        return media_node_type;
    }

    public NodeType getFeedNodeType() { return feed_node_type; }

    public NodeType getTroughNodeType() { return trough_node_type; }

    public NodeType getStockpileNodeType() { return stockpile_node_type; }
    
    public PropertyType getTitlePropertyType() {
        return title_property_type;
    }
    
    public PropertyType getWeightPropertyType() {
        return weight_property_type;
    }
    
    public PropertyType getSizePropertyType() {
        return size_property_type;
    }

    public PropertyType getCreatedPropertyType() {
        return created_property_type;
    }
    
    public PropertyType getFarmerPropertyType() {
        return farmer_property_type;
    }
    
    public PropertyType getNamePropertyType() {
        return name_property_type;
    }
    
    public PropertyType getMboxPropertyType() {
        return mbox_property_type;
    }

    public PropertyType getSpeciesPropertyType() {
        return species_property_type;
    }
    
    public StructuralRelation getPartRelation() {
        return has_part_rel;
    }
    
    public StructuralRelation getDataRelation() {
        return has_data_rel;
    }
    
    public StructuralRelation getOccupantRelation() {
        return has_occupant_rel;
    }
    
    public NodeTransform getCowToStockpileTransform() {
        return cowToStockpileTransform;
    }
    
    public NodeTransform getBarnNoChildToFarmTransform() {
        return barnNoChildToFarmTransform;
    }
    
    public NodeTransform getBarnMediaChildToFarmTransform() {
        return barnMediaChildToFarmTransform;
    }
    
    public NodeTransform getTroughToCowTransform() {
        return troughToCowTransform;
    }
    
    public NodeTransform getMoveMediaFromCowToBarnTransform() {
        return moveMediaFromCowToBarnTransform;
    }
}
