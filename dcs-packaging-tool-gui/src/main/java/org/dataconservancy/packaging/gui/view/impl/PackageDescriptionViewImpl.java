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

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.dataconservancy.dcs.util.DisciplineLoadingService;
import org.dataconservancy.packaging.gui.Errors;
import org.dataconservancy.packaging.gui.Errors.ErrorKey;
import org.dataconservancy.packaging.gui.Help.HelpKey;
import org.dataconservancy.packaging.gui.InternalProperties;
import org.dataconservancy.packaging.gui.Labels;
import org.dataconservancy.packaging.gui.Labels.LabelKey;
import org.dataconservancy.packaging.gui.Messages;
import org.dataconservancy.packaging.gui.OntologyLabels;
import org.dataconservancy.packaging.gui.model.Relationship;
import org.dataconservancy.packaging.gui.presenter.PackageDescriptionPresenter;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;
import org.dataconservancy.packaging.gui.view.PackageDescriptionView;
import org.dataconservancy.packaging.tool.api.PackageOntologyService;
import org.dataconservancy.packaging.tool.model.PackageArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * Implementation of the view that displays the package description tree, and the controls for applying inherited metadata. 
 */
public class PackageDescriptionViewImpl extends BaseViewImpl<PackageDescriptionPresenter> implements PackageDescriptionView {
    
    private Labels labels;
    private Messages messages;
    private TreeTableView<PackageArtifact> artifactTree;

    private Stage artifactDetailsWindow;
    private Scene artifactDetailsScene;
    private PackageArtifact popupArtifact;
    private PackageOntologyService packageOntologyService;

    //Warning popup and controls
    public PackageToolPopup warningPopup;
    private Button warningPopupPositiveButton;
    private Button warningPopupNegativeButton;
    private CheckBox hideFutureWarningPopupCheckBox;

    private Button reenableWarningsButton;

    private Label errorMessageLabel;
    
    //Metadata Inheritance Controls
    private Map<String, CheckBox> metadataInheritanceButtonMap;
    
    //File chooser for where to save package description. 
    private FileChooser packageDescriptionFileChooser;

    //Full Path checkbox
    private CheckBox fullPath;

    //Whether to show ignored Artifacts
    private CheckBox showIgnored;
 
    //Controls that are displayed in the package artifact popup.
    private Hyperlink cancelPopupLink;
    private Button applyPopupButton;

    //Storage for mapping popup fields to properties on the artifacts. 
    private Map<String, PackageDescriptionViewImpl.ArtifactPropertyContainer> artifactPropertyFields;
    private Set<ArtifactRelationshipContainer> artifactRelationshipFields;
    
    private Errors errors;
    private OntologyLabels ontologyLabels;
    private InternalProperties internalProperties;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Preferences preferences;
    private PackageArtifactWindowBuilder windowBuilder;

    private static final String synthesizedArtifactMarker = " *";

