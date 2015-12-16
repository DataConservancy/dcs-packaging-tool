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

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import org.dataconservancy.dcs.util.Util;
import org.dataconservancy.packaging.gui.CssConstants;
import org.dataconservancy.packaging.gui.Labels.LabelKey;
import org.dataconservancy.packaging.gui.TextFactory;
import org.dataconservancy.packaging.gui.presenter.Presenter;
import org.dataconservancy.packaging.gui.view.HeaderView;
import org.dataconservancy.packaging.gui.view.View;

/**
 * Base view that handles controls and view elements that are the same across all views.
 * Handles the setting up and retrieval of the header and footer controls.
 */
public abstract class BaseViewImpl<T extends Presenter> extends BorderPane implements View<T>, CssConstants {
    protected T presenter;
    protected Button continueButton;
    protected Hyperlink cancelLink;
    protected Button saveButton;
    protected Label packageName;
    protected HeaderView headerView;
    private String wikiLink;
    protected Popup helpPopup;
    protected Popup aboutPopup;
    private Node aboutContent;
    protected Label errorLabel;

    final double rem = javafx.scene.text.Font.getDefault().getSize();

    public BaseViewImpl() {

        getStyleClass().add(VIEW_CLASS);

        BorderPane footerView = new BorderPane();
        footerView.getStyleClass().add(VIEW_FOOTER_CLASS);
        footerView.setPrefHeight(48);

        //Sets the name of the package in the footer.
        packageName = new Label();
        packageName.getStyleClass().add(PACKAGE_NAME_LABEL);
        packageName.setAlignment(Pos.CENTER_LEFT);
        packageName.setPrefHeight(48);
        packageName.setMaxWidth(300);
        footerView.setLeft(packageName);

        HBox footerControls = new HBox();
        footerControls.setSpacing(24);
        footerControls.setAlignment(Pos.CENTER_RIGHT);
        footerControls.setMinWidth(300);

        cancelLink = new Hyperlink();
        footerControls.getChildren().add(cancelLink);

        saveButton = new Button();
        saveButton.setVisible(false);
        saveButton.setPrefWidth(7*rem);
        saveButton.getStyleClass().add(CLICKABLE);
        footerControls.getChildren().add(saveButton);

        continueButton = new Button();
        continueButton.setPrefWidth(10*rem);
        continueButton.getStyleClass().add(CLICKABLE);
        footerControls.getChildren().add(continueButton);

        errorLabel=new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(600);

        footerView.setRight(footerControls);

        setBottom(footerView);
    }

    public Node asNode() {
        return this;
    }

    public void setPresenter(T presenter) {
        this.presenter = presenter;
    }

    @Override
    public Button getContinueButton() {
        return continueButton;
    }

    @Override
    public Hyperlink getCancelLink() {
        return cancelLink;
    }

    @Override
    public Label getPackageNameLabel() {
        return packageName;
    }

    @Override
    public Button getSaveButton() {
        return saveButton;
    }

    public String getHelpText() {
        return "";
    }

    @Override
    public void showHelpPopup() {
        Scene scene = headerView.getHelpLink().getScene();

        Point2D point = headerView.getHelpLink().localToScene(0.0,  0.0);
        double x = scene.getWindow().getX() + point.getX();
        double y = scene.getWindow().getY() + point.getY();
        x -= 250;
        y += 80;

        if (helpPopup == null) {
            helpPopup = new Popup();

            //helpPopup.
            //Set the default content for the popups, this should be overwritten in the specific views
            //helpPopup.();
            helpPopup.setAutoHide(true);
            //helpPopup.setOwner(getScene().getWindow());

            BorderPane popupHelpView = new BorderPane();
            popupHelpView.getStyleClass().add(PACKAGE_TOOL_POPUP_CLASS);

            HBox header = new HBox();

            Label titleLabel = new Label();
            titleLabel.setText(TextFactory.getText(LabelKey.BASIC_HELP_TITLE));
            titleLabel.setMaxWidth(400);
            titleLabel.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
            header.getStyleClass().add(PACKAGE_TOOL_POPUP_HEADER_CLASS);

            header.getChildren().add(titleLabel);
            popupHelpView.setTop(header);

            helpPopup.getContent().add(popupHelpView);

            if (Util.isEmptyOrNull(getHelpText())) {
                VBox defaultHelpContent = new VBox();
                defaultHelpContent.setPrefHeight(200);
                defaultHelpContent.setPrefWidth(200);

                Label helpText = new Label(TextFactory.getText(LabelKey.BASIC_HELP_TEXT));
                helpText.setWrapText(true);
                defaultHelpContent.getChildren().add(helpText);
                popupHelpView.setCenter(defaultHelpContent);
            } else {
                Label helpContent = new Label(getHelpText());
                helpContent.setMaxWidth(300);
                helpContent.setWrapText(true);
                helpContent.setTextAlignment(TextAlignment.LEFT);
                helpContent.getStyleClass().add(PACKAGE_TOOL_POPUP_CONTENT_CLASS);
                popupHelpView.setCenter(helpContent);
            }
        }

        helpPopup.show(scene.getWindow(), x, y);
    }

