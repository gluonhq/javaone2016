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
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.util.EulaManager;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.util.WebViewUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class EulaPresenter extends GluonPresenter<OTNApplication> {

    @FXML
    private View webViewView;

    @FXML
    private WebView webView;

    public void initialize() {
        webView.setZoom(0.8);

        WebEngine webEngine = webView.getEngine();
        String content = WebViewUtils.retrieveContent(getClass().getResourceAsStream("eula.html"));
        webEngine.loadContent(content);
        
        webViewView.setOnShowing(e -> {
            AppBar appBar = getApp().getAppBar();
            // Use Back button, not the Menu button, to prevent showing the drawer
            // This prevents also from adding this view to the stack, so there is no
            // need for adding a Skip policy
            appBar.setNavIcon(getApp().getNavBackButton());
            appBar.setTitleText(OTNView.EULA.getTitle());
            // This fixes OTN-193: the WebView with the initial EULA on iOS is 
            // displayed on top of the AppBar.
            // We solve it by forcing a glassPane.requestLayout()
            // FIXME: It should be removed when the issue is properly solved
            appBar.setVisible(false);
        });
        
        // This fixes OTN-193: the WebView with the initial EULA on iOS is 
        // displayed on top of the AppBar.
        // We solve it by forcing a glassPane.requestLayout()
        // FIXME: It should be removed when the issue is properly solved
        webViewView.setOnShown(e -> getApp().getAppBar().setVisible(true));
        
    }
    
    public void setBottom(boolean showAccept) {
        if (showAccept) {
            Button accept = new Button(OTNBundle.getString("OTN.BUTTON.ACCEPT"));
            accept.setOnAction(e -> {
                getApp().getAppBar().getNavIcon().setVisible(true);
                getApp().getAppBar().getNavIcon().setManaged(true);
                EulaManager.acceptEula();
                OTNView.ACTIVITY_FEED.switchView();
            });
            HBox buttonBox = new HBox(accept);
            HBox.setHgrow(accept, Priority.ALWAYS);
            webViewView.setBottom(buttonBox);
            accept.getStyleClass().add("accept-button");
            buttonBox.getStyleClass().add("button-box");
            getApp().getAppBar().getNavIcon().setVisible(false);
            getApp().getAppBar().getNavIcon().setManaged(false);
            webViewView.getBottom().setDisable(true);
            webView.getEngine().getLoadWorker().stateProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    switch(webView.getEngine().getLoadWorker().getState()) {
                        case SUCCEEDED:
                        case CANCELLED:
                        case FAILED: 
                            webViewView.getBottom().setDisable(false);
                            webView.getEngine().getLoadWorker().stateProperty().removeListener(this);
                            break;
                    }
                }
            });
        } else {
            webViewView.setBottom(null);
        }
    }
    
}