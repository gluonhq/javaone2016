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
import com.gluonhq.otn.model.OTNEmbroidery;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.util.ImageCache;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.views.dialog.QRDialog;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public class EmbroiderPresenter extends GluonPresenter<OTNApplication> {
    private static final String ANONYMOUS_MESSAGE = OTNBundle.getString("OTN.EMBROIDER.ANONYMOUS_MESSAGE");
    private static final String QR_MESSAGE = OTNBundle.getString("OTN.EMBROIDER.QR_MESSAGE");

    @FXML
    private View embroiderView;

    @FXML
    private VBox instructionsContainer;

    @FXML
    private HBox submitContainer;

    @FXML
    private ImageGallery<OTNEmbroidery> imageGallery;

    @FXML
    private Label instructions;

    @FXML
    private Button embroiderButton;

    @Inject
    private Service service;

    public void initialize() {
        showPlaceHolder();

        imageGallery.setItems(service.retrieveOTNEmbroideries());
        imageGallery.setImageFactory(param -> {
            ImageView imageView = new ImageView(ImageCache.get(param.getImage()));
            imageView.setPreserveRatio(true);
            return imageView;
        });

        embroiderView.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavBackButton());
            appBar.setTitleText(OTNView.EMBROIDER.getTitle());
        });

        embroiderView.setOnHidden(event -> {
            if(service.isAuthenticated()) {
                imageGallery.clearSelection();
            }
        });

        instructions.setText(OTNBundle.getString("OTN.EMBROIDER.INSTRUCTION"));
        BorderPane.setAlignment(instructions, Pos.CENTER);

        embroiderButton.setText(OTNBundle.getString("OTN.BUTTON.EMBROIDER"));
        embroiderButton.setOnAction(event -> {
            if(imageGallery.getSelectedItem() != null) {
                embroider(imageGallery.getSelectedItem());
            } else {
                Toast toast = new Toast(OTNBundle.getString("OTN.EXPERIENCE.SELECT_IMAGE"));
                toast.show();
            }
        });

        Bindings.isEmpty(imageGallery.getItems()).addListener((obs, ov, nv) -> {
            loadView();
        });
    }

    private void loadView() {
        if(!imageGallery.getItems().isEmpty()) {
            embroiderView.setTop(instructionsContainer);
            embroiderView.setCenter(imageGallery);
            embroiderView.setBottom(submitContainer);
        } else {
            showPlaceHolder();
        }
    }

    private void embroider(OTNEmbroidery embroidery) {
        QRDialog.show(embroidery.getSelf(), QR_MESSAGE);
    }

    private void showPlaceHolder() {
        Label placeholder = new Label(OTNBundle.getString("OTN.EMBROIDER.PLACEHOLDER"));
        embroiderView.setCenter(new StackPane(placeholder));
        embroiderView.setTop(null);
        embroiderView.setBottom(null);
    }
}
