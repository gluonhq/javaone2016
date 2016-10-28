/**
 * Copyright (c) 2016, Gluon Software
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse
 *    or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.otn.views;

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.ImageGallery;
import com.gluonhq.charm.glisten.control.Toast;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.LatestClearThreeDModelVotes;
import com.gluonhq.otn.model.OTN3DModel;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.util.ImageCache;
import com.gluonhq.otn.util.OTNBundle;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public class Vote3DPresenter extends GluonPresenter<OTNApplication> {
    private static final String ANONYMOUS_MESSAGE = OTNBundle.getString("OTN.VOTE3D.ANONYMOUS_MESSAGE");

    @FXML
    private View vote3DView;

    @FXML
    private VBox printContainer;

    @FXML
    private HBox submitContainer;

    @FXML
    private Label instructions;

    @FXML
    private Label voteInstruction;

    @FXML
    private ImageView printingImage;

    @FXML
    private VBox voteContainer;

    @FXML
    private Button voteButton;

    @Inject
    private Service service;
    private ReadOnlyListProperty<OTN3DModel> otn3DModels;
    private ObservableList<OTN3DModel> nonPrinting3DModels = FXCollections.observableArrayList();

    private ImageGallery<OTN3DModel> imageGallery;
    
    public Vote3DPresenter() {
    }

    public void initialize() {
        ReadOnlyObjectProperty<LatestClearThreeDModelVotes> latestClearVotes = service.retrieveLatestClearVotes();
        if (latestClearVotes.getValue() == null) {
            latestClearVotes.addListener((obs, ov, nv) -> {
                if (ov == null && nv != null) {
                    refreshView();
                    loadView();
                    if (!service.canVoteForOTN3DModel()) {
                        addResults();
                    }
                }
            });
        } else {
            latestClearVotes.getValue().timestampProperty().addListener((obs2, ov2, nv2) -> {
                refreshView();
                loadView();
                if (!service.canVoteForOTN3DModel()) {
                    addResults();
                }
            });
        }

        imageGallery = new ImageGallery<>();
        imageGallery.setImageFactory(otn3DModel -> {
            ImageView imageView = new ImageView(ImageCache.get(otn3DModel.getImage()));
            imageView.setPreserveRatio(true);
            return imageView;
        });
        imageGallery.setTitleFactory(OTN3DModel::getName);

        vote3DView.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavBackButton());
            appBar.setTitleText(OTNView.VOTE3D.getTitle());

            loadView();
            if (!service.canVoteForOTN3DModel()) {
                addResults();
            }
        });

        vote3DView.setOnHidden(event -> {
            if(service.isAuthenticated()) {
                imageGallery.clearSelection();
            }
        });

        otn3DModels = service.retrieveOTN3DModels();
        otn3DModels.addListener((ListChangeListener.Change<? extends OTN3DModel> c) -> {
            refreshView();
            loadView();
            if (!service.canVoteForOTN3DModel()) {
                addResults();
            }
        });

        refreshView();
        loadView();
        if (!service.canVoteForOTN3DModel()) {
            addResults();
        }

        imageGallery.setItems(nonPrinting3DModels);

//        printingImage.fitWidthProperty().bind(vote3DView.widthProperty().subtract(10));

        voteButton.setText(OTNBundle.getString("OTN.BUTTON.VOTE_TO_PRINT"));
        voteButton.setOnAction(event -> {
            if(imageGallery.getSelectedItem() != null) {
                vote(imageGallery.getSelectedItem());
            } else {
                Toast toast = new Toast(OTNBundle.getString("OTN.EXPERIENCE.SELECT_IMAGE"));
                toast.show();
            }
        });
    }

    private void refreshView() {
        nonPrinting3DModels.clear();
        for (OTN3DModel otn3DModel : otn3DModels.get()) {
            if (otn3DModel.isCurrentModel()) {
                printingImage.setImage(ImageCache.get(otn3DModel.getImage()));
            } else {
                nonPrinting3DModels.add(otn3DModel);
            }
        }
    }

    private void loadView() {
        if (!imageGallery.getItems().isEmpty()) {
            if (voteContainer.getChildren().size() == 2) {
                voteContainer.getChildren().set(1, imageGallery);
            } else {
                voteContainer.getChildren().add(imageGallery);
            }
            instructions.setText(OTNBundle.getString("OTN.VOTE3D.PRINTING_INSTRUCTION"));
            voteInstruction.setText(OTNBundle.getString("OTN.VOTE3D.VOTING_INSTRUCTION"));
            voteButton.setDisable(false);
            imageGallery.setOverlayFactory(null);
            printContainer.setAlignment(Pos.CENTER);
            vote3DView.setTop(printContainer);
            vote3DView.setCenter(voteContainer);
            vote3DView.setBottom(submitContainer);
        } else {
            showPlaceHolder();
        }
    }

    private void vote(OTN3DModel model) {
        service.voteForOTN3DModel(model.getUuid());
        addResults();
    }

    private void showPlaceHolder() {
        Label placeholder = new Label(OTNBundle.getString("OTN.VOTE3D.PLACEHOLDER"));
        vote3DView.setCenter(new StackPane(placeholder));
        vote3DView.setTop(null);
        vote3DView.setBottom(null);
    }

    private void addResults() {
        imageGallery.clearSelection();
        imageGallery.setSelectable(false);
        imageGallery.setOverlayFactory(otn3DModel -> {
            Label label = new Label();
            label.textProperty().bind(otn3DModel.currentVotesProperty().asString());
            label.getStyleClass().add("result-label");
            return label;
        });
        voteInstruction.setText(OTNBundle.getString("OTN.VOTE3D.VOTING_RESULT"));
        voteButton.setDisable(true);
    }
}
