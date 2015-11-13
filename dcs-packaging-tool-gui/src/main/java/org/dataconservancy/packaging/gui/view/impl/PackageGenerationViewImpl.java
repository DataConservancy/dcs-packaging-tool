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

import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import org.dataconservancy.packaging.gui.Help;
import org.dataconservancy.packaging.gui.Help.HelpKey;
import org.dataconservancy.packaging.gui.Labels.LabelKey;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.presenter.PackageGenerationPresenter;
import org.dataconservancy.packaging.gui.util.ControlFactory;
import org.dataconservancy.packaging.gui.util.ControlType;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;
import org.dataconservancy.packaging.gui.util.ProgressDialogPopup;
import org.dataconservancy.packaging.gui.view.PackageGenerationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the view that displays the controls for generating a package.
 */
public class PackageGenerationViewImpl extends BaseViewImpl<PackageGenerationPresenter> implements PackageGenerationView {

    //Controls for setting the package name and output directory
    private DirectoryChooser outputDirectoryChooser;
    private TextField currentOutputDirectoryTextField;
    private Button selectOutputDirectoryButton;

    //Radio buttons for the archive types.
    private RadioButton tarArchiveButton;
    private RadioButton zipArchiveButton;
    private RadioButton explodedArchiveButton;
    
    //Radio buttons for the compression types.
    private RadioButton gZipCompressionButton;
    private RadioButton zipCompressionButton;
    private RadioButton noneCompressionButton;

    //Toggle groups that control the input of the archive and compression groups, and simplify the presenter.
    private ToggleGroup compressionToggleGroup;
    private ToggleGroup archiveToggleGroup;

    //Checkbox for serialization format
    private ToggleGroup serializationToggleGroup;
    private RadioButton jsonRadioButton;
    private RadioButton xmlRadioButton;
    private RadioButton turtleRadioButton;

    //Checkbox for the checksum algorithms
    private CheckBox md5CheckBox;
    private CheckBox sha1CheckBox;

    //Popup to warn of existing package file overwrite
    public PackageToolPopup packageFileExistsWarningPopup;
    public Button cancelOverwriteButton;
    public Button okOverwriteButton;
    
    //Popup when generation was successful and the controls of the popup. 
    public PackageToolPopup packageGenerationSuccessPopup;
    public Hyperlink noThanksLink;
    public Button createAnotherPackageButton;
    
    private ProgressDialogPopup progressDialogPopup;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private ScrollPane contentScrollPane;

