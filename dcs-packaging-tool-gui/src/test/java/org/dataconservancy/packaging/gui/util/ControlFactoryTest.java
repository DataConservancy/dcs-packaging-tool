package org.dataconservancy.packaging.gui.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

    @Test(expected=IllegalArgumentException.class)
    public void testBadCreate(){
         Control control = ControlFactory.createControl(null, initialValue, null);
    }
}
