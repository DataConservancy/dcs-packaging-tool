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
 * Each page contains it's order in the application as well as a title.
 */
public enum Page {

    //Positions must be in numerical order of there appearance in the workflow but don't need to be sequential
    //Space is left between pages to allow for the future addition of more screens
    HOMEPAGE(Labels.LabelKey.HOMEPAGE_PAGE),
    PACKAGE_METADATA(Labels.LabelKey.CREATE_PACKAGE_PAGE),
    CREATE_NEW_PACKAGE(Labels.LabelKey.CREATE_PACKAGE_PAGE),
    SELECT_CONTENT_DIRECTORY(Labels.LabelKey.CREATE_PACKAGE_PAGE),
    DEFINE_RELATIONSHIPS(Labels.LabelKey.DEFINE_RELATIONSHIPS_PAGE),
    GENERATE_PACKAGE(Labels.LabelKey.GENERATE_PACKAGE_PAGE),
    SELECT_PACKAGE_DIRECTORY(Labels.LabelKey.CREATE_PACKAGE_PAGE),
    EXISTING_PACKAGE_METADATA(Labels.LabelKey.CREATE_PACKAGE_PAGE);

    private Labels.LabelKey labelKey;

    Page(Labels.LabelKey label) {
        this.labelKey = label;
    }

    /**
     * Static method to get the page that corresponds to a specific position.
     * Note: This method is required because the pages are not zero indexed, or sequential so you can't simply access or loop through to find the correct page based on position.
     *
     * @param position The position to retrieve the page for.
     * @return The page corresponding to the given position or null if none exist.
     */
    public static Page getPageByPosition(int position) {
        switch (position) {
            case 10:
                return HOMEPAGE;
            case 11:
                return CREATE_NEW_PACKAGE;
            case 12:
                return SELECT_CONTENT_DIRECTORY;
            case 20:
                return DEFINE_RELATIONSHIPS;
            case 30:
                return GENERATE_PACKAGE;
        }

        return null;
    }

    /**
     * Returns the label key to get the title of the page.
     *
     * @return the label key to get the title of the page
     */
    public Labels.LabelKey getLabelKey() {
        return labelKey;
    }

    /**
     * Determines if the page is valid to be shown, this is for conditional pages such as the the SELECT_CONTENT_DIRECTORY page.
     * For most pages this method will default to true since they're always valid to be shown.
     *
     * @param controller The controller instance that's checking for page validity
     * @return True if the page should be shown, false if not
     */
    //TODO: I was hoping to make this a parameter of the enum but getting that to work proved to be beyond my java foo. So this method exists instead. -BMB
    public boolean isValidPage(Controller controller) {
        switch (this) {
            case SELECT_CONTENT_DIRECTORY:
                if (controller.getPackageDescriptionFile() == null) {
                    return false;
                }
                break;
        }

        return true;
    }
}

