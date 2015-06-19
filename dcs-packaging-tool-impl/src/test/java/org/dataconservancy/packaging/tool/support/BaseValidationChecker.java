/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.packaging.validation.PackageValidationException;

import static org.dataconservancy.dcs.model.AttributeSetName.ORE_REM_COLLECTION;
import static org.dataconservancy.dcs.model.AttributeSetName.ORE_REM_DATAITEM;
import static org.dataconservancy.dcs.model.AttributeSetName.ORE_REM_FILE;
import static org.dataconservancy.dcs.model.AttributeSetName.ORE_REM_PACKAGE;
import static org.dataconservancy.dcs.model.AttributeSetName.ORE_REM_PROJECT;
import static org.dataconservancy.dcs.model.Metadata.COLLECTION_AGGREGATED_BY_PROJECT;
import static org.dataconservancy.dcs.model.Metadata.COLLECTION_AGGREGATES_COLLECTION;
import static org.dataconservancy.dcs.model.Metadata.COLLECTION_AGGREGATES_DATAITEM;
import static org.dataconservancy.dcs.model.Metadata.COLLECTION_AGGREGATES_FILE;
import static org.dataconservancy.dcs.model.Metadata.COLLECTION_IS_PART_OF;
import static org.dataconservancy.dcs.model.Metadata.DATA_ITEM_AGGREGATES_FILE;
import static org.dataconservancy.dcs.model.Metadata.DATA_ITEM_IS_PART_OF_COLLECTION;
import static org.dataconservancy.dcs.model.Metadata.PACKAGE_AGGREGATES_COLLECTION;
import static org.dataconservancy.dcs.model.Metadata.PACKAGE_AGGREGATES_DATAITEM;
import static org.dataconservancy.dcs.model.Metadata.PACKAGE_AGGREGATES_FILE;
import static org.dataconservancy.dcs.model.Metadata.PACKAGE_AGGREGATES_PROJECT;
import static org.dataconservancy.dcs.model.Metadata.PROJECT_AGGREGATES_COLLECTION;
import static org.dataconservancy.dcs.model.Metadata.PROJECT_AGGREGATES_FILE;

/**
 * A base class providing minimal logic for iterating over the relationships between objects in the ORE graph, allowing
 * subclasses execute implementation-dependent tasks.  This class also provides basic error handling; subclasses may
 * populate a {@code List} of error messages and this class will throw StatefulIngestServiceException if the list is
 * not empty.
 */
public abstract class BaseValidationChecker {

    static final String CONCAT_CHAR = "_";

    private static final String RESERVED_CHAR_ERR = "Cannot compose a key using the string '%s' because it contains " +
            "the reserved character sequence '%s'";
    
    /**
     * Contains a list of errors that are encountered when type-checking the AttributeSets. <em>Only</em> for use
     * by unit tests!
     */
    private List<String> errors;

    /**
     * A regular expression Pattern that is meant to match the name of ResourceId Attributes.  For example:
     * 'Project-ResourceId', 'Collection-ResourceId', 'DataItem-ResourceId', etc.  The Attribute should occur exactly
     * once in each ORE-related AttributeSet, serving to uniquely identify the AttributeSet within the package.
     */
    static final Pattern IDENTIFIER_ATTR_NAME = Pattern.compile(".*-ResourceId");

    /**
     * Public, no-arg constructor typically used in Production.
     */
    public BaseValidationChecker() {

    }