    public PackageGenerationViewImpl(Help help) {
        super();
        
        contentScrollPane = new ScrollPane();
        contentScrollPane.setFitToWidth(true);
        VBox content = new VBox();

        //Set up the text for the controls in the footer.
        getContinueButton().setText(TextFactory.getText(LabelKey.FINISH_BUTTON));
        getCancelLink().setText(TextFactory.getText(LabelKey.BACK_LINK));

        content.getStyleClass().add(PACKAGE_GENERATION_VIEW_CLASS);
        contentScrollPane.setContent(content);

        setCenter(contentScrollPane);
        
        //Create a label to show any status messages at the top of the screen.
        HBox status = new HBox();
        status.getChildren().add(errorLabel);
        status.setAlignment(Pos.TOP_CENTER);

        content.getChildren().add(status);

        //Create a section for setting the packaging options.
        VBox packagingSection = new VBox(4);
        Label packagingOptionsLabel = new Label(TextFactory.getText(LabelKey.PACKAGING_OPTIONS_LABEL));
        packagingOptionsLabel.getStyleClass().add(FORM_FIELDS_DIVISION_CLASS);
        packagingSection.getChildren().add(packagingOptionsLabel);
        packagingSection.getChildren().add(new Separator(Orientation.HORIZONTAL));

        HBox packagingOptions = new HBox(40);
        packagingOptions.setAlignment(Pos.TOP_LEFT);

        //Create a vbox for the archiving options.
        VBox archivingOptions = new VBox(10);
        archivingOptions.setAlignment(Pos.TOP_LEFT);
        Label archivingLabel = new Label(TextFactory.getText(LabelKey.ARCHIVE_FORMAT_LABEL));
        archivingOptions.getChildren().add(archivingLabel);
        
        //Create a toggle group for the archiving options. 
        archiveToggleGroup = new ToggleGroup();
        
        tarArchiveButton = new RadioButton(TextFactory.getText(LabelKey.TAR_BUTTON));
        tarArchiveButton.setToggleGroup(archiveToggleGroup);
        tarArchiveButton.setUserData("tar");
        tarArchiveButton.setSelected(true);
        archivingOptions.getChildren().add(tarArchiveButton);
        
        zipArchiveButton = new RadioButton(TextFactory.getText(LabelKey.ZIP_BUTTON));
        zipArchiveButton.setToggleGroup(archiveToggleGroup);
        zipArchiveButton.setUserData("zip");
        
        zipArchiveButton.setSelected(false);
        archivingOptions.getChildren().add(zipArchiveButton);

        explodedArchiveButton = new RadioButton(TextFactory.getText(LabelKey.EXPLODED_BUTTON));
        explodedArchiveButton.setToggleGroup(archiveToggleGroup);
        explodedArchiveButton.setUserData("exploded");
        archivingOptions.getChildren().add(explodedArchiveButton);

        packagingOptions.getChildren().add(archivingOptions);
        
        //Create a vbox for the compression options.
        VBox compressionOptions = new VBox(10);
        compressionOptions.setAlignment(Pos.TOP_LEFT);

        Label compressionLabel = new Label(TextFactory.getText(LabelKey.COMPRESSION_FORMAT_LABEL));
        compressionOptions.getChildren().add(compressionLabel);
        
        //Create a toggle group for the compression options.
        compressionToggleGroup = new ToggleGroup();
        
        gZipCompressionButton = new RadioButton(TextFactory.getText(LabelKey.GZIP_BUTTON));
        gZipCompressionButton.setToggleGroup(compressionToggleGroup);
        gZipCompressionButton.setSelected(true);
        gZipCompressionButton.setUserData("gz");

        compressionOptions.getChildren().add(gZipCompressionButton);
        
        zipCompressionButton = new RadioButton(TextFactory.getText(LabelKey.ZIP_BUTTON));
        zipCompressionButton.setToggleGroup(compressionToggleGroup);
        zipCompressionButton.setSelected(false);
        zipCompressionButton.setUserData("zip");
        //compressionOptions.getChildren().add(zipCompressionButton);
        
        noneCompressionButton = new RadioButton(TextFactory.getText(LabelKey.NONE_LABEL));
        noneCompressionButton.setToggleGroup(compressionToggleGroup);
        noneCompressionButton.setSelected(false);
        noneCompressionButton.setUserData("");
        compressionOptions.getChildren().add(noneCompressionButton);

        gZipCompressionButton = new RadioButton(TextFactory.getText(LabelKey.GZIP_BUTTON));
        gZipCompressionButton.setToggleGroup(compressionToggleGroup);
        gZipCompressionButton.setSelected(true);
        gZipCompressionButton.setUserData("gz");

        packagingOptions.getChildren().add(compressionOptions);

        //Create a vbox for the serialization options.
        VBox serializationOptions = new VBox(10);
        serializationOptions.setAlignment(Pos.TOP_LEFT);

        Label serializationLabel = new Label(TextFactory.getText(LabelKey.SERIALIZATION_FORMAT_LABEL));
        serializationOptions.getChildren().add(serializationLabel);

        //Create a toggle group for the compression options.
        serializationToggleGroup = new ToggleGroup();

        jsonRadioButton = new RadioButton(TextFactory.getText(LabelKey.JSON_BUTTON));
        jsonRadioButton.setToggleGroup(serializationToggleGroup);
        jsonRadioButton.setUserData("json");
        jsonRadioButton.setSelected(true);
        serializationOptions.getChildren().add(jsonRadioButton);

        xmlRadioButton = new RadioButton(TextFactory.getText(LabelKey.XML_BUTTON));
        xmlRadioButton.setToggleGroup(serializationToggleGroup);
        xmlRadioButton.setUserData("xml");
        xmlRadioButton.setSelected(false);
        serializationOptions.getChildren().add(xmlRadioButton);

        turtleRadioButton = new RadioButton(TextFactory.getText(LabelKey.TURTLE_BUTTON));
        turtleRadioButton.setToggleGroup(serializationToggleGroup);
        turtleRadioButton.setUserData("turtle");
        turtleRadioButton.setSelected(false);
        serializationOptions.getChildren().add(turtleRadioButton);

        packagingOptions.getChildren().add(serializationOptions);

        //Create a vbox for the checksum options.
        VBox checksumOptions = new VBox(10);
        checksumOptions.setAlignment(Pos.TOP_LEFT);

        Label checksumLabel = new Label(TextFactory.getText(LabelKey.CHECKSUM_LABEL));
        checksumOptions.getChildren().add(checksumLabel);
        
        md5CheckBox = new CheckBox(TextFactory.getText(LabelKey.MD5_CHECKBOX));
        checksumOptions.getChildren().add(md5CheckBox);
        
        sha1CheckBox = new CheckBox(TextFactory.getText(LabelKey.SHA1_CHECKBOX));
        checksumOptions.getChildren().add(sha1CheckBox);
        
        packagingOptions.getChildren().add(checksumOptions);
        
        packagingSection.getChildren().add(packagingOptions);

        content.getChildren().add(packagingSection);

        //Sets up the controls for selecting an output directory.
        VBox outputDirectoryBox = new VBox(4);
        outputDirectoryBox.setAlignment(Pos.CENTER_LEFT);

        Label outputDirectoryLabel = new Label(TextFactory.getText(LabelKey.PACKAGE_OUTPUT_DIRECTORY_LABEL));
        outputDirectoryBox.getChildren().add(outputDirectoryLabel);

        HBox directorySelectionBox = new HBox(8);
        directorySelectionBox.setAlignment(Pos.CENTER_LEFT);

        HBox directoryBox = new HBox();
        directoryBox.setAlignment(Pos.CENTER_LEFT);
        //directoryBox.setMaxWidth(350);
        directoryBox.setMinWidth(350);
        directoryBox.getStyleClass().add(DIRECTORY_BOX);

        currentOutputDirectoryTextField = (TextField) ControlFactory.createControl(ControlType.TEXT_FIELD, null, null);
        currentOutputDirectoryTextField.setMinWidth(340);
        currentOutputDirectoryTextField.setEditable(false);
        currentOutputDirectoryTextField.getStyleClass().add(INVISBILE_TEXT_FIELD);
        directoryBox.getChildren().add(currentOutputDirectoryTextField);
        HBox.setHgrow(currentOutputDirectoryTextField, Priority.ALWAYS);
        directorySelectionBox.getChildren().add(directoryBox);
        HBox.setHgrow(directoryBox, Priority.ALWAYS);
        selectOutputDirectoryButton = new Button(TextFactory.getText(LabelKey.BROWSEDIR_BUTTON));
        directorySelectionBox.getChildren().add(selectOutputDirectoryButton);

        outputDirectoryBox.getChildren().add(directorySelectionBox);

        outputDirectoryChooser = new DirectoryChooser();
        outputDirectoryChooser.setTitle(TextFactory.getText(LabelKey.OUTPUT_DIRECTORY_CHOOSER_KEY));

        content.getChildren().add(outputDirectoryBox);

        //PopupControls
        noThanksLink = new Hyperlink(TextFactory.getText(LabelKey.NO_THANKS_LINK));
        createAnotherPackageButton = new Button(TextFactory.getText(LabelKey.CREATE_ANOTHER_PACKAGE_BUTTON));
        createAnotherPackageButton.setPrefWidth(20*rem);

        cancelOverwriteButton = new Button(TextFactory.getText(LabelKey.CANCEL_BUTTON));
        okOverwriteButton = new Button(TextFactory.getText(LabelKey.OK_BUTTON));

        setHelpPopupContent(help.get(HelpKey.PACKAGE_GENERATION_HELP));         
    }

