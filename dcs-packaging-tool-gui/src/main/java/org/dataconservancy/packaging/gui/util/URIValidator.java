package org.dataconservancy.packaging.gui.util;

import java.net.URI;
import java.net.URISyntaxException;

public class URIValidator implements Validator {

    @Override
    public boolean isValid(String string) {
        try {
            new URI(string);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
