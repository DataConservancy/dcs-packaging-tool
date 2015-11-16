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

/**
 * Created by jrm on 11/12/15.
 */
public class FilenameValidatorTest {

    ValidatorFactory vf = new ValidatorFactory();
    Validator fnv = ValidatorFactory.getValidator(PropertyValueHint.FILE_NAME);

    @Test
    public void testBlacklistedCharacters(){
        Assert.assertFalse(fnv.isValid("has:Colon"));
        Assert.assertFalse(fnv.isValid("has\"Quote"));
        Assert.assertFalse(fnv.isValid("has*Asterisk"));
        Assert.assertFalse(fnv.isValid("has/Solidus"));
        Assert.assertFalse(fnv.isValid("has<LessThan"));
        Assert.assertFalse(fnv.isValid("has>GreaterThan"));
        Assert.assertFalse(fnv.isValid("has\\ReverseSolus"));
        Assert.assertFalse(fnv.isValid("has|VerticalLine"));
        Assert.assertFalse(fnv.isValid("has~Tilde"));
    }

    @Test
    public void testFileNameTooLong(){
        Assert.assertFalse(fnv.isValid("12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "1234567890123456" ));      //256 characters
    }

    @Test
    public void testFileNameOKLength(){
        Assert.assertTrue(fnv.isValid("12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "123456789012345" ));      //255 characters
    }

    @Test
    public void testFunkyCharactersNotOK(){
        Assert.assertFalse(fnv.isValid("FileNameOKExceptFor页"));
    }

    @Test
    public void testReservedNames(){
        Assert.assertFalse(fnv.isValid("COM1"));
        Assert.assertFalse(fnv.isValid("COM1.txt"));
        Assert.assertTrue(fnv.isValid("COMPACT"));
        Assert.assertFalse(fnv.isValid("LPT1"));
        Assert.assertTrue(fnv.isValid("LPT0"));
        Assert.assertTrue(fnv.isValid("COM10"));
    }

    @Test
    public void testBadComponents(){
        Assert.assertFalse(fnv.isValid("."));
        Assert.assertFalse(fnv.isValid(".."));
        Assert.assertFalse(fnv.isValid("~"));
    }

}
