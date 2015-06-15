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
 * Utility class for creating Solr queries.
 */
public class SolrQueryUtil {

    private static String createLiteralQuery(StringBuilder sb, String field,
            String s) {
        if (field != null) {
            sb.append(field);
            sb.append(':');
        }

        sb.append("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '&' || c == '|' || c == '(' || c == ')' || c == '}'
                    || c == '{' || c == '[' || c == ']' || c == ':' || c == '^'
                    || c == '!' || c == '\"' || c == '+' || c == '-'
                    || c == '~' || c == '*' || c == '?' || c == '\\') {
                sb.append('\\');
            }

            sb.append(c);
        }
        sb.append("\"");

        return sb.toString();
    }

    /**
     * Return a solr query as a list of terms joined by the specified operation
     * such that each term literally matches the given string in the given
     * field.
     * @param op the operation
     * @param args the arguments to the operation
     * @return a solr query  as a list of terms joined by the specified operation
     *   such that each term literally matches the given string in the given field
     */
    public static String createLiteralQuery(String op, String... args) {
        StringBuilder sb = new StringBuilder();

        if (args.length == 0 || (args.length & 1) > 0) {
            throw new IllegalArgumentException(
                    "Arguments must be (field, string)+");
        }

        sb.append('(');

        for (int i = 0; i < args.length;) {
            String field = args[i++];
            String string = args[i++];

            createLiteralQuery(sb, field, string);

            if (i < args.length) {
                sb.append(' ');
                sb.append(op);
                sb.append(' ');
            }
        }

        sb.append(')');

        return sb.toString();
    }

    /**
     * Return a solr query that exactly matches a string in a field.
     * @param field the supplied field
     * @param string the string to be matched
     * @return  a solr query that exactly matches a string in the field
     */
    public static String createLiteralQuery(String field, String string) {
        StringBuilder sb = new StringBuilder();
        createLiteralQuery(sb, field, string);
        return sb.toString();
    }
}