    public PackageDescriptionViewImpl(final Labels labels, final Errors errors, final Messages messages, final OntologyLabels ontologyLabels,
                                      final InternalProperties internalProperties, final String availableRelationshipsPath, DisciplineLoadingService disciplineService) {
        super(labels);
        this.labels = labels;        
        this.errors = errors;
        this.messages = messages;
        this.ontologyLabels = ontologyLabels;
        this.internalProperties = internalProperties;

        //Sets the text of the footer controls.
        getContinueButton().setText(labels.get(LabelKey.SAVE_AND_CONTINUE_BUTTON));
        getCancelLink().setText(labels.get(LabelKey.BACK_LINK));
        getSaveButton().setText(labels.get(LabelKey.SAVE_BUTTON));
        getSaveButton().setVisible(true);
        VBox content = new VBox();

        content.getStyleClass().add(PACKAGE_DESCRIPTION_VIEW_CLASS);
        setCenter(content);

        artifactDetailsWindow = new Stage();
        artifactDetailsWindow.setMinWidth(540);
        artifactDetailsWindow.setMinHeight(500);

        preferences = Preferences.userRoot().node(internalProperties.get(InternalProperties.InternalPropertyKey.PREFERENCES_NODE_NAME));
        boolean hideWarningPopup = preferences.getBoolean(internalProperties.get(InternalProperties.InternalPropertyKey.HIDE_PROPERTY_WARNING_PREFERENCE), false);

        HBox buttonBar = new HBox(24);
        buttonBar.setAlignment(Pos.TOP_RIGHT);
        reenableWarningsButton = new Button(labels.get(LabelKey.RENABLE_PROPERTY_WARNINGS_BUTTON));
        reenableWarningsButton.setVisible(hideWarningPopup);
        reenableWarningsButton.setPrefWidth(23 * rem);
        buttonBar.getChildren().add(reenableWarningsButton);
        content.getChildren().add(buttonBar);

        //Creates the error message that appears at the top of the screen.
        errorMessageLabel = new Label();
        errorMessageLabel.setTextFill(Color.RED);
        errorMessageLabel.setVisible(false);
        errorMessageLabel.setWrapText(true);
        errorMessageLabel.setMaxWidth(600);
        content.getChildren().add(errorMessageLabel);

        //Creates the file chooser that's used to save the package description to a file.
        packageDescriptionFileChooser = new FileChooser();
        packageDescriptionFileChooser.setTitle(labels.get(LabelKey.PACKAGE_DESCRIPTION_FILE_CHOOSER_KEY));
        packageDescriptionFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Package Description (*.json)", "*.json"));
        packageDescriptionFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files (*.*)", "*.*"));

        //Toggles whether the full paths should be displayed in the package artifact tree. 
        fullPath = new CheckBox(labels.get(LabelKey.SHOW_FULL_PATHS));
        fullPath.selectedProperty().addListener((ov, old_val, new_val) -> {
            presenter.rebuildTreeView();
        });

        showIgnored = new CheckBox(labels.get(LabelKey.SHOW_IGNORED));
        showIgnored.selectedProperty().setValue(true);
        showIgnored.selectedProperty().addListener((observableValue, aBoolean, aBoolean2) -> {
            presenter.rebuildTreeView();
        });

        if(Platform.isFxApplicationThread()){
            Tooltip t = new Tooltip(labels.get(LabelKey.SHOW_FULL_PATHS_TIP));
            t.setPrefWidth(300);
            t.setWrapText(true);
            fullPath.setTooltip(t);
        }
        if(Platform.isFxApplicationThread()){
            Tooltip t = new Tooltip(labels.get(LabelKey.SHOW_IGNORED_TIP));
            t.setPrefWidth(300);
            t.setWrapText(true);
            showIgnored.setTooltip(t);
        }

        content.getChildren().add(fullPath);
        content.getChildren().add(showIgnored);

        Label syntheticArtifactLabel = new Label(labels.get(LabelKey.SYNTHESIZED_ARTIFACT_NOTATION));

        content.getChildren().add(syntheticArtifactLabel);

        //The main element of the view a tree of all the package artifacts.
        artifactTree = new TreeTableView<>();

        //disable column sorting in the view
        artifactTree.setSortPolicy(treeTableView -> false);

        artifactTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (artifactDetailsWindow.isShowing()) {
                presenter.saveCurrentArtifact();
                showArtifactDetails(newValue.getValue(), null);
            }
        });

        //set up the columns for the artifact, its type and the options control
        TreeTableColumn<PackageArtifact, Label> artifactColumn = new TreeTableColumn<>("Artifact");
        TreeTableColumn<PackageArtifact, Label> typeColumn = new TreeTableColumn<>("Type");
        TreeTableColumn<PackageArtifact, Label> optionsColumn = new TreeTableColumn<>("");

        //make the last two columns fixed width, and the first column variable, so that increasing window width widens the first column
        typeColumn.setPrefWidth(100); //make wide enough so that any displayed text will not truncate
        optionsColumn.setPrefWidth(42); //make wide enough to comfortably fit image and vertical scroll bar
        //add 2 here to get rid of horizontal scroll bar
        artifactColumn.prefWidthProperty().bind(artifactTree.widthProperty().subtract(typeColumn.getWidth() + optionsColumn.getWidth() + 2));

        artifactColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<PackageArtifact, Label>, ObservableValue<Label>>() {
            public ObservableValue<Label> call(TreeTableColumn.CellDataFeatures<PackageArtifact, Label> p) {
                // p.getValue() returns the TreeItem<PackageArtifact> instance for a particular TreeTableView row,
                // p.getValue().getValue() returns the PackageArtifact instance inside the TreeItem<PackageArtifact>
                PackageArtifact packageArtifact = p.getValue().getValue();
                Label viewLabel = new Label();
                viewLabel.setPrefWidth(artifactColumn.getWidth());
                viewLabel.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);

                String fragment = packageArtifact.getArtifactRef().getFragment();
                String labelText;

                if (getFullPathCheckBox().selectedProperty().getValue()) {
                    labelText = packageArtifact.getArtifactRef().getRefPath();
                } else {
                    labelText = packageArtifact.getArtifactRef().getRefName();
                }

                if (fragment != null) {
                    labelText = labelText + synthesizedArtifactMarker;
                }
                viewLabel.setText(labelText);


                Tooltip t = new Tooltip(viewLabel.getText());
                t.setPrefWidth(300);
                t.setWrapText(true);
                viewLabel.setTooltip(t);

                return new ReadOnlyObjectWrapper<>(viewLabel);
            }
        });

        typeColumn.setCellValueFactory(p -> {
            // p.getValue() returns the TreeItem<PackageArtifact> instance for a particular TreeTableView row,
            // p.getValue().getValue() returns the PackageArtifact instance inside the TreeItem<PackageArtifact>
            String type = p.getValue().getValue().getType();
            Label typeLabel = new Label(type);
            typeLabel.setPrefWidth(typeColumn.getWidth());
            return new ReadOnlyObjectWrapper<>(typeLabel);
        });

        optionsColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<PackageArtifact, Label>, ObservableValue<Label>>() {
            public ObservableValue<Label> call(TreeTableColumn.CellDataFeatures<PackageArtifact, Label> p) {
                // p.getValue() returns the TreeItem<PackageArtifact> instance for a particular TreeTableView row,
                // p.getValue().getValue() returns the PackageArtifact instance inside the TreeItem<PackageArtifact>
                PackageArtifact packageArtifact = p.getValue().getValue();
                Label optionsLabel = new Label();
                final ContextMenu contextMenu = new ContextMenu();
                TreeItem<PackageArtifact> treeItem = p.getValue();
                ImageView image = new ImageView();
                image.setFitHeight(20);
                image.setFitWidth(20);
                image.getStyleClass().add(ARROWS_IMAGE);
                optionsLabel.setGraphic(image);
                //make sure the current artifact type is valid - this status may have changed if its
                //parent's type has changed
                if (packageArtifact.isIgnored()) {
                    contextMenu.getItems().add(createIgnoreMenuItem(treeItem));
                } else {
                    Set<String> validTypeSet = presenter.getValidTypes(packageArtifact);
                    List<String> validTypes = new ArrayList<>();
                    validTypes.addAll(validTypeSet);
                    if (validTypes.size() > 0 ) {
                        if (!validTypes.contains(packageArtifact.getType())) {
                            String oldType = packageArtifact.getType();
                            presenter.changeType(packageArtifact, validTypes.get(0));

                            log.warn("Changing artifact " + packageArtifact.getArtifactRef() + " from " + oldType + " to " + packageArtifact.getType());
                            //Notify the user that we are changing types
                            showWarningPopup(errors.get(ErrorKey.PACKAGE_DESCRIPTION_CHANGE_WARNING), messages.formatPackageDescriptionModificationWarning(packageArtifact.getArtifactRef().getRefString(), oldType, packageArtifact.getType()), false, false);
                            getWarningPopupPositiveButton().setOnAction(actionEvent -> getWarningPopup().hide());
                        }
                            contextMenu.getItems().addAll(createMenuItemList(treeItem, validTypes, optionsLabel));
                            optionsLabel.setContextMenu(contextMenu);
                    }
                }

                //When the options label is clicked show the context menu.
                optionsLabel.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        contextMenu.show(optionsLabel, e.getScreenX(), e.getScreenY());
                    }
                });
                return new ReadOnlyObjectWrapper<>(optionsLabel);
            }
        });


        artifactTree.getColumns().addAll(artifactColumn, typeColumn, optionsColumn);

        //set up row factory to allow for a little alternate row styling for ignored package artifacts
        artifactTree.setRowFactory(new Callback<TreeTableView<PackageArtifact>, TreeTableRow<PackageArtifact>>() {
            @Override
             public TreeTableRow<PackageArtifact> call(TreeTableView<PackageArtifact> ttv) {
                return new TreeTableRow<PackageArtifact>() {
                    @Override
                    public void updateItem(PackageArtifact packageArtifact, boolean empty) {
                        super.updateItem(packageArtifact, empty);
                        if (packageArtifact != null && packageArtifact.isIgnored()) {
                            getStyleClass().add(PACKAGE_DESCRIPTION_ROW_IGNORE);
                        } else {
                            getStyleClass().removeAll(PACKAGE_DESCRIPTION_ROW_IGNORE);
                        }
                    }
                };
            }
        });

        content.getChildren().add(artifactTree);
        VBox.setVgrow(artifactTree, Priority.ALWAYS);

        //Controls for the package artifact popup
        cancelPopupLink = new Hyperlink(labels.get(LabelKey.CANCEL_BUTTON));
        applyPopupButton = new Button(labels.get(LabelKey.APPLY_BUTTON));

        //Controls for the validation error popup.
        warningPopupPositiveButton = new Button(labels.get(LabelKey.OK_BUTTON));
        warningPopupNegativeButton = new Button(labels.get(LabelKey.CANCEL_BUTTON));
        hideFutureWarningPopupCheckBox = new CheckBox(labels.get(LabelKey.DONT_SHOW_WARNING_AGAIN_CHECKBOX));

        //Instantiating metadata inheritance button map
        metadataInheritanceButtonMap = new HashMap<>();

        windowBuilder = new PackageArtifactWindowBuilder(labels, ontologyLabels, cancelPopupLink, applyPopupButton, availableRelationshipsPath, disciplineService, messages);
    }

    @Override
    public TreeItem<PackageArtifact> getRoot() {
        return artifactTree.getRoot(); 
    }

    @Override
    public TreeTableView<PackageArtifact> getArtifactTreeView() {
        return artifactTree;
    }

    @Override
    public FileChooser getPackageDescriptionFileChooser(){
        File existingDescriptionFile = presenter.getController().getPackageDescriptionFile();
        if(existingDescriptionFile != null){
            packageDescriptionFileChooser.setInitialDirectory(existingDescriptionFile.getParentFile());
            packageDescriptionFileChooser.setInitialFileName(existingDescriptionFile.getName());
        } else if (presenter.getController().getRootArtifactDir() != null) {
            String fileName = presenter.getController().getRootArtifactDir().getName();
            if (fileName.isEmpty()) {
                fileName = "description";
            }
            packageDescriptionFileChooser.setInitialFileName(fileName + ".json");
            if (presenter.getController().getContentRoot() != null) {
                packageDescriptionFileChooser.setInitialDirectory(presenter.getController().getContentRoot());
            } else {
                packageDescriptionFileChooser.setInitialDirectory(presenter.getController().getRootArtifactDir());
            }
        } else {
            packageDescriptionFileChooser.setInitialFileName("default.json");
        }
        return packageDescriptionFileChooser;
    }

    @Override
    public CheckBox getFullPathCheckBox(){
        return fullPath;
    }

    /*
     * Create the menu items for a package artifact.
     * @param packageArtifact
     * @param validTypes
     * @param label
     * @return
     */
    private List<MenuItem> createMenuItemList(final TreeItem<PackageArtifact> treeItem, List<String> validTypes, final Label label){
        List<MenuItem> itemList = new ArrayList<>();

        final PackageArtifact packageArtifact = treeItem.getValue();

        //Create a menu item that will show the package artifacts popup.
        MenuItem item = new MenuItem(labels.get(LabelKey.PROPERTIES_LABEL));
        itemList.add(item);
        item.setOnAction(actionEvent -> {
            VBox detailsBox = new VBox();
            detailsBox.getStyleClass().add(PACKAGE_ARTIFACT_POPUP);
            showArtifactDetails(packageArtifact, label);
            artifactTree.getSelectionModel().select(treeItem);
        });

        //Create a menu item for each available artifact type.
        if(validTypes.size() > 0) {
            SeparatorMenuItem separator =  new SeparatorMenuItem();
            separator.setStyle("-fx-color: #FFFFFF");
            itemList.add(separator);

            for (final String type : validTypes) {
                final List<String> invalidProperties = presenter.findInvalidProperties(packageArtifact, type);

                String prefix = type.equals(packageArtifact.getType()) ? labels.get(LabelKey.KEEP_TYPE_LABEL) + " " :  labels.get(LabelKey.CHANGE_TYPE_LABEL) + " ";
                item = new MenuItem(prefix + ontologyLabels.get(type));

                itemList.add(item);

                if (!invalidProperties.isEmpty() && !type.equals(packageArtifact.getType())) {
                    ImageView invalidImage = new ImageView("/images/orange_exclamation.png");
                    invalidImage.setFitWidth(8);
                    invalidImage.setFitHeight(24);
                    item.setGraphic(invalidImage);
                }

                item.setOnAction(actionEvent -> {
                    boolean hideWarningPopup = preferences.getBoolean(internalProperties.get(InternalProperties.InternalPropertyKey.HIDE_PROPERTY_WARNING_PREFERENCE), false);

                    if (!invalidProperties.isEmpty() && !hideWarningPopup && !type.equals(packageArtifact.getType())) {
                        showWarningPopup(errors.get(ErrorKey.PROPERTY_LOSS_WARNING), messages.formatInvalidPropertyWarning(type, formatInvalidProperties(invalidProperties)), true, true);
                        getWarningPopupNegativeButton().setOnAction(actionEvent1 -> {
                            getWarningPopup().hide();
                            preferences.putBoolean(internalProperties.get(InternalProperties.InternalPropertyKey.HIDE_PROPERTY_WARNING_PREFERENCE), hideFutureWarningPopupCheckBox.selectedProperty().getValue());
                        });

                        getWarningPopupPositiveButton().setOnAction(actionEvent1 -> {
                            getWarningPopup().hide();
                            presenter.changeType(packageArtifact, type);
                            preferences.putBoolean(internalProperties.get(InternalProperties.InternalPropertyKey.HIDE_PROPERTY_WARNING_PREFERENCE), hideFutureWarningPopupCheckBox.selectedProperty().getValue());
                        });
                    } else {
                        presenter.changeType(packageArtifact, type);
                    }
                });
            }
        }

        SeparatorMenuItem separator =  new SeparatorMenuItem();
        separator.setStyle("-fx-color: #FFFFFF");
        itemList.add(separator);

        if (presenter.canCollapseParentArtifact(packageArtifact)) {

            item = new MenuItem("Collapse up one level");

            item.setOnAction(actionEvent -> {
                showWarningPopup(errors.get(ErrorKey.ARTIFACT_LOSS_WARNING),
                        errors.get(ErrorKey.ARTIFACT_LOSS_WARNING_MESSAGE), true, false);

                getWarningPopupNegativeButton().setOnAction(actionEvent1 -> getWarningPopup().hide());

                getWarningPopupPositiveButton().setOnAction(actionEvent1 -> {
                    getWarningPopup().hide();
                    presenter.collapseParentArtifact(packageArtifact);
                    TreeItem item1 = presenter.findItem(packageArtifact);
                    presenter.displayPackageTree();
                    item1.setExpanded(true);
                });
            });
            itemList.add(item);
            separator =  new SeparatorMenuItem();
            separator.setStyle("-fx-color: #FFFFFF");
            itemList.add(separator);

        }

        itemList.add(createIgnoreMenuItem(treeItem));
                
       return itemList;
    }

    private MenuItem createIgnoreMenuItem(final TreeItem<PackageArtifact> treeItem) {
        final CheckMenuItem ignoreCheck = new CheckMenuItem(labels.get(LabelKey.IGNORE_CHECKBOX));
        ignoreCheck.setSelected(treeItem.getValue().isIgnored());

        ignoreCheck.setOnAction(event -> toggleItemIgnore(treeItem, ignoreCheck.isSelected()));

        return ignoreCheck;
    }

    //Note this code is broken out into a protected method so that it can be executed by the test
    protected void toggleItemIgnore(TreeItem<PackageArtifact> node, boolean status) {
        if (status) {
            setIgnoredDescendants(node, true);
        } else {
            setIgnoredAncestors(node, false);
            setIgnoredDescendants(node, false);
        }

        // Rebuild the entire TreeView and reselect the current PackageArtifact

        presenter.rebuildTreeView();
        artifactTree.getSelectionModel().select(presenter.findItem(node.getValue()));
    }

    private void setIgnoredAncestors(TreeItem<PackageArtifact> node, boolean status) {
        do {
            node.getValue().setIgnored(status);        
        } while ((node = node.getParent()) != null); 
    }

    private void setIgnoredDescendants(TreeItem<PackageArtifact> node, boolean status) {
        node.getValue().setIgnored(status);        

        for (TreeItem<PackageArtifact> kid: node.getChildren()) {
            setIgnoredDescendants(kid, status);
        }
    }
    
    //Creates a string that lists all the invalid properties in single line list.
    private String formatInvalidProperties(List<String> invalidProperties) {
        String invalidPropertyString = "";

        for (String property : invalidProperties) {
            invalidPropertyString += ontologyLabels.get(property) + "\n";
        }

        return invalidPropertyString;
    }

    @Override
    public void showArtifactDetails(PackageArtifact artifact, Node anchorNode) {

        popupArtifact = artifact;

        //Initialize the containers that are used to hold the text fields.
        artifactPropertyFields = new HashMap<>();
        artifactRelationshipFields = new HashSet<>();

        Pane propertiesPane = windowBuilder.buildArtifactPropertiesLayout(artifact, artifactPropertyFields, artifactRelationshipFields, metadataInheritanceButtonMap, presenter, packageOntologyService);

        if (artifactDetailsScene == null) {
            artifactDetailsScene = new Scene(propertiesPane, 540, 500);
        } else {
            artifactDetailsScene.setRoot(propertiesPane);
        }
        artifactDetailsWindow.setTitle(artifact.getArtifactRef().getRefName() + " Properties");
        artifactDetailsWindow.setScene(artifactDetailsScene);

        if(!artifactDetailsWindow.isShowing()) {
            if (anchorNode != null) {
                Point2D point = anchorNode.localToScene(0.0, 0.0);
                double x = getScene().getWindow().getX() + point.getX();
                double y = getScene().getWindow().getY() + point.getY();

                //X and Y are now the location of the menu, offset slightly from that
                x -= 600;
                y -= 80;

                artifactDetailsWindow.setX(x);
                artifactDetailsWindow.setY(y);
            }
            artifactDetailsWindow.show();
        }
    }

    @Override
    public Stage getArtifactDetailsWindow() {
        return artifactDetailsWindow;
    }

    @Override
    public Button getReenableWarningsButton() {
        return reenableWarningsButton;
    }

    public void setPackageOntologyService(PackageOntologyService service) {
        this.packageOntologyService = service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Hyperlink getCancelPopupHyperlink() {
        return cancelPopupLink;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Button getApplyPopupButton() {
        return applyPopupButton;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ArtifactPropertyContainer> getArtifactPropertyFields() {
        return artifactPropertyFields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PackageArtifact getPopupArtifact() {
        return popupArtifact;
    }

    public void setPopupArtifact (PackageArtifact artifact) {
        popupArtifact = artifact;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public PackageToolPopup getWarningPopup() {
        return warningPopup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showWarningPopup(String title, String errorMessage, boolean allowNegative, boolean allowFutureHide) {
        if (warningPopup == null) {
            warningPopup = new PackageToolPopup();
        }

        warningPopup.setTitleText(title);

        VBox content = new VBox(48);
        content.setPrefWidth(300);
        Label errorMessageLabel = new Label(errorMessage);
        errorMessageLabel.setWrapText(true);
        content.getChildren().add(errorMessageLabel);

        if (allowFutureHide) {
            content.getChildren().add(hideFutureWarningPopupCheckBox);
        }

        HBox controls = new HBox(16);
        controls.setAlignment(Pos.BOTTOM_RIGHT);
        if (allowNegative) {
            controls.getChildren().add(warningPopupNegativeButton);
        }
        controls.getChildren().add(warningPopupPositiveButton);
        content.getChildren().add(controls);
        
        warningPopup.setContent(content);

        //Quickly display the popup so we can measure the content
        double x = getScene().getWindow().getX() + getScene().getWidth()/2.0 - 150;
        double y = getScene().getWindow().getY() + getScene().getHeight()/2.0 - 150;
        warningPopup.setOwner(getScene().getWindow());
        warningPopup.show(x, y);
        warningPopup.hide();

        //Get the content width and height to property center the popup.
        x = getScene().getWindow().getX() + getScene().getWidth()/2.0 - content.getWidth()/2.0;
        y = getScene().getWindow().getY() + getScene().getHeight()/2.0 - content.getHeight()/2.0;
        warningPopup.setOwner(getScene().getWindow());
        warningPopup.show(x, y);

    }
    
    @Override 
    public Button getWarningPopupPositiveButton() {
        return warningPopupPositiveButton;
    }

    @Override
    public Button getWarningPopupNegativeButton() {
        return warningPopupNegativeButton;
    }

    @Override
    public CheckBox getHideFutureWarningPopupCheckbox() {
        return hideFutureWarningPopupCheckBox;
    }

    /**
     * Simple Container to store property text fields. Simple one value properties are stored in the value field.
     * Complex property values are stored in the subProperties map.
     */
    public static class ArtifactPropertyContainer {
        Set<Map<String, Set<StringProperty>>> subProperties = new HashSet<>();
        Set<StringProperty> values = new HashSet<>();
        boolean isComplex = false;

        public boolean isComplex() {
            return isComplex;
        }

        public Set<StringProperty> getValues() {
            return values;
        }

        public Set<Map<String, Set<StringProperty>>> getSubProperties() {
            return subProperties;
        }
    }

    /**
     * Simple container class for holding artifact relationships
     * text fields to link from the UI to artifact.
     */
    public static class ArtifactRelationshipContainer {
        public ObservableValue<Relationship> relationship;
        public Set<StringProperty> relationshipTargets = new HashSet<>();
        public BooleanProperty requiresURI = new SimpleBooleanProperty(true);

        public ObservableValue<Relationship> getRelationship() {
            return relationship;
        }

        public Set<StringProperty> getRelationshipTargets() {
            return relationshipTargets;
        }

        public BooleanProperty requiresURI() { return requiresURI; }
    }

    @Override
    public Set<ArtifactRelationshipContainer> getArtifactRelationshipFields() {
        return artifactRelationshipFields;
    }

    @Override
    public Label getErrorMessageLabel() {
        return errorMessageLabel;
    }

    @Override
    public CheckBox getShowIgnored() {
        return showIgnored;
    }

    @Override
    public Map<String, CheckBox> getInheritMetadataCheckBoxMap() {
        return metadataInheritanceButtonMap;
    }

    @Override
    public void setupHelp() {
        Label helpText = new Label(help.get(HelpKey.PACKAGE_DESCRIPTION_HELP));
        helpText.setMaxWidth(300);
        helpText.setWrapText(true);
        helpText.setTextAlignment(TextAlignment.CENTER);
        setHelpPopupContent(helpText);         
    }
}
