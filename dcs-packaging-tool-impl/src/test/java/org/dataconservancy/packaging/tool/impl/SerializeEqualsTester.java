/*
 *
 *  * Copyright 2015 Johns Hopkins University
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.dataconservancy.packaging.tool.impl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.dataconservancy.packaging.tool.model.ser.Serialize;
import org.dataconservancy.packaging.tool.model.ser.StreamId;
import org.dataconservancy.packaging.tool.ser.SerializationAnnotationUtil;

import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Performs equality checks on two objects containing {@link Serialize} annotations.
 */
public class SerializeEqualsTester {

    private static final String ERR_UNEQUAL_STREAMS =
            "Unequal number of streams present: %s %s streams, %s %s streams.";

    private static final String ERR_MISSING_STREAM = "Missing streamId %s in %s.";

    private static final String CLASS_AND_STREAMS = "[%s]: %s";

    /**
     * This method considers two objects equal if each object contains the same stream identifiers, and if the
     * fields containing those stream identifiers are equal according to {@link Object#equals(Object)}.  If the
     * field is a <em>Jena {@code Model}</em>, then this method will use {@link Model#isIsomorphicWith(Model)} to
     * determine equality, instead of {@link Object#equals(Object)}.
     *
     * @param one an Object containing fields annotated with {@code @Serialize}
     * @param two an Object containing fields annotated with {@code @Serialize}
     * @return true if the two objects contain the same streams with the same content
     */
    public static boolean serializeEquals(Object one, Object two) {

        Map<StreamId, PropertyDescriptor> streamsOne = SerializationAnnotationUtil.getStreamDescriptors(one.getClass());
        Map<StreamId, PropertyDescriptor> streamsTwo = SerializationAnnotationUtil.getStreamDescriptors(two.getClass());

        if (streamsOne.size() != streamsTwo.size()) {
            fail(String.format(ERR_UNEQUAL_STREAMS, one.getClass(),
                    streamsOne.size(), two.getClass(), streamsTwo.size()) + "  " +
                String.format(CLASS_AND_STREAMS, one.getClass(), streamsOne.keySet().stream().map(Enum::name)
                    .collect(Collectors.joining(", "))) + " " +
                String.format(CLASS_AND_STREAMS, two.getClass(), streamsTwo.keySet().stream().map(Enum::name)
                    .collect(Collectors.joining(", "))));
        }

        streamsOne.forEach((streamId, pd) -> {
            if (!streamsTwo.containsKey(streamId)) {
                fail(String.format(ERR_MISSING_STREAM, streamId.name(), two.getClass()) + "  " +
                    String.format(CLASS_AND_STREAMS, one.getClass(), streamsOne.keySet().stream().map(Enum::name)
                            .collect(Collectors.joining(", "))) + " " +
                    String.format(CLASS_AND_STREAMS, two.getClass(), streamsTwo.keySet().stream().map(Enum::name)
                            .collect(Collectors.joining(", "))));
            }

            Object fieldOne = null;
            Object fieldTwo = null;
            try {
                fieldOne = pd.getReadMethod().invoke(one);
                fieldTwo = streamsTwo.get(streamId).getReadMethod().invoke(two);
            } catch (Exception e) {
                fail(e.getMessage());
            }

            if (fieldOne == null) {
                assertNull("Field " + pd.getName() + " from the first object was null, but it was not null for the" +
                        "second object.", fieldTwo);
                return;
            }

            assertNotNull("Field " + pd.getName() + " from the first object was not null, but it was null for the" +
                    " second object.", fieldTwo);

            if (Model.class.isAssignableFrom(fieldOne.getClass()) &&
                    Model.class.isAssignableFrom(fieldTwo.getClass())) {
                assertModelEquals((Model) fieldOne, (Model) fieldTwo);
            } else {
                assertEquals(fieldOne, fieldTwo);
            }
        });


        return true;
    }

    /**
     * Models are considered equal if the two Models are isomorphic.  {@link Statement#equals(Object) Statement
     * equality} isn't sufficient, because anonymous resources may have different ids between models.
     *
     * @param one the first Model
     * @param two the second Model
     */
    public static void assertModelEquals(Model one, Model two) {
        assertTrue(one.isIsomorphicWith(two));
        assertTrue(two.isIsomorphicWith(one));
    }

}
