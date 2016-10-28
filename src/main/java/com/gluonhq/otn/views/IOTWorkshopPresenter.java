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
import com.gluonhq.charm.glisten.control.ProgressBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class IOTWorkshopPresenter extends GluonPresenter<OTNApplication> {

    @FXML
    private View iotworkshopView;

    @FXML
    private WebView webView;

    private ChangeListener<Worker.State> listener;
    
    public void initialize() {
        webView.setZoom(1.0);

        WebEngine webEngine = webView.getEngine();
        
        iotworkshopView.setOnShowing(e -> {
            AppBar appBar = getApp().getAppBar();
            // Use Back button, not the Menu button, to prevent showing the drawer
            // This prevents also from adding this view to the stack, so there is no
            // need for adding a Skip policy
            appBar.setNavIcon(getApp().getNavBackButton());
            appBar.setTitleText(OTNView.IOT_WORKSHOP.getTitle());
        });
        
        ProgressBar indicator = new ProgressBar();
        indicator.progressProperty().bind(webView.getEngine().getLoadWorker().progressProperty());
        iotworkshopView.setTop(indicator); 
        listener = (obs, ov, nv) -> {
            switch(nv) {
                case SUCCEEDED:
                case CANCELLED:
                case FAILED: 
                    indicator.progressProperty().unbind();
                    iotworkshopView.setTop(null); 
                    webView.getEngine().getLoadWorker().stateProperty().removeListener(listener);
                    break;
            }
        };
        
        webView.getEngine().getLoadWorker().stateProperty().addListener(listener);
        
        webEngine.load("https://apex.oracle.com/pls/apex/f?p=IOTWS:SIGNUP");
        
    }
}