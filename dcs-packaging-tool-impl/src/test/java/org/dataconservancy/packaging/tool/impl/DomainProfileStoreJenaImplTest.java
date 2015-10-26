package org.dataconservancy.packaging.tool.impl;

import org.apache.commons.collections.CollectionUtils;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class DomainProfileStoreJenaImplTest {

    private List<DomainProfile> primaryProfiles;
    private List<DomainProfile> secondaryProfiles;
    private NodeType primaryNodeType;
    private NodeType secondaryNodeType;
    private DomainProfileStoreJenaImpl underTest;

    @Before
    public void setUp() throws URISyntaxException {
        primaryProfiles = new ArrayList<>();
        DomainProfile profileOne = new FarmDomainProfile();
        primaryNodeType = profileOne.getNodeTypes().get(0);
        primaryProfiles.add(profileOne);

        DomainProfile profileTwo = new FarmDomainProfile();
        updateProfileIds(profileTwo);
        primaryProfiles.add(profileTwo);

        DomainProfile profileThree = new FarmDomainProfile();
        updateProfileIds(profileThree);
        primaryProfiles.add(profileThree);

        secondaryProfiles = new ArrayList<>();
        DomainProfile secondaryProfileOne = new FarmDomainProfile();
        secondaryNodeType = secondaryProfileOne.getNodeTypes().get(1);
        updateProfileIds(secondaryProfileOne);
        secondaryProfiles.add(secondaryProfileOne);

        DomainProfile secondaryProfileTwo = new FarmDomainProfile();
        updateProfileIds(secondaryProfileTwo);
        secondaryProfiles.add(secondaryProfileTwo);

        underTest = new DomainProfileStoreJenaImpl(primaryProfiles, secondaryProfiles);
    }

    /**
     * Tests that the list of primary profiles is correctly returned.
     */
    @Test
    public void testGetPrimaryProfiles() {
        assertTrue(CollectionUtils.isEqualCollection(primaryProfiles, underTest.getPrimaryDomainProfiles()));
    }

    /**
     * Tests that the list of secondary profiles is correctly returned.
     */
    @Test
    public void testGetSecondaryProfiles() {
        assertTrue(CollectionUtils.isEqualCollection(secondaryProfiles, underTest.getSecondaryDomainProfiles()));
    }

    /**
     * Tests that node types can be retrieved from both the primary and secondary profiles.
     */
    @Test
    public void testGetNodeType() {
        assertEquals(primaryNodeType, underTest.getNodeType(primaryNodeType.getIdentifier()));
        assertEquals(secondaryNodeType, underTest.getNodeType(secondaryNodeType.getIdentifier()));
    }

    private void updateProfileIds(DomainProfile profile)
        throws URISyntaxException {
        profile.setIdentifier(new URI(UUID.randomUUID().toString()));
        for (NodeType type : profile.getNodeTypes()) {
            type.setIdentifier(new URI(UUID.randomUUID().toString()));
        }
    }
}
