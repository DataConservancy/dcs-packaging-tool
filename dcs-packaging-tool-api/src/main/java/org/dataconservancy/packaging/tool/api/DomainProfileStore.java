package org.dataconservancy.packaging.tool.api;

import java.net.URI;
import java.util.List;

import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;

/**
 * Manage a set of domain profiles.
 */
public interface DomainProfileStore {
    /**
     * @return A primary domain profile
     */
    List<DomainProfile> getPrimaryDomainProfiles();

    /**
     * @param profiles
     *            The primary profiles to set.
     */
    void setPrimaryDomainProfiles(List<DomainProfile> profiles);

    /**
     * @return Secondary domain profiles which may be used with a primary domain
     *         profile.
     */
    List<DomainProfile> getSecondaryDomainProfiles();

    /**
     * @param profiles
     *            The secondary profiles to set.
     */
    void setSecondaryDomainProfiles(List<DomainProfile> profiles);

    /**
     * @param uri
     * @return NodeType identified by the given uri or null if it does not
     *         exist.
     */
    NodeType getNodeType(URI uri);
}
