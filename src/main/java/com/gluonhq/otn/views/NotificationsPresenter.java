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
import com.gluonhq.charm.glisten.control.CardCell;
import com.gluonhq.charm.glisten.control.CardPane2;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.News;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.util.OTNSettings;
import com.gluonhq.otn.views.helper.Placeholder;
import com.gluonhq.otn.views.helper.Util;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class NotificationsPresenter extends GluonPresenter<OTNApplication> {

    private static final String PLACEHOLDER_MESSAGE = OTNBundle.getString("OTN.NOTIFICATIONS.PLACEHOLDER_MESSAGE");

    @FXML
    private View notificationsView;

    @FXML
    private CardPane2<News> cPNotifications;

    public void initialize() {
        notificationsView.setOnShowing(e -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavBackButton());
            appBar.setTitleText(OTNView.NOTIFICATIONS.getTitle());
        });
        
        cPNotifications.setPlaceholder(
                        new Placeholder(PLACEHOLDER_MESSAGE, OTNView.NOTIFICATIONS.getMenuIcon()));
        
        // FIXME: Create a NotificationCell and move it to the cells package
        cPNotifications.setCellFactory(param -> new CardCell<News>() {
            
            private News news;
            private final Label header =  new Label();
            private final Label message = new Label();
            private final Label date = new Label();
            private final HBox hBox = new HBox(date);
            private final VBox content = new VBox(10, message, hBox); // TODO: CSS
            
            {
                setHeader(header);
                content.setAlignment(Pos.TOP_LEFT);
                hBox.setAlignment(Pos.BOTTOM_RIGHT);
                setContent(content);
            }
            
            private void update() {
                getImages().clear();
                if (news != null) {
                    header.setText(news.getTitle());
                    message.setText(news.getContent());
                    LocalDateTime localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(news.getCreationDate()), ZoneId.systemDefault());
                    date.setText(OTNSettings.NEWS_FORMATTER.format(localDate));
                } else {
                    header.setText("");
                    message.setText("");
                    date.setText("");
                }
            }
            
            @Override
            public void updateItem(News item, boolean empty) {
                super.updateItem(item, empty);
                news = item;
                update();
            }
        });
        
        // randomly assign one of the three background images we have available
        Util.assignCommonRandomBackgroundImageStyleClass(cPNotifications);
    }
    
    // entry point for notifications
    public void addNotification(News news) {
        cPNotifications.getItems().add(news);
    }
}