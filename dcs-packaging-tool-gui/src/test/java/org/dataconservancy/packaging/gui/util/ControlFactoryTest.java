package org.dataconservancy.packaging.gui.util;

import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jrm on 9/14/15.
 */
public class ControlFactoryTest {

    @Test
    public void testCreateTextAreaNoText(){
        Control control = ControlFactory.createControl(ControlType.TEXT_AREA, null);
        Assert.assertTrue(control instanceof TextArea);
        Assert.assertTrue(ControlFactory.textPrefWidth == control.getPrefWidth());
    }

    @Test
    public void testCreateTextAreaWithText(){
        Control control = ControlFactory.createControl(ControlType.TEXT_AREA, "Text Area");
        Assert.assertTrue(control instanceof TextArea);
        Assert.assertTrue(ControlFactory.textPrefWidth == control.getPrefWidth());
    }

    @Test
    public void testCreateTextFieldNoText(){
        Control control = ControlFactory.createControl(ControlType.TEXT_FIELD, null);
        Assert.assertTrue(control instanceof TextField);
        Assert.assertTrue(ControlFactory.textPrefWidth == control.getPrefWidth());
    }

    @Test
    public void testCreateTextFieldWithText(){
        Control control = ControlFactory.createControl(ControlType.TEXT_FIELD, "Text Area");
        Assert.assertTrue(control instanceof TextField);
        Assert.assertTrue(ControlFactory.textPrefWidth == control.getPrefWidth());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadCreate(){
         Control control = ControlFactory.createControl(null, "Text Area");
    }
}
