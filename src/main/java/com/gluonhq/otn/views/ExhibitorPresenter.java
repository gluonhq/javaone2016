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
import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.Exhibitor;
import com.gluonhq.otn.util.ImageCache;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.views.helper.Util;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class ExhibitorPresenter extends GluonPresenter<OTNApplication> {
    private static final String PLACEHOLDER_MESSAGE = OTNBundle.getString("OTN.EXHIBITOR.PLACEHOLDER_MESSAGE");

    @FXML
    private View exhibitor;

    @FXML
    private Region imageSpacer;

    @FXML
    private Label name;

    @FXML
    private Label booth;

    @FXML
    private Label details;

    @FXML
    private ImageView imageView;

    @FXML
    private ImageView logo;

    private final ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> resizeImages();
    private final ChangeListener<Number> heightListener = (observable, oldValue, newValue) -> resizeImages();
    private FloatingActionButton webLaunchFAB;
    
    // exhibitorProperty
    private final ObjectProperty<Exhibitor> exhibitorProperty = new SimpleObjectProperty<Exhibitor>(this, "exhibitor") {
        @Override
        protected void invalidated() {
            Exhibitor exhibitor = get();
            logo.setImage(ImageCache.get(exhibitor.getPicture()));
            name.setText(exhibitor.getName());

            String boothText = exhibitor.getBooth();
            if(boothText != null && !boothText.isEmpty()) {
                booth.setText(OTNBundle.getString("OTN.EXHIBITOR.BOOTH_NUMBER", boothText));
            }

            String url = exhibitor.getUrl();
            webLaunchFAB.setVisible(url != null && !url.isEmpty());

            details.setText(exhibitor.getSummary());
            resizeImages();
        }
    };

    public final ObjectProperty<Exhibitor> exhibitorProperty() {
        return exhibitorProperty;
    }
    public final Exhibitor getExhibitor() {
        return exhibitorProperty.get();
    }
    public final void setExhibitor(Exhibitor value) {
        exhibitorProperty.set(value);
    }

    public void initialize() {
        webLaunchFAB = Util.createWebLaunchFAB(() -> getExhibitor().getUrl());
        exhibitor.getLayers().add(webLaunchFAB.getLayer());

        exhibitor.setOnShowing( event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavBackButton());
            appBar.setTitleText(OTNView.EXHIBITOR.getTitle());

            // give images full width of scene
            exhibitor.getScene().widthProperty().addListener(widthListener);
            exhibitor.getScene().heightProperty().addListener(heightListener);

            // randomly change image on each showing
            imageView.setImage(Util.getRandomBackgroundImage());
        });

        exhibitor.setOnHiding(event -> {
            exhibitor.getScene().widthProperty().removeListener(widthListener);
            exhibitor.getScene().heightProperty().removeListener(heightListener);
        });
    }

    private void resizeImages() {
        if (exhibitor == null || exhibitor.getScene() == null) {
            return;
        }
            
        double newWidth = exhibitor.getScene().getWidth();
        double newHeight = exhibitor.getScene().getHeight() - getApp().getAppBar().getHeight(); // Exclude the AppBar

        // Resize logo to a max value. Do not stretch anymore if the width threshold.
        Util.resizeImageView(logo, newWidth, newHeight / 3.0);

        // Resize and translate ImageView
        // Resize imageSpacer and stop expanding when a maxHeight is reached.
        Util.resizeImageViewAndImageSpacer(imageSpacer, imageView, newWidth, newHeight / 3.5);
    }


}
