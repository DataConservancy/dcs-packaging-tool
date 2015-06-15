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

package org.dataconservancy.packaging.tool.impl.support;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.packaging.tool.model.description.Action;
import org.dataconservancy.packaging.tool.model.description.MappingSpec;
import org.dataconservancy.packaging.tool.model.description.MappingsSpec;
import org.dataconservancy.packaging.tool.model.description.PropertiesSpec;
import org.dataconservancy.packaging.tool.model.description.PropertySpec;
import org.dataconservancy.packaging.tool.model.description.RelationshipSpec;
import org.dataconservancy.packaging.tool.model.description.RelationshipsSpec;
import org.dataconservancy.packaging.tool.model.description.RuleSpec;
import org.dataconservancy.packaging.tool.model.description.RulesSpec;
import org.dataconservancy.packaging.tool.model.description.SelectSpec;
import org.dataconservancy.packaging.tool.model.description.ValueSpec;
import org.dataconservancy.packaging.tool.model.description.ValueType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("serial")
public class SystemPropertyPreferencesRulePropertiesManagerTest {

    private static final String PROPERTY_1 = "property1";

    private static final String PROPERTY_2 = "property2";

    @Before
    public void clearProperties() throws BackingStoreException {
        Preferences
                .userRoot()
                .node(SystemPropertyPreferencesRulePropertiesManager.class
                        .getName().replace(".", "/")).clear();
    }

    /* Verify that undefined properties are detected */
    @Test
    public void undefinedPropertiesTest() {
        Map<String, String> properties = new HashMap<String, String>() {

            {
                put("undefinedPropertiesTest." + PROPERTY_1, "description1");
                put("undefinedPropertiesTest." + PROPERTY_2, "description2");
            }
        };

        SystemPropertyPreferencesRulePropertiesManager mgr =
                new SystemPropertyPreferencesRulePropertiesManager();
        
        /* Make sure the entire set of relationships is undefined */
        mgr.init(createRuleSpecs(true, properties));
        assertTrue(mgr.hasUndefinedProperties());
        assertEquals(properties.size(), mgr.getUndefinedProperties().size());
        assertTrue(properties.keySet().containsAll(mgr.getUndefinedProperties()
                .keySet()));
        assertTrue(properties.values().containsAll(mgr.getUndefinedProperties()
                .values()));
        
        /* Just make sure props in relationships are sane too */
        mgr.init(createRuleSpecs(false, properties));
        assertTrue(mgr.hasUndefinedProperties());
    }

    /*
     * Verify that setting/unsetting the ignore undefined properties flag works
     * as intended
     */
    @Test
    public void ignoreUndefinedPropertiesTest() {
        Map<String, String> properties = new HashMap<String, String>() {

            {
                put("ignoreUndefinedPropertiesTest" + PROPERTY_1, "description1");
                put("ignoreUndefinedPropertiesTest" + PROPERTY_2, "description2");
            }
        };

        SystemPropertyPreferencesRulePropertiesManager mgr =
                new SystemPropertyPreferencesRulePropertiesManager();

        /* Make sure the entire set of relationships is undefined */
        mgr.init(createRuleSpecs(true, properties));
        assertTrue(mgr.hasUndefinedProperties());

        mgr.setIgnoreUndefinedProperties(true);
        assertFalse(mgr.hasUndefinedProperties());

        mgr.setIgnoreUndefinedProperties(false);
        assertTrue(mgr.hasUndefinedProperties());

    }

    /* Verify that properties can be set */
    @Test
    public void setPropertyTest() {

        Map<String, String> properties = new HashMap<String, String>() {

            {
                put("setPropertyTest" + PROPERTY_1, "description1");
                put("setPropertyTest" + PROPERTY_2, "description2");
            }
        };

        SystemPropertyPreferencesRulePropertiesManager mgr =
                new SystemPropertyPreferencesRulePropertiesManager();

        /* Make sure the entire set of relationships is undefined */
        mgr.init(createRuleSpecs(true, properties));
        assertEquals(properties.size(), mgr.getUndefinedProperties().size());

        /* Now, set one */
        mgr.setProperty("setPropertyTest" + PROPERTY_1, "value");

        /* Now, verify that there are one fewer undefined properties */
        assertEquals((properties.size() - 1), mgr.getUndefinedProperties()
                .size());

        /* Re-initialize */
        mgr.init(createRuleSpecs(true, properties));

        /* Now, verify that there are still one fewer undefined properties */
        assertEquals((properties.size() - 1), mgr.getUndefinedProperties()
                .size());

    }
    
    /* Verify that properties can be set externaly via System.setProperties() */
    @Test
    public void externalSetPropertyTest() {

        Map<String, String> properties = new HashMap<String, String>() {

            {
                put("externalSetPropertyTest" + PROPERTY_1, "description1");
                put("externalSetPropertyTest" + PROPERTY_2, "description2");
            }
        };

        SystemPropertyPreferencesRulePropertiesManager mgr =
                new SystemPropertyPreferencesRulePropertiesManager();

        /* Make sure the entire set of relationships is undefined */
        mgr.init(createRuleSpecs(true, properties));
        assertEquals(properties.size(), mgr.getUndefinedProperties().size());

        /* Now, set one via System.setProperty*/
        System.setProperty("externalSetPropertyTest" + PROPERTY_1, "value");

        /* Now, verify that there are one fewer undefined properties */
        assertEquals((properties.size() - 1), mgr.getUndefinedProperties()
                .size());

        /* Re-initialize */
        mgr.init(createRuleSpecs(true, properties));

        /* Now, verify that there are still one fewer undefined properties */
        assertEquals((properties.size() - 1), mgr.getUndefinedProperties()
                .size());

    }

    private RulesSpec createRuleSpecs(boolean inProperties,
                                      Map<String, String> properties) {
        RulesSpec specs = new RulesSpec();

        for (Map.Entry<String, String> property : properties.entrySet()) {
            RuleSpec spec = new RuleSpec();
            spec.setSelect(new SelectSpec());
            spec.getSelect().setAction(Action.INCLUDE);
            spec.setMappings(new MappingsSpec());
            spec.getMappings().getMapping().add(new MappingSpec());
            
            if (inProperties) {
                /* Put value in properties section */
                spec.getMappings().getMapping().get(0).setProperties(new PropertiesSpec());

                PropertySpec ps = new PropertySpec();
                ps.setName(UUID.randomUUID().toString());
                ps.setValue(new ValueSpec());
                ps.getValue().setType(ValueType.PROPERTY);
                ps.getValue().setSpecifier(property.getKey());
                ps.getValue().setDescription(property.getValue());

                spec.getMappings().getMapping().get(0).getProperties().getProperty().add(ps);
                
            } else {
                /* Put value in relationship section */
                spec.getMappings().getMapping().get(0)
                        .setRelationships(new RelationshipsSpec());

                RelationshipSpec rs = new RelationshipSpec();
                rs.setName(UUID.randomUUID().toString());
                rs.setValue(new ValueSpec());
                rs.getValue().setType(ValueType.PROPERTY);
                rs.getValue().setSpecifier(property.getKey());
                rs.getValue().setDescription(property.getValue());

                spec.getMappings().getMapping().get(0).getRelationships().getRelationship().add(rs);
            }

            specs.getRule().add(spec);
        }

        return specs;
    }
}
