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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.dcs.model.Attribute;
import org.dataconservancy.dcs.model.AttributeSet;


/**
 * Insures that the Subject and Object AttributeSets of ORE-extracted relationships are the correct type.
 * The following relationships are examined by this service:
 * <ul>
 *     <li>{@link org.dataconservancy.dcs.model.Metadata#PACKAGE_AGGREGATES_PROJECT}</li>
 *     <li>{@link org.dataconservancy.dcs.model.Metadata#PACKAGE_AGGREGATES_FILE}</li>
 *     <li>{@link org.dataconservancy.dcs.model.Metadata#PACKAGE_AGGREGATES_COLLECTION}</li>
 *     <li>{@link org.dataconservancy.dcs.model.Metadata#PACKAGE_AGGREGATES_DATAITEM}</li>
 *     <li>{@link org.dataconservancy.dcs.model.Metadata#PROJECT_AGGREGATES_FILE}</li>
 *     <li>{@link org.dataconservancy.dcs.model.Metadata#PROJECT_AGGREGATES_COLLECTION}</li>
 *     <li>{@link org.dataconservancy.dcs.model.Metadata#COLLECTION_AGGREGATES_COLLECTION}</li>
 *     <li>{@link org.dataconservancy.dcs.model.Metadata#COLLECTION_AGGREGATES_DATAITEM}</li>
 *     <li>{@link org.dataconservancy.dcs.model.Metadata#COLLECTION_AGGREGATES_FILE}</li>
 *     <li>{@link org.dataconservancy.dcs.model.Metadata#COLLECTION_AGGREGATED_BY_PROJECT}</li>
 *     <li>{@link org.dataconservancy.dcs.model.Metadata#DATA_ITEM_AGGREGATES_FILE}</li>
 *     <li>{@link org.dataconservancy.dcs.model.Metadata#DATA_ITEM_IS_PART_OF_COLLECTION}</li>
 * </ul>
 * For example, this service insures that the Subject of a {@code PACKAGE_AGGREGATES_PROJECT} relationship is a Package,
 * and the Object is a Project.  The name of an AttributeSet is used as a proxy for type; so in this example the
 * Subject AttributeSet name must be {@link org.dataconservancy.dcs.model.AttributeSetName#ORE_REM_PACKAGE}, and
 * the Object AttributeSet name must be {@link org.dataconservancy.dcs.model.AttributeSetName#ORE_REM_PROJECT}.
 * <p/>
 * The resource identifier Attribute is used to look up the Object of the relationship.
 * <p/>
 * This service is <em>not</em> responsible for insuring the existence of the Subject or Object, nor is it responsible
 * for checking the cardinality of relationships.  These are the responsibility of other services (e.g. the
 * {@code InternalReferenceChecker} and {@code RelationshipCardinalityVerificationService}).
 * <p/>
 * This ingest service is not thread safe.
 */
public class RelationshipTypeChecker extends BaseValidationChecker {

    private static final String INCORRECT_OBJECT_TYPE = "AttributeSet '%s' (key '%s') references Object '%s' with " +
            "a '%s' relationship; expected referenced Object type '%s', but found type '%s'";

    private static final String INCORRECT_SUBJECT_TYPE = "AttributeSet '%s' (key '%s') is a Subject of relationship " +
            "'%s'; expected AttributeSet type '%s', but found type '%s'";

    /**
     * Public, no-arg constructor typically used in Production.
     */
    public RelationshipTypeChecker() {

    }

    /**
     * Insures that the types in the {@code typeMap} match the {@code expectedType}.
     *
     * @param aggregatingAsKey the aggregating attribute set key (used for logging errors)
     * @param aggregatingAs the aggregating attribute set (used for logging errors)
     * @param aggregatingRelationship the aggregating relationship
     * @param typeMap a map of String business object identifiers to a Set of types
     * @param expectedType the expected type
     */
    private void checkTypes(String aggregatingAsKey, AttributeSet aggregatingAs, String aggregatingRelationship,
                            Map<String, Set<String>> typeMap, String expectedType) {
        for (Map.Entry<String, Set<String>> e : typeMap.entrySet()) {
            for (String type : e.getValue()) {
                if (!type.equals(expectedType)) {
                    if (aggregatingAsKey == null || aggregatingAsKey.trim().length() == 0) {
                        aggregatingAsKey = "unknown";
                    }
                    throw new RuntimeException(String.format(INCORRECT_OBJECT_TYPE, aggregatingAs.getName(), aggregatingAsKey,
                                                             e.getKey(), aggregatingRelationship, expectedType, e.getValue()));
                    //errors.add(String.format(INCORRECT_OBJECT_TYPE, aggregatingAs.getName(), aggregatingAsKey,
                    //        e.getKey(), aggregatingRelationship, expectedType, e.getValue()));
                }
            }
        }
    }

