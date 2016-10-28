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
package com.gluonhq.otn.views.helper;

import com.gluonhq.charm.glisten.control.Toast;
import com.gluonhq.charm.glisten.visual.GlistenStyleClasses;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.model.Session;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.util.OTNNotifications;
import com.gluonhq.otn.util.OTNSettings;
import com.gluonhq.otn.views.dialog.SessionConflictDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.gluonhq.charm.glisten.visual.MaterialDesignIcon.*;
import static com.gluonhq.otn.util.OTNLogging.LOGGING_ENABLED;

@Singleton
public class SessionVisuals {

    private static final Logger LOG = Logger.getLogger(SessionVisuals.class.getName());
    
    public enum SessionListType {
        FAVORITES("favorites",  "favorite",  FAVORITE_BORDER, FAVORITE),
        SCHEDULED("scheduled",  "scheduled", STAR_BORDER,     STAR);

        private final String id;
        private final MaterialDesignIcon offIcon;
        private final MaterialDesignIcon onIcon;
        private final String style;

        SessionListType(String description, String style, MaterialDesignIcon offIcon, MaterialDesignIcon onIcon) {
            id = description;
            this.style = style;
            this.offIcon = offIcon;
            this.onIcon = onIcon;
        }

        public String getStyleClass() {
            return style;
        }

        public Node getOffGraphic() {
            return offIcon.graphic();
        }

        public Node getOnGraphic() {
            return onIcon.graphic();
        }

        public MaterialDesignIcon getOnIcon() {
            return onIcon;
        }

        public SessionListType other() {
            return this == FAVORITES? SCHEDULED: FAVORITES;
        }
    }

    @Inject
    private Service service;
    
    @Inject
    private OTNNotifications otnNotifications;

    private boolean usingOfflineEmptyLists = true;
    private final Map<SessionListType, ObservableList<Session>> cloudLists = new HashMap<>();

    // Method marked with @PostConstruct is called immediately after
    // instance is created and and members are injected
    // It should be used for initialization instead of constructor in DI contexts
    @PostConstruct
    private void init() {
        retrieveLists();
    }

    public Service getService() {
        return service;
    }

    public String formatMultilineInfo(Session session) {
        ZonedDateTime startDate = session.getStartDate();
        ZonedDateTime endDate = session.getEndDate();
        return String.format(OTNBundle.getString("OTN.VISUALS.FORMAT.MULTILINE", 
                session.getTrack().getTitle(),
                session.getConferenceDayIndex(),
                startDate.format(OTNSettings.TIME_FORMATTER),
                endDate.format(OTNSettings.TIME_FORMATTER),
                session.getLocation())
        );
    }

    public String formatOneLineInfo(Session session) {
        ZonedDateTime startDate = session.getStartDate();
        ZonedDateTime endDate = session.getEndDate();
        return String.format(OTNBundle.getString("OTN.VISUALS.FORMAT.ONELINE", 
                session.getConferenceDayIndex(),
                startDate.format(OTNSettings.TIME_FORMATTER),
                endDate.format(OTNSettings.TIME_FORMATTER),
                session.getLocation())
        );

    }

    public ToggleButton getFavoriteButton(Session session) {
        return buildButton(SessionListType.FAVORITES, session);
    }

    public ToggleButton getSelectedButton(Session session) {
        return buildButton(SessionListType.SCHEDULED, session);
    }

    private ToggleButton buildButton(SessionListType listType, Session session) {
        ToggleButton button = new ToggleButton("", listType.getOffGraphic());
        button.getStyleClass().addAll(
                listType.getStyleClass(),
                GlistenStyleClasses.BUTTON_FLAT,
                GlistenStyleClasses.BUTTON_ROUND);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        // should be a separate listener or styles are messed up
        button.selectedProperty().addListener((o, ov, selected) -> {
            button.setGraphic(selected ? listType.getOnGraphic() : listType.getOffGraphic());
        });

        // setup  binding between button and appropriate list of session ids
        boolean isSelected = listContains(session, listType);
        button.setSelected(isSelected);
        button.selectedProperty().addListener((o, ov, selected) -> {
            if (button.getUserData() == LOCK) return;

            if (!service.isAuthenticated()) {
                boolean authenticated = service.authenticate();
                if (!authenticated) {
                    quietUpdateBtnState(button, () -> button.setSelected(ov));
                    return;
                }
            }

            // we must use the session stored in the button - not the one provided
            // as an argument to the buildButton method
            Session actualSession = (Session) button.getProperties().get(SESSION_KEY);

            if (selected) {
                // Resolve scheduling conflicts
                if (listType == SessionListType.SCHEDULED) {
                    Optional<Session> conflictOption = findConflictingSession(actualSession);
                    if (conflictOption.isPresent()) {
                        Session conflict = conflictOption.get();
                        Optional<Session> dialogResult = new SessionConflictDialog(conflict, actualSession, this).showAndWait();
                        if (dialogResult.isPresent()) {
                            Session selectedSession = dialogResult.get();
                            if (selectedSession.equals(actualSession)) {
                                // It is important to preserve this order: first remove and then add because of the
                                // toast messages. This way "session scheduled" will be displayed.
                                listRemove(conflict, listType);
                                listAdd(actualSession, listType);
                                updateButton(button, true, actualSession);
                            } else {
                                button.setSelected(false);
                            }
                        } else {
                            button.setSelected(false);
                        }


                    } else {
                        listAdd(actualSession, listType);
                    }
                } else {
                    listAdd(actualSession, listType);
                }
            } else {
                listRemove(actualSession, listType);
            }
        });

        updateButton(button, isSelected, session);

        return button;

    }

