package org.dataconservancy.packaging.tool.impl;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.junit.experimental.categories.Category;

/**
 * Marker interface for tests that may require escalated privileges on certain
 * hosts.
 * <p>
 * Certain operations such as <a href=
 * "https://social.msdn.microsoft.com/Forums/en-US/windowssdk/thread/fa504848-a5ea-4e84-99b7-0eb4e469cbef?outputAs=rss">
 * symbolic linking</a> may be disabled by default some operating systems. Junit
 * tests marked with a {@link Category} of Privileged may require such
 * privileges.
 * </p>
 * 
 * @author apb
 *
 */
public interface Privileged {

}
