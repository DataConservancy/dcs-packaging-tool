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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dataconservancy.packaging.tool.api.support.RulePropertiesManager;
import org.dataconservancy.packaging.tool.model.description.MappingSpec;
import org.dataconservancy.packaging.tool.model.description.PropertySpec;
import org.dataconservancy.packaging.tool.model.description.RelationshipSpec;
import org.dataconservancy.packaging.tool.model.description.RuleSpec;
import org.dataconservancy.packaging.tool.model.description.RulesSpec;
import org.dataconservancy.packaging.tool.model.description.ValueType;

public class SystemPropertyPreferencesRulePropertiesManager
        implements RulePropertiesManager {

    private static final String IGNORE_UNDEFINED =
            SystemPropertyPreferencesRulePropertiesManager.class
                    .getSimpleName() + ".ignoreUndefined";

    private static final String TRUE = Boolean.TRUE.toString();

    private final Preferences prefs;

    private final Map<String, String> propertyMap =
            new HashMap<String, String>();

    public SystemPropertyPreferencesRulePropertiesManager() {
        prefs =
                Preferences.userRoot().node(this.getClass().getName()
                        .replace(".", "/"));
    }

    @Override
    public void init(RulesSpec rules) {
        propertyMap.clear();

        for (RuleSpec ruleSpec : rules.getRule()) {
            if (ruleSpec.getMappings() != null) {
                for (MappingSpec mappingSpec : ruleSpec.getMappings()
                        .getMapping()) {
                    if (mappingSpec.getProperties() != null) {
                        for (PropertySpec propertySpec : mappingSpec
                                .getProperties().getProperty()) {
                            if (ValueType.PROPERTY.equals(propertySpec
                                    .getValue().getType())) {
                                propertyMap.put(propertySpec.getValue()
                                        .getSpecifier(), propertySpec
                                        .getValue().getDescription());
                            }
                        }
                    }

                    if (mappingSpec.getRelationships() != null) {
                        for (RelationshipSpec relSpec : mappingSpec
                                .getRelationships().getRelationship()) {
                            if (relSpec.getValue() != null
                                    && ValueType.PROPERTY.equals(relSpec
                                            .getValue().getType())) {
                                propertyMap.put(relSpec.getValue()
                                        .getSpecifier(), relSpec.getValue()
                                        .getDescription());
                            }
                        }
                    }

                }
            }
        }

        try {
            for (String key : prefs.keys()) {
                System.setProperty(key, prefs.get(key, null));
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasUndefinedProperties() {

        if (TRUE.toString().equals(System.getProperty(IGNORE_UNDEFINED))) {
            return false;
        }

        for (Map.Entry<String, String> prop : propertyMap.entrySet()) {
            if (System.getProperty(prop.getKey()) == null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, String> getUndefinedProperties() {

        Map<String, String> undefined = new HashMap<String, String>();

        if (TRUE.toString().equals(System.getProperty(IGNORE_UNDEFINED))) {
            return undefined;
        }

        for (Map.Entry<String, String> prop : propertyMap.entrySet()) {
            if (System.getProperty(prop.getKey()) == null) {
                undefined.put(prop.getKey(), prop.getValue());
            }
        }

        return undefined;
    }

    @Override
    public Map<String, String> getAllProperties() {
        return Collections.unmodifiableMap(propertyMap);
    }

    @Override
    public void setProperty(String key, String value) {
        System.setProperty(key, value);
        prefs.put(key, value);
    }

    @Override
    public void setIgnoreUndefinedProperties(boolean ignore) {
        prefs.put(IGNORE_UNDEFINED, new Boolean(ignore).toString());
        System.setProperty(IGNORE_UNDEFINED, new Boolean(ignore).toString());
    }

}