    private static final Object LOCK = new Object();
    private static final String SESSION_KEY = "SESSION";

    private ObservableList<Session> getList(SessionListType listType) {
        if (usingOfflineEmptyLists && service.isAuthenticated()) {
            // OTN-513 - First time user logs in: stop the listener
            otnNotifications.stopPreloadingScheduledSessions();
            
            retrieveLists();
        }
        return cloudLists.get(listType);
    }

    /**
     * Is session a part of the list
     *
     * @param session
     * @param listType
     * @return
     */
    private boolean listContains(Session session, SessionListType listType) {
        return getList(listType).contains(session);
    }

    /**
     * Add session to a list
     *
     * @param session
     * @param listType
     */
    private void listAdd(Session session, SessionListType listType) {
        if (!service.isAuthenticated() && service.authenticate()) {
            retrieveLists();
        }

        if (service.isAuthenticated()) {
            if (!listContains(session, listType)) {
                if (listType == SessionListType.SCHEDULED) {
                    otnNotifications.addScheduledSessionNotifications(session, false);
                }

                getList(listType).add(session);
                showToast(listType, true);
            }
        }
    }
    
    /**
     * Remove session from a list
     *
     * @param session
     * @param listType
     */
    private void listRemove(Session session, SessionListType listType) {
        if (!service.isAuthenticated() && service.authenticate()) {
            retrieveLists();
        }

        if (service.isAuthenticated()) {
            if (listType == SessionListType.SCHEDULED) {    
                otnNotifications.removeScheduledSessionNotifications(session);
            }

            getList(listType).remove(session);
            showToast(listType, false);
        }
    }

    private void retrieveLists() {
        if (service.isAuthenticated()) {
            cloudLists.put(SessionListType.FAVORITES, service.retrieveFavoriteSessions());
            cloudLists.put(SessionListType.SCHEDULED, service.retrieveScheduledSessions());
            usingOfflineEmptyLists = false;
        } else {
            cloudLists.put(SessionListType.FAVORITES, FXCollections.emptyObservableList());
            cloudLists.put(SessionListType.SCHEDULED, FXCollections.emptyObservableList());
            usingOfflineEmptyLists = true;
        }
    }

    private Optional<Session> findConflictingSession(Session session) {
        for(Session s : getList(SessionListType.SCHEDULED)) {
            if (s == null) {
                if (LOGGING_ENABLED) {
                    LOG.log(Level.WARNING, String.format("Session %s is not found in the session index!", session.getUuid()));
                }
            } else {
                if (s.isOverlappingWith(session)) {
                    return Optional.of(s);
                }
            }
        }
        return Optional.empty();
    }

    private void updateButton(ToggleButton button, boolean selected, Session session) {
        quietUpdateBtnState(button, () -> {
            button.setSelected(selected);
            button.getProperties().put(SESSION_KEY, session);
        });
    }

    private void quietUpdateBtnState(ToggleButton button, Runnable r) {
        button.setUserData(LOCK);
        r.run();
        button.setUserData(null);
    }

    private void showToast(SessionListType listType, boolean added) {
        Toast toast = new Toast();
        if (added) {
            if (listType.equals(SessionListType.FAVORITES)) {
                toast.setMessage(OTNBundle.getString("OTN.VISUALS.SESSION_MARKED_AS_FAVORITE"));
            } else {
                toast.setMessage(OTNBundle.getString("OTN.VISUALS.SESSION_SCHEDULED"));
            }
        } else {
            if (listType.equals(SessionListType.FAVORITES)) {
                toast.setMessage(OTNBundle.getString("OTN.VISUALS.SESSION_UNFAVORITED"));
            } else {
                toast.setMessage(OTNBundle.getString("OTN.VISUALS.SESSION_UNSCHEDULED"));
            }
        }
        toast.show();
    }
}
