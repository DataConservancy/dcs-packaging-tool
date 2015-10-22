package org.dataconservancy.packaging.tool.impl;

import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.dataconservancy.packaging.tool.api.DomainProfileObjectStore;
import org.dataconservancy.packaging.tool.model.dprofile.NodeConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.StructuralRelation;
import org.dataconservancy.packaging.tool.model.dprofile.SuppliedProperty;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Implement store for domain objects on top of Jena model.
 */
public class DomainProfileObjectStoreImpl implements DomainProfileObjectStore {
    private final Model model;

    public DomainProfileObjectStoreImpl(Model model) {
        this.model = model;
    }

    @Override
    public void updateObject(Node node) {
        if (node.getDomainObject() == null) {
            node.setDomainObject(generate_unique_uri());
        } else {
            clear_types(node.getDomainObject());

            Node parent = node.getParent();

            if (parent != null) {
                clear_relations(node.getDomainObject(), parent.getDomainObject());
            }
        }

        create_properties(node);
        create_parent_relations(node);

        if (node.getSubNodeTypes() != null) {
            node.getSubNodeTypes().forEach(type -> create_properties(node));
        }
    }

    private URI generate_unique_uri() {
        return URI.create("urn:uuid:" + UUID.randomUUID().toString());
    }

    private void clear_types(URI subject) {
        as_resource(subject).removeAll(RDF.type);
    }

    private void clear_relations(URI subject, URI object) {
        model.removeAll(as_resource(subject), null, as_resource(object));
        model.removeAll(as_resource(object), null, as_resource(subject));
    }

    private void create_properties(Node node) {
        Resource object = as_resource(node.getDomainObject());
        NodeType type = node.getNodeType();

        if (type.getDomainTypes() != null) {
            type.getDomainTypes().forEach(dt -> object.addProperty(RDF.type, as_resource(dt)));
        }

        if (type.getDefaultPropertyValues() != null) {
            type.getDefaultPropertyValues().forEach(v -> add_property(object, v));
        }

        if (type.getSuppliedProperties() != null) {
            type.getSuppliedProperties().forEach(
                    (pt, sp) -> as_property_values(pt, sp, node.getFileInfo()).forEach(v -> add_property(object, v)));
        }
    }

    private void create_parent_relations(Node node) {
        Node parent_node = node.getParent();

        if (parent_node == null) {
            return;
        }

        if (node.getNodeType().getParentConstraints() == null || node.getNodeType().getParentConstraints().isEmpty()) {
            // No relationship to parent.
            return;
        }

        NodeConstraint nc = find_parent_constraint(node.getNodeType(), parent_node.getNodeType());

        if (nc == null) {
            throw new IllegalStateException("Cannot find parent constraint for " + node.getIdentifier());
        }

        Resource object = as_resource(node.getDomainObject());
        Resource parent = as_resource(parent_node.getDomainObject());

        StructuralRelation rel = nc.getStructuralRelation();

        if (rel != null) {
            if (rel.getHasParentPredicate() != null) {
                object.addProperty(as_property(rel.getHasParentPredicate()), parent);
            }

            if (rel.getHasChildPredicate() != null) {
                parent.addProperty(as_property(rel.getHasChildPredicate()), object);
            }
        }
    }

    private NodeConstraint find_parent_constraint(NodeType type, NodeType parent_type) {
        for (NodeConstraint nc : type.getParentConstraints()) {
            if (nc.getNodeType().getIdentifier().equals(parent_type.getIdentifier())) {
                return nc;
            }
        }

        return null;
    }

    private List<Property> as_property_values(PropertyType type, SuppliedProperty sup, FileInfo info) {
        List<Property> result = new ArrayList<>();
        Property value;

        switch (sup) {
        case FILE_CREATED_DATE:
            value = new Property(type);
            value.setDateTimeValue(new DateTime(info.getCreationTime()));
            result.add(value);
            break;
        case FILE_FORMAT_URI:
            for (String fmt : info.getFormats()) {
                value = new Property(type);
                value.setStringValue(fmt);
                result.add(value);
            }
            break;
        case FILE_MODIFIED_DATE:
            value = new Property(type);
            value.setDateTimeValue(new DateTime(info.getLastModifiedTime()));
            result.add(value);
            break;
        case FILE_NAME:
            value = new Property(type);
            value.setStringValue(info.getName());
            result.add(value);
            break;
        case FILE_SIZE:
            value = new Property(type);
            value.setLongValue(info.getSize());
            result.add(value);
            break;
        default:
            throw new IllegalStateException("Unknown supplied property.");
        }

        return result;
    }

    @Override
    public void addProperty(URI object, Property value) {
        add_property(as_resource(object), value);
    }

    private void add_property(Resource obj, Property value) {
        obj.addProperty(as_property(value.getPropertyType().getDomainPredicate()), as_rdf_node(value));
    }

    @Override
    public void removeProperty(URI object, Property prop) {
        RDFNode node = find_property(as_resource(object), prop);
        
        if (node != null) {
            model.remove(as_statement(object, prop.getPropertyType().getDomainPredicate(), node));
        }
    }

