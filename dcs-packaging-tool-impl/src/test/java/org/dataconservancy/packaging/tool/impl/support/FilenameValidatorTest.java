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

package org.dataconservancy.packaging.tool.impl.support;

import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueHint;
import org.junit.Assert;
import org.junit.Test;

public class FilenameValidatorTest {

    Validator fnv = ValidatorFactory.getValidator(PropertyValueHint.FILE_NAME);

    @Test
    public void testBlacklistedCharacters(){
        Assert.assertFalse(fnv.isValid("has:Colon").getResult());
        Assert.assertFalse(fnv.isValid("has\"Quote").getResult());
        Assert.assertFalse(fnv.isValid("has*Asterisk").getResult());
        Assert.assertFalse(fnv.isValid("has/Solidus").getResult());
        Assert.assertFalse(fnv.isValid("has<LessThan").getResult());
        Assert.assertFalse(fnv.isValid("has>GreaterThan").getResult());
        Assert.assertFalse(fnv.isValid("has\\ReverseSolus").getResult());
        Assert.assertFalse(fnv.isValid("has|VerticalLine").getResult());
        Assert.assertFalse(fnv.isValid("has~Tilde").getResult());
    }

    @Test
    public void testFileNameTooLong(){
        Assert.assertFalse(fnv.isValid("12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "1234567890123456" ).getResult());      //256 characters
    }

    @Test
    public void testFileNameOKLength(){
        Assert.assertTrue(fnv.isValid("12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "123456789012345" ).getResult());      //255 characters
    }

    @Test
    public void testFunkyCharactersNotOK(){
        Assert.assertFalse(fnv.isValid("FileNameOKExceptFor页").getResult());
    }

    @Test
    public void testReservedNames(){
        Assert.assertFalse(fnv.isValid("COM1").getResult());
        Assert.assertFalse(fnv.isValid("COM1.txt").getResult());
        Assert.assertTrue(fnv.isValid("COMPACT").getResult());
        Assert.assertFalse(fnv.isValid("LPT1").getResult());
        Assert.assertTrue(fnv.isValid("LPT0").getResult());
        Assert.assertTrue(fnv.isValid("COM10").getResult());
    }

    @Test
    public void testBadComponents(){
        Assert.assertFalse(fnv.isValid(".").getResult());
        Assert.assertFalse(fnv.isValid("..").getResult());
        Assert.assertFalse(fnv.isValid("~").getResult());
    }

}
