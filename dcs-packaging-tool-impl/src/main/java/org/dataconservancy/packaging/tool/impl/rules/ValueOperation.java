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

/**
 * Returns a String value based on some criteria
 * <p>
 * Value operations are evaluated in the context of a file, and return a String
 * based on some aspect of the file (file metadata), or other factor (literal,
 * environment variable, etc) that has nothing to do with the file in context.
 * </p>
 * <p>
 * If additional FileOperation constraints are given, the return value(s) will
 * be determined by the Files returned by these operations rather than the
 * supplied File context.
 * </p>
 */
public interface ValueOperation
        extends Operation<String, FileOperation> {

    /**
     * Specify the nature of the value to return.
     * <p>
     * The meaning of the specifier depends on implementation. For example, a
     * specifier may name a certain file metadata attribute to return, or may
     * name an environment variable.
     * </p>
     * 
     * @param spec Specifier.
     */
    public void setSpecifier(String spec);

}
