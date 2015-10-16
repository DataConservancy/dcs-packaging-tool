package org.dataconservancy.packaging.tool.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.dataconservancy.packaging.tool.api.DomainProfileObjectStore;
import org.dataconservancy.packaging.tool.model.dprofile.NodeConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValue;
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

        // TODO What semantics here...
        if (node.getNodeType().getParentConstraints() == null || node.getNodeType().getParentConstraints().isEmpty()) {
            return;
        }

        NodeConstraint nc = find_parent_constraint(node.getNodeType(), parent_node.getNodeType());

        if (nc == null) {
            throw new IllegalStateException("Cannot find parent constraint.");
        }

        // Use first relation if it exists

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

    private List<PropertyValue> as_property_values(PropertyType type, SuppliedProperty sup, FileInfo info) {
        List<PropertyValue> result = new ArrayList<>();
        PropertyValue value;

        switch (sup) {
        case FILE_CREATED_DATE:
            value = new PropertyValue(type);
            value.setDateTimeValue(new DateTime(info.getCreationTime()));
            result.add(value);
            break;
        case FILE_FORMAT_URI:
            for (String fmt : info.getFormats()) {
                value = new PropertyValue(type);
                value.setStringValue(fmt);
                result.add(value);
            }
            break;
        case FILE_MODIFIED_DATE:
            value = new PropertyValue(type);
            value.setDateTimeValue(new DateTime(info.getLastModifiedTime()));
            result.add(value);
            break;
        case FILE_NAME:
            value = new PropertyValue(type);
            value.setStringValue(info.getName());
            result.add(value);
            break;
        case FILE_SIZE:
            value = new PropertyValue(type);
            value.setLongValue(info.getSize());
            result.add(value);
            break;
        default:
            throw new IllegalStateException("Unknown supplied property.");
        }

        return result;
    }

    @Override
    public void addProperty(URI object, PropertyValue value) {
        add_property(as_resource(object), value);
    }

    private void add_property(Resource obj, PropertyValue value) {
        obj.addProperty(as_property(value.getPropertyType().getDomainPredicate()), as_rdf_node(value));
    }

    @Override
    public void removeProperty(URI object, PropertyValue value) {
        RDFNode rdf_value = as_rdf_node(value);
        
        if (rdf_value.isAnon()) {
            throw new IllegalArgumentException("Complex properties can only be removed by type.");
        } else {
            model.remove(as_statement(object, value.getPropertyType().getDomainPredicate(), rdf_value));
        }
    }

    @Override
    public void removeProperty(URI object, PropertyType type) {
        Resource obj = model.createResource(object.toString());

        obj.removeAll(as_property(type.getDomainPredicate()));
    }

    @Override
    public List<PropertyValue> getProperties(URI object, NodeType type) {
        List<PropertyValue> result = new ArrayList<PropertyValue>();

        Resource obj = model.createResource(object.toString());

        // Return all properties corresponding to a property constraint

        for (PropertyConstraint pc : type.getPropertyConstraints()) {
            get_properties(obj, pc.getPropertyType(), result);
        }

        return result;
    }

    @Override
    public List<PropertyValue> getProperties(URI object, PropertyType type) {
        List<PropertyValue> result = new ArrayList<PropertyValue>();

        Resource obj = model.createResource(object.toString());

        get_properties(obj, type, result);

        return result;
    }

    private void get_properties(Resource obj, PropertyType type, List<PropertyValue> result) {
        NodeIterator iter = model.listObjectsOfProperty(obj, as_property(type.getDomainPredicate()));

        while (iter.hasNext()) {
            result.add(as_property_value(iter.next(), type));
        }
    }

    private Resource as_resource(URI uri) {
        return model.createResource(uri.toString());
    }

    private Property as_property(URI uri) {
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

    private RDFNode as_rdf_node(PropertyValue value) {
        PropertyType type = value.getPropertyType();

        switch (type.getPropertyValueType()) {
        case COMPLEX:
            Resource res = model.createResource();

            for (PropertyValue subval : value.getComplexValue()) {
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

    private PropertyValue as_property_value(RDFNode rdfnode, PropertyType type) {
        PropertyValue value = new PropertyValue(type);

        switch (type.getPropertyValueType()) {
        case COMPLEX:
            List<PropertyValue> subvalues = new ArrayList<>();

            if (rdfnode.isResource()) {
                for (PropertyType subtype : type.getPropertySubTypes()) {
                    // TODO Might be many objects of predicate. Just pick one.

                    Statement s = model.getProperty(rdfnode.asResource(), as_property(subtype.getDomainPredicate()));

                    if (s != null) {
                        subvalues.add(as_property_value(s.getObject(), subtype));
                    }
                }
            }

            value.setComplexValue(subvalues);
            return value;
        case DATE_TIME:
            if (rdfnode.isLiteral() && rdfnode.asLiteral().getValue() instanceof XSDDateTime) {
                XSDDateTime dt = XSDDateTime.class.cast(rdfnode.asLiteral().getValue());

                if (dt != null) {
                    value.setDateTimeValue(as_date_time(dt));
                }
            }

            return value;
        case LONG:
            if (rdfnode.isLiteral()) {
                value.setLongValue(rdfnode.asLiteral().getLong());
            }
            return value;
        case STRING:
            if (rdfnode.isLiteral()) {
                value.setStringValue(rdfnode.asLiteral().getString());
            }
            return value;
        default:
            throw new RuntimeException("Unhandled value type");
        }
    }

    @Override
    public boolean hasRelationship(URI subject, URI predicate, URI object) {
        return model.contains(as_statement(subject, predicate, object));
    }
}
