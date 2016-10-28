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

import com.airhacks.afterburner.injection.Injector;
import com.gluonhq.charm.down.Platform;
import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.ConnectivityService;
import com.gluonhq.charm.down.plugins.DisplayService;
import com.gluonhq.charm.glisten.afterburner.AppView;
import com.gluonhq.charm.glisten.afterburner.GluonInstanceProvider;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.layout.layer.SidePopupView;
import com.gluonhq.charm.glisten.license.License;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.model.cloudlink.CloudLinkService;
import com.gluonhq.otn.util.OTNSearch;
import com.gluonhq.otn.views.helper.ConnectivityUtils;
import com.gluonhq.otn.views.helper.SessionVisuals;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Locale;

import static com.gluonhq.otn.OTNView.SEARCH;
import com.gluonhq.otn.util.OTNLogging;
import com.gluonhq.otn.util.OTNNotifications;
import javafx.stage.Window;

@License(key = "??????????????????????")
public class OTNApplication extends MobileApplication {
    
    public static final String MENU_LAYER = "SideMenu";
    public static final String POPUP_FILTER_SESSIONS_MENU = "FilterSessionsMenu";

    private static final GluonInstanceProvider instanceSupplier = new GluonInstanceProvider() {{
        bindProvider(Service.class, CloudLinkService::new);
        bindProvider(OTNSearch.class, OTNSearch::new);
        bindProvider(OTNNotifications.class, OTNNotifications::new);
        bindProvider(SessionVisuals.class, SessionVisuals::new);

        Injector.setInstanceSupplier(this);
    }};

    private final Button navMenuButton   = MaterialDesignIcon.MENU.button(e -> showLayer(OTNApplication.MENU_LAYER));
    private final Button navBackButton   = MaterialDesignIcon.ARROW_BACK.button(e -> switchToPreviousView());
    private final Button navHomeButton   = MaterialDesignIcon.HOME.button(e -> goHome());
    private final Button navSearchButton = MaterialDesignIcon.SEARCH.button(e -> SEARCH.switchView());

    private OTNDrawerPresenter drawerPresenter;
    
    private OTNNotifications otnNotifications;

    @Override
    public void init() {

        // Config logging
        OTNLogging.config();

        // start service data preloading as soon as possible
        Injector.instantiateModelOrService(Service.class);

        // check if the app starts from a notification
        otnNotifications = Injector.instantiateModelOrService(OTNNotifications.class);
        otnNotifications.findNotificationIdAtStartup(getParameters().getNamed());

        for (AppView view : OTNView.registry.getViews()) {
            view.registerView(this);
        }
        
        addLayerFactory(MENU_LAYER, () -> {
            SidePopupView sidePopupView = new SidePopupView(drawerPresenter.getDrawer());
            drawerPresenter.setSidePopupView(sidePopupView);
            return sidePopupView;
        });
    }

    @Override
    public void postInit(Scene scene) {
        String formFactorSuffix = Services.get(DisplayService.class)
                .map(s -> s.isTablet() ? "_tablet" : "")
                .orElse("");

        String stylesheetName = String.format("javaone_%s%s.css",
                Platform.getCurrent().name().toLowerCase(Locale.ROOT),
                formFactorSuffix);
        scene.getStylesheets().add(OTNApplication.class.getResource(stylesheetName).toExternalForm());
        
        if (Platform.isDesktop()) {
            Window window = scene.getWindow();
            ((Stage) window).getIcons().add(new Image(OTNApplication.class.getResourceAsStream("/icon.png")));
            window.setWidth(350);
            window.setHeight(700);
        }
        
        drawerPresenter = Injector.instantiateModelOrService(OTNDrawerPresenter.class);

        scene.getWindow().showingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    initConnectivityServices();
                    scene.getWindow().showingProperty().removeListener(this);
                }
            }
        });
    }

    private void initConnectivityServices() {
        Services.get(ConnectivityService.class).ifPresent(connectivityService -> {
            connectivityService.connectedProperty().addListener((observable, oldValue, newValue) -> {
                ConnectivityUtils.showConnectivityIndication(newValue);
            });

            ConnectivityUtils.showConnectivityIndication(connectivityService.isConnected());
        });

    }


    public Button getNavMenuButton() {
        return navMenuButton;
    }

    public Button getNavBackButton() {
        return navBackButton;
    }

    public Button getNavHomeButton() {
        return navHomeButton;
    }
    
    public Button getSearchButton() {
        return navSearchButton;
    }

}