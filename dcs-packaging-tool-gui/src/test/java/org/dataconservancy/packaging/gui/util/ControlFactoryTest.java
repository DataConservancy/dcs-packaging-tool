package org.dataconservancy.packaging.gui.util;

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


import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class to verify proper construction of Controls
 */
public class ControlFactoryTest {
    private String initialValue="Some Text";

    @Test
    public void testCreateComboBox(){
        Control control = ControlFactory.createControl(ControlType.COMBO_BOX, null, null);
        Assert.assertTrue(control instanceof ComboBox);
        Assert.assertTrue(ControlFactory.textPrefWidth == control.getPrefWidth());
        Assert.assertFalse(((ComboBox)control).editableProperty().getValue());
    }

    @Test
    public void testCreateEditableComboBox(){
        Control control = ControlFactory.createControl(ControlType.EDITABLE_COMBO_BOX, null, null);
        Assert.assertTrue(control instanceof ComboBox);
        Assert.assertTrue(ControlFactory.textPrefWidth == control.getPrefWidth());
        Assert.assertTrue(((ComboBox)control).editableProperty().getValue());
    }
    @Test
    public void testCreateTextAreaNoText(){
        Control control = ControlFactory.createControl(ControlType.TEXT_AREA, null, null);
        Assert.assertTrue(control instanceof TextArea);
        Assert.assertTrue(ControlFactory.textPrefWidth == control.getPrefWidth());
    }

    @Test
    public void testCreateTextAreaWithText(){
        Control control = ControlFactory.createControl(ControlType.TEXT_AREA,initialValue, null);
        Assert.assertTrue(control instanceof TextArea);
        Assert.assertEquals(initialValue, ((TextArea)control).getText());
        Assert.assertTrue(ControlFactory.textPrefWidth == control.getPrefWidth());
    }

    @Test
    public void testCreateTextFieldNoText(){
        Control control = ControlFactory.createControl(ControlType.TEXT_FIELD, null, null);
        Assert.assertTrue(control instanceof TextField);
        Assert.assertTrue(ControlFactory.textPrefWidth == control.getPrefWidth());
    }

    @Test
    public void testCreateTextFieldWithText(){
        Control control = ControlFactory.createControl(ControlType.TEXT_FIELD, initialValue, null);
        Assert.assertTrue(control instanceof TextField);
         Assert.assertEquals(initialValue, ((TextField) control).getText());
        Assert.assertTrue(ControlFactory.textPrefWidth == control.getPrefWidth());
    }

    @Test
    public void testCreateDatePicker(){
        Control control = ControlFactory.createControl(ControlType.DATE_PICKER, initialValue, null);
        Assert.assertTrue(control instanceof DatePicker);
        Assert.assertTrue(ControlFactory.textPrefWidth == control.getPrefWidth());
    }

    @Test
    public void testCreateTextFieldWRemovableLabel(){
        Control control = ControlFactory.createControl("Type and enter", "Some help", new VBox(), ControlType.TEXT_FIELD_W_REMOVABLE_LABEL);
        Assert.assertTrue(control instanceof TextField);
        Assert.assertTrue(ControlFactory.textPrefWidth == control.getPrefWidth());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadCreate(){
         ControlFactory.createControl(null, initialValue, null);
    }
}
