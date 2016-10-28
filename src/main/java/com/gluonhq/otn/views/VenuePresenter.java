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

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.DialerService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.layout.Layer;
import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import com.gluonhq.maps.demo.PoiLayer;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.Venue;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.views.helper.Util;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class VenuePresenter extends GluonPresenter<OTNApplication> {

    private static double DEFAULT_ZOOM = 15.0;

    @FXML
    private View venue;

    @FXML
    private MapView mapView;

    @FXML
    private Region imageSpacer;

    @FXML
    private ImageView imageView;

    @FXML
    private Label name;

    @FXML
    private Label address;

    private MapLayer venueMarker;
    private final ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> resizeImages();
    private final ChangeListener<Number> heightListener = (observable, oldValue, newValue) -> resizeImages();
    private FloatingActionButton callActionButton;
    private FloatingActionButton webActionButton;

    // venueProperty
    private final ObjectProperty<Venue> venueProperty = new SimpleObjectProperty<Venue>(this, "venue") {
        @Override protected void invalidated() {
            Venue venue = get();
            name.setText(venue.getName());
            address.setText(venue.getLocation());

            MapPoint venuePoint = new MapPoint(venue.getLatitude(), venue.getLongitude());
            mapView.setCenter(venuePoint);
            mapView.setZoom(DEFAULT_ZOOM);

            if (venueMarker != null) {
                mapView.removeLayer(venueMarker);
            }
            venueMarker = createHotelMarker(venuePoint);
            mapView.addLayer(venueMarker);
            
            String phone = venue.getPhoneNumber();
            String url = venue.getUrl();
            callActionButton.setVisible(phone != null && !phone.isEmpty());
            webActionButton.setVisible(url != null && !url.isEmpty());

            resizeImages();
        }
    };

    public final ObjectProperty<Venue> venueProperty() {
       return venueProperty;
    }
    public final Venue getVenue() {
       return venueProperty.get();
    }
    public final void setVenue(Venue value) {
        venueProperty.set(value);
    }

    public void initialize() {
        venue.getLayers().add(createFloatingActionButtons());

        venue.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavBackButton());
            appBar.setTitleText(OTNView.VENUE.getTitle());

            // FixME: The following is a hack to reset zoom value (OTN-254)
            mapView.setZoom(10.0);
            mapView.setZoom(DEFAULT_ZOOM);

            // randomly change image on each showing
            imageView.setImage(Util.getRandomBackgroundImage());
            venue.getScene().widthProperty().addListener(widthListener);
            venue.getScene().heightProperty().addListener(heightListener);
        });

        venue.setOnHiding(event -> {
            venue.getScene().widthProperty().removeListener(widthListener);
            venue.getScene().heightProperty().removeListener(heightListener);
        });
    }

    private Layer createFloatingActionButtons() {
        callActionButton = Util.createFAB(MaterialDesignIcon.CALL, e -> {
            Dialog confirmCallDialog = new Dialog(OTNBundle.getString("OTN.VENUE.CALLDIALOG.TITLE"), OTNBundle.getString("OTN.VENUE.CALLDIALOG.CONTENT", getVenue().getName(), getVenue().getPhoneNumber())) {
                {
                    rootNode.setPrefWidth(MobileApplication.getInstance().getView().getScene().getWidth() * 0.9);
                }

            };
            Button cancel = new Button(OTNBundle.getString("OTN.VENUE.CALLDIALOG.NO"));
            Button ok = new Button(OTNBundle.getString("OTN.VENUE.CALLDIALOG.YES"));
            cancel.setOnAction(event -> confirmCallDialog.hide());
            ok.setOnAction(event -> {
                Services.get(DialerService.class).ifPresent(d -> d.call(getVenue().getPhoneNumber()));
                confirmCallDialog.hide();
            });
            confirmCallDialog.getButtons().addAll(cancel, ok);
            confirmCallDialog.showAndWait();

        });
        webActionButton = Util.createWebLaunchFAB(() -> getVenue().getUrl());
        webActionButton.getStyleClass().add("secondary");
        webActionButton.attachTo(callActionButton, Side.TOP);
        return callActionButton.getLayer();
    }

    private MapLayer createHotelMarker(MapPoint venue) {
        PoiLayer answer = new PoiLayer();
        answer.getStyleClass().add("poi-layer");
        Node marker = MaterialDesignIcon.ROOM.graphic();
        marker.getStyleClass().add("marker");
        Group box = new Group(marker);
        box.getStyleClass().add("marker-container");
        // FIXME: Better Solution ?
        // StackPane added because of OTN-320.
        // Avoids Group to translate when zoom in / zoom out events takes place
        answer.addPoint(venue, new StackPane(box));
        return answer;
    }

    private void resizeImages() {
        if (venue == null || venue.getScene() == null) {
            return;
        }
        double newWidth = venue.getScene().getWidth();
        double newHeight = venue.getScene().getHeight() - getApp().getAppBar().getHeight(); // Exclude the AppBar
        // Resize and translate ImageView
        // Resize imageSpacer and stop expanding when a maxHeight is reached.
        Util.resizeImageViewAndImageSpacer(imageSpacer, imageView, newWidth, newHeight / 3.5);
    }
}
