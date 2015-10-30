package org.dataconservancy.packaging.tool.impl;

import org.apache.commons.collections.CollectionUtils;
import org.dataconservancy.packaging.tool.model.RDFTransformException;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DomainProfileRdfTransformServiceTest {

    private DomainProfileRdfTransformService service;
    private FarmDomainProfile profile;

    @Before
    public void setup() {
        service = new DomainProfileRdfTransformService();
        profile = new FarmDomainProfile();
    }
    @Test
    public void roundTrip() throws RDFTransformException {
        DomainProfile returnedProfile = service.transformToProfile(service.transformToRdf(profile));


        assertEquals(profile, returnedProfile);

        //Since profile equals ignores node type check those here
        CollectionUtils.isEqualCollection(profile.getNodeTypes(), returnedProfile.getNodeTypes());

    }
}
