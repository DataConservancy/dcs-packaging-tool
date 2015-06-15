/*
 * Copyright 2012 Johns Hopkins University
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

package org.dataconservancy.dcs.util;

/**
 * {@code MimeTypeComparator} supports comparison of content mime types, taking in consideration wild cards. It is used
 * by {@code Controllers} to determine appropriate responses to HTTP requests.
 */
public class MimeTypeComparator {
    public static final String WILD_CARD_MIME_TYPE = "*/*";
    public static final String WILD_CARD = "*";

    /**
     * Determine whether a mime type is acceptable given the expected mime type. The expected and actual mime types are
     * compared based on their type and subtype parts. Additional parameters in mime type, such as charset, is ignored.
     * A specific mime type is considered acceptable against an expected mime type if:
     * <ul>
     * <li>{@code acceptedMimeType} is a wild card string *&#47;* which allows any type and subtypes to be a match </li>
     * <li>{@code acceptedMimeType} string and the {@code actualMimeType} string are identical</li>
     * <li>{@code acceptedMimeType} string and the {@code actualMimeType} string has identical type part, and the
     * {@code acceptedMimeType} has a wild card for its subtype.</li>
     * </ul>
     *
     * @param acceptedMimeTypes String describing mime types that are acceptable. This field could contain wildcards.
     * @param actualMimeType   String describing the mime type to be tested.
     * @return {@code true} if actualMimeType is acceptable
     *         {@code false} if actualMimeType is not acceptable
     */
    public static boolean isAcceptableMimeType(String acceptedMimeTypes, String actualMimeType) {
        if (acceptedMimeTypes == null || actualMimeType == null) {
            throw new IllegalArgumentException("Cannot compare null mime types.");
        }

        String [] multipleAcceptedTypes = acceptedMimeTypes.split(",");
        actualMimeType = actualMimeType.split(";")[0].toLowerCase().trim();
        for (String acceptedMimeType : multipleAcceptedTypes) {
            //Strip acceptedMimeType and actualMimeType of additional type parameter
            acceptedMimeType = acceptedMimeType.split(";")[0].toLowerCase().trim();
                if (acceptedMimeType.equals(WILD_CARD_MIME_TYPE)) {
                return true;
            } else if (acceptedMimeType.equals(actualMimeType)) {
                return true;
            } else if (getMainType(acceptedMimeType).equals(getMainType(actualMimeType))
                    && getSubType(acceptedMimeType).equals(WILD_CARD)) {
                return true;
            }
        }
        return false;
    }

    private static String getMainType(String mimeType) {
        return mimeType.split("/")[0].trim();
    }

    private static String getSubType(String mimeType) {
        return mimeType.split("/")[1].trim();

    }
}
