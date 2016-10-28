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
package com.gluonhq.otn;

import com.gluonhq.charm.glisten.afterburner.AppView;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.layout.layer.SidePopupView;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import java.util.Optional;

import static com.gluonhq.charm.glisten.application.MobileApplication.HOME_VIEW;
import static com.gluonhq.otn.OTNApplication.MENU_LAYER;
import com.gluonhq.otn.model.Service;

import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.views.EulaPresenter;
import com.gluonhq.otn.views.helper.Util;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.gluonhq.charm.glisten.control.Toast;
import com.gluonhq.otn.util.OTNSettings;

@Singleton
public class OTNDrawerPresenter extends GluonPresenter<OTNApplication> {

    private final NavigationDrawer drawer;
    private final Header header;
    private final NavigationDrawer.Item logOut;
    
    @Inject
    private Service service;
    
    public OTNDrawerPresenter() {
        drawer = new NavigationDrawer();
        header = new Header();
        
        drawer.setHeader(header);

        for (AppView view : OTNView.registry.getViews()) {
            if (view.isShownInDrawer()) {
                drawer.getItems().add(view.getMenuItem());
            }
        }
        
        logOut = new NavigationDrawer.Item(OTNBundle.getString("OTN.DRAWER.LOG_OUT"), MaterialDesignIcon.CANCEL.graphic());
        logOut.managedProperty().bind(logOut.visibleProperty());
        logOut.selectedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                if (service.logOut()) {
                    Toast toast = new Toast(OTNBundle.getString("OTN.LOGGED_OUT_MESSAGE"));
                    toast.show();

                    // switch to home view
                    getApp().switchView(HOME_VIEW);
                }
            }
        });
        drawer.getItems().add(logOut);
        
        drawer.addEventHandler(NavigationDrawer.ITEM_SELECTED, e -> getApp().hideLayer(MENU_LAYER));
        
        getApp().viewProperty().addListener((obs, oldView, newView) -> {
            Optional.ofNullable(oldView)
                    .flatMap(v -> OTNView.registry.getView(oldView))
                    .ifPresent(otnView -> otnView.getMenuItem().setSelected(false));
            updateDrawer(newView);
        });
        updateDrawer(getApp().getView());
    }
    
    private void updateDrawer(View view) {
        OTNView.registry.getView(view)
                .ifPresent(otnView -> {
                    drawer.setSelectedItem(otnView.getMenuItem());
                    otnView.selectMenuItem();
                });
    }
    
    public final void setSidePopupView(SidePopupView sidePopupView) {
        sidePopupView.showingProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                logOut.setVisible(service.isAuthenticated() && !OTNSettings.AUTO_AUTHENTICATION);
            }
        });
        header.setSidePopupView(sidePopupView);
    }
    
    public final NavigationDrawer getDrawer() {
        return drawer;
    }

    private class Header extends Region {

        private final ImageView background;
//        private final Button profileButton;
        private Button eulaButton;
        private final Label text;
        private final double aspectRatio;

        public Header() {
            // background image
            background = Util.getRandomBackgroundImageView();
            aspectRatio = background.getImage().getHeight() / background.getImage().getWidth();
            background.setFitWidth(getWidth());

            // text
            text = new Label(OTNBundle.getString("OTN.DRAWER.JAVAONE_2016"));
            text.getStyleClass().add("primary-title");

//            // profile button
//            profileButton  = MaterialDesignIcon.SETTINGS.button(e -> {
//                getApp().hideLayer(MENU_LAYER);
//                OTNView.PROFILE.switchView();
//            });
//            // the profile button is only visible when the user is logged in
//            profileButton.setVisible(false);
//            profileButton.managedProperty().bind(profileButton.visibleProperty());
            
            getChildren().addAll(background, text/*, profileButton*/);

            if (OTNSettings.SHOW_EULA) {
                // EULA button
                eulaButton = MaterialDesignIcon.ASSESSMENT.button(e -> {
                    getApp().hideLayer(MENU_LAYER);
                    OTNView.EULA.switchView().ifPresent(p -> ((EulaPresenter) p).setBottom(false));
                });
                getChildren().add(eulaButton);
            }
        }

        protected void setSidePopupView(SidePopupView sidePopupView) {
//            sidePopupView.showingProperty().addListener((obs, ov, nv) -> {
//                if (nv) {
//                    // the profile button is only visible when the user is logged in,
//                    // and we check every time the drawer is shown
//                    profileButton.setVisible(service.isAuthenticated());
//                }
//            });
        }
        
        @Override
        protected void layoutChildren() {
            double w = getWidth();
            double h = getHeight();

            background.setFitWidth(w);

            // Position text in bottom-left
            final double labelWidth = text.prefWidth(-1);
            final double labelHeight = text.prefHeight(-1);
            text.resizeRelocate(0, h - labelHeight, labelWidth, labelHeight);

            // put profile and eula button down bottom-right
            if (OTNSettings.SHOW_EULA) {
                final double padding = 5;
                final double eulaBtnWidth = eulaButton.prefWidth(-1);
                final double eulaBtnHeight = eulaButton.prefHeight(-1);
                final double x = w - eulaBtnWidth - padding;
                eulaButton.resizeRelocate(x, h - eulaBtnHeight - padding, eulaBtnWidth, eulaBtnHeight);
            }
//            final double profileBtnWidth = profileButton.prefWidth(-1);
//            final double profileBtnHeight = profileButton.prefHeight(-1);
//            profileButton.resizeRelocate(x - profileBtnWidth, h - profileBtnHeight - padding, profileBtnWidth, profileBtnHeight);
        }

        @Override
        protected double computePrefHeight(double width) {
            return width * aspectRatio;
        }
        
    }
}