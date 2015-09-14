package org.dataconservancy.packaging.gui.util;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jrm on 9/14/15.
 */
public class ControlFactoryTest {

    @Test
    public void testCreateTextAreaNoText(){
        TextInputControl control = ControlFactory.createControl(ControlType.TEXT_AREA, null);
        Assert.assertTrue(control instanceof TextArea);
    }

    @Test
    public void testCreateTextAreaWithText(){
        TextInputControl control = ControlFactory.createControl(ControlType.TEXT_AREA, "Text Area");
        Assert.assertTrue(control instanceof TextArea);
    }

    @Test
    public void testCreateTextFieldNoText(){
        TextInputControl control = ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        Assert.assertTrue(control instanceof TextField);
    }

    @Test
    public void testCreateTextFieldWithText(){
        TextInputControl control = ControlFactory.createControl(ControlType.TEXT_FIELD, "Text Area");
        Assert.assertTrue(control instanceof TextField);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadCreate(){
         TextInputControl control = ControlFactory.createControl(null, "Text Area");
    }
}
