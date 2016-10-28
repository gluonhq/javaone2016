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
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.CardCell;
import com.gluonhq.charm.glisten.control.CardPane2;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.News;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.util.EulaManager;
import com.gluonhq.otn.util.ImageCache;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.util.OTNSettings;
import com.gluonhq.otn.views.helper.Util;
import com.gluonhq.otn.views.layer.ImageViewLayer;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javax.inject.Inject;

public class ActivityFeedPresenter extends GluonPresenter<OTNApplication> {
    private static final String PLACEHOLDER_MESSAGE = OTNBundle.getString("OTN.ACTIVITY.PLACEHOLDER_MESSAGE");

    @FXML
    private View activityFeedView;

    @FXML
    private CardPane2<News> cPActivityFeed;

    @Inject
    private Service service;

    private boolean firstTime = true;

    public void initialize() {
        activityFeedView.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavMenuButton());
            appBar.setTitleText(OTNView.ACTIVITY_FEED.getTitle());
            appBar.getActionItems().add(getApp().getSearchButton());

            if (firstTime && !EulaManager.isEulaAccepted() && OTNSettings.SHOW_EULA) {
                OTNView.EULA.switchView(ViewStackPolicy.SKIP)
                        .ifPresent(p -> ((EulaPresenter) p).setBottom(true));
                firstTime = false;
            }
        });

//        cPActivityFeed.setPlaceholder(Util.createCenteredCard(
//                        "Activity Feed Loading...",
//                        new Placeholder(PLACEHOLDER_MESSAGE, OTNView.ACTIVITY_FEED.getMenuIcon())));
        Bindings.bindContent(cPActivityFeed.getItems(), service.retrieveNews());

        cPActivityFeed.setOnPullToRefresh(event -> {
            // Do nothing we only want pull to refresh to be enabled.
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) { }
        });

        cPActivityFeed.setCellFactory(param->new CardCell<News>() {
            final Label headerLabel = new Label();
            final ImageView headerGraphic = new ImageView();
            final Label contentText = new Label();

            {
                headerLabel.setMaxHeight(Double.MAX_VALUE);
                contentText.setWrapText(true);

                setHeaderGraphic(headerGraphic);
                setHeader(headerLabel);
                setContent(contentText);

                setOnImageClicked(imageView -> {
                    new ImageViewLayer(imageView.getImage()).show();
                });
            }

            @Override public void updateItem(News item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    headerLabel.setText(item.getTitle());
                    headerGraphic.setImage(ImageCache.get(item.getType().getImage()));
                    contentText.setText(item.getContent());

                    getImages().clear();
                    if (item.getThumbnails() != null) {
                        for (String url : item.getThumbnails()) {
                            getImages().add(ImageCache.get(url));
                        }
                    }
                }
            }
        });

        // randomly assign one of the three background images we have available
        Util.assignCommonRandomBackgroundImageStyleClass(cPActivityFeed);
    }
}