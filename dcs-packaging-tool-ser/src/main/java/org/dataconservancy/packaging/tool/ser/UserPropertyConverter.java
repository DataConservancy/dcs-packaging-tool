package org.dataconservancy.packaging.tool.ser;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.ser.StreamId;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dataconservancy.packaging.tool.ser.XstreamUtil.hasAttribute;

/**
 * Converts the user specified properties {@code Map&lt;URI,Property>} to a serialized form and back.
 */
public class UserPropertyConverter extends AbstractPackageToolConverter {
    static final String E_USER_PROPERTY = "userProperty";
    static final String E_NODE = "node";
    static final String A_NODE_ID = "id";
    static final String E_PROPERTY = "property";
    static final String A_DOMAIN_PREDICATE = "predicate";
    static final String A_VALUE_TYPE = "valueType";

    public UserPropertyConverter() {
        setStreamId(StreamId.USER_SPECIFIED_PROPERTIES.name());
    }

    @Override
    public void marshalInternal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        @SuppressWarnings("unchecked") Map<URI, List<Property>> map = (Map<URI, List<Property>>) source;

        writer.startNode(E_USER_PROPERTY);

        map.forEach((key, valueList) -> {
            writer.startNode(E_NODE);
            writer.addAttribute(A_NODE_ID, key.toString());
            valueList.forEach(value -> {
                writer.startNode(E_PROPERTY);
                writer.addAttribute(A_DOMAIN_PREDICATE, value.getPropertyType().getDomainPredicate().toString());
                PropertyValueType type = value.getPropertyType().getPropertyValueType();
                if (type != null) {
                    writer.addAttribute(A_VALUE_TYPE, value.getPropertyType().getPropertyValueType().toString());
                }
                if (type != null && value.getPropertyType().getPropertyValueType() != null && value.getPropertyType().getPropertyValueType().equals(PropertyValueType.URI)) {
                    writer.setValue(value.getUriValue().toString());
                } else {
                    writer.setValue(value.getStringValue());
                }

                writer.endNode();
            });

            writer.endNode();
        });

        writer.endNode(); // E_PACKAGE_METADATA
    }

    @Override
    public Object unmarshalInternal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        HashMap<URI, List<Property>> result = new HashMap<>();

        if (!E_USER_PROPERTY.equals(reader.getNodeName())) {
            throw new ConversionException(
                    String.format(ERR_MISSING_EXPECTED_ELEMENT, E_USER_PROPERTY, reader.getNodeName()));
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            try {
                if (!hasAttribute(A_NODE_ID, reader)) {
                    throw new ConversionException(
                            String.format(ERR_MISSING_EXPECTED_ATTRIBUTE, A_NODE_ID));
                }
                URI key = new URI(reader.getAttribute(A_NODE_ID));
                ArrayList<Property> values = new ArrayList<>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (!hasAttribute(A_DOMAIN_PREDICATE, reader)) {
                        throw new ConversionException(
                                String.format(ERR_MISSING_EXPECTED_ATTRIBUTE, A_DOMAIN_PREDICATE));
                    }

                    URI domainPredicate = new URI(reader.getAttribute(A_DOMAIN_PREDICATE));

                    if (!hasAttribute(A_VALUE_TYPE, reader)) {
                        throw new ConversionException(
                                String.format(ERR_MISSING_EXPECTED_ATTRIBUTE, A_VALUE_TYPE));
                    }
                    PropertyValueType type = PropertyValueType.valueOf(reader.getAttribute(A_VALUE_TYPE));
                    if (type != null) {
                        PropertyType propertyType = new PropertyType();
                        propertyType.setDomainPredicate(domainPredicate);
                        propertyType.setPropertyValueType(type);
                        Property property = new Property(propertyType);

                        Object value = reader.getValue();

                        if (type.equals(PropertyValueType.URI)) {
                            property.setUriValue(new URI((String) value));
                        } else {
                            property.setStringValue((String) value);
                        }

                        values.add(property);
                    } else {
                        throw new ConversionException(
                                    String.format(ERR_CANNOT_CONVERT, E_USER_PROPERTY, this.getClass().getName()));
                    }
                    reader.moveUp();
                }
                reader.moveUp();
                result.put(key, values);
            } catch (URISyntaxException e) {
                throw new ConversionException(
                    String.format(ERR_CANNOT_CONVERT, E_USER_PROPERTY, this.getClass().getName()));
            }
        }

        return result;
    }

    @Override
    public boolean canConvert(Class type) {
        if (!Map.class.isAssignableFrom(type)) {
            return false;
        }

        try {
            @SuppressWarnings("unchecked")
            Class<Map<URI, List<Property>>> foo = (Class<Map<URI, List<Property>>>) type;
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
