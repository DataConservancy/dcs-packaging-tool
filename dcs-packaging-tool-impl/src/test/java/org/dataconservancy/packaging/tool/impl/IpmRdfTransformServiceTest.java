package org.dataconservancy.packaging.tool.impl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.dataconservancy.packaging.tool.api.DomainProfileStore;
import org.dataconservancy.packaging.tool.model.PackageResourceMapConstants;
import org.dataconservancy.packaging.tool.model.RDFTransformException;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class IpmRdfTransformServiceTest {

    private final String rootTypeId = "http://dc.org/type/root";
    private final String childTypeId = "http://dc.org/typr/child";

    private final String subTypeOneId = "http://dataconservancy.org/bo/Collection";
    private final String subTypeTwoId = "http://foo.org/interview";

    private Node root;
    private Node childOne;
    private Node childTwo;

    private NodeType rootType;
    private NodeType childType;
    private NodeType subTypeOne;
    private NodeType subTypeTwo;

    private IpmRdfTransformService transformService;

    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();

    @Before
    public void setup() throws URISyntaxException, IOException {

        File rootFolder = tmpfolder.newFolder("root");
        File childFileOne = tmpfolder.newFile("childOne");
        File childFileTwo = tmpfolder.newFile("childTwo");

        DomainProfileStore profileStore = mock(DomainProfileStore.class);

        rootType = new NodeType();
        rootType.setIdentifier(new URI(rootTypeId));

        childType = new NodeType();
        childType.setIdentifier(new URI(childTypeId));

        subTypeOne = new NodeType();
        subTypeOne.setIdentifier(new URI(subTypeOneId));

        subTypeTwo = new NodeType();
        subTypeTwo.setIdentifier(new URI(subTypeTwoId));

        doAnswer(invocation -> {
            URI uri = (URI) invocation.getArguments()[0];
            switch (uri.toString()) {
                case rootTypeId:
                    return rootType;
                case childTypeId:
                    return childType;
                case subTypeOneId:
                    return subTypeOne;
                case subTypeTwoId:
                    return subTypeTwo;
            }

            return null;
        }).when(profileStore).getNodeType(any(URI.class));

        transformService = new IpmRdfTransformService();
        transformService.setDomainProfileStore(profileStore);

        root = new Node(new URI("bag://node/1"));
        root.setNodeType(rootType);
        URI rootDomainObject = new URI("http://dataconservancy.org/bo/Collection");
        root.setDomainObject(rootDomainObject);

        FileInfo rootFileInfo = new FileInfo(rootFolder.toPath());
        root.setFileInfo(rootFileInfo);
        root.addSubNodeType(subTypeOne);
        root.setIgnored(false);

        childOne = new Node(new URI("bag://node/2"));
        childOne.setNodeType(childType);
        URI childOneDomainObject = new URI("http://dataconservancy.org/bo/File");
        childOne.setDomainObject(childOneDomainObject);

        FileInfo childOneFileInfo = new FileInfo(childFileOne.toPath());
        childOne.setFileInfo(childOneFileInfo);
        childOne.addSubNodeType(subTypeTwo);
        childOne.setIgnored(false);
        root.addChild(childOne);
        childOne.setParent(root);

        childTwo = new Node(new URI("bag://node/3"));
        childTwo.setNodeType(childType);
        URI childTwoDomainObject = new URI("http://dataconservancy.org/bo/File");
        childTwo.setDomainObject(childTwoDomainObject);

        FileInfo childTwoFileInfo = new FileInfo(childFileTwo.toPath());
        childTwo.setFileInfo(childTwoFileInfo);
        childTwo.addSubNodeType(subTypeOne);
        childTwo.setIgnored(true);
        root.addChild(childTwo);
        childTwo.setParent(root);
    }

    /**
     * Tests that a small node tree can be converted to RDF and that expected statements are present.
     * @throws RDFTransformException
     */
    @Test
    public void testToRDF() throws RDFTransformException {
        Model rootModel = transformService.transformToRDF(root);
        List<Resource> descriptionResource = rootModel.listResourcesWithProperty(RDF.type, IpmRdfTransformService.IPM_NODE_TYPE).toList();
        assertEquals(3, descriptionResource.size());

        List<Resource> rootResources = rootModel.listResourcesWithProperty(IpmRdfTransformService.IS_ROOT).toList();
        assertEquals(1, rootResources.size());

        Resource rootResource = rootResources.get(0);
        assertEquals(1, rootResource.listProperties(PackageResourceMapConstants.HAS_ID).toList().size());
        assertEquals(1, rootResource.listProperties(IpmRdfTransformService.HAS_NODE_TYPE).toList().size());
        assertEquals(1, rootResource.listProperties(IpmRdfTransformService.HAS_SUB_TYPE).toList().size());

        assertEquals(1, rootResource.listProperties(IpmRdfTransformService.IS_ROOT).toList().size());
        assertEquals(0, rootResource.listProperties(IpmRdfTransformService.HAS_PARENT).toList().size());

        List<RDFNode> rootFileInfo = rootModel.listObjectsOfProperty(rootResource, IpmRdfTransformService.HAS_FILE_INFO).toList();
        assertEquals(1, rootFileInfo.size());

        Resource rootFileInfoResource = rootFileInfo.get(0).asResource();
        assertEquals(1, rootFileInfoResource.listProperties(PackageResourceMapConstants.HAS_NAME).toList().size());
        assertEquals(1, rootFileInfoResource.listProperties(IpmRdfTransformService.HAS_LOCATION).toList().size());
        assertEquals(1, rootFileInfoResource.listProperties(IpmRdfTransformService.HAS_SIZE).toList().size());
        assertEquals(0, rootFileInfoResource.listProperties(IpmRdfTransformService.HAS_FORMAT).toList().size());
        assertEquals(0, rootFileInfoResource.listProperties(IpmRdfTransformService.HAS_SHA1_CHECKSUM).toList().size());
        assertEquals(0, rootFileInfoResource.listProperties(IpmRdfTransformService.HAS_MD5_CHECKSUM).toList().size());
        assertTrue(rootFileInfoResource.hasLiteral(IpmRdfTransformService.IS_DIRECTORY, true));
        assertTrue(rootFileInfoResource.hasLiteral(IpmRdfTransformService.IS_BYTE_STREAM, false));

        List<RDFNode> rootChildren = rootModel.listObjectsOfProperty(rootResource, IpmRdfTransformService.HAS_CHILD).toList();
        assertEquals(2, rootChildren.size());

        for (RDFNode child : rootChildren) {
            Resource childResource = child.asResource();
            assertEquals(1, childResource.listProperties(PackageResourceMapConstants.HAS_ID).toList().size());
            assertEquals(1, childResource.listProperties(IpmRdfTransformService.HAS_NODE_TYPE).toList().size());
            assertEquals(1, childResource.listProperties(IpmRdfTransformService.HAS_SUB_TYPE).toList().size());

            assertEquals(0, childResource.listProperties(IpmRdfTransformService.IS_ROOT).toList().size());
            assertEquals(1, childResource.listProperties(IpmRdfTransformService.HAS_PARENT).toList().size());

            List<RDFNode> childFileInfo = rootModel.listObjectsOfProperty(childResource, IpmRdfTransformService.HAS_FILE_INFO).toList();
            assertEquals(1, childFileInfo.size());

            Resource childFileInfoResource = childFileInfo.get(0).asResource();
            assertEquals(1, childFileInfoResource.listProperties(PackageResourceMapConstants.HAS_NAME).toList().size());
            assertEquals(1, childFileInfoResource.listProperties(IpmRdfTransformService.HAS_LOCATION).toList().size());
            assertEquals(1, childFileInfoResource.listProperties(IpmRdfTransformService.HAS_SIZE).toList().size());
            assertEquals(1, childFileInfoResource.listProperties(IpmRdfTransformService.HAS_FORMAT).toList().size());
            assertEquals(1, childFileInfoResource.listProperties(IpmRdfTransformService.HAS_SHA1_CHECKSUM).toList().size());
            assertEquals(1, childFileInfoResource.listProperties(IpmRdfTransformService.HAS_MD5_CHECKSUM).toList().size());
            assertTrue(childFileInfoResource.hasLiteral(IpmRdfTransformService.IS_DIRECTORY, false));
            assertTrue(childFileInfoResource.hasLiteral(IpmRdfTransformService.IS_BYTE_STREAM, true));

            List<RDFNode> children = rootModel.listObjectsOfProperty(childResource, IpmRdfTransformService.HAS_CHILD).toList();
            assertEquals(0, children.size());
        }

    }

    /**
     * Tests that a node can be round tripped to/from RDF. Since node equality merely checks the identifier we do some deeper equality between objects in the tree
     * @throws RDFTransformException
     */
    @Test
    public void testRoundTrip() throws RDFTransformException {
        Node returnedNode = transformService.transformToNode(transformService.transformToRDF(root));

        assertEquals(root, returnedNode);

        //Node equals only checks identifier so make some additional checks to ensure everything is in order
        assertEquals(2, returnedNode.getChildren().size());

        assertEquals(rootType, returnedNode.getNodeType());
        assertEquals(1, returnedNode.getSubNodeTypes().size());
        assertEquals(subTypeOne, returnedNode.getSubNodeTypes().get(0));

        assertEquals(root.getFileInfo(), returnedNode.getFileInfo());

        assertFalse(returnedNode.isIgnored());
        boolean nodeOneFound = false;
        boolean nodeTwoFound = false;

        for (Node child : returnedNode.getChildren()) {
            assertEquals(childType, child.getNodeType());
            if (child.getIdentifier().equals(childOne.getIdentifier())) {
                assertEquals(childOne.getFileInfo(), child.getFileInfo());
                assertEquals(1, child.getSubNodeTypes().size());
                assertEquals(subTypeTwo, child.getSubNodeTypes().get(0));
                assertEquals(root, child.getParent());
                assertFalse(child.isIgnored());
                nodeOneFound = true;
            } else if (child.getIdentifier().equals(childTwo.getIdentifier())) {
                assertEquals(childTwo.getFileInfo(), child.getFileInfo());
                assertEquals(1, child.getSubNodeTypes().size());
                assertEquals(subTypeOne, child.getSubNodeTypes().get(0));
                assertEquals(root, child.getParent());
                assertTrue(child.isIgnored());
                nodeTwoFound = true;
            }
        }

        assertTrue(nodeOneFound);
        assertTrue(nodeTwoFound);
    }
}