    private RDFNode find_property(Resource res, Property prop) {
        PropertyType type = prop.getPropertyType();

        NodeIterator iter = model.listObjectsOfProperty(res, as_property(type.getDomainPredicate()));

        while (iter.hasNext()) {
            RDFNode node = iter.next();
            
            Property p = as_property_value(node, type);

            if (prop.equals(p)) {
                return node;
            }
        }

        return null;
    }

    @Override
    public void removeProperty(URI object, PropertyType type) {
        Resource obj = model.createResource(object.toString());

        obj.removeAll(as_property(type.getDomainPredicate()));
    }

    @Override
    public List<Property> getProperties(URI object, NodeType type) {
        List<Property> result = new ArrayList<Property>();

        Resource obj = model.createResource(object.toString());

        // Return all properties corresponding to a property constraint

        for (PropertyConstraint pc : type.getPropertyConstraints()) {
            get_properties(obj, pc.getPropertyType(), result);
        }

        return result;
    }

    @Override
    public List<Property> getProperties(URI object, PropertyType type) {
        List<Property> result = new ArrayList<Property>();

        Resource obj = model.createResource(object.toString());

        get_properties(obj, type, result);

        return result;
    }

    private void get_properties(Resource obj, PropertyType type, List<Property> result) {
        NodeIterator iter = model.listObjectsOfProperty(obj, as_property(type.getDomainPredicate()));

        while (iter.hasNext()) {
            result.add(as_property_value(iter.next(), type));
        }
    }

    private Resource as_resource(URI uri) {
        return model.createResource(uri.toString());
    }

    private org.apache.jena.rdf.model.Property as_property(URI uri) {
        return model.createProperty(uri.toString());
    }

    private Statement as_statement(URI subject, URI predicate, URI object) {
        return model.createStatement(as_resource(subject), as_property(predicate), as_resource(object));
    }

    private Statement as_statement(URI subject, URI predicate, RDFNode node) {
        return model.createStatement(as_resource(subject), as_property(predicate), node);
    }

    private DateTime as_date_time(XSDDateTime dt) {
        return ISODateTimeFormat.dateTimeParser().parseDateTime(dt.toString());
    }

    private RDFNode as_rdf_node(Property value) {
        if (!value.hasValue()) {
            throw new IllegalArgumentException("No value set on property.");
        }

        PropertyType type = value.getPropertyType();

        switch (type.getPropertyValueType()) {
        case COMPLEX:
            Resource res = model.createResource();

            for (Property subval : value.getComplexValue()) {
                res.addProperty(as_property(subval.getPropertyType().getDomainPredicate()), as_rdf_node(subval));
            }

            return res;
        case DATE_TIME:
            return model.createTypedLiteral(value.getDateTimeValue().toGregorianCalendar());
        case LONG:
            return model.createTypedLiteral(value.getLongValue());
        case STRING:
            return model.createTypedLiteral(value.getStringValue());
        default:
            throw new RuntimeException("Unhandled value type.");
        }
    }

    // Attempt to convert a given rdf node to a property value of the given
    // type. Return null on failure.
    private Property as_property_value(RDFNode rdfnode, PropertyType type) {
        Property value = new Property(type);

        switch (type.getPropertyValueType()) {
        case COMPLEX:
            if (rdfnode.isResource()) {
                List<Property> subvalues = new ArrayList<>();

                for (PropertyConstraint subtypecon : type.getPropertySubTypes()) {
                    PropertyType subtype = subtypecon.getPropertyType();
                    NodeIterator iter = model.listObjectsOfProperty(rdfnode.asResource(), as_property(subtype.getDomainPredicate()));

                    while (iter.hasNext()) {
                        Property subval = as_property_value(iter.next(), subtype);

                        if (subval != null) {
                            subvalues.add(subval);
                        }
                    }
                }

                value.setComplexValue(subvalues);

                return value;
            } else {
                return null;
            }
        case DATE_TIME:
            if (rdfnode.isLiteral() && rdfnode.asLiteral().getValue() instanceof XSDDateTime) {
                XSDDateTime dt = XSDDateTime.class.cast(rdfnode.asLiteral().getValue());

                if (dt != null) {
                    value.setDateTimeValue(as_date_time(dt));
                }

                return value;
            } else {
                return null;
            }
        case LONG:
            if (rdfnode.isLiteral()) {
                value.setLongValue(rdfnode.asLiteral().getLong());

                return value;
            } else {
                return null;
            }
        case STRING:
            if (rdfnode.isLiteral()) {
                value.setStringValue(rdfnode.asLiteral().getString());

                return value;
            } else {
                return null;
            }
        default:
            throw new RuntimeException("Unhandled value type");
        }
    }

    @Override
    public boolean hasRelationship(URI subject, URI predicate, URI object) {
        return model.contains(as_statement(subject, predicate, object));
    }

    public String toString() {
        StringWriter result = new StringWriter();
        
        StmtIterator iter = model.listStatements();
        
        while (iter.hasNext()) {
            result.append(iter.next() + "\n");
        }

        result.append("\n\n");
        
        RDFDataMgr.write(result, model, RDFFormat.TURTLE_PRETTY);
        
        return result.toString();
    }
}
