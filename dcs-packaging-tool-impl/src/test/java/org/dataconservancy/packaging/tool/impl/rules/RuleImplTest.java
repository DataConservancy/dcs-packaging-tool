/* 
 * Copyright 2014 Johns Hopkins University 
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

package org.dataconservancy.packaging.tool.impl.rules;

import java.io.File;

import java.net.URI;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.dataconservancy.packaging.tool.impl.rules.operations.Value_Literal;
import org.dataconservancy.packaging.tool.model.description.Action;
import org.dataconservancy.packaging.tool.model.description.ArtifactTypeSpec;
import org.dataconservancy.packaging.tool.model.description.FileSpec;
import org.dataconservancy.packaging.tool.model.description.MappingSpec;
import org.dataconservancy.packaging.tool.model.description.MappingsSpec;
import org.dataconservancy.packaging.tool.model.description.PropertiesSpec;
import org.dataconservancy.packaging.tool.model.description.PropertySpec;
import org.dataconservancy.packaging.tool.model.description.RelationshipSpec;
import org.dataconservancy.packaging.tool.model.description.RelationshipsSpec;
import org.dataconservancy.packaging.tool.model.description.RuleSpec;
import org.dataconservancy.packaging.tool.model.description.SelectSpec;
import org.dataconservancy.packaging.tool.model.description.TestSpec;
import org.dataconservancy.packaging.tool.model.description.ValueSpec;
import org.dataconservancy.packaging.tool.model.description.ValueType;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TestOperationFactory.class, ValueOperationFactory.class,
        FileOperationFactory.class})
@SuppressWarnings({"unchecked", "rawtypes"})
public class RuleImplTest {

    /* Verify that correct action is specified */
    @Test
    public void actionTest() {
        RuleSpec ruleSpec = new RuleSpec();
        SelectSpec select = new SelectSpec();
        TestSpec selectTestSpec = new TestSpec();
        select.setTest(selectTestSpec);

        /* We specify the exclude action */
        select.setAction(Action.EXCLUDE);
        ruleSpec.setSelect(select);

        /* Just because it's required - we don't care about this here */
        PowerMockito.mockStatic(TestOperationFactory.class);
        when(TestOperationFactory.getOperation(selectTestSpec))
                .thenReturn(mock(TestOperation.class));

        RuleImpl selectRule = new RuleImpl(ruleSpec);
        assertEquals(Action.EXCLUDE, selectRule.getAction());
    }

    /* Verify that select test operation is run via select() */
    @Test
    public void selectTest() {
        TestSpec selectTestSpec = new TestSpec();
        FileContext fileCandidate = new FileContextImpl(new File("/"), false);
        TestOperation selectTestOp = mock(TestOperation.class);

        PowerMockito.mockStatic(TestOperationFactory.class);
        when(TestOperationFactory.getOperation(selectTestSpec))
                .thenReturn(selectTestOp);
        when(selectTestOp.operate(fileCandidate))
                .thenReturn(new Boolean[] {Boolean.TRUE});

        RuleSpec ruleSpec = new RuleSpec();
        SelectSpec select = new SelectSpec();
        select.setTest(selectTestSpec);
        ruleSpec.setSelect(select);

        RuleImpl selectRule = new RuleImpl(ruleSpec);

        assertTrue(selectRule.select(fileCandidate));
        verify(selectTestOp).operate(fileCandidate);
    }

    /* Verify that 'type' from the mapping is correct */
    @Test
    public void typeMappingTest() {
        TestSpec selectTestSpec = new TestSpec();
        FileContext fileCandidate = new FileContextImpl(new File("/"), false);
        final String TYPE = "type!";

        /* Just because it's required - we don't care about this here */
        PowerMockito.mockStatic(TestOperationFactory.class);
        when(TestOperationFactory.getOperation(selectTestSpec))
                .thenReturn(mock(TestOperation.class));

        MappingSpec mappingSpec = new MappingSpec();
        ArtifactTypeSpec type = new ArtifactTypeSpec();
        type.setValue(TYPE);
        mappingSpec.setType(type);

        RuleSpec ruleSpec = new RuleSpec();
        SelectSpec select = new SelectSpec();
        select.setTest(selectTestSpec);
        ruleSpec.setSelect(select);
        ruleSpec.setMappings(new MappingsSpec());
        ruleSpec.getMappings().getMapping().add(mappingSpec);

        RuleImpl rule = new RuleImpl(ruleSpec);

        assertEquals(TYPE, rule.getMappings(fileCandidate).get(0).getType().getValue());
    }

    @Test
    public void propertiesMappingTest() {
        TestSpec selectTestSpec = new TestSpec();
        FileContext fileCandidate = new FileContextImpl(new File("/"), false);

        final String VALUE_APPEAR_KEY = "value_key";
        final String VALUE_NOT_APPEAR_KEY = "No appear";
        final String[] VALUE = {"value!"};

        /* Just because it's required - we don't care about this here */
        PowerMockito.mockStatic(TestOperationFactory.class);
        when(TestOperationFactory.getOperation(selectTestSpec))
                .thenReturn(mock(TestOperation.class));

        ValueOperation valueOfPropertyToAppear = mock(ValueOperation.class);
        when(valueOfPropertyToAppear.operate(fileCandidate))
                .thenReturn(VALUE);

        ValueOperation valueOfPropertyNotToAppear = mock(ValueOperation.class);
        when(valueOfPropertyNotToAppear.operate(fileCandidate))
                .thenReturn(new String[] {});

        ValueSpec valueSpecOfPropertyToAppear = mock(ValueSpec.class);
        ValueSpec valueSpecOfPropertyNotToAppear = mock(ValueSpec.class);

        MappingSpec mappingSpec = new MappingSpec();
        PropertySpec propToAppear = new PropertySpec();
        PropertySpec propToNotAppear = new PropertySpec();
        propToAppear.setName(VALUE_APPEAR_KEY);
        propToAppear.setValue(valueSpecOfPropertyToAppear);

        propToNotAppear.setName(VALUE_NOT_APPEAR_KEY);
        propToNotAppear.setValue(valueSpecOfPropertyNotToAppear);
        PropertiesSpec propertiesSpec = new PropertiesSpec();
        propertiesSpec.getProperty().addAll(Arrays.asList(new PropertySpec[] {
                propToAppear, propToNotAppear}));
        mappingSpec.setProperties(propertiesSpec);

        PowerMockito.mockStatic(ValueOperationFactory.class);
        when(ValueOperationFactory.getOperation(valueSpecOfPropertyToAppear))
                .thenReturn(valueOfPropertyToAppear);
        when(ValueOperationFactory.getOperation(valueSpecOfPropertyNotToAppear))
                .thenReturn(valueOfPropertyNotToAppear);

        RuleSpec ruleSpec = new RuleSpec();
        SelectSpec select = new SelectSpec();
        select.setTest(selectTestSpec);
        ruleSpec.setSelect(select);
        ruleSpec.setMappings(new MappingsSpec());
        ruleSpec.getMappings().getMapping().add(mappingSpec);

        RuleImpl rule = new RuleImpl(ruleSpec);
        Map<String, List<String>> properties =
                rule.getMappings(fileCandidate).get(0).getProperties();
        assertEquals(1, properties.size());
        assertTrue(properties.containsKey(VALUE_APPEAR_KEY));
        assertEquals(properties.get(VALUE_APPEAR_KEY), Arrays.asList(VALUE));
        verify(valueOfPropertyToAppear).operate(fileCandidate);
        verify(valueOfPropertyNotToAppear).operate(fileCandidate);
    }

    @Test
    public void relationshipsMappingTest() {
        TestSpec selectTestSpec = new TestSpec();
        FileContext fileCandidate = new FileContextImpl(new File("/"), false);

        final String RELATIONSHIP_APPEAR_KEY = "Appear";
        final String RELATIONSHIP_NOT_APPEAR_KEY = "No appear";
        final String VALUE_RELS_KEY = "valueRels";

        final File relFile1 = new File("/one");
        final File relFile2 = new File("/two");
        final URI externalRelURI = URI.create("http://dataconservancy.org/test");

        /* Just because it's required - we don't care about this here */
        PowerMockito.mockStatic(TestOperationFactory.class);
        when(TestOperationFactory.getOperation(selectTestSpec))
                .thenReturn(mock(TestOperation.class));

        FileOperation fileOfRelsToAppear = mock(FileOperation.class);
        when(fileOfRelsToAppear.operate(fileCandidate)).thenReturn(new File[] {
                relFile1, relFile2});
        
        ValueOperation externalRelValue = new Value_Literal();
        externalRelValue.setSpecifier(externalRelURI.toString());

        FileOperation fileOfRelsNotToAppear = mock(FileOperation.class);
        when(fileOfRelsNotToAppear.operate(fileCandidate))
                .thenReturn(new File[] {});

        FileSpec fileSpecOfRelsToAppear = mock(FileSpec.class);
        FileSpec fileSpecOfRelsNotToAppear = mock(FileSpec.class);
        ValueSpec valueSpec = new ValueSpec();
        valueSpec.setSpecifier(externalRelURI.toString());
        valueSpec.setType(ValueType.LITERAL);

        MappingSpec mappingSpec = new MappingSpec();
        RelationshipSpec relsToAppear = new RelationshipSpec();
        RelationshipSpec relsNotToAppear = new RelationshipSpec();
        RelationshipSpec valueRels = new RelationshipSpec();

        relsToAppear.setName(RELATIONSHIP_APPEAR_KEY);
        relsToAppear.setFile(fileSpecOfRelsToAppear);

        relsNotToAppear.setName(RELATIONSHIP_NOT_APPEAR_KEY);
        relsNotToAppear.setFile(fileSpecOfRelsNotToAppear);
        
        valueRels.setName(VALUE_RELS_KEY);
        valueRels.setValue(valueSpec);

        RelationshipsSpec relationshipsSpec = new RelationshipsSpec();
        relationshipsSpec.getRelationship().addAll(Arrays.asList(new RelationshipSpec[] {
                        relsToAppear, relsNotToAppear, valueRels}));
        mappingSpec.setRelationships(relationshipsSpec);

        PowerMockito.mockStatic(FileOperationFactory.class);
        when(FileOperationFactory.getOperation(fileSpecOfRelsToAppear))
                .thenReturn(fileOfRelsToAppear);
        when(FileOperationFactory.getOperation(fileSpecOfRelsNotToAppear))
                .thenReturn(fileOfRelsNotToAppear);

        RuleSpec ruleSpec = new RuleSpec();
        SelectSpec select = new SelectSpec();
        select.setTest(selectTestSpec);
        ruleSpec.setSelect(select);
        ruleSpec.setMappings(new MappingsSpec());
        ruleSpec.getMappings().getMapping().add(mappingSpec);

        RuleImpl rule = new RuleImpl(ruleSpec);
        Map<String, Set<URI>> rels =
                rule.getMappings(fileCandidate).get(0).getRelationships();

        assertEquals(2, rels.size());
        assertTrue(rels.containsKey(RELATIONSHIP_APPEAR_KEY));
        assertTrue(rels.containsKey(VALUE_RELS_KEY));

        assertTrue(rels.get(RELATIONSHIP_APPEAR_KEY)
                .containsAll(Arrays.asList(new URI[] {relFile1.toURI(), relFile2.toURI()})));
        assertTrue(rels.get(VALUE_RELS_KEY).containsAll(Arrays.asList(externalRelURI)));

        verify(fileOfRelsToAppear).operate(fileCandidate);
        verify(fileOfRelsNotToAppear).operate(fileCandidate);
    }

}