    @Override
    public DirectoryChooser getOutputDirectoryChooser() {
        return outputDirectoryChooser;
    }
    
    @Override
    public Button getSelectOutputDirectoryButton() {
        return selectOutputDirectoryButton;
    }
    
    @Override
    public ToggleGroup getCompressionToggleGroup() {
        return compressionToggleGroup;
    }
    
    @Override 
    public ToggleGroup getArchiveToggleGroup() {
        return archiveToggleGroup;
    }
    
    @Override
    public TextField getCurrentOutputDirectoryTextField() {
        return currentOutputDirectoryTextField;
    }

    @Override
    public void showSuccessPopup(String packageName, String location) {
        //Create a simple package tool popup for the generation success message.
        packageGenerationSuccessPopup = new PackageToolPopup();
        packageGenerationSuccessPopup.setTitleText(TextFactory.getText(LabelKey.SUCCESS_LABEL));
        packageGenerationSuccessPopup.setMoveable(false);

        VBox popupContent = new VBox();
        popupContent.setMaxWidth(600);

        popupContent.setAlignment(Pos.TOP_CENTER);

        Label successfulPackageNameAndLocation =
                new Label(String.format(TextFactory.getText(LabelKey.FINAL_PACKAGE_NAME_LOCATION), packageName, location));
        successfulPackageNameAndLocation.setWrapText(true);
        popupContent.getChildren().add(successfulPackageNameAndLocation);

        Label anotherFormatMessage = new Label(TextFactory.getText(LabelKey.ANOTHER_FORMAT_LABEL));
        anotherFormatMessage.setWrapText(true);

        popupContent.getChildren().add(anotherFormatMessage);

        HBox popupControls = new HBox();
        popupControls.setAlignment(Pos.CENTER);
        popupControls.setSpacing(40);
        popupControls.getChildren().add(noThanksLink);
        popupControls.getChildren().add(createAnotherPackageButton);

        popupContent.getChildren().add(popupControls);

        packageGenerationSuccessPopup.setContent(popupContent);


        Point2D point = continueButton.localToScene(0.0,  0.0);
        double x = point.getX();
        double y = point.getY();
        if (getScene() != null && getScene().getWindow() != null) {
            x = getScene().getWindow().getX() + point.getX();
            y = getScene().getWindow().getY() + point.getY();
        }

        //packageGenerationSuccessPopup.setAutoHide(false);
        //Offset the popup to cover the buttons but not be off the screen.
        x -= 340;
        y -= 60;

        if (getScene() != null && getScene().getWindow() != null) {
            packageGenerationSuccessPopup.setOwner(getScene().getWindow());
            packageGenerationSuccessPopup.show(x, y);
        }
    }

