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



import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class MimeTypeComparatorTest {
	private static final String WILDCARD_MIMETYPE = "*/*";
	private static final String WILDCARD_SUBTYPE = "application/*";
	private static final String SPECIFIC_MIMETYPE= "image/jpeg";
    private static final String COMMON_ACCEPT_HEADER = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

	@Test
	public void testAcceptableMimeType() {
		assertTrue(MimeTypeComparator.isAcceptableMimeType(WILDCARD_MIMETYPE, "image/png"));
		assertTrue(MimeTypeComparator.isAcceptableMimeType(WILDCARD_SUBTYPE, "application/octet-stream"));
		assertTrue(MimeTypeComparator.isAcceptableMimeType(SPECIFIC_MIMETYPE, "image/jpeg"));
		assertTrue(MimeTypeComparator.isAcceptableMimeType(COMMON_ACCEPT_HEADER, "image/jpeg"));
	}
	
	@Test
	public void testUnacceptableMimeType() {
		assertTrue(!MimeTypeComparator.isAcceptableMimeType(WILDCARD_SUBTYPE, "image/png"));
		assertTrue(!MimeTypeComparator.isAcceptableMimeType(SPECIFIC_MIMETYPE, "image/png"));
	}
}
