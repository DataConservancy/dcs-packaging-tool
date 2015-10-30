package org.dataconservancy.packaging.tool.impl;

import java.net.URI;
import java.util.UUID;

import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Generate opaque URIs using random UUIDs.
 */
public class SimpleURIGenerator implements URIGenerator {
    private URI gen_uri() {
        return URI.create("urn:uuid:" + UUID.randomUUID().toString());
    }

    @Override
    public URI generateDomainObjectURI(Node node) {
        return gen_uri();
    }

    @Override
    public URI generateNodeURI() {
        return gen_uri();
    }
}