    @Override
    public void showFileOverwriteWarningPopup() {
        //Create simple warning popup for the warning message
        packageFileExistsWarningPopup = new PackageToolPopup();
        packageFileExistsWarningPopup.setTitleText(TextFactory.getText(LabelKey.FILE_EXISTS_WARNING_TITLE_LABEL));

        VBox popupContent = new VBox();
        popupContent.setMaxWidth(400);

        popupContent.setAlignment(Pos.TOP_CENTER);

        Label warningText = new Label(TextFactory.getText(LabelKey.FILE_EXISTS_WARNING_TEXT_LABEL));
        warningText.setWrapText(true);

        popupContent.getChildren().add(warningText);

        HBox popupControls = new HBox();
        popupControls.setAlignment(Pos.CENTER);
        popupControls.setSpacing(40);
        popupControls.getChildren().add(cancelOverwriteButton);
        popupControls.getChildren().add(okOverwriteButton);

        popupContent.getChildren().add(popupControls);

        packageFileExistsWarningPopup.setContent(popupContent);

        packageFileExistsWarningPopup.setOwner(getScene().getWindow());
        packageFileExistsWarningPopup.show();
    }

    @Override
    public PackageToolPopup getSuccessPopup() {
        return packageGenerationSuccessPopup;
    }

    @Override
    public PackageToolPopup getFileOverwriteWarningPopup() { return packageFileExistsWarningPopup; }

    @Override
    public Hyperlink getNoThanksLink() {
        return noThanksLink;
    }

    @Override
    public Button getCreateNewPackageButton() {
        return createAnotherPackageButton;
    }

    @Override
    public Button getCancelFileOverwriteButton() { return cancelOverwriteButton; }

    @Override
    public Button getOkFileOverwriteButton() { return okOverwriteButton; }

    @Override
    public CheckBox getMd5CheckBox() {
        return md5CheckBox;
    }

    @Override
    public CheckBox getSHA1CheckBox() {
        return sha1CheckBox;
    }

    @Override
    public ToggleGroup getSerializationToggleGroup() {
        return serializationToggleGroup;
    }

    @Override
    public RadioButton getJSONRadioButton() {
        return jsonRadioButton;
    }

    @Override
    public RadioButton getXMLRadioButton() {
        return xmlRadioButton;
    }

    @Override
    public RadioButton getTurtleRadioButton() {
        return turtleRadioButton;
    }

    @Override
    public PackageToolPopup getProgressPopup() {
        if (progressDialogPopup == null) {
            progressDialogPopup = new ProgressDialogPopup();
            progressDialogPopup.setTitleText(TextFactory.getText(LabelKey.GENERATING_PACKAGE_LABEL));
        }
        if (getScene() != null && getScene().getWindow() != null) {
            double x = getScene().getWindow().getX() + getScene().getWidth()/2.0 - 150;
            double y = getScene().getWindow().getY() + getScene().getHeight()/2.0 - 150;
            progressDialogPopup.setOwner(getScene().getWindow());
            progressDialogPopup.show(x, y);
            progressDialogPopup.hide();

            //Get the content width and height to property center the popup.
            x = getScene().getWindow().getX() + getScene().getWidth()/2.0 - progressDialogPopup.getWidth()/2.0;
            y = getScene().getWindow().getY() + getScene().getHeight()/2.0 - progressDialogPopup.getHeight()/2.0;
            progressDialogPopup.setOwner(getScene().getWindow());
            progressDialogPopup.show(x, y);
        }
        return progressDialogPopup;
    }

    @Override
    public void scrollToTop() {
        contentScrollPane.setVvalue(0);
    }

    public void loadAvailableProjects(String availableProjectsFilePath) {
        List<String> projects = new ArrayList<>();
        try {
            InputStream fileStream;
            if(availableProjectsFilePath.startsWith("classpath:")) {
                String path = availableProjectsFilePath.substring("classpath:".length());
                if (!path.startsWith("/")){
                    path = "/" + path;
                }
                fileStream = PackageGenerationViewImpl.class.getResourceAsStream(path);
            } else {
                fileStream = new FileInputStream(availableProjectsFilePath);
            }
            if (fileStream != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        projects.add(line);
                    }
                }
                br.close();
            } else {
                log.error("Error loading available projects.");
            }
        } catch (IOException e) {
            log.error("Error loading available projects.");
        }

    }
}