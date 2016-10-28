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
import com.gluonhq.charm.glisten.control.BottomNavigation;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.model.Session;
import com.gluonhq.otn.model.Speaker;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.views.cell.ScheduleCell;
import com.gluonhq.otn.views.helper.SpeakerCard;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;

import javax.inject.Inject;
import java.time.LocalDate;

public class SpeakerPresenter extends GluonPresenter<OTNApplication> {

    @FXML
    private View speakerView;

    @Inject
    private Service service;

    private Toggle lastSelectedButton;
    private CharmListView<Session, LocalDate> sessionsListView;

    private ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> resizeSpeakerCard();
    private ChangeListener<Number> heightListener = (observable, oldValue, newValue) -> resizeSpeakerCard();

//    @Inject
//    private SessionVisuals sessionVisuals;

    public void initialize() {
        speakerView.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavBackButton());
            appBar.setTitleText(OTNView.SPEAKER.getTitle());
            if (lastSelectedButton != null) {
                lastSelectedButton.setSelected(true);
            }
            if(sessionsListView != null) {
                sessionsListView.setSelectedItem(null);
            }
            speakerView.widthProperty().addListener(widthListener);
            speakerView.heightProperty().addListener(heightListener);
        });

        speakerView.setOnHiding(event -> {
            speakerView.widthProperty().removeListener(widthListener);
            speakerView.heightProperty().removeListener(heightListener);
        });
    }
    
    public void setSpeaker(Speaker activeSpeaker) {
        // details about session (name, company, title)
        final SpeakerCard sessionCard = new SpeakerCard(activeSpeaker);
        speakerView.setTop(sessionCard);
        resizeSpeakerCard();

        // navigation between the two views (info, sessions)
        final BottomNavigation bottomNavigation = createBottomNavigation(activeSpeaker);
        speakerView.setBottom(bottomNavigation);
    }

    private BottomNavigation createBottomNavigation(final Speaker activeSpeaker) {

        BottomNavigation bottomNavigation = new BottomNavigation();

        final ToggleButton infoButton = bottomNavigation.createButton(OTNBundle.getString("OTN.BUTTON.INFO"), MaterialDesignIcon.INFO.graphic(), e -> {
            // when clicked create a label in a scrollpane. Label will contain
            // the speaker summary
            Label speakerSummary = new Label(activeSpeaker.getSummary());
            speakerSummary.setWrapText(true);
            speakerSummary.getStyleClass().add("speaker-summary");
            speakerView.setCenter(createScrollPane(speakerSummary));
        });

        final ToggleButton sessionsButton = bottomNavigation.createButton(OTNBundle.getString("OTN.BUTTON.SESSIONS"), MaterialDesignIcon.EVENT_NOTE.graphic(), e -> {
            // when clicked we create a pane containing all sessions.
            speakerView.setCenter(createSessionsListView(activeSpeaker));
        });

        bottomNavigation.getActionItems().addAll(infoButton, sessionsButton);

        // listen to the selected toggle so we ensure it is selected when the view is returned to
        infoButton.getToggleGroup().selectedToggleProperty().addListener((o,ov,nv) -> lastSelectedButton = nv);
        infoButton.setSelected(true);

        return bottomNavigation;
    }
    
    private ObservableList<Session> fetchSessions(Speaker activeSpeaker) {
        ObservableList<Session> sessions = FXCollections.observableArrayList();
        for (Session session : service.retrieveSessions()) {
            if (session.getSpeakersUuid() != null && 
                    session.getSpeakersUuid().contains(activeSpeaker.getUuid())) {
                sessions.add(session);
            }
        }
        return sessions;
    }
    
    private CharmListView<Session, LocalDate> createSessionsListView(Speaker activeSpeaker) {
        sessionsListView = new CharmListView<>(fetchSessions(activeSpeaker));
        sessionsListView.getStyleClass().add("sessions-list");
        sessionsListView.setCellFactory(p -> new ScheduleCell(service, true));
        sessionsListView.setPlaceholder(new Label(OTNBundle.getString("OTN.SPEAKER.THERE_ARE_NO_SESSIONS")));
        sessionsListView.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                OTNView.SESSION.switchView().ifPresent(presenter ->
                        ((SessionPresenter) presenter).showSession(newValue));
            }
        });
        return sessionsListView;
    }
    
    private ScrollPane createScrollPane(Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        return sp;
    }

    private void resizeSpeakerCard() {
        if (speakerView.getTop() != null) {
            SpeakerCard speakerCard = (SpeakerCard) speakerView.getTop();
            speakerCard.setMaxHeight(speakerView.getHeight() / 2.5);
            speakerCard.requestLayout();
        }
    }
    
}
