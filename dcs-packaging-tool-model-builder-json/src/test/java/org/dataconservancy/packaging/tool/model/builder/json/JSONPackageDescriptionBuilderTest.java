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
package org.dataconservancy.packaging.tool.model.builder.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.jsonldjava.utils.JsonUtils;
import org.dataconservancy.packaging.tool.model.PackageRelationship;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.dataconservancy.packaging.tool.model.DcsPackageArtifactType;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.dataconservancy.packaging.tool.model.PackageDescription;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
public class JSONPackageDescriptionBuilderTest {
    
    private PackageDescription sampleDescription;
    private String serializedDescription;
    private final static String ARTIFACT_ONE_ID = "id:a";
    private final static String ARTIFACT_TWO_ID = "id:b";
    private final static String ARTIFACT_THREE_ID = "id:c";
    private final static String ARTIFACT_FOUR_ID = "id:d";

    private final static String RELATIONSHIP_ONE_TYPE = "relationshipsOne";
    private final static String RELATIONSHIP_TWO_TYPE = "relationshipTwo";
    private JSONPackageDescriptionBuilder builder = new JSONPackageDescriptionBuilder();
    
    
    @Before
    public void setup() {
        List<PackageRelationship> relationshipSetOne = new ArrayList<PackageRelationship>();
        relationshipSetOne.add(new PackageRelationship(RELATIONSHIP_ONE_TYPE, true, "foo", "bar", "baz"));

        List<PackageRelationship> relationshipSetTwo = new ArrayList<PackageRelationship>();
        relationshipSetTwo.add(new PackageRelationship(RELATIONSHIP_TWO_TYPE, true, "foo"));

        PackageArtifact artifactOne = new PackageArtifact();
        PackageArtifact artifactTwo = new PackageArtifact();
        PackageArtifact artifactThree = new PackageArtifact();
        PackageArtifact artifactFour = new PackageArtifact();

        artifactOne.setId(ARTIFACT_ONE_ID);
        artifactOne.setType(DcsPackageArtifactType.Collection.name());
        artifactOne.setArtifactRef("this.ref");
        artifactOne.addSimplePropertyValue("PropertyOne", "valueOne");
        artifactOne.addSimplePropertyValue("PropertyTwo", "valueTwo");
        artifactOne.setRelationships(relationshipSetOne);
        artifactOne.setByteStream(false);
        artifactOne.setIgnored(true);

        artifactTwo.setId(ARTIFACT_TWO_ID);
        artifactTwo.setType(DcsPackageArtifactType.DataItem.name());
        artifactTwo.setArtifactRef("this.ref");
        artifactTwo.addSimplePropertyValue("PropertyOne", "valueOne");
        artifactTwo.addSimplePropertyValue("PropertyTwo", "valueTwo");
        artifactTwo.setRelationships(relationshipSetOne);
        artifactTwo.setByteStream(false);

        artifactThree.setId(ARTIFACT_THREE_ID);
        artifactThree.setType(DcsPackageArtifactType.DataItem.name());
        artifactThree.setArtifactRef("this.ref");
        artifactThree.addSimplePropertyValue("PropertyThree", "valueThree");
        artifactThree.setRelationships(relationshipSetTwo);
        artifactThree.setByteStream(false);

        artifactFour.setId(ARTIFACT_FOUR_ID);
        artifactFour.setType(DcsPackageArtifactType.DataFile.name());
        artifactFour.setArtifactRef("this.ref");
        artifactFour.addSimplePropertyValue("PropertyThree", "valueThree");
        artifactFour.addSimplePropertyValue("PropertyThree", "valueFour");
        artifactFour.setRelationships(relationshipSetTwo);
        artifactFour.setByteStream(true);

        Set<PackageArtifact> artifactSetOne = new HashSet<PackageArtifact>();
        artifactSetOne.add(artifactOne);
        artifactSetOne.add(artifactTwo);
        artifactSetOne.add(artifactThree);
        artifactSetOne.add(artifactFour);

        String specIdentifierOne = "Spec:one";
        sampleDescription = new PackageDescription();

        sampleDescription.setPackageOntologyIdentifier(specIdentifierOne);
        sampleDescription.setPackageArtifacts(artifactSetOne);
        sampleDescription.setRootArtifactRef("root");
        serializedDescription = "{\n" +
                "  \"@context\" : {\n" +
                "    \"dc\" : \"http://dataconservancy.org/ns/types/\"\n" +
                "  },\n" +
                "  \"@graph\" : [ {\n" +
                "    \"@id\" : \"_:t0\",\n" +
                "    \"@type\" : \"dc:PackageDescription\",\n" +
                "    \"dc:hasArtifact\" : [ {\n" +
                "      \"@id\" : \"_:t1\"\n" +
                "    }, {\n" +
                "      \"@id\" : \"_:t2\"\n" +
                "    }, {\n" +
                "      \"@id\" : \"_:t3\"\n" +
                "    }, {\n" +
                "      \"@id\" : \"_:t4\"\n" +
                "    } ],\n" +
                "    \"dc:hasProperty\" : {\n" +
                "      \"@id\" : \"_:t5\"\n" +
                "    },\n" +
                "    \"dc:hasSpecificationId\" : \"Spec:one\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t1\",\n" +
                "    \"@type\" : \"dc:Artifact\",\n" +
                "    \"dc:hasId\" : \"id:a\",\n" +
                "    \"dc:hasProperty\" : [ {\n" +
                "      \"@id\" : \"_:t14\"\n" +
                "    }, {\n" +
                "      \"@id\" : \"_:t12\"\n" +
                "    } ],\n" +
                "    \"dc:hasRef\" : \"this.ref\",\n" +
                "    \"dc:hasRelationship\" : {\n" +
                "      \"@id\" : \"_:t13\"\n" +
                "    },\n" +
                "    \"dc:hasType\" : \"Collection\",\n" +
                "    \"dc:isByteStream\" : \"false\",\n" +
                "    \"dc:isIgnored\" : \"true\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t10\",\n" +
                "    \"@type\" : \"dc:Property\",\n" +
                "    \"dc:hasName\" : \"PropertyTwo\",\n" +
                "    \"dc:hasValue\" : \"valueTwo\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t11\",\n" +
                "    \"@type\" : \"dc:Relationship\",\n" +
                "    \"dc:hasTarget\" : \"foo\",\n" +
                "    \"dc:hasType\" : \"relationshipTwo\",\n" +
                "    \"dc:requiresURI\" : \"true\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t12\",\n" +
                "    \"@type\" : \"dc:Property\",\n" +
                "    \"dc:hasName\" : \"PropertyTwo\",\n" +
                "    \"dc:hasValue\" : \"valueTwo\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t13\",\n" +
                "    \"@type\" : \"dc:Relationship\",\n" +
                "    \"dc:hasTarget\" : [ \"bar\", \"foo\", \"baz\" ],\n" +
                "    \"dc:hasType\" : \"relationshipsOne\",\n" +
                "    \"dc:requiresURI\" : \"true\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t14\",\n" +
                "    \"@type\" : \"dc:Property\",\n" +
                "    \"dc:hasName\" : \"PropertyOne\",\n" +
                "    \"dc:hasValue\" : \"valueOne\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t15\",\n" +
                "    \"@type\" : \"dc:Property\",\n" +
                "    \"dc:hasName\" : \"PropertyOne\",\n" +
                "    \"dc:hasValue\" : \"valueOne\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t2\",\n" +
                "    \"@type\" : \"dc:Artifact\",\n" +
                "    \"dc:hasId\" : \"id:d\",\n" +
                "    \"dc:hasProperty\" : {\n" +
                "      \"@id\" : \"_:t8\"\n" +
                "    },\n" +
                "    \"dc:hasRef\" : \"this.ref\",\n" +
                "    \"dc:hasRelationship\" : {\n" +
                "      \"@id\" : \"_:t7\"\n" +
                "    },\n" +
                "    \"dc:hasType\" : \"DataFile\",\n" +
                "    \"dc:isByteStream\" : \"true\",\n" +
                "    \"dc:isIgnored\" : \"false\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t3\",\n" +
                "    \"@type\" : \"dc:Artifact\",\n" +
                "    \"dc:hasId\" : \"id:b\",\n" +
                "    \"dc:hasProperty\" : [ {\n" +
                "      \"@id\" : \"_:t15\"\n" +
                "    }, {\n" +
                "      \"@id\" : \"_:t10\"\n" +
                "    } ],\n" +
                "    \"dc:hasRef\" : \"this.ref\",\n" +
                "    \"dc:hasRelationship\" : {\n" +
                "      \"@id\" : \"_:t6\"\n" +
                "    },\n" +
                "    \"dc:hasType\" : \"DataItem\",\n" +
                "    \"dc:isByteStream\" : \"false\",\n" +
                "    \"dc:isIgnored\" : \"false\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t4\",\n" +
                "    \"@type\" : \"dc:Artifact\",\n" +
                "    \"dc:hasId\" : \"id:c\",\n" +
                "    \"dc:hasProperty\" : {\n" +
                "      \"@id\" : \"_:t9\"\n" +
                "    },\n" +
                "    \"dc:hasRef\" : \"this.ref\",\n" +
                "    \"dc:hasRelationship\" : {\n" +
                "      \"@id\" : \"_:t11\"\n" +
                "    },\n" +
                "    \"dc:hasType\" : \"DataItem\",\n" +
                "    \"dc:isByteStream\" : \"false\",\n" +
                "    \"dc:isIgnored\" : \"false\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t5\",\n" +
                "    \"@type\" : \"dc:Property\",\n" +
                "    \"dc:hasName\" : \"root\",\n" +
                "    \"dc:hasValue\" : \"root\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t6\",\n" +
                "    \"@type\" : \"dc:Relationship\",\n" +
                "    \"dc:hasTarget\" : [ \"bar\", \"foo\", \"baz\" ],\n" +
                "    \"dc:hasType\" : \"relationshipsOne\",\n" +
                "    \"dc:requiresURI\" : \"true\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t7\",\n" +
                "    \"@type\" : \"dc:Relationship\",\n" +
                "    \"dc:hasTarget\" : \"foo\",\n" +
                "    \"dc:hasType\" : \"relationshipTwo\",\n" +
                "    \"dc:requiresURI\" : \"true\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t8\",\n" +
                "    \"@type\" : \"dc:Property\",\n" +
                "    \"dc:hasName\" : \"PropertyThree\",\n" +
                "    \"dc:hasValue\" : [ \"valueThree\", \"valueFour\" ]\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:t9\",\n" +
                "    \"@type\" : \"dc:Property\",\n" +
                "    \"dc:hasName\" : \"PropertyThree\",\n" +
                "    \"dc:hasValue\" : \"valueThree\"\n" +
                "  } ]\n" +
                "}";
    }
    
