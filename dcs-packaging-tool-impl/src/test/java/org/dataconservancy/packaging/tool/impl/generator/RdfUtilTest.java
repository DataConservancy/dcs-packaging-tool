/*
 * Copyright 2015 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.impl.generator;

import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RdfUtilTest {

    /*
     * If all the triples are 'local' to a given subject, then we should be able
     * to cut all the triples out of that model
     */
    @Test
    public void allLocalTest() throws Exception {
        try (InputStream in =
                RdfUtilTest.class.getResourceAsStream("/TestDomainObjects/1.ttl")) {
            Model orig = ModelFactory.createDefaultModel();
            orig.read(in, null, "TTL");

            int COUNT = orig.listStatements().toSet().size();
            assertTrue(COUNT > 0);

            Model excised =
                    RdfUtil.cut(orig, RdfUtil.selectLocal(orig
                            .getResource("http://example.org/TestDomainObject")));

            assertEquals(0, orig.listStatements().toSet().size());
            assertEquals(COUNT, excised.listStatements().toSet().size());

        }
    }

    /*
     * If we select by a hash URI, it should still match all local to the URI
     * from which it was derived.
     */
    @Test
    public void singleTreeHashTest() throws Exception {
        try (InputStream in =
                RdfUtilTest.class.getResourceAsStream("/TestDomainObjects/1.ttl")) {
            Model orig = ModelFactory.createDefaultModel();
            orig.read(in, null, "TTL");

            int COUNT = orig.listStatements().toSet().size();
            assertTrue(COUNT > 0);

            Model excised =
                    RdfUtil.cut(orig,
                                RdfUtil.selectLocal(orig
                                        .getResource("http://example.org/TestDomainObject#File1")));

            assertEquals(0, orig.listStatements().toSet().size());
            assertEquals(COUNT, excised.listStatements().toSet().size());
        }
    }

    @Test
    public void disjointResourcesTest() throws Exception {
        Model orig = ModelFactory.createDefaultModel();
        int triple_count_2a;
        int triple_count_2b;

        try (InputStream in =
                RdfUtilTest.class.getResourceAsStream("/TestDomainObjects/2/2.ttl")) {
            orig.read(in, null, "TTL");
            triple_count_2a = orig.listStatements().toSet().size();
        }

        try (InputStream in =
                RdfUtilTest.class.getResourceAsStream("/TestDomainObjects/2/file.txt.ttl")) {
            orig.read(in, null, "TTL");
            triple_count_2b =
                    orig.listStatements().toSet().size() - triple_count_2a;
        }

        Model triples_2a =
                RdfUtil.cut(orig,
                            RdfUtil.selectLocal(orig
                                    .getResource("http://example.org/TestDomainObject/Directory1")));
        assertEquals(triple_count_2a, triples_2a.listStatements().toSet()
                .size());

        Model triples_2b =
                RdfUtil.cut(orig, RdfUtil.selectLocal(orig
                        .getResource("http://example.org/TestDomainObject/File1")));
        assertEquals(triple_count_2b, triples_2b.listStatements().toSet()
                .size());

        assertEquals(0, orig.listStatements().toSet().size());
    }
}