    /**
     * Accepts a mutable List that is updated with the errors.  <em>Only</em> for use in a test environment!  The
     * implementation is not thread-safe because access to the supplied list is not mediated in any way.
     * <p/>
     * Subclasses are expected to populate this list as appropriate.
     *
     * @param errors a mutable List populated with errors after each run of the {@code execute(...)} method
     */
    public BaseValidationChecker(List<String> errors) {
        this.errors = errors;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation details:<br/>
     * Checks the types of the ORE-related aggregations in the AttributeSetManager.  It manages a {@code List} of
     * errors; as errors are found, they are appended to the {@code List}.  If the List is non-empty at the end of
     * execution, a {@code StatefulIngestServiceException} is thrown.  No events are emitted by this service.
     *
     * @throws PackageValidationException 
     * @throws PackageValidationException if any type check fails
     */
    public void validate(Map<String, AttributeSet> attributeMap) throws PackageValidationException {

        final List<String> localErrors;

        if (errors == null) {
            localErrors = new ArrayList<>();
        } else {
            localErrors = errors;
            localErrors.clear();
        }

        // Check Package aggregations that aggregate Projects, Files, Collections, and DataItems
        checkAggregation(ORE_REM_PACKAGE, ORE_REM_PROJECT, PACKAGE_AGGREGATES_PROJECT, attributeMap, localErrors);
        checkAggregation(ORE_REM_PACKAGE, ORE_REM_FILE, PACKAGE_AGGREGATES_FILE, attributeMap, localErrors);
        checkAggregation(ORE_REM_PACKAGE, ORE_REM_COLLECTION, PACKAGE_AGGREGATES_COLLECTION, attributeMap, localErrors);
        checkAggregation(ORE_REM_PACKAGE, ORE_REM_DATAITEM, PACKAGE_AGGREGATES_DATAITEM, attributeMap, localErrors);

        // Check Project aggregations that aggregate Files and Collections
        checkAggregation(ORE_REM_PROJECT, ORE_REM_FILE, PROJECT_AGGREGATES_FILE, attributeMap, localErrors);
        checkAggregation(ORE_REM_PROJECT, ORE_REM_COLLECTION, PROJECT_AGGREGATES_COLLECTION, attributeMap, localErrors);

        // Check Collection aggregations that aggregate Collections, DataItems, and Files
        checkAggregation(ORE_REM_COLLECTION, ORE_REM_COLLECTION, COLLECTION_AGGREGATES_COLLECTION, attributeMap, localErrors);
        checkAggregation(ORE_REM_COLLECTION, ORE_REM_DATAITEM, COLLECTION_AGGREGATES_DATAITEM, attributeMap, localErrors);
        checkAggregation(ORE_REM_COLLECTION, ORE_REM_FILE, COLLECTION_AGGREGATES_FILE, attributeMap, localErrors);

        // Check Collections that are part of Other Collections
        checkAggregation(ORE_REM_COLLECTION, ORE_REM_COLLECTION, COLLECTION_IS_PART_OF, attributeMap, localErrors);

        // Check Collections that are aggregated by Projects
        checkAggregation(ORE_REM_COLLECTION, ORE_REM_PROJECT, COLLECTION_AGGREGATED_BY_PROJECT, attributeMap, localErrors);

        // Check DataItem aggregations that aggregate Files
        checkAggregation(ORE_REM_DATAITEM, ORE_REM_FILE, DATA_ITEM_AGGREGATES_FILE, attributeMap, localErrors);

        // Check DataItems that are aggregated by Collections
        checkAggregation(ORE_REM_DATAITEM, ORE_REM_COLLECTION, DATA_ITEM_IS_PART_OF_COLLECTION, attributeMap, localErrors);

        if (localErrors.size() > 0) {
            StringBuilder msg = new StringBuilder();
            for (int i = 0; i < localErrors.size(); i++) {
                msg.append(localErrors.get(i));
                if (i + 1 < localErrors.size()) {
                    msg.append("\n");
                }
            }
            throw new PackageValidationException(msg.toString());
        }
    }

    /**
     * Subclasses use the supplied parameters to look up AttributeSets from the supplied {@code asm} and perform
     * implementation dependent logic.
     * <p/>
     * Parameters are:
     * <dl>
     *     <dt>aggregatingType</dt>
     *     <dd>the type (name) of the AttributeSet that is aggregating the {@code aggregatedType}; for example
     *     <strong>Ore-Rem-Collection</strong></dd>
     *     <dt>aggregatedType</dt>
     *     <dd>the type (name) of the AttributeSet that is being aggregated by {@code aggregatingType}; for example
     *     <strong>Ore-Rem-DataItem</strong></dd>
     *     <dt>aggregatingRelationship</dt>
     *     <dd>the relationship that is used to aggregate {@code aggregatingType} and {@code aggregatedType}; for
     *     example <strong>Collection-Aggregates-DataItem</strong></dd>
     * </dl>
     *
     * @param aggregatingType the type (name) of the AttributeSet that is aggregating the {@code aggregatedType}
     * @param aggregatedType the type (name) of the AttributeSet that is being aggregated by {@code aggregatingType}
     * @param aggregatingRelationship the relationship that is used to aggregate {@code aggregatingType} and
     *                                {@code aggregatedType}
     * @param attributeMap the AttributeSetManager used by implementations to retrieve AttributeSets
     * @param errors a mutable List populated by subclasses, containing Strings of error messages.
     */
    abstract void checkAggregation(String aggregatingType, String aggregatedType, String aggregatingRelationship, Map<String, AttributeSet> attributeMap, List<String> errors);

    protected Set<AttributeSet> matchAttribute(Map<String, AttributeSet> attributeMap, Attribute matchingAttribute) {
        Set<AttributeSet> results = new HashSet<>();
        for (AttributeSet attrSet : attributeMap.values()) {
            
            for (Attribute attr : attrSet.getAttributes()) {
                boolean matches = true;
    
                if (matchingAttribute.getName() != null) {
                    matches = matchingAttribute.getName().equals(attr.getName());
                }
    
                if (matches && matchingAttribute.getType() != null) {
                    matches = matchingAttribute.getType().equals(attr.getType());
                }
    
                if (matches && matchingAttribute.getValue() != null) {
                    matches = matchingAttribute.getValue().equals(attr.getValue());
                }
                
                if (matches) {
                    results.add(attrSet);
                    break;
                }
            }
        }
        
        return results;
    }
    
    protected Set<AttributeSet> matchAttributeSetName(Map<String, AttributeSet> attributeMap, String nameToMatch) {
        Set<AttributeSet> results = new HashSet<>();
        
        for (AttributeSet attrSet : attributeMap.values()) {
            if(attrSet.getName().equalsIgnoreCase(nameToMatch)) {
                results.add(attrSet);
            }
        }
        return results;
    }
    
    protected Set<String> values(AttributeSet attrSet, String attrName) {
        final Set<String> results = new HashSet<>();
        for (Attribute idAttr : attrSet.getAttributesByName(attrName)) {
            results.add(idAttr.getValue());
        }

        return results;
    }
    
    protected String composeKey(String attributeSetName, String s) {
        if (attributeSetName.contains(CONCAT_CHAR)) {
            throw new IllegalArgumentException(String.format(RESERVED_CHAR_ERR, attributeSetName, CONCAT_CHAR));
        }

        if (s.contains(CONCAT_CHAR)) {
            throw new IllegalArgumentException(String.format(RESERVED_CHAR_ERR, s, CONCAT_CHAR));
        }

        return attributeSetName + CONCAT_CHAR + s;
    }
}
