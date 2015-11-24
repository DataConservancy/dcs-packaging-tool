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

package org.dataconservancy.packaging.gui;

/**
 * A simple enumeration that is used to control flow in the application. There is an entry for each page in the application.
 *
 */
public enum Page {

    HOMEPAGE(Labels.LabelKey.HOMEPAGE_PAGE),
    PACKAGE_METADATA(Labels.LabelKey.CREATE_PACKAGE_PAGE),
    CREATE_NEW_PACKAGE(Labels.LabelKey.CREATE_PACKAGE_PAGE),
    EDIT_PACKAGE_CONTENTS(Labels.LabelKey.EDIT_PACKAGE_CONTENTS_PAGE),
    GENERATE_PACKAGE(Labels.LabelKey.GENERATE_PACKAGE_PAGE),
    OPEN_EXISTING_PACKAGE(Labels.LabelKey.CREATE_PACKAGE_PAGE);

    private Labels.LabelKey labelKey;

    Page(Labels.LabelKey label) {
        this.labelKey = label;
    }

    /**
     * Returns the label key to get the title of the page.
     *
     * @return the label key to get the title of the page
     */
    public Labels.LabelKey getLabelKey() {
        return labelKey;
    }

}

