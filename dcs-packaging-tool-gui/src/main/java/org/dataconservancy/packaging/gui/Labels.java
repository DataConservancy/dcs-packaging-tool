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

import java.util.ResourceBundle;

/**
 * Wrapper to access bundled resource for labels.
 */
public class Labels {

    public enum LabelKey {
        CANCEL_BUTTON("cancel.button"),
        OPEN_PACKAGE_STATE_LABEL_KEY("openpackagestate.label"),
        PACKAGE_STATE_FILE_CHOOSER_KEY("packagestatefile.save"),
        OUTPUT_DIRECTORY_CHOOSER_KEY("outputdirectory.chooser"),
        OUTPUT_DIRECTORY_LABEL_KEY("outputdirectory.label"),
        USER_PROPERTIES_LABEL("userproperties.label"),
        INHERITANCE_LABEL("inheritance.label"),
        SHOW_FULL_PATHS("showfullpaths.label"),
        CREATE_PACKAGE_PAGE("createPackage.page"),
        EDIT_PACKAGE_CONTENTS_PAGE("editpackagecontents.page"),
        GENERATE_PACKAGE_PAGE("generatePackage.page"),
        CONTINUE_BUTTON("continue.button"),
        HELP_LABEL("help.label"),
        ABOUT_LABEL("about.label"),
        SAVE_AND_CONTINUE_BUTTON("saveandcontinue.button"),
        FINISH_BUTTON("finish.button"),
        NO_THANKS_LINK("nothanks.link"),
        CREATE_ANOTHER_PACKAGE_BUTTON("createanotherpackage.button"),
        PACKAGE_NAME_LABEL("packagename.label"),
        BASE_DIRECTORY_LABEL("basedirectory.label"),
        BROWSE_BUTTON("browse.button"),
        BROWSEDIR_BUTTON("browsedir.button"),
        ARCHIVE_FORMAT_LABEL("archiveformat.label"),
        COMPRESSION_FORMAT_LABEL("compressionformat.label"),
        TAR_BUTTON("tar.button"),
        ZIP_BUTTON("zip.button"),
        GZIP_BUTTON("gzip.button"),
        EXPLODED_BUTTON("exploded.button"),
        SUCCESS_LABEL("success.label"),
        FINAL_PACKAGE_NAME_LOCATION("finalpackagenamelocation.label"),
        ANOTHER_FORMAT_LABEL("anotherformat.label"),
        USER_PROPERTY_DEFINITION_LABEL("userpropertydefinition.label"),
        SAVE_BUTTON("save.button"),
        APPLY_BUTTON("apply.button"),
        OK_BUTTON("ok.button"),
        BACK_LINK("back.link"),
        BASIC_HELP_TITLE("basichelptitle.label"),
        BASIC_HELP_TEXT("basichelptext.label"),
        BASIC_ABOUT_TITLE("basicabouttitle.label"),
        BASIC_ABOUT_TEXT("basicabouttext.label"),
        ADD_NEW_BUTTON("addnew.button"),
        ADD_NEW_PROPERTY_BUTTON("addnewproperty.button"),
        NONE_LABEL("none.label"),
        CHECKSUM_LABEL("checksum.label"),
        MD5_CHECKBOX("md5.checkbox"),
        SHA1_CHECKBOX("sha1.checkbox"),
        HIERARCHICAL_ADVICE_LABEL("hierarchical.label"),
        PACKAGING_OPTIONS_LABEL("packagingoptions.label"),
        BUILD_NUMBER_LABEL("buildnumber.label"),
        BUILD_REVISION_LABEL("buildrevision.label"),
        BUILD_TIMESTAMP_LABEL("buildtimestamp.label"),
        INHERITANCE_TAB_INTRO("inheritanceTabIntro.label"),
        INHERITANCE_BUTTON_EXPLAINED("inheritanceButtonExplained.label"),
        INHERITANCE_DESCENDANT_TYPE("inheritanceDescendantType.label"),
        FILE_EXISTS_WARNING_TITLE_LABEL("fileexistswarningtitle.label"),
        FILE_EXISTS_WARNING_TEXT_LABEL("fileexistswarningtext.label"),
        DONT_SHOW_WARNING_AGAIN_CHECKBOX("dontshowwarningagain.checkbox"),
        RENABLE_PROPERTY_WARNINGS_BUTTON("reenablepropertywarnings.button"),
        PROGRESS_INDICATOR("progressindicator.label"),
        BUILDING_PACKAGE_TREE("packagetreebuilding.label"),
        PROPERTIES_LABEL("properties.label"),
        VOCABULARY_LABEL("vocabulary.label"),
        PREDICATE_LABEL("predicate.label"),
        REQURIRES_URI_LABEL("requiresuri.label"),
        PREDICATE_MUST_BE_URI_OR_KNOWN("mustbeuriorknown.label"),
        USER_DEFINED_PROPERTY_VALUE_LABEL("propertyvalue.label"),
        URI_LABEL("uri.label"),
        LITERAL_LABEL("literal.label"),
        IGNORE_CHECKBOX("ignore.checkbox"),
        GENERATING_PACKAGE_LABEL("generatingpackage.label"),
        SHOW_FULL_PATHS_TIP("showfullpaths.tip"),
        SHOW_IGNORED("showignored.label"),
        SHOW_IGNORED_TIP("showignored.tip"),
        PACKAGE_DIRECTORY_LABEL("packagedirectory.label"),
        SELECT_IN_PROGRESS_PACKAGE_FILE_LABEL("inprogresspackagefile.label"),
        CREATE_NEW_PACKAGE("createnewpackage.label"),
        OPEN_EXISTING_PACKAGE("openexistingpackage.label"),
        HOMEPAGE_PAGE("homepage.page"),
        SELECT_DOMAIN_PROFILE_LABEL("selectdomainprofile.label"),
        ADD_BUTTON("add.button"),
        SERIALIZATION_FORMAT_LABEL("serialization.label"),
        JSON_BUTTON("json.button"),
        XML_BUTTON("xml.button"),
        TURTLE_BUTTON("turtle.button"),
        SELECT_PACKAGE_FILE_LABEL("selectpackagefile.label"),
        REQUIRED_FIELDS_LABEL("requiredfields.label"),
        RECOMMENDED_FIELDS_LABEL("recommendedfields.label"),
        OPTIONAL_FIELDS_LABEL("optionalfields.label"),
        TYPE_VALUE_AND_ENTER_PROMPT("typevalueandenter.prompt"),
        PACKAGE_OUTPUT_DIRECTORY_LABEL("packageoutputdirectory.label"),
        SELECT_ONE_OPTION_LABEL("selectoneoption.label"),
        WARNING_POPUP_TITLE("warningpopup.title"),
        ALL_FIELDS_CLEAR_WARNING_MESSAGE("allfieldsclearwarning.message"),
        ADD_FILE_ITEM_LABEL("addfileitem.label"),
        ADD_FOLDER_ITEM_LABEL("addfolderitem.label"),
        REFRESH_ITEM_LABEL("refreshitem.label"),
        REMAP_ITEM_LABEL("remapitem.label"),
        ACCEPT_BUTTON("accept.button"),
        REJECT_BUTTON("reject.button"),
        DETECTED_CHANGES_LABEL("detectedchanges.label"),
        FILE_MISSING_TIP("backingfilenotfound.tip"),
        REFRESH_STATUS_LABEL("refreshstatus.label"),
        REFRESH_LOCATION_LABEL("refreshlocation.label"),
        NO_BACKING_FILE_LABEL("nobackingfile.label"),
        UNASSIGNED_LABEL("unassigned.label"),
        OPTIONAL_PROPERTIES_LABEL("optionalproperties.label"),
        REQUIRED_PROPERTIES_LABEL("requiredproperties.label"),
        SELECT_STAGING_DIRECTORY("selectstagingdir.label"),
        PROPERTY_MISSING_LABEL("propertymissing.label"),
        MISSING_FILE_LEGEND("missingfilelegend.label"),
        MISSING_PROPERTY_LEGEND("missingpropertylegend.label");

        private String property;

        LabelKey(String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
        }
    }

    private ResourceBundle bundle;

    public Labels(ResourceBundle bundle) {
        this.bundle = bundle;

        for (LabelKey key : LabelKey.values()) {
            if (!bundle.containsKey(key.getProperty())) {
                throw new IllegalArgumentException("Missing resource in bundle: " + key.getProperty());
            }
        }
    }

    public String get(LabelKey key) {
        if (!bundle.containsKey(key.getProperty())) {
            throw new IllegalArgumentException("No such resource: " + key.getProperty());
        }

        return bundle.getString(key.getProperty());
    }

    public String format(LabelKey key, Object... args) {
        if (!bundle.containsKey(key.property)) {
            throw new IllegalArgumentException("No such resource: " +
                                                   key);
        }

        return String.format(bundle.getLocale(), bundle.getString(key.property), args);
    }
}
