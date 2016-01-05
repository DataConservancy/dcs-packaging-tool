package org.dataconservancy.packaging.tool.api;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
     * @param uri The identifier of the node type.
     * @return NodeType identified by the given uri or null if it does not
     *         exist.
     */
    NodeType getNodeType(URI uri);
}