    @Test
    public void testSerializedDescription() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        builder.serialize(sampleDescription, outStream);
        
        String outputDescription = outStream.toString();
        //TODO: Maybe a better method of testing can be done.  Can't test a direct string with
        //serializedDescription because there's no guarantee to how things will be ordered
        
        // Ensure JSON can be parsed.
        JsonUtils.fromString(outputDescription);
        
        // TODO Eventually test JSON has correct structure, instead just check for some literals that should be there.
                
        assertTrue(outputDescription.contains("\"foo\""));
        assertTrue(outputDescription.contains("\"Spec:one\""));
        assertTrue(outputDescription.contains("\"root\""));
        assertTrue(outputDescription.contains("\"this.ref\""));
        assertTrue(outputDescription.contains("\"PropertyOne\""));
        assertTrue(outputDescription.contains("\"valueOne\""));
    }
    
    @Test
    public void testDeserializedDescription() {
        InputStream is = new ByteArrayInputStream(serializedDescription.getBytes());
        PackageDescription description = builder.deserialize(is);
        assertEquals(sampleDescription, description);
    }
    
    @Test
    public void testRoundTrip() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        builder.serialize(sampleDescription, outStream);
        
        InputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        PackageDescription roundTrippedDescription = builder.deserialize(inStream);
        
        
        assertEquals(sampleDescription, roundTrippedDescription);
    }
}