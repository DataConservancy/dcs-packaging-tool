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

package org.dataconservancy.packaging.gui.util;

import javafx.scene.control.TextField;
import org.apache.commons.lang.StringUtils;

/**
 * Class that extends TextField but only allow numeric values to be entered.
 */
public class NumericTextField extends TextField
{
    public NumericTextField(String initialValue) {
        super();
        if (validate(initialValue)) {
            super.setText(initialValue);
        }
    }

    public NumericTextField() {
        super();
    }

    @Override
    public void replaceText(int start, int end, String text)
    {
        if (validate(text))
        {
            super.replaceText(start, end, text);
        }
    }

    @Override
    public void replaceSelection(String text)
    {
        if (validate(text))
        {
            super.replaceSelection(text);
        }
    }

    private boolean validate(String text)
    {
        //Only allow numeric values or backspaces/deletes
        return StringUtils.isNumeric(text) || text.isEmpty();
    }
}