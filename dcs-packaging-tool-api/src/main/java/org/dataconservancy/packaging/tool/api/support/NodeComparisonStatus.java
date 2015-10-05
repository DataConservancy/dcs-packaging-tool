package org.dataconservancy.packaging.tool.api.support;

/**
 * Enum for storing the status of node comparisons when trees are compared.
 * //TODO: This can likely be moved into some service instead of living on it's own.
 */
public enum NodeComparisonStatus {
    ADDED,
    DELETED,
    UPDATED,
    UNCHANGED

}
