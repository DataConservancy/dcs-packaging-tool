package org.dataconservancy.packaging.gui;

import java.util.ResourceBundle;

/**
 * Convenience class for getting text from the resource bundles. This class manages all different classes of resources
 * with convenient methods for returning strings.
 */
public class TextFactory {

    private Labels labels;
    private Messages messages;
    private Errors errors;
    private Help help;

    private static TextFactory instance;

    private TextFactory() {
        ResourceBundle labelsResource = ResourceBundle.getBundle("bundles/labels");
        labels = new Labels(labelsResource);

        ResourceBundle messagesResource = ResourceBundle.getBundle("bundles/messages");
        messages = new Messages(messagesResource);

        ResourceBundle errorResource = ResourceBundle.getBundle("bundles/errors");
        errors = new Errors(errorResource);

        ResourceBundle helpResource = ResourceBundle.getBundle("bundles/help");
        help = new Help(helpResource);
    }

    /**
     * Gets the resource string associated with the provided Key.
     * @param key The key of the resource string to retrieve.
     * @return The string that corresponds to the provided resource key, or an empty string if the resource key is not found.
     */
    public static String getText(Enum key) {
        if (instance == null) {
            instance = new TextFactory();
        }

        if (key instanceof Labels.LabelKey) {
            return instance.labels.get((Labels.LabelKey) key);
        } else if (key instanceof Messages.MessageKey) {
            return instance.messages.get((Messages.MessageKey)key);
        } else if (key instanceof Errors.ErrorKey) {
            return instance.errors.get((Errors.ErrorKey) key);
        } else if (key instanceof Help.HelpKey) {
            return  instance.help.get((Help.HelpKey) key);
        }

        return "";
    }

    /**
     * Gets the formatted resource string associated with the provided key.
     * @param key The key of the resource string to retrieve.
     * @param args The arguments used to format the string.
     * @return The string that corresponds to the provided resource key, or an empty string if the resource key is not found.
     */
    public static String format(Enum key, Object... args) {
        if (instance == null) {
            instance = new TextFactory();
        }

        if (key instanceof Labels.LabelKey) {
            return instance.labels.format((Labels.LabelKey) key, args);
        } else if (key instanceof Messages.MessageKey) {
            return instance.messages.format((Messages.MessageKey)key, args);
        } else if (key instanceof Errors.ErrorKey) {
            return instance.errors.format((Errors.ErrorKey) key, args);
        } else if (key instanceof Help.HelpKey) {
            return instance.help.format((Help.HelpKey) key, args);
        }

        return "";
    }
}
