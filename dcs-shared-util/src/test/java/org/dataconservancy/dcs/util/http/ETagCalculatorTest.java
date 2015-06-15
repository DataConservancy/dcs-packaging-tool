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
package org.dataconservancy.dcs.util.http;

import org.dataconservancy.dcs.util.http.ETagCalculator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Created by IntelliJ IDEA.
 * User: jrm
 * Date: 5/31/12
 * Time: 2:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ETagCalculatorTest {

    private ETagCalculator underTest;

    @Test
    public void testEmptyId(){

        String id = "";
        String result = underTest.calculate(id);
        assertNull(result);        
    }
    
    @Test
    public void testNonEmptyId() {

        String id = "testId";

        //output of:  echo -n "testId" | md5sum
        String md5 = "98440bc4aa1b5d404fe9423514bbbf10";
        
        String result = underTest.calculate(id);
        assertEquals(md5, result);
    }
}