    /**
     * Produces a "type map" containing the reported types of each business object id in {@code resourceIds}.  The name of an
     * AttributeSet can be considered a proxy for a business object type.  For example, if the business id 'id:1234' is
     * present in an AttributeSet named 'Ore-Rem-Project' (<em>and</em> the Attribute with the value 'id:1234' carries
     * "identifier" semantics), then we can infer that 'id:1234' identifies a Project business object.
     * <p/>
     * <em>If</em> the AttributeSets have been produced properly, we expect exactly one 'Ore-Rem-Project' AttributeSet
     * to contain the identifer Attribute value 'id:1234'.  However, it is possible that the AttributeSets were not
     * produced correctly.  So this method returns a {@code Map} which allows for multiple types (AttributeSet names) to
     * be returned for a particular business id.
     *
     * @param resourceIds the resource ids to produce the type map for.  The supplied ids will be keys in the returned Map.
     * @param attributeMap the AttributeSetManager
     * @return a Map of AttributeSet names keyed by resource id.  As noted above, AttributeSet names can be used
     *         as a proxy for business object type.
     */
    static Map<String, Set<String>> types(Collection<String> resourceIds, Map<String, AttributeSet> attributeMap) {
        Map<String, Set<String>> typeMap = new HashMap<>();

        for (final String id : resourceIds) {
            typeMap.put(id, new HashSet<>());
            final Set<AttributeSet> candidates = findAttributeSetContainingId(id, attributeMap);


            for (AttributeSet candidate : candidates) {
                typeMap.get(id).add(candidate.getName());
            }
        }

        return typeMap;
    }

    /**
     * Searches the {@code AttributeSetManager} for AttributeSets that use the supplied string as an identifier.
     * <p/>
     * The supplied String is used to match Attributes that:
     * <ol>
     *     <li>have a value equal to the string, <em>AND</em></li>
     *     <li>have a name that ends with "-ResourceId" (see {@link #IDENTIFIER_ATTR_NAME})</li>
     * </ol>
     * If the Attribute satisfies these criteria, then the AttributeSet it belongs to is returned in the results.
     *
     * @param attributeMap the AttributeSetManager containing the AttributeSets to search
     * @param s a String that may be an identifier of a business object
     * @return AttributeSets that use the supplied string as an identifier
     */
    static Set<AttributeSet> findAttributeSetContainingId(final String s, Map<String, AttributeSet> attributeMap) {
        Set<AttributeSet> matches = new HashSet<>();
        // Match any Attribute Set that has the identifier of the Business Object as an Attribute value
        for (AttributeSet candidate : attributeMap.values()) {
            for (Attribute candidateAttr : candidate.getAttributes()) {
                if (candidateAttr.getValue().equals(s)) {
                    if (candidateAttr.getName().endsWith("ResourceId")) {
                        matches.add(candidate);
                    }
                }
            }
        }
        return matches;
    }

    /**
     * Searches for AttributeSets that contain {@code aggregatingRelationship}, then verifies that the Subject and
     * Object AttributeSets have the correct type.
     *
     * @param aggregatingType the type of AttributeSet that is the aggregator (i.e., the Subject)
     * @param aggregatedType the type of AttributeSet that is being aggregated (i.e. the Object)
     * @param aggregatingRelationship the relationship that relates the Subject to the Object
     * @param attributeMap the AttributeSetManager, used to look up the relationships
     * @param errors a map containing errors generated when checking aggregations
     */
    @Override
    protected void checkAggregation(String aggregatingType, String aggregatedType, String aggregatingRelationship,
                                    Map<String, AttributeSet> attributeMap, List<String> errors) {
        for (AttributeSet aggregatingAs : matchAttribute(attributeMap, new AttributeImpl(aggregatingRelationship, null, null))) {

            // Using the name of the AttributeSet as a proxy for type, check to insure that the aggregating AttributeSet
            // has the expected type
            if (!aggregatingAs.getName().equals(aggregatingType)) {
                errors.add(String.format(INCORRECT_SUBJECT_TYPE, aggregatingAs.getName(), "unknown",
                        aggregatingRelationship, aggregatedType, aggregatingAs.getName()));
            }

            // Retrieve a Map of ResourceIds to types
            Map<String, Set<String>> typeMap = types(values(aggregatingAs, aggregatingRelationship), attributeMap);
            checkTypes(null, aggregatingAs, aggregatingRelationship, typeMap, aggregatedType);
        }
    }
}
