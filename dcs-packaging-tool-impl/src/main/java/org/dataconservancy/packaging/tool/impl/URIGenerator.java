package org.dataconservancy.packaging.tool.impl;

import java.net.URI;

import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Generate URIs for objects.
 */
public interface URIGenerator {
    /**
     * @param node
     *            Node holding domain object to create identifier for.
     * @return Unique identifier for a domain object with this node.
     */
    URI generateDomainObjectURI(Node node);

    /**
     * @return Unique identifier for a URI.
     */
    URI generateNodeURI();
}
