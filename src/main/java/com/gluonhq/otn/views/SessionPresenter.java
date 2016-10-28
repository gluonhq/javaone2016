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
import com.gluonhq.charm.glisten.control.AvatarPane;
import com.gluonhq.charm.glisten.control.BottomNavigation;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.GlistenStyleClasses;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.impl.charm.glisten.control.skin.AvatarPaneSkin;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.views.dialog.VoteDialog;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.model.Session;
import com.gluonhq.otn.model.Speaker;
import com.gluonhq.otn.model.Vote;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.util.OTNSettings;
import com.gluonhq.otn.views.helper.LoginPrompter;
import com.gluonhq.otn.views.helper.SessionNotesEditor;
import com.gluonhq.otn.views.helper.SessionVisuals;
import com.gluonhq.otn.views.helper.Util;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

public class SessionPresenter extends GluonPresenter<OTNApplication> {

    @FXML
    private View sessionView;

    @Inject
    private Service service;

    @Inject
    private SessionVisuals sessionVisuals;

    private AvatarPane<Speaker> speakerAvatarPane;
    private SessionNotesEditor sessionNotesEditor;

    private Node scheduleBtn;
    private Node favoriteBtn;
    private Toggle lastSelectedButton;

    public void initialize() {
        sessionView.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavBackButton());
            appBar.setTitleText(OTNView.SESSION.getTitle());
            if(lastSelectedButton != null) {
                lastSelectedButton.setSelected(true);
            }
        });
        sessionView.setOnHiding(event -> {
            if (service.isAuthenticated() && sessionNotesEditor != null) {
                sessionNotesEditor.saveNotes();
            }
        });
    }

    public void showSession(Session activeSession) {
        showSession(activeSession, Pane.INFO);
    }

    public void showSession(Session activeSession, Pane visiblePane) {
        // details about session (name, time, location)
        sessionView.setTop(createSessionDetails(activeSession));

        // center area is scrollable, populated by the bottom nav bar,
        // already has ScrollPane via FXML

        // navigation between the three views (info, speakers, notes)
        final BottomNavigation bottomNavigation = createBottomNavigation(activeSession);
        sessionView.setBottom(bottomNavigation);

        for (Node node : bottomNavigation.getActionItems()) {
            ToggleButton toggleButton = (ToggleButton) node;
            if (toggleButton.getUserData().equals(visiblePane)) {
                toggleButton.setSelected(true);
                lastSelectedButton = toggleButton;
            }
        }

        // update app bar
        final AppBar appBar = sessionView.getApplication().getAppBar();
        if (OTNSettings.FAV_AND_SCHEDULE_ENABLED) {
            appBar.getActionItems().removeAll(scheduleBtn, favoriteBtn);
            scheduleBtn = sessionVisuals.getSelectedButton(activeSession);
            favoriteBtn = sessionVisuals.getFavoriteButton(activeSession);
        }
                
        if (activeSession.getEndDate().minusMinutes(10).isBefore(ZonedDateTime.now()) && 
                activeSession.getEndDate().plusDays(1).isAfter(ZonedDateTime.now()) && 
                service.isAuthenticated()) {
            ToggleButton voteButton = new ToggleButton("", MaterialDesignIcon.THUMBS_UP_DOWN.graphic());
            voteButton.getStyleClass().addAll(GlistenStyleClasses.BUTTON_FLAT, GlistenStyleClasses.BUTTON_ROUND);
            voteButton.setOnAction(event -> {
                ObservableList<Vote> votes = service.retrieveVotes();
                Vote retrievedVote = null;
                for (Vote currentVote : votes) {
                    if (currentVote.getSessionUuid().equals(activeSession.getUuid())) {
                        retrievedVote = currentVote;
                        break;
                    }
                }
                VoteDialog voteDialog = new VoteDialog(activeSession.getTitle());
                AtomicBoolean addVote = new AtomicBoolean();
                if (retrievedVote != null) {
                    voteDialog.setVote(retrievedVote);
                    addVote.set(false);
                } else {
                    voteDialog.setVote(new Vote(activeSession.getUuid()));
                    addVote.set(true);
                }
                voteDialog.showAndWait().ifPresent(usersVote -> {
                    if (addVote.get()) {
                        votes.add(usersVote);
                    }
                });
            });
            if (OTNSettings.FAV_AND_SCHEDULE_ENABLED) {
                appBar.getActionItems().addAll(scheduleBtn, favoriteBtn, voteButton);
            } else {
                appBar.getActionItems().add(voteButton);
            }
        } else if (OTNSettings.FAV_AND_SCHEDULE_ENABLED){
            appBar.getActionItems().addAll(scheduleBtn, favoriteBtn);
        }

    }

    private BottomNavigation createBottomNavigation(final Session session) {

        BottomNavigation bottomNavigation = new BottomNavigation();

        final ToggleButton infoButton = bottomNavigation.createButton(OTNBundle.getString("OTN.BUTTON.INFO"), MaterialDesignIcon.INFO.graphic(), e -> {
            // when clicked create a label in a scrollpane. Label will contain
            // session summary for this session.
            Label sessionSummary = new Label(session.getSummary());
            sessionSummary.setWrapText(true);
            sessionSummary.getStyleClass().add("session-summary");
            sessionView.setCenter(createScrollPane(sessionSummary));
        });
        infoButton.setUserData(Pane.INFO);

        final ToggleButton speakerButton = bottomNavigation.createButton(OTNBundle.getString("OTN.BUTTON.SPEAKERS"), MaterialDesignIcon.ACCOUNT_CIRCLE.graphic(), e -> {
            // when clicked we create an avatar pane containing all speakers.
            // The entire avatar pane is not scrollable, as we want the speaker
            // avatars to remain fixed. Instead, we make the avatar content area
            // scrollable below.

            // create avatar pane for speakers in this session.
            // We create it now (rather than in showSession) so that the animations play every time.
            speakerAvatarPane = createSpeakerAvatarPane(fetchSpeakers(session));
            sessionView.setCenter(speakerAvatarPane);
        });
        speakerButton.setUserData(Pane.SPEAKER);

        final ToggleButton noteButton = bottomNavigation.createButton(OTNBundle.getString("OTN.BUTTON.NOTES"), MaterialDesignIcon.MESSAGE.graphic(), e -> {
            if (service.isAuthenticated() || !OTNSettings.USE_REMOTE_NOTES) {
                loadAuthenticatedNotesView(session);
            } else {
                LoginPrompter loginPromptView = new LoginPrompter(
                        service,
                        OTNBundle.getString("OTN.SESSION.LOGIN_TO_RECORD_NOTES"),
                        MaterialDesignIcon.MESSAGE,
                        () -> loadAuthenticatedNotesView(session));
                sessionView.setCenter(loginPromptView);
            }
        });
        noteButton.setUserData(Pane.NOTE);

        if (session.getSpeakersUuid() == null || session.getSpeakersUuid().isEmpty()) {
            bottomNavigation.getActionItems().addAll(infoButton, noteButton);
        } else {
            bottomNavigation.getActionItems().addAll(infoButton, speakerButton, noteButton);
        }

        // listen to the selected toggle so we ensure it is selected when the view is returned to
        noteButton.getToggleGroup().selectedToggleProperty().addListener((o,ov,nv) -> {
            if(ov != null && ov.getUserData() == Pane.SPEAKER) {
                ObjectProperty<EventHandler<ActionEvent>> eventHandler = new SimpleObjectProperty<>();
                if(nv.getUserData() == Pane.INFO) {
                    eventHandler.setValue(infoButton.getOnAction());
                    infoButton.setOnAction(null);
                } else if(nv.getUserData() == Pane.NOTE) {
                    eventHandler.setValue(noteButton.getOnAction());
                    noteButton.setOnAction(null);
                }

                // Code to be executed after exit animation
                speakerAvatarPane.setExitAction(() -> {
                    if(infoButton.getOnAction() == null) {
                        infoButton.setOnAction(eventHandler.getValue());
                        infoButton.fireEvent(new ActionEvent());
                    } else if(noteButton.getOnAction() == null) {
                        noteButton.setOnAction(eventHandler.getValue());
                        noteButton.fireEvent(new ActionEvent());
                    }
                });
                ((AvatarPaneSkin)speakerAvatarPane.getSkin()).requestAnimateUp();
            }
            lastSelectedButton = nv;
        });
        infoButton.setSelected(true);

        return bottomNavigation;
    }

    private void loadAuthenticatedNotesView(final Session session) {
        sessionNotesEditor = new SessionNotesEditor(session.getUuid(), service);
        sessionView.setCenter(sessionNotesEditor);
    }

    private ObservableList<Speaker> fetchSpeakers(Session activeSession) {
        ObservableList<Speaker> speakers = FXCollections.observableArrayList();
        if (activeSession.getSpeakersUuid() != null) {
            for (String speakerUuid : activeSession.getSpeakersUuid()) {
                for (Speaker speaker : service.retrieveSpeakers()) {
                    if (speakerUuid.equals(speaker.getUuid())) {
                        speakers.add(speaker);
                        break;
                    }
                }
            }
        }
        return speakers;
    }

    private AvatarPane<Speaker> createSpeakerAvatarPane(ObservableList<Speaker> speakers) {
        AvatarPane<Speaker> avatarPane = new AvatarPane<>(speakers);
        avatarPane.setExpanded(true);
        avatarPane.setCollapsible(false);
        avatarPane.setAvatarFactory(Util::getSpeakerAvatar);
        avatarPane.setContentFactory(speaker -> {
            Label name = new Label(speaker.getFullName());
            name.getStyleClass().add("name");
            name.setWrapText(true);
            GridPane.setHgrow(name, Priority.ALWAYS);

            Label jobTitle = new Label(speaker.getJobTitle());
            jobTitle.getStyleClass().add("job-title");
            jobTitle.setWrapText(true);
            GridPane.setHgrow(jobTitle, Priority.ALWAYS);

            Label company = new Label(speaker.getCompany());
            company.getStyleClass().add("company");
            company.setWrapText(true);
            GridPane.setHgrow(company, Priority.ALWAYS);

            Label summary = new Label(speaker.getSummary());
            summary.getStyleClass().add("summary");
            summary.setWrapText(true);
            GridPane.setHgrow(summary, Priority.ALWAYS);
            GridPane.setVgrow(summary, Priority.ALWAYS);

            Button speakerBtn = MaterialDesignIcon.CHEVRON_RIGHT.button(e -> {
                OTNView.SPEAKER.switchView().ifPresent(presenter -> {
                    SpeakerPresenter speakerPresenter = (SpeakerPresenter) presenter;
                    speakerPresenter.setSpeaker(speaker);
                });
            });
            speakerBtn.getStyleClass().add("speaker-btn");
            GridPane.setHgrow(speakerBtn, Priority.NEVER);

            // put everything in its right place
            GridPane gridPane = new GridPane();
            gridPane.getStyleClass().add("content-box");
            gridPane.add(name, 0, 0);
            gridPane.add(jobTitle, 0, 1);
            gridPane.add(company, 0, 2);
            gridPane.add(speakerBtn, 1, 0, 1, 3);
            gridPane.add(summary, 0, 3, 2, 1);


            return createScrollPane(gridPane);
        });
        return avatarPane;
    }

    private ScrollPane createScrollPane(Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        return sp;
    }

    private Node createSessionDetails(Session session) {
        Label sessionTitle = new Label(session.getTitle());
        sessionTitle.getStyleClass().add("header");

        Label sessionInfo = new Label(sessionVisuals.formatMultilineInfo(session));
        sessionInfo.getStyleClass().add("info");

        VBox vbox = new VBox(sessionTitle, sessionInfo);
        vbox.getStyleClass().add("session-info");

        return vbox;
    }

    public enum Pane {
        INFO,
        SPEAKER,
        NOTE
    }
}