    @Override
    public void showAboutPopup() {
        Scene scene = headerView.getAboutLink().getScene();
        
        Point2D point = headerView.getAboutLink().localToScene(0.0,  0.0);
        double x = scene.getWindow().getX() + point.getX();
        double y = scene.getWindow().getY() + point.getY();

        //X and Y are now the location of the button, if we leave that as the position the button will be covered. Offset slightly so it doesn't cover the button
        x -= 250;
        y += 80;

        if (aboutPopup == null) {
            aboutPopup = new Popup();
            aboutPopup.setAutoHide(true);

            BorderPane popupAboutView = new BorderPane();
            popupAboutView.getStyleClass().add(PACKAGE_TOOL_POPUP_CLASS);

            aboutPopup.getContent().add(popupAboutView);

            HBox header = new HBox();

            Label titleLabel = new Label();
            titleLabel.setText(TextFactory.getText(LabelKey.BASIC_ABOUT_TITLE));
            titleLabel.setMaxWidth(400);
            titleLabel.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
            header.getStyleClass().add(PACKAGE_TOOL_POPUP_HEADER_CLASS);

            header.getChildren().add(titleLabel);
            popupAboutView.setTop(header);

            if (aboutContent == null) {
                VBox defaultAboutContent = new VBox();
                defaultAboutContent.setPrefHeight(200);
                defaultAboutContent.setPrefWidth(200);

                Label aboutText = new Label(TextFactory.getText(LabelKey.BASIC_ABOUT_TEXT));
                aboutText.setWrapText(true);

                Hyperlink packageToolWikiLink = new Hyperlink("See the tool's wiki site for more details.");
                packageToolWikiLink.setOnAction(event -> presenter.getController().getApplicationHostServices().
                        showDocument(wikiLink));

                defaultAboutContent.getChildren().add(aboutText);
                defaultAboutContent.getChildren().add(packageToolWikiLink);
                defaultAboutContent.getStyleClass().add(PACKAGE_TOOL_POPUP_CONTENT_CLASS);
                popupAboutView.setCenter(defaultAboutContent);

            } else {
                aboutContent.getStyleClass().add(PACKAGE_TOOL_POPUP_CONTENT_CLASS);
                popupAboutView.setCenter(aboutContent);
            }

            VBox versionFooter = new VBox(8);
            versionFooter.setAlignment(Pos.CENTER);
            versionFooter.getStyleClass().add(ABOUT_FOOTER);

            HBox buildRow = new HBox(30);
            buildRow.setAlignment(Pos.CENTER);
            Label buildNumberLabel = new Label(TextFactory.getText(LabelKey.BUILD_NUMBER_LABEL) + " " + presenter.getController().getPackageState().getCreationToolVersion().getBuildNumber());
            buildRow.getChildren().add(buildNumberLabel);

            Label buildRevisionLabel = new Label(TextFactory.getText(LabelKey.BUILD_REVISION_LABEL) + " " + presenter.getController().getPackageState().getCreationToolVersion().getBuildRevision());
            buildRow.getChildren().add(buildRevisionLabel);

            versionFooter.getChildren().add(buildRow);

            String timeStampString = TextFactory.getText(LabelKey.BUILD_TIMESTAMP_LABEL) + " " + presenter.getController().getPackageState().getCreationToolVersion().getBuildTimeStamp();
            Label timeStampLabel = new Label(timeStampString);
            versionFooter.getChildren().add(timeStampLabel);

            popupAboutView.setBottom(versionFooter);

        }
        
        aboutPopup.show(scene.getWindow(), x, y);
    }

    @Override
    public void setAboutPopupContent(Node content) {
        aboutContent = content;
    }

    public void setHeaderView(HeaderView headerView) {
        this.headerView = headerView;
    }

    public void setWikiLink(String wikiLink) {
        this.wikiLink = wikiLink;
    }

    @Override
    public Hyperlink getHeaderViewAboutLink() {
        return headerView.getAboutLink();
    }

    @Override
    public Hyperlink getHeaderViewHelpLink() {
        return headerView.getHelpLink();
    }
    
    @Override
    public Label getErrorLabel(){ return errorLabel;}
}
