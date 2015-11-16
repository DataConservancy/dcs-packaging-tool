package org.dataconservancy.packaging.tool.ser;

import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppReader;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.dataconservancy.packaging.tool.model.ser.StreamId;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UserPropertyConverterTest extends AbstractRoundTripConverterTest {
    private TreeMap<URI, List<Property>> testMap = new TreeMap<URI, List<Property>>() {
        {
            try {
                PropertyType typeOne = new PropertyType();
                typeOne.setDomainPredicate(new URI("pred:1"));
                typeOne.setPropertyValueType(PropertyValueType.STRING);

                PropertyType typeTwo = new PropertyType();
                typeTwo.setDomainPredicate(new URI("pred:2"));
                typeTwo.setPropertyValueType(PropertyValueType.URI);

                Property propertyOne = new Property(typeOne);
                propertyOne.setStringValue("foo");

                Property propertyTwo = new Property(typeTwo);
                propertyTwo.setUriValue(new URI("value:foo"));

                Property propertyThree = new Property(typeOne);
                propertyThree.setStringValue("bar");

                put(new URI("node:1"), Arrays.asList(propertyOne, propertyTwo));
                put(new URI("node:2"), Collections.singletonList(propertyThree));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    };

    private ClassPathResource serialization = new ClassPathResource(
            "org/dataconservancy/packaging/tool/ser/user-property-v1.ser");

    private UserPropertyConverter underTest = new UserPropertyConverter();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        underTest.setStreamId(StreamId.USER_SPECIFIED_PROPERTIES.name());
    }

    @Override
    public InputStream getSerializationInputStream() throws IOException {
        return serialization.getInputStream();
    }

    @Override
    public Object getSerializationObject() {
        return testMap;
    }

    @Override
    public AbstractPackageToolConverter getUnderTest() {
        return underTest;
    }

    @Test
    public void testCanConvert() throws Exception {
        assertTrue(underTest.canConvert(new HashMap<String, List<Property>>().getClass()));
    }

    @Test
    public void testMarshal() throws Exception {
        StringWriter writer = new StringWriter();
        underTest.marshal(testMap, new PrettyPrintWriter(writer), getMarshalingContext());

        assertTrue(writer.getBuffer().length() > 1);

        String result = writer.getBuffer().toString();

        assertTrue(result.contains(UserPropertyConverter.E_USER_PROPERTY));
        assertTrue(result.contains("foo"));
        assertTrue(result.contains("value:foo"));
        assertTrue(result.contains("bar"));
    }

    @Test(expected = ClassCastException.class)
    public void testMarshalWithNonStringMap() throws Exception {
        StringWriter writer = new StringWriter();

        Map<Number, Number> testMap = new HashMap<Number, Number>() {
            {
                put(1, 2);
                put(3, 4);
            }
        };

        underTest.marshal(testMap, new PrettyPrintWriter(writer), getMarshalingContext());
    }

    @Test
    public void testUnmarshal() throws Exception {
        XmlPullParser parser = getPullParser();
        InputStreamReader reader = new InputStreamReader(getSerializationInputStream());

        Object result = underTest.unmarshal(new XppReader(reader, parser), getUnmarshallingContext());

        assertNotNull(result);
        assertTrue(Map.class.isAssignableFrom(result.getClass()));

        @SuppressWarnings("unchecked")
        Map<URI, List<Property>> metadata = (Map<URI, List<Property>>) result;

        assertEquals(testMap, metadata);
    }
}
