/*
 * Copyright 2014 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.packaging.gui.view.impl;

import javafx.stage.FileChooser;
import org.dataconservancy.packaging.gui.BaseGuiTest;
import org.dataconservancy.packaging.gui.Controller;
import org.dataconservancy.packaging.gui.presenter.impl.EditPackageContentsPresenterImpl;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.DomainProfileStore;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.profile.DcsBOProfile;
import org.dataconservancy.packaging.tool.profile.util.DcBoIpmFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;

/**
 * Tests that the package description view imple initializes all fields correctly. 
 */
public class EditPackageContentsViewImplTest extends BaseGuiTest {
    private EditPackageContentsViewImpl view;


    @Autowired
    @Qualifier("profileService")
    private DomainProfileService profileService;

    @Autowired
    @Qualifier("ipmService")
    private IPMService ipmService;

    @Autowired
    @Qualifier("domainProfileStore")
    private DomainProfileStore domainProfileStore;

    private Node project;

    @Before
    public void setup() {
        DcBoIpmFactory boFactory = new DcBoIpmFactory();
        project = boFactory.createSmallLinearTree();

        view = new EditPackageContentsViewImpl(internalProperties);
        HeaderView headerView = new HeaderViewImpl();
        view.setIpmService(ipmService);
        view.setHeaderView(headerView);

        PackageState state = new PackageState();
        state.setDomainProfileIdList(Collections.singletonList(new DcsBOProfile().getIdentifier()));
        Controller controller = new Controller() {
            @Override
            public File showSaveFileDialog(FileChooser chooser) {
                return null;
            }

            @Override
            public DomainProfileService getDomainProfileService() {
                return profileService;
            }

            @Override
            public Node getPackageTree() {
                return project;
            }

            @Override
            public PackageState getPackageState() {
                return state;
            }
        };

        controller.setDomainProfileStore(domainProfileStore);
        controller.setPackageState(state);

        controller.getDomainProfileService().assignNodeTypes(controller.getPrimaryDomainProfile(), project);

        EditPackageContentsPresenterImpl presenter = new EditPackageContentsPresenterImpl(view);
        presenter.setController(controller);
        presenter.setInternalProperties(internalProperties);
        presenter.setIpmService(ipmService);
    }
    
    /**
     * Make sure controls can be retrieved.
     */
    @Test
    public void testComponentsNotNull() {
        assertNotNull(view.getArtifactTreeView());
        assertNotNull(view.getContinueButton());
        assertNotNull(view.getPackageNameLabel());
        assertNotNull(view.getInheritMetadataCheckBoxMap());
    }
}