package org.dataconservancy.packaging.tool.model.dprofile;

/**
 * A human readable label and description.
 */
public interface HasDescription {
    /**
     * @return Short label for something.
     */
    String getLabel();

    /**
     * @return Description of something.
     */
    String getDescription();
}
