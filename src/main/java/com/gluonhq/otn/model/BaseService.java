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
package com.gluonhq.otn.model;

import com.airhacks.afterburner.injection.Injector;
import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.StorageService;
import com.gluonhq.charm.glisten.afterburner.GluonView;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.util.OTNNotifications;
import com.gluonhq.otn.util.OTNSettings;
import com.gluonhq.otn.views.helper.Placeholder;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;

import java.io.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseService implements Service {

    private static final Logger LOG = Logger.getLogger(BaseService.class.getName());
    
    protected static File rootDir;
    static {
        try {
            rootDir = Services.get(StorageService.class)
                    .flatMap(StorageService::getPrivateStorage)
                    .orElseThrow(() -> new IOException("Private storage file not available"));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    // user specific data
    private ObservableList<Session> favoriteSessions;
    private ObservableList<Session> scheduledSessions;
    private ObservableList<Note> notes;
    private ObservableList<Vote> votes;

    private String authenticatedUserId = "";

    protected BaseService() {
    }

    public abstract ObservableList<Session> internalRetrieveFavoriteSessions();

    public abstract ObservableList<Session> internalRetrieveScheduledSessions(Runnable onStateSucceeded);

    public abstract ObservableList<Note> internalRetrieveNotes();

    public abstract ObservableList<Vote> internalRetrieveVotes();

    public abstract void retrieveSurveyAnswers();

    public abstract GluonView getAuthenticationView();

    @Override
    public ObservableList<Session> retrieveFavoriteSessions() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("An authenticated user must be available when calling this method.");
        }

        if (favoriteSessions == null) {
            favoriteSessions = internalRetrieveFavoriteSessions();
        }

        return favoriteSessions;
    }

    @Override
    public ObservableList<Session> retrieveScheduledSessions() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("An authenticated user must be available when calling this method.");
        }

        if (scheduledSessions == null) {
            OTNNotifications notifications = Injector.instantiateModelOrService(OTNNotifications.class);
            // stop recreating notifications, after the list of scheduled sessions is fully retrieved
            scheduledSessions = internalRetrieveScheduledSessions(notifications::stopPreloadingScheduledSessions);
            // start recreating notifications as soon as the scheduled sessions are being retrieved
            notifications.preloadScheduledSessions();
            
        }

        return scheduledSessions;
    }

    @Override
    public ObservableList<Note> retrieveNotes() {
        if (!isAuthenticated() && OTNSettings.USE_REMOTE_NOTES) {
            throw new IllegalStateException("An authenticated user must be available when calling this method.");
        }

        if (notes == null) {
            notes = internalRetrieveNotes();
        }

        return notes;
    }

    @Override
    public ObservableList<Vote> retrieveVotes() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("An authenticated user must be available when calling this method.");
        }

        if (votes == null) {
            votes = internalRetrieveVotes();
        }

        return votes;
    }

    @Override
    public boolean authenticate() {
        if (!isAuthenticated()) {
            if (OTNSettings.AUTO_AUTHENTICATION) {
                authenticatedUserId = OTNSettings.getUserUUID();
                storeAuthenticatedUser();
            } else {
                Dialog<Button> dialog = new Dialog<>();
//                final AuthenticationView authenticationView = getAuthenticationView();
//                final View view = authenticationView.getView();
//                final AuthenticationPresenter presenter = authenticationView.getAuthenticationPresenter();
//                view.setPrefWidth(MobileApplication.getInstance().getView().getScene().getWidth() - 40);
//                view.setPrefHeight(MobileApplication.getInstance().getView().getScene().getHeight() - 200);

                Placeholder loginDialogContent = new Placeholder("Temp Login Prompt", "(Awaiting Oracle SSO)\n\nPlease enter *any* username below", MaterialDesignIcon.LOCK);

                // FIXME: Too narrow Dialogs in Glisten
                loginDialogContent.setPrefWidth(MobileApplication.getInstance().getView().getScene().getWidth() - 40);

                TextField usernameField = new TextField();
                usernameField.setFloatText("User Name");
                loginDialogContent.getChildren().add(usernameField);

                dialog.setContent(loginDialogContent);
                Button okButton = new Button(OTNBundle.getString("OTN.LOGIN_DIALOG.LOGIN"));
                Button cancelButton = new Button(OTNBundle.getString("OTN.LOGIN_DIALOG.CANCEL"));
                okButton.setOnAction(e -> {
                    authenticatedUserId = usernameField.getText();
                    storeAuthenticatedUser();
                    dialog.hide();
                });
                cancelButton.setOnAction(e -> dialog.hide());
                dialog.getButtons().addAll(cancelButton, okButton);
                dialog.showAndWait();
            }
        }

        if (isAuthenticated()) {
            loadAuthenticatedData();
        }

        return isAuthenticated();
    }

    @Override
    public boolean isAuthenticated() {
        return authenticatedUserId != null && !authenticatedUserId.isEmpty();
    }

    private boolean loggedOut;
    
    @Override
    public boolean logOut() {
        loggedOut = false;

        Dialog<Button> dialog = new Dialog<>();
        Placeholder logoutDialogContent = new Placeholder("Confirm Logout", OTNBundle.getString("OTN.LOGOUT_DIALOG.CONTENT"), MaterialDesignIcon.HELP);

        // FIXME: Too narrow Dialogs in Glisten
        logoutDialogContent.setPrefWidth(MobileApplication.getInstance().getView().getScene().getWidth() - 40);

        dialog.setContent(logoutDialogContent);
        Button yesButton = new Button(OTNBundle.getString("OTN.LOGOUT_DIALOG.YES"));
        Button noButton = new Button(OTNBundle.getString("OTN.LOGOUT_DIALOG.NO"));
        yesButton.setOnAction(e -> {
            loggedOut = removeAuthenticatedUser();
            if (loggedOut) {
                authenticatedUserId = "";
            }
            dialog.hide();
        });
        noButton.setOnAction(e -> dialog.hide());
        dialog.getButtons().addAll(noButton, yesButton);

        dialog.showAndWait();

        return loggedOut;
    }

    @Override
    public Optional<Session> findSession(String uuid) {
        for (Session session : retrieveSessions()) {
            if (session.getUuid().equals(uuid)) {
                return Optional.of(session);
            }
        }
        return Optional.empty();
    }

    protected String getAuthenticatedUserId() {
        return authenticatedUserId;
    }

    protected void retrieveAuthenticatedUser() {
        LOG.log(Level.INFO, "Retrieving Authenticated User from private storage.");

        if (rootDir != null) {
            try {
                File authenticatedUserInfoFile = new File(rootDir, "authenticatedUser.info");
                if (authenticatedUserInfoFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(authenticatedUserInfoFile))) {
                        authenticatedUserId = reader.readLine();
                    }
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Could not retrieve authenticated user from private storage.", ex);
            }
        }

        if (isAuthenticated()) {
            loadAuthenticatedData();
        } else if (OTNSettings.AUTO_AUTHENTICATION) {
            authenticate();
        }
    }

    private void storeAuthenticatedUser() {
        if (isAuthenticated()) {
            LOG.log(Level.INFO, "Storing Authenticated User to private storage.");

            if (rootDir != null) {
                try {
                    File authenticatedUserInfoFile = new File(rootDir, "authenticatedUser.info");
                    try (FileWriter fileWriter = new FileWriter(authenticatedUserInfoFile)) {
                        fileWriter.write(authenticatedUserId);
                    }
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Could not store authenticated user to private storage.", ex);
                }
            }
        }
    }
    
    private boolean removeAuthenticatedUser() {
        if (isAuthenticated()) {
            LOG.log(Level.INFO, "Removing Authenticated User from private storage.");

            if (rootDir != null) {
                File authenticatedUserInfoFile = new File(rootDir, "authenticatedUser.info");
                if (authenticatedUserInfoFile.exists()) {
                    return authenticatedUserInfoFile.delete();
                }
            }
        }
        return false;
    }

    /**
     * Loads all authenticated data when user is authenticated.
     */
    protected void loadAuthenticatedData() {
        if ( isAuthenticated() ) {
            retrieveFavoriteSessions();
            retrieveScheduledSessions();
            retrieveNotes();
            retrieveVotes();
            retrieveSurveyAnswers();
        }
    }
    
}
