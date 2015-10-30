package org.dataconservancy.packaging.tool.impl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.dataconservancy.packaging.tool.api.DomainProfileStore;
import org.dataconservancy.packaging.tool.model.RDFTransformException;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DomainProfileStoreJenaImpl implements DomainProfileStore {
    private Model primaryDomainProfiles;
    private Model secondaryDomainProfiles;
    DomainProfileRdfTransformService transformService;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public DomainProfileStoreJenaImpl(Model primaryProfiles, Model secondaryProfiles) {
        this.primaryDomainProfiles = primaryProfiles;
        this.secondaryDomainProfiles = secondaryProfiles;
        transformService = new DomainProfileRdfTransformService();
    }

    public DomainProfileStoreJenaImpl(List<DomainProfile> primaryProfiles, List<DomainProfile> secondaryProfiles) {
        transformService = new DomainProfileRdfTransformService();
        setPrimaryDomainProfiles(primaryProfiles);
        setSecondaryDomainProfiles(secondaryProfiles);
    }

    public DomainProfileStoreJenaImpl() {
        transformService = new DomainProfileRdfTransformService();
    }

    @Override
    public List<DomainProfile> getPrimaryDomainProfiles() {
        return getDomainProfiles(primaryDomainProfiles);
    }

    @Override
    public void setPrimaryDomainProfiles(List<DomainProfile> profiles) {
        //Clear out existing primary domain profiles
        primaryDomainProfiles = ModelFactory.createDefaultModel();

        for (DomainProfile profile : profiles) {
            Model profileModel = null;
            try {
                profileModel = transformService.transformToRdf(profile);
            } catch (RDFTransformException e) {
                //TODO Revisit if we want to log these exceptions or surface them to the caller, likely the PTG
                log.error("Error serializing DomainProfile: " + profile.getIdentifier() + " Exception: " + e.getMessage());
            }
            if (profileModel != null) {
                primaryDomainProfiles.add(profileModel);
            } else {
                log.error("Unable to deserialize DomainProfile: " + profile.getIdentifier());
            }
        }
    }

    private List<DomainProfile> getDomainProfiles(Model profileModel) {
        List<DomainProfile> profiles = new ArrayList<>();

        List<Resource> domainProfiles = profileModel.listResourcesWithProperty(RDF.type, DomainProfileRdfTransformService.DP_TYPE).toList();
        for (Resource profileResource : domainProfiles) {
            DomainProfile profile = null;
            try {
                profile = transformService.transformToDomainProfile(profileResource, profileModel);
            } catch (RDFTransformException e) {
                //TODO Revisit if we want to log these exceptions or surface them to the caller
                String id = "";
                if (profileResource.hasProperty(DomainProfileRdfTransformService.HAS_ID)) {
                    RDFNode value = profileResource.getProperty(DomainProfileRdfTransformService.HAS_ID).getObject();

                    if (value.isLiteral()) {
                        id = value.asLiteral().toString();
                    }
                }
                log.error("Error deserializing DomainProfile: " + id + " Exception: " + e.getMessage());
            }
            if (profile != null) {
                profiles.add(profile);
            }
        }
        return profiles;
    }

    @Override
    public List<DomainProfile> getSecondaryDomainProfiles() {
        return getDomainProfiles(secondaryDomainProfiles);
    }

    @Override
    public void setSecondaryDomainProfiles(List<DomainProfile> profiles) {
        //Clear out and initialize the secondary domain profiles
        secondaryDomainProfiles = ModelFactory.createDefaultModel();
        for (DomainProfile profile : profiles) {
            Model profileModel = null;
            try {
                profileModel = transformService.transformToRdf(profile);
            } catch (RDFTransformException e) {
                //TODO Revisit if we want to log these exceptions or surface them to the caller, likely the PTG
                log.error("Error serializing DomainProfile: " + profile.getIdentifier() + " Exception: " + e.getMessage());
            }

            if (profileModel != null) {
                secondaryDomainProfiles.add(profileModel);
            } else {
                log.error("Unable to deserialize DomainProfile: " + profile.getIdentifier());
            }
        }
    }

    @Override
    public NodeType getNodeType(URI uri) {
        NodeType type;

        //First checks the primary domain profiles for the node type
        type = findNodeType(primaryDomainProfiles, uri);

        //If we don't find the node type check the secondary profiles for the node type
        if (type == null) {
            type = findNodeType(secondaryDomainProfiles, uri);
        }


        return type;
    }

    /**
     * Finds a node type and it's corresponding profile in the given model.
     * @param profileModel The model to check for the node type.
     * @param nodeTypeId The id of the node type to find.
     * @return The NodeType object, or null if no match is found.
     */
    private NodeType findNodeType(Model profileModel, URI nodeTypeId) {
        NodeType type = null;

        //Find any resources that use the given node id as an id.
        List<Resource> nodeTypeList = profileModel.listResourcesWithProperty(DomainProfileRdfTransformService.HAS_ID, nodeTypeId.toString()).toList();
        if (!nodeTypeList.isEmpty()) {
            //There should be only one resource returned but loop through any possibilities
            for (Resource possibleResource : nodeTypeList) {
                try {

                    DomainProfile nodeTypeProfile = null;
                    //Retrieve the domain profile resources that have this resource as a node type.
                    List<Resource> possibleProfiles = profileModel.listResourcesWithProperty(DomainProfileRdfTransformService.HAS_NODE_TYPE, possibleResource.asNode()).toList();
                    //Note that this will likely also return blank nodes so loop through and find the one that's actually a profile
                    for (Resource possibleProfile : possibleProfiles) {
                        DomainProfile profile = transformService.transformToDomainProfile(possibleProfile, profileModel);
                        //Ensure that what's returned is actually a profile and has an identifier.
                        if (profile != null && profile.getIdentifier() != null) {
                            nodeTypeProfile = profile;
                            break;
                        }
                    }

                    //If we've found the profile for the node type now we can deserialize the node type.
                    if (nodeTypeProfile != null) {
                        type = transformService.transformToNodeType(possibleResource, nodeTypeProfile, profileModel);
                        if (type != null) {
                            break;
                        }
                    } else {
                        log.warn("Unable to find profile for node type: " + nodeTypeId.toString());
                    }

                } catch (RDFTransformException e) {
                    log.error("Unable to deserialize node type: " + nodeTypeId.toString());
                }
            }
        }

        return type;
    }
}
