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
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
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
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.dataconservancy.dcs.util.DisciplineLoadingService;
import org.dataconservancy.packaging.gui.Help.HelpKey;
import org.dataconservancy.packaging.gui.InternalProperties;
import org.dataconservancy.packaging.gui.Labels.LabelKey;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.model.Relationship;
import org.dataconservancy.packaging.gui.presenter.EditPackageContentsPresenter;
import org.dataconservancy.packaging.gui.util.PackageToolPopup;
import org.dataconservancy.packaging.gui.util.ProfilePropertyBox;
import org.dataconservancy.packaging.gui.view.EditPackageContentsView;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.api.IPMService;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class EditPackageContentsViewImpl extends BaseViewImpl<EditPackageContentsPresenter> implements EditPackageContentsView {

    private TreeTableView<Node> artifactTree;

    private Stage artifactDetailsWindow;
    private Scene artifactDetailsScene;
    private Node popupNode;
    private DomainProfileService profileService;
    private IPMService ipmService;

    //Warning popup and controls
    public PackageToolPopup warningPopup;
    private Button warningPopupPositiveButton;
    private Button warningPopupNegativeButton;
    private CheckBox hideFutureWarningPopupCheckBox;

    private Button reenableWarningsButton;

    private Label errorMessageLabel;

    //Metadata Inheritance Controls
    private Map<PropertyType, CheckBox> metadataInheritanceButtonMap;

    //File chooser for where to save package description. 
    private FileChooser packageStateFileChooser;

    //Full Path checkbox
    private CheckBox fullPath;

    //Whether to show ignored Artifacts
    private CheckBox showIgnored;

    //Controls that are displayed in the package artifact popup.
    private Hyperlink cancelPopupLink;
    private Button applyPopupButton;

    //Storage for mapping popup fields to properties on the artifacts. 
    private Set<NodeRelationshipContainer> artifactRelationshipFields;

    private InternalProperties internalProperties;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Preferences preferences;
    private NodePropertyWindowBuilder windowBuilder;

    private String availableRelationshipsPath;
    private PackageToolPopup refreshPopup;
    private Button refreshPopupPositiveButton;
    private Button refreshPopupNegativeButton;

    public EditPackageContentsViewImpl(final InternalProperties internalProperties, final String availableRelationshipsPath) {
        super();
        this.internalProperties = internalProperties;
        this.availableRelationshipsPath = availableRelationshipsPath;

        //Sets the text of the footer controls.
        getContinueButton().setText(TextFactory.getText(LabelKey.SAVE_AND_CONTINUE_BUTTON));
        getCancelLink().setText(TextFactory.getText(LabelKey.BACK_LINK));
        getSaveButton().setText(TextFactory.getText(LabelKey.SAVE_BUTTON));
        getSaveButton().setVisible(true);
        VBox content = new VBox();

        content.getStyleClass().add(PACKAGE_DESCRIPTION_VIEW_CLASS);
        setCenter(content);

        if (Platform.isFxApplicationThread()) {
            artifactDetailsWindow = new Stage();
            artifactDetailsWindow.setMinWidth(540);
            artifactDetailsWindow.setMinHeight(500);
        }

        preferences = Preferences.userRoot().node(internalProperties.get(InternalProperties.InternalPropertyKey.PREFERENCES_NODE_NAME));
        boolean hideWarningPopup = preferences.getBoolean(internalProperties.get(InternalProperties.InternalPropertyKey.HIDE_PROPERTY_WARNING_PREFERENCE), false);

        HBox buttonBar = new HBox(24);
        buttonBar.setAlignment(Pos.TOP_RIGHT);
        reenableWarningsButton = new Button(TextFactory.getText(LabelKey.RENABLE_PROPERTY_WARNINGS_BUTTON));
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
        packageStateFileChooser = new FileChooser();
        packageStateFileChooser.setTitle(TextFactory.getText(LabelKey.PACKAGE_DESCRIPTION_FILE_CHOOSER_KEY));
        packageStateFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Package Description (*.json)", "*.json"));
        packageStateFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files (*.*)", "*.*"));

        //Toggles whether the full paths should be displayed in the package artifact tree. 
        fullPath = new CheckBox(TextFactory.getText(LabelKey.SHOW_FULL_PATHS));
        fullPath.selectedProperty().addListener((ov, old_val, new_val) -> {
            presenter.rebuildTreeView();
        });

        showIgnored = new CheckBox(TextFactory.getText(LabelKey.SHOW_IGNORED));
        showIgnored.selectedProperty().setValue(true);
        showIgnored.selectedProperty().addListener((observableValue, aBoolean, aBoolean2) -> {
            presenter.rebuildTreeView();
        });

        if(Platform.isFxApplicationThread()){
            Tooltip t = new Tooltip(TextFactory.getText(LabelKey.SHOW_FULL_PATHS_TIP));
            t.setPrefWidth(300);
            t.setWrapText(true);
            fullPath.setTooltip(t);
        }
        if(Platform.isFxApplicationThread()){
            Tooltip t = new Tooltip(TextFactory.getText(LabelKey.SHOW_IGNORED_TIP));
            t.setPrefWidth(300);
            t.setWrapText(true);
            showIgnored.setTooltip(t);
        }

        content.getChildren().add(fullPath);
        content.getChildren().add(showIgnored);

        Label syntheticArtifactLabel = new Label(TextFactory.getText(LabelKey.SYNTHESIZED_ARTIFACT_NOTATION));

        content.getChildren().add(syntheticArtifactLabel);

        //The main element of the view a tree of all the package artifacts.
        artifactTree = new TreeTableView<>();

        //disable column sorting in the view
        artifactTree.setSortPolicy(treeTableView -> false);

        artifactTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (artifactDetailsWindow != null && artifactDetailsWindow.isShowing()) {
                presenter.saveCurrentNode();
                showArtifactDetails(newValue.getValue(), null);
            }
        });

        //set up the columns for the artifact, its type and the options control
        TreeTableColumn<Node, HBox> artifactColumn = new TreeTableColumn<>("Artifact");
        artifactColumn.setResizable(false);
        TreeTableColumn<Node, Label> typeColumn = new TreeTableColumn<>("Type");
        typeColumn.setResizable(false);
        TreeTableColumn<Node, Label> optionsColumn = new TreeTableColumn<>("");
        optionsColumn.setResizable(false);

        //make the last two columns fixed width, and the first column variable, so that increasing window width widens the first column
        typeColumn.setPrefWidth(100); //make wide enough so that any displayed text will not truncate
        optionsColumn.setPrefWidth(42); //make wide enough to comfortably fit image and vertical scroll bar
        //add 2 here to get rid of horizontal scroll bar
        artifactColumn.prefWidthProperty().bind(artifactTree.widthProperty().subtract(typeColumn.getWidth() + optionsColumn.getWidth() + 2));

        artifactColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Node, HBox>, ObservableValue<HBox>>() {
            public ObservableValue<HBox> call(TreeTableColumn.CellDataFeatures<Node, HBox> p) {
                // p.getValueAsString() returns the TreeItem<PackageArtifact> instance for a particular TreeTableView row,
                // p.getValueAsString().getValueAsString() returns the PackageArtifact instance inside the TreeItem<PackageArtifact>
                Node packageNode = p.getValue().getValue();

                HBox hbox = new HBox(3);

                ImageView exclamImage = new ImageView();
                exclamImage.getStyleClass().add(EXCLAMATION_IMAGE);
                exclamImage.setFitHeight(12);
                exclamImage.setFitWidth(5);

                Label exclamLabel = new Label();
                exclamLabel.setGraphic(exclamImage);

                Tooltip exclamTooltip = new Tooltip(TextFactory.getText(LabelKey.FILE_MISSING_TIP));
                exclamTooltip.setPrefWidth(300);
                exclamTooltip.setWrapText(true);
                exclamTooltip.setFont(Font.font(12));
                Tooltip.install(exclamLabel, exclamTooltip);

                if (packageNode.getFileInfo() != null && !ipmService.checkFileInfoIsAccessible(packageNode)) {
                    hbox.getChildren().add(exclamLabel);
                }

                Label viewLabel = new Label();
                viewLabel.setPrefWidth(artifactColumn.getWidth());
                viewLabel.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);

                String labelText;

                if (packageNode.getFileInfo() != null) {
                    if (getFullPathCheckBox().selectedProperty().getValue()) {
                        labelText = packageNode.getFileInfo().getLocation().toString();
                    } else {
                        labelText = packageNode.getFileInfo().getName();
                    }
                    viewLabel.setText(labelText);
                }

                Tooltip t = new Tooltip(viewLabel.getText());
                t.setPrefWidth(300);
                t.setWrapText(true);
                viewLabel.setTooltip(t);

                hbox.getChildren().add(viewLabel);



                return new ReadOnlyObjectWrapper<>(hbox);
            }
        });

        typeColumn.setCellValueFactory(p -> {
            // p.getValueAsString() returns the TreeItem<PackageArtifact> instance for a particular TreeTableView row,
            // p.getValueAsString().getValueAsString() returns the PackageArtifact instance inside the TreeItem<PackageArtifact>
            String type = p.getValue().getValue().getNodeType().getLabel();
            Label typeLabel = new Label(type);
            typeLabel.setPrefWidth(typeColumn.getWidth());
            return new ReadOnlyObjectWrapper<>(typeLabel);
        });

        optionsColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Node, Label>, ObservableValue<Label>>() {
            public ObservableValue<Label> call(TreeTableColumn.CellDataFeatures<Node, Label> p) {
                // p.getValueAsString() returns the TreeItem<PackageArtifact> instance for a particular TreeTableView row,
                // p.getValueAsString().getValueAsString() returns the PackageArtifact instance inside the TreeItem<PackageArtifact>
                Node packageNode = p.getValue().getValue();
                Label optionsLabel = new Label();
                final ContextMenu contextMenu = new ContextMenu();
                TreeItem<Node> treeItem = p.getValue();
                ImageView image = new ImageView();
                image.setFitHeight(20);
                image.setFitWidth(20);
                image.getStyleClass().add(ARROWS_IMAGE);
                optionsLabel.setGraphic(image);
                //make sure the current artifact type is valid - this status may have changed if its
                //parent's type has changed
                if (packageNode.isIgnored()) {
                    contextMenu.getItems().add(createIgnoreMenuItem(treeItem));
                } else {
                    List<NodeTransform> nodeTransforms = profileService.getNodeTransforms(packageNode);

                    contextMenu.getItems().addAll(createMenuItemList(treeItem, nodeTransforms, optionsLabel));
                    optionsLabel.setContextMenu(contextMenu);
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
        artifactTree.setRowFactory(new Callback<TreeTableView<Node>, TreeTableRow<Node>>() {
            @Override
             public TreeTableRow<Node> call(TreeTableView<Node> ttv) {
                return new TreeTableRow<Node>() {
                    @Override
                    public void updateItem(Node packageNode, boolean empty) {
                        super.updateItem(packageNode, empty);
                        if (packageNode != null && packageNode.isIgnored()) {
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
        cancelPopupLink = new Hyperlink(TextFactory.getText(LabelKey.CANCEL_BUTTON));
        applyPopupButton = new Button(TextFactory.getText(LabelKey.APPLY_BUTTON));

        //Controls for the validation error popup.
        warningPopupPositiveButton = new Button(TextFactory.getText(LabelKey.OK_BUTTON));
        warningPopupPositiveButton.getStyleClass().add(CLICKABLE);
        warningPopupNegativeButton = new Button(TextFactory.getText(LabelKey.CANCEL_BUTTON));
        warningPopupNegativeButton.getStyleClass().add(CLICKABLE);
        hideFutureWarningPopupCheckBox = new CheckBox(TextFactory.getText(LabelKey.DONT_SHOW_WARNING_AGAIN_CHECKBOX));

        //Instantiating metadata inheritance button map
        metadataInheritanceButtonMap = new HashMap<>();

        // controls for the refresh popup
        refreshPopupPositiveButton = new Button(TextFactory.getText(LabelKey.ACCEPT_BUTTON));
        refreshPopupPositiveButton.getStyleClass().add(CLICKABLE);
        refreshPopupNegativeButton = new Button(TextFactory.getText(LabelKey.REJECT_BUTTON));
        refreshPopupNegativeButton.getStyleClass().add(CLICKABLE);

       // windowBuilder = new PackageArtifactWindowBuilder(labels, ontologyLabels, cancelPopupLink, applyPopupButton, availableRelationshipsPath, disciplineService, messages);
    }

    @Override
    public TreeItem<Node> getRoot() {
        return artifactTree.getRoot();
    }

    @Override
    public TreeTableView<Node> getArtifactTreeView() {
        return artifactTree;
    }

    public FileChooser getPackageStateFileChooser(){
        File existingDescriptionFile = presenter.getController().getPackageDescriptionFile();
        if(existingDescriptionFile != null){
            packageStateFileChooser.setInitialDirectory(existingDescriptionFile.getParentFile());
            packageStateFileChooser.setInitialFileName(existingDescriptionFile.getName());
        } else if (presenter.getController().getRootArtifactDir() != null) {
            String fileName = presenter.getController().getRootArtifactDir().getName();
            if (fileName.isEmpty()) {
                fileName = "description";
            }
            packageStateFileChooser.setInitialFileName(fileName + ".json");
            if (presenter.getController().getContentRoot() != null) {
                packageStateFileChooser.setInitialDirectory(presenter.getController().getContentRoot());
            } else {
                packageStateFileChooser.setInitialDirectory(presenter.getController().getRootArtifactDir());
            }
        } else {
            packageStateFileChooser.setInitialFileName("default.json");
        }
        return packageStateFileChooser;
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
    private List<MenuItem> createMenuItemList(final TreeItem<Node> treeItem, List<NodeTransform> nodeTransforms, final Label label){
        List<MenuItem> itemList = new ArrayList<>();

        final Node packageNode = treeItem.getValue();

        //Create a menu item that will show the package artifacts popup.
        MenuItem propertiesItem = new MenuItem(TextFactory.getText(LabelKey.PROPERTIES_LABEL));
        itemList.add(propertiesItem);
        propertiesItem.setOnAction(actionEvent -> {
            VBox detailsBox = new VBox();
            detailsBox.getStyleClass().add(PACKAGE_ARTIFACT_POPUP);
            showArtifactDetails(packageNode, label);
            artifactTree.getSelectionModel().select(treeItem);
        });

        // TODO: Addition of these items to the list should be determined by the service
        //Create a menu item that will allow the user to pick a file.
        MenuItem addFileItem = new MenuItem(TextFactory.getText(LabelKey.ADD_FILE_ITEM_LABEL));
        itemList.add(addFileItem);
        addFileItem.setOnAction(event -> {
            File file = presenter.getController().showOpenFileDialog(new FileChooser());
            // TODO: do something with this file.
        });

        //Create a menu item that will allow the user to pick a folder.
        MenuItem addDirItem = new MenuItem(TextFactory.getText(LabelKey.ADD_FOLDER_ITEM_LABEL));
        itemList.add(addDirItem);
        addDirItem.setOnAction(event -> {
            File file = presenter.getController().showOpenDirectoryDialog(new DirectoryChooser());
            // TODO: do something with this dir.
        });

        //Create a menu item that will allow the user to refresh the tree.
        // TODO: the showing of this item should be determined by a service
        MenuItem refreshItem = new MenuItem(TextFactory.getText(LabelKey.REFRESH_ITEM_LABEL));
        itemList.add(refreshItem);
        refreshItem.setOnAction(event -> {
            // TODO: Do the refresh and pass in a RefreshResults object of some sort
            // For now, the refresh is faked out exactly like the mock up.
            showRefreshResultsPopup();
        });

        //Create a menu item that will allow the user to pick a file.
        if (packageNode.getFileInfo() != null && !ipmService.checkFileInfoIsAccessible(packageNode)) {
            MenuItem remapFileItem = new MenuItem(TextFactory.getText(LabelKey.REMAP_ITEM_LABEL));
            itemList.add(remapFileItem);
            remapFileItem.setOnAction(event -> {
                File file = null;
                if (packageNode.getFileInfo().isFile()) {
                    file = presenter.getController().showOpenFileDialog(new FileChooser());
                } else {
                    file = presenter.getController().showOpenDirectoryDialog(new DirectoryChooser());
                }
                if (file != null) {
                    //Remap the node to the new file and check all it's descendants to see if they can be remapped also.
                    remapNode(packageNode, file.toPath());

                    //Redisplay the tree to update the warnings
                    presenter.displayPackageTree();
                }

            });
        }

        //Create a menu item for each available artifact type.
        if (nodeTransforms != null && nodeTransforms.size() > 0) {
            SeparatorMenuItem separator =  new SeparatorMenuItem();
            separator.setStyle("-fx-color: #FFFFFF");
            itemList.add(separator);

            for (final NodeTransform transform : nodeTransforms) {
                //final List<String> invalidProperties = presenter.findInvalidProperties(packageNode, type);

                MenuItem item = new MenuItem(transform.getLabel());

                itemList.add(item);

                /*
                 TODO: Do we still want to warn users about potential property loss, that seems unsustainable.
                if (!invalidProperties.isEmpty() && !type.equals(packageNode.getType())) {
                    ImageView invalidImage = new ImageView("/images/orange_exclamation.png");
                    invalidImage.setFitWidth(8);
                    invalidImage.setFitHeight(24);
                    item.setGraphic(invalidImage);
                }
                */
                item.setOnAction(actionEvent -> {
                    boolean hideWarningPopup = preferences.getBoolean(internalProperties.get(InternalProperties.InternalPropertyKey.HIDE_PROPERTY_WARNING_PREFERENCE), false);

                    /*TODO: Determine what we want to do about possible property loss
                        if (!invalidProperties.isEmpty() && !hideWarningPopup && !type.equals(packageNode.getType())) {

                        showWarningPopup(errors.get(ErrorKey.PROPERTY_LOSS_WARNING), messages.formatInvalidPropertyWarning(type, formatInvalidProperties(invalidProperties)), true, true);
                        getWarningPopupNegativeButton().setOnAction(actionEvent1 -> {
                            getWarningPopup().hide();
                            preferences.putBoolean(internalProperties.get(InternalProperties.InternalPropertyKey.HIDE_PROPERTY_WARNING_PREFERENCE), hideFutureWarningPopupCheckBox.selectedProperty().getValueAsString());
                        });

                        getWarningPopupPositiveButton().setOnAction(actionEvent1 -> {
                            getWarningPopup().hide();
                            presenter.changeType(packageNode, type);
                            preferences.putBoolean(internalProperties.get(InternalProperties.InternalPropertyKey.HIDE_PROPERTY_WARNING_PREFERENCE), hideFutureWarningPopupCheckBox.selectedProperty().getValueAsString());
                        });
                    } else {
                        presenter.changeType(packageNode, type);
                    } */
                    presenter.changeType(packageNode, transform);
                });
            }
        }

        SeparatorMenuItem separator =  new SeparatorMenuItem();
        separator.setStyle("-fx-color: #FFFFFF");
        itemList.add(separator);

        itemList.add(createIgnoreMenuItem(treeItem));

       return itemList;
    }

    private void remapNode(Node node, Path newPath) {
        Path oldPath = Paths.get(node.getFileInfo().getLocation());
        node.setFileInfo(new FileInfo(newPath));

        if (node.getChildren() != null) {
            //If this path can't be relativized we won't automatically remap
            node.getChildren().stream().filter(child -> child.getFileInfo() !=
                null).forEach(child -> {
                try {
                    Path oldRelativePath = Paths.get(child.getFileInfo().getLocation()).relativize(oldPath);
                    Path newChildPath = newPath.resolve(oldRelativePath);
                    if (newChildPath.toFile().exists()) {
                        remapNode(child, newChildPath);
                    }
                } catch (IllegalArgumentException e) {
                    //If this path can't be relativized we won't automatically remap
                }
            });
        }
    }

    private MenuItem createIgnoreMenuItem(final TreeItem<Node> treeItem) {
        final CheckMenuItem ignoreCheck = new CheckMenuItem(TextFactory.getText(LabelKey.IGNORE_CHECKBOX));
        ignoreCheck.setSelected(treeItem.getValue().isIgnored());

        ignoreCheck.setOnAction(event -> toggleItemIgnore(treeItem, ignoreCheck.isSelected()));

        return ignoreCheck;
    }

    //Note this code is broken out into a protected method so that it can be executed by the test
    protected void toggleItemIgnore(TreeItem<Node> node, boolean status) {
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

    private void setIgnoredAncestors(TreeItem<Node> node, boolean status) {
        do {
            node.getValue().setIgnored(status);
        } while ((node = node.getParent()) != null);
    }

    private void setIgnoredDescendants(TreeItem<Node> node, boolean status) {
        node.getValue().setIgnored(status);

        for (TreeItem<Node> kid: node.getChildren()) {
            setIgnoredDescendants(kid, status);
        }
    }

    @Override
    public void showArtifactDetails(Node packageNode, javafx.scene.Node anchorNode) {

        if (artifactDetailsWindow != null) {
            popupNode = packageNode;
            artifactRelationshipFields = new HashSet<>();

            Pane propertiesPane = windowBuilder.buildArtifactPropertiesLayout(packageNode, metadataInheritanceButtonMap, profileService);

            if (artifactDetailsScene == null) {
                artifactDetailsScene = new Scene(propertiesPane, 540, 500);
            } else {
                artifactDetailsScene.setRoot(propertiesPane);
            }

            artifactDetailsWindow.setTitle(
                packageNode.getFileInfo().getName() + " Properties");
            artifactDetailsWindow.setScene(artifactDetailsScene);

            if (!artifactDetailsWindow.isShowing()) {
                if (anchorNode != null) {
                    Point2D point = anchorNode.localToScene(0.0, 0.0);
                    double x = getScene().getWindow().getX() + point.getX();
                    double y = getScene().getWindow().getY() + point.getY();

                    //X and Y are now the location of the menu, offset slightly in from that this ensures if the app is full screen we don't open right on the edge
                    x -= 100;

                    artifactDetailsWindow.setX(x);
                    artifactDetailsWindow.setY(y);
                }
                artifactDetailsWindow.show();
            }
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

    public void setProfileService(DomainProfileService profileService) {
        this.profileService = profileService;
    }

    public void setIpmService(IPMService ipmService) {
        this.ipmService = ipmService;
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
    public List<ProfilePropertyBox> getProfilePropertyBoxes() {
        return windowBuilder.getNodePropertyBoxes();
    }

    /**
     * {@inheritDoc}
     */
    public Node getPopupNode() {
        return popupNode;
    }

    public void setPopupNode(Node artifact) {
        popupNode = artifact;
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

    private void showRefreshResultsPopup() {
        if (refreshPopup == null) {
            refreshPopup = new PackageToolPopup();
        }

        refreshPopup.setTitleText(TextFactory.getText(LabelKey.DETECTED_CHANGES_LABEL));

        VBox content = new VBox(48);
        content.setPrefWidth(300);

        VBox changesVBox = new VBox(4);
        Label changesLabel = new Label("1 new file found");
        changesLabel.setWrapText(true);

        Label changesLabel2 = new Label("/dir/file");
        changesLabel2.setFont(Font.font(12));
        changesLabel2.setPadding(new Insets(0, 0, 0, 10));
        changesLabel2.setWrapText(true);

        changesVBox.getChildren().add(changesLabel);
        changesVBox.getChildren().add(changesLabel2);

        content.getChildren().add(changesVBox);

        HBox controls = new HBox(16);
        controls.setAlignment(Pos.BOTTOM_RIGHT);
        controls.getChildren().add(refreshPopupNegativeButton);
        controls.getChildren().add(refreshPopupPositiveButton);
        content.getChildren().add(controls);

        refreshPopup.setContent(content);

        //Quickly display the popup so we can measure the content
        double x = getScene().getWindow().getX() + getScene().getWidth()/2.0 - 150;
        double y = getScene().getWindow().getY() + getScene().getHeight()/2.0 - 150;
        refreshPopup.setOwner(getScene().getWindow());
        refreshPopup.show(x, y);
        refreshPopup.hide();

        //Get the content width and height to property center the popup.
        x = getScene().getWindow().getX() + getScene().getWidth()/2.0 - content.getWidth()/2.0;
        y = getScene().getWindow().getY() + getScene().getHeight()/2.0 - content.getHeight()/2.0;
        refreshPopup.setOwner(getScene().getWindow());
        refreshPopup.show(x, y);
    }

    @Override
    public Button getWarningPopupNegativeButton() {
        return warningPopupNegativeButton;
    }

    @Override
    public Button getRefreshPopupPositiveButton() {
        return refreshPopupPositiveButton;
    }

    @Override
    public Button getRefreshPopupNegativeButton() {
        return refreshPopupNegativeButton;
    }

    @Override
    public CheckBox getHideFutureWarningPopupCheckbox() {
        return hideFutureWarningPopupCheckBox;
    }

    /**
     * Simple container class for holding artifact relationships
     * text fields to link from the UI to artifact.
     */
    public static class NodeRelationshipContainer {
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
    public Set<NodeRelationshipContainer> getArtifactRelationshipFields() {
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
    public Map<PropertyType, CheckBox> getInheritMetadataCheckBoxMap() {
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

    @Override
    public void setupWindowBuilder(String disciplineFilePath){
        DisciplineLoadingService disciplineService = new DisciplineLoadingService(disciplineFilePath);
        windowBuilder = new NodePropertyWindowBuilder(cancelPopupLink, applyPopupButton, availableRelationshipsPath, disciplineService);
    }

    @Override
    public PackageToolPopup getRefreshPopup() {
        return refreshPopup;
    }

}
