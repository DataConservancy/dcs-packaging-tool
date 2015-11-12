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

package org.dataconservancy.packaging.tool.ser;

import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ser.Serialize;
import org.dataconservancy.packaging.tool.model.ser.StreamId;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SerializationAnnotationUtil {

    /**
     * Answers a {@code Map} of {@link PropertyDescriptor} instances, which are used to reflectively access the
     * {@link Serialize serializable} streams on {@code annotatedClass} instances.
     * <p>
     * Use of {@code PropertyDescriptor} is simply a convenience in lieu of the use of underlying Java reflection.
     * </p>
     * <p>
     * This method looks for fields annotated by the {@code Serialize} annotation on the {@code annotatedClass}.
     * A {@code PropertyDescriptor} is created for each field, and is keyed by the {@code StreamId} in the returned
     * {@code Map}.
     * </p>
     *
     * @param annotatedClass the class to scan for the presense of {@code @Serialize}
     * @return a Map of PropertyDescriptors keyed by their StreamId.
     */
    public static Map<StreamId, PropertyDescriptor> getStreamDescriptors(Class annotatedClass) {
        HashMap<StreamId, PropertyDescriptor> results = new HashMap<>();

        Arrays.stream(annotatedClass.getDeclaredFields())
                .filter(candidateField -> AnnotationUtils.getAnnotation(candidateField, Serialize.class) != null)
                .forEach(annotatedField -> {
                    AnnotationAttributes attributes = AnnotationUtils.getAnnotationAttributes(annotatedField,
                            AnnotationUtils.getAnnotation(annotatedField, Serialize.class));
                    StreamId streamId = (StreamId) attributes.get("streamId");
                    PropertyDescriptor descriptor =
                            BeanUtils.getPropertyDescriptor(PackageState.class, annotatedField.getName());
                    results.put(streamId, descriptor);
                });

        return results;
    }
}
