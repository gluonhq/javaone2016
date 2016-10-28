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
import com.gluonhq.charm.glisten.afterburner.GluonView;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.BottomNavigation;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.layout.layer.SidePopupView;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.model.Session;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.util.OTNSettings;
import com.gluonhq.otn.views.cell.ScheduleCell;
import com.gluonhq.otn.views.cell.ScheduleHeaderCell;
import com.gluonhq.otn.views.helper.FilterSessionsPresenter;
import com.gluonhq.otn.views.helper.LoginPrompter;
import com.gluonhq.otn.views.helper.Placeholder;
import com.gluonhq.otn.views.helper.SessionVisuals;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.function.Predicate;

import static com.gluonhq.otn.OTNApplication.POPUP_FILTER_SESSIONS_MENU;

public class SessionsPresenter  extends GluonPresenter<OTNApplication> {
    private static final String SESSIONS_PLACEHOLDER_MESSAGE = OTNBundle.getString("OTN.CONTENT_CATALOG.ALL_SESSIONS.PLACEHOLDER_MESSAGE");

    private static final String SCHEDULE_LOGIN_PROMPT_MESSAGE = OTNBundle.getString("OTN.CONTENT_CATALOG.SCHEDULED_SESSIONS.LOGIN_PROMPT");
    private static final String SCHEDULE_EMPTY_LIST_MESSAGE = OTNBundle.getString("OTN.CONTENT_CATALOG.SCHEDULED_SESSIONS.EMPTY_LIST_MESSAGE");

    private static final String FAVORITE_LOGIN_PROMPT_MESSAGE = OTNBundle.getString("OTN.CONTENT_CATALOG.FAVORITE_SESSIONS.LOGIN_PROMPT");
    private static final String FAVORITE_EMPTY_LIST_MESSAGE = OTNBundle.getString("OTN.CONTENT_CATALOG.FAVORITE_SESSIONS.EMPTY_LIST_MESSAGE");

    private static final MaterialDesignIcon SESSIONS_ICON = MaterialDesignIcon.DASHBOARD;
    private static final MaterialDesignIcon SCHEDULER_ICON = MaterialDesignIcon.STAR;
    private static final MaterialDesignIcon FAVORITE_ICON = SessionVisuals.SessionListType.FAVORITES.getOnIcon();

    private static final PseudoClass PSEUDO_FILTER_ENABLED = PseudoClass.getPseudoClass("filter-enabled");

    private static enum ContentDisplayMode {
        ALL, SCHEDULED, FAVORITE
    }

    @FXML
    private View sessions;

    private CharmListView<Session, LocalDate> scheduleListView;

    @Inject
    private Service service;

    private final ObjectProperty<Predicate<Session>> filterPredicateProperty = new SimpleObjectProperty<>();
    private SidePopupView filterPopup;
    private FilteredList<Session> filteredSessions;

    private Toggle lastSelectedButton;
    private EventHandler<ActionEvent> currentHandler;

    public void initialize() {
        // navigation between all sessions and the users scheduled sesssions
        if (OTNSettings.FAV_AND_SCHEDULE_ENABLED) {
            final BottomNavigation bottomNavigation = createBottomNavigation();
            sessions.setBottom(bottomNavigation);
        } else {
            sessions.setCenter(createSessionsList(ContentDisplayMode.ALL));
        }

        final Button filterButton = createFilterButton();

        sessions.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavMenuButton());
            appBar.setTitleText(OTNView.SESSIONS.getTitle());
            appBar.getActionItems().addAll(getApp().getSearchButton(), filterButton);

