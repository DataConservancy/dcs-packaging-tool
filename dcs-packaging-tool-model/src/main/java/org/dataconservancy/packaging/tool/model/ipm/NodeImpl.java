package org.dataconservancy.packaging.tool.model.ipm;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class NodeImpl implements Node {

    private URI identifier;
    private Node parent;
    private List<Node> children;
    private URI domainObject;
    private FileInfo fileInfo;
    private URI nodeType;
    private boolean isIgnored;

    @Override
    public URI getIdentifier() {
        return identifier;
    }

    @Override
    public void setIdentifier(URI id) {
        this.identifier = id;
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public List<Node> getChildren() {
        return children;
    }

    @Override
    public void addChild(Node node) {
        if (children == null) {
            children = new ArrayList<>();
        }

        children.add(node);
    }

    @Override
    public void removeChild(Node node) {
        if (children != null) {
            children.remove(node);
        }
    }

    @Override
    public URI getDomainObject() {
        return domainObject;
    }

    @Override
    public void setDomainObject(URI id) {
        this.domainObject = id;
    }

    @Override
    public FileInfo getFileInfo() {
        return fileInfo;
    }

    @Override
    public void setFileInfo(FileInfo info) {
        this.fileInfo = info;
    }

    @Override
    public URI getNodeType() {
        return nodeType;
    }

    @Override
    public void setNodeType(URI type) {
        this.nodeType = type;
    }

    @Override
    public boolean isIgnored() {
        return isIgnored;
    }

    @Override
    public void setIgnored(boolean status) {
        this.isIgnored = status;
    }

    @Override
    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }
}
