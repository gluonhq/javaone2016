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
import com.gluonhq.otn.model.OTNGame;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.util.ImageCache;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.views.dialog.QRDialog;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public class GamePresenter extends GluonPresenter<OTNApplication> {
    private static final String ANONYMOUS_MESSAGE = OTNBundle.getString("OTN.GAME.ANONYMOUS_MESSAGE");

    @FXML
    private View gameView;

    @FXML
    private VBox instructionsContainer;

    @FXML
    private ImageGallery<OTNGame> imageGallery;

    @FXML
    private HBox submitContainer;

    @FXML
    private Label instructions;

    @FXML
    private Button gameButton;

    @Inject
    private Service service;

    private static final String QR_MESSAGE = OTNBundle.getString("OTN.GAME.QR_MESSAGE");

    public void initialize() {
        showPlaceHolder();

        gameView.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavBackButton());
            appBar.setTitleText(OTNView.GAME.getTitle());
        });

        gameView.setOnHidden(event -> {
            if(service.isAuthenticated()) {
                imageGallery.clearSelection();
            }
        });

        imageGallery.setItems(service.retrieveOTNGames());
        imageGallery.setImageFactory(otnGame -> {
            ImageView imageView = new ImageView(ImageCache.get(otnGame.getImage()));
            imageView.setPreserveRatio(true);
            return imageView;
        });
        imageGallery.setTitleFactory(OTNGame::getTitle);

        instructions.setText(OTNBundle.getString("OTN.GAME.INSTRUCTION"));

        gameButton.setText(OTNBundle.getString("OTN.BUTTON.GAME"));
        gameButton.setOnAction(event -> {
            if(imageGallery.getSelectedItem() != null) {
                play(imageGallery.getSelectedItem());
            } else {
                Toast toast = new Toast(OTNBundle.getString("OTN.EXPERIENCE.SELECT_IMAGE"));
                toast.show();
            }
        });

        Bindings.isEmpty(imageGallery.getItems()).addListener((obs, ov, nv) -> {
            loadView();
        });
    }

    private void play(OTNGame selectedItem) {
        QRDialog.show(selectedItem.getSelf(), QR_MESSAGE);
    }

    private void loadView() {
        if(!imageGallery.getItems().isEmpty()) {
            gameView.setTop(instructionsContainer);
            gameView.setCenter(imageGallery);
            gameView.setBottom(submitContainer);
        } else {
            showPlaceHolder();
        }
    }

    private void showPlaceHolder() {
        Label placeholder = new Label(OTNBundle.getString("OTN.GAME.PLACEHOLDER"));
        gameView.setCenter(new StackPane(placeholder));
        gameView.setTop(null);
        gameView.setBottom(null);
    }
}
