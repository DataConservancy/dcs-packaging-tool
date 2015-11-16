package org.dataconservancy.packaging.tool.impl.support;

import java.net.URI;
import java.net.URISyntaxException;

public class URIValidator implements Validator {

    @Override
    public boolean isValid(String string) {
        try {
            URI uri = new URI(string);
            if (uri.getScheme() == null || uri.getScheme().isEmpty()) {
                return false;
            } else if (uri.getSchemeSpecificPart() == null || uri.getSchemeSpecificPart().isEmpty()) {
                return false;
            }
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