            // Will never be null
            if (OTNSettings.FAV_AND_SCHEDULE_ENABLED) {
                lastSelectedButton.setSelected(true);
                if (currentHandler != null) {
                    currentHandler.handle(null);
                }
            }
            if(scheduleListView != null) {
                scheduleListView.setSelectedItem(null);
            }
        });
        
        MobileApplication.getInstance().addLayerFactory(POPUP_FILTER_SESSIONS_MENU, () -> { 
            if (filterPopup == null) {
                GluonView filterSessionsView = new GluonView(FilterSessionsPresenter.class);
                FilterSessionsPresenter filterPresenter = (FilterSessionsPresenter) filterSessionsView.getPresenter();
                filterPopup = new SidePopupView(filterSessionsView.getView(), Side.TOP, true);
                filterPredicateProperty.bind(filterPresenter.searchPredicateProperty());
                filterPresenter.showingProperty().bind(filterPopup.showingProperty());
                filterPresenter.filterAppliedProperty().addListener((ov, oldValue, newValue) -> {
                    filterButton.pseudoClassStateChanged(PSEUDO_FILTER_ENABLED, newValue);
                });
            }
            return filterPopup;
        });
    }

    private BottomNavigation createBottomNavigation() {
        BottomNavigation bottomNavigation = new BottomNavigation();

        // Show all sessions
        ToggleButton sessionsButton = bottomNavigation.createButton(OTNBundle.getString("OTN.BUTTON.SESSIONS"), SESSIONS_ICON.graphic(), e -> {
            sessions.setCenter(createSessionsList(ContentDisplayMode.ALL));
        });

        // show scheduled sessions
        EventHandler<ActionEvent> scheduleHandler = e -> {
            if (!service.isAuthenticated()) {
                sessions.setCenter(new LoginPrompter(service, SCHEDULE_LOGIN_PROMPT_MESSAGE, SCHEDULER_ICON, () -> sessions.setCenter(createSessionsList(ContentDisplayMode.SCHEDULED))));
            } else {
                sessions.setCenter(createSessionsList(ContentDisplayMode.SCHEDULED));
            }
        };
        ToggleButton scheduleButton = bottomNavigation.createButton(OTNBundle.getString("OTN.BUTTON.MY_SCHEDULE"), SCHEDULER_ICON.graphic(), scheduleHandler);

        // show favorite sessions
        EventHandler<ActionEvent> favoriteHandler = e -> {
            if (!service.isAuthenticated()) {
                sessions.setCenter(new LoginPrompter(service, FAVORITE_LOGIN_PROMPT_MESSAGE, FAVORITE_ICON, () -> sessions.setCenter(createSessionsList(ContentDisplayMode.FAVORITE))));
            } else {
                sessions.setCenter(createSessionsList(ContentDisplayMode.FAVORITE));
            }
        };
        ToggleButton favoriteButton = bottomNavigation.createButton(OTNBundle.getString("OTN.BUTTON.MY_FAVORITES"), FAVORITE_ICON.graphic(), favoriteHandler);

        bottomNavigation.getActionItems().addAll(sessionsButton, scheduleButton, favoriteButton);

        // listen to the selected toggle so we ensure it is selected when the view is returned to
        favoriteButton.getToggleGroup().selectedToggleProperty().addListener((o,ov,nv) -> {
            lastSelectedButton = nv;
            if (nv == sessionsButton) {
                currentHandler = null;
            } else if (nv == scheduleButton) {
                currentHandler = scheduleHandler;
            } else if (nv == favoriteButton) {
                currentHandler = favoriteHandler;
            }
        });
        sessionsButton.setSelected(true);

        return bottomNavigation;
    }

    private Button createFilterButton() {
        Button filterButton = MaterialDesignIcon.FILTER_LIST.button();
        filterButton.getStyleClass().add("filter-button");
        filterButton.setOnAction(a -> MobileApplication.getInstance().showLayer(POPUP_FILTER_SESSIONS_MENU));
        return filterButton;
    }

    private Node createSessionsList(ContentDisplayMode mode) {
        if (scheduleListView == null) {
            scheduleListView = new CharmListView<>();
            scheduleListView.getStyleClass().add("schedule-view");
            scheduleListView.setHeadersFunction(s -> s.getStartDate().toLocalDate());
            scheduleListView.setHeaderCellFactory(c -> new ScheduleHeaderCell());
            scheduleListView.setComparator((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()));
            scheduleListView.setCellFactory(p -> new ScheduleCell(service));
//            scheduleListView.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//                if (newValue != null) {
//                    OTNView.SESSION.switchView().ifPresent(presenter ->
//                        ((SessionPresenter) presenter).showSession(newValue));
//                }
//            });
        }

        if(filteredSessions!= null && filteredSessions.predicateProperty().isBound()) {
            filteredSessions.predicateProperty().unbind();
        }

        switch (mode) {
            case ALL: {
                scheduleListView.setPlaceholder(new Placeholder(SESSIONS_PLACEHOLDER_MESSAGE, SESSIONS_ICON));
                filteredSessions = new FilteredList<>(service.retrieveSessions());
                break;
            }
            case SCHEDULED: {
                scheduleListView.setPlaceholder(new Placeholder(SCHEDULE_EMPTY_LIST_MESSAGE, SCHEDULER_ICON));
                filteredSessions = new FilteredList<>(service.retrieveScheduledSessions());
                break;
            }
            case FAVORITE: {
                scheduleListView.setPlaceholder(new Placeholder(FAVORITE_EMPTY_LIST_MESSAGE, FAVORITE_ICON));
                filteredSessions = new FilteredList<>(service.retrieveFavoriteSessions());
                break;
            }
        }
        filteredSessions.predicateProperty().bind(filterPredicateProperty);
        scheduleListView.setItems(filteredSessions);

        return scheduleListView;
    }
}
