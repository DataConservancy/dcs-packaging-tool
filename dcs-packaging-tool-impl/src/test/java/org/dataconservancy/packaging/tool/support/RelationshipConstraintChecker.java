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

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.dcs.model.AttributeSetName;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.dataconservancy.dcs.model.AttributeSetName.ORE_REM_COLLECTION;
import static org.dataconservancy.dcs.model.AttributeSetName.ORE_REM_DATAITEM;
import static org.dataconservancy.dcs.model.AttributeSetName.ORE_REM_PACKAGE;
import static org.dataconservancy.dcs.model.Metadata.COLLECTION_AGGREGATES_DATAITEM;
import static org.dataconservancy.dcs.model.Metadata.COLLECTION_RESOURCEID;
import static org.dataconservancy.dcs.model.Metadata.DATA_ITEM_IS_PART_OF_COLLECTION;
import static org.dataconservancy.dcs.model.Metadata.PACKAGE_AGGREGATES_DATAITEM;
import static org.dataconservancy.dcs.model.Metadata.PACKAGE_RESOURCEID;

/**
 * Checks the following constraints:
 * <ul>
 * <li>Data Items in the ORE ReM may be aggregated by a Collection or the Package in the same ReM.  Data Items
 * aggregated by Collections <em>must not</em> reference another Collection using the 'isPartOf' relationship.
 * Data Items aggregated by the Package <em>must</em> reference an existing Collection in a Data Conservancy
 * instance using the 'isPartOf' relationship; exactly one of those relationships must be present.</li>
 * </ul>
 *
 */
public class RelationshipConstraintChecker extends BaseValidationChecker {

    private static final String MISSING_IS_PART_OF = "DataItem (id '%s') aggregated by Package (id '%s') is missing " +
            "a " + DATA_ITEM_IS_PART_OF_COLLECTION + " attribute.  DataItems aggregated by a Package must reference " +
            "an existing Collection.";

    private static final String MULTIPLE_IS_PART_OF = "DataItem (id '%s') aggregated by Collection (id '%s') has " +
            "multiple " + DATA_ITEM_IS_PART_OF_COLLECTION + " attributes: %s.  DataItems aggregated by a Package must " +
            "reference single Collection.";

    private static final String HAS_IS_PART_OF = "DataItem (id '%s') aggregated by Collection (id '%s') has an " +
            "illegal value (or values) for the " + DATA_ITEM_IS_PART_OF_COLLECTION + " attribute: %s.  DataItems " +
            "aggregated by a Collection must not reference any other Collections using " +
            DATA_ITEM_IS_PART_OF_COLLECTION + ".";

    public RelationshipConstraintChecker() {

    }

    public RelationshipConstraintChecker(List<String> errors) {
        super(errors);
    }

    @Override
    protected void checkAggregation(String aggregatingType, String aggregatedType, String aggregatingRelationship,
                                    Map<String, AttributeSet> attributeMap, List<String> errors) {

        if (aggregatedType.equals(ORE_REM_DATAITEM)) {
            // DataItems may be aggregated by a Package or a Collection
            // DataItems aggregated by the Package must have an 'isPartOf' relationship
            // DataItems aggregated by the Collection must not have an 'isPartOf' relationship

            switch (aggregatingType) {
                case ORE_REM_PACKAGE:
                    final Set<AttributeSet> packagesAs = matchAttributeSetName(attributeMap, AttributeSetName.ORE_REM_PACKAGE);
                    AttributeSet packageAs = packagesAs.iterator().next();
                    for (String dataItemResourceId : values(packageAs, PACKAGE_AGGREGATES_DATAITEM)) {
                        AttributeSet dataItemAs = attributeMap.get(composeKey(ORE_REM_DATAITEM, dataItemResourceId));
                        final Collection<Attribute> collections = dataItemAs.getAttributesByName(DATA_ITEM_IS_PART_OF_COLLECTION);
                        if (collections == null || collections.isEmpty()) {
                            errors.add(String.format(MISSING_IS_PART_OF, dataItemResourceId, values(packageAs, PACKAGE_RESOURCEID).iterator().next()));
                        } else if (collections.size() != 1) {
                            errors.add(String.format(MULTIPLE_IS_PART_OF, dataItemResourceId, values(packageAs, PACKAGE_RESOURCEID).iterator().next(), concatAttrValues(collections)));
                        }
                    }
                    break;
                case ORE_REM_COLLECTION:
                    Set<AttributeSet> collectionsAs = matchAttributeSetName(attributeMap, AttributeSetName.ORE_REM_COLLECTION);
                    for (AttributeSet collectionAs : collectionsAs) {
                        final String collectionResourceId = values(collectionAs, COLLECTION_RESOURCEID).iterator().next();
                        for (String dataItemResourceId : values(collectionAs, COLLECTION_AGGREGATES_DATAITEM)) {
                            AttributeSet dataItemAs = attributeMap.get(composeKey(ORE_REM_DATAITEM, dataItemResourceId));
                            final Collection<Attribute> collections = dataItemAs.getAttributesByName(DATA_ITEM_IS_PART_OF_COLLECTION);
                            if (collections == null || collections.isEmpty()) {
                                continue;
                            }

                            if (collections.size() != 1) {
                                errors.add(String.format(HAS_IS_PART_OF, dataItemResourceId, values(collectionAs, COLLECTION_RESOURCEID).iterator().next(), concatAttrValues(collections)));
                            }

                            if (collections.size() == 1) {
                                if (!collectionResourceId.equals(collections.iterator().next().getValue())) {
                                    errors.add(String.format(HAS_IS_PART_OF, dataItemResourceId, values(collectionAs, COLLECTION_RESOURCEID).iterator().next(), concatAttrValues(collections)));
                                }
                            }
                        }
                    }
                    break;
                default:
                    errors.add(
                        "DataItem should only be aggregated by Collections or a Package; found " +
                            aggregatingType);
                    break;
            }
        }
    }

    /**
     * Concatenates the attribute value present in {@code collections} with a ', ' character sequence.
     *
     * @param collections the collection of attributes to concat
     * @return a string of attribute values concatenated with ', '
     */
    private String concatAttrValues(Collection<Attribute> collections) {
        StringBuilder isPartOfAttrs = new StringBuilder();
        Iterator<Attribute> itr = collections.iterator();
        while (itr.hasNext()) {
            isPartOfAttrs.append(itr.next().getValue());
            if (itr.hasNext()) {
                isPartOfAttrs.append(", ");
            }
        }
        return isPartOfAttrs.toString();
    }

}
