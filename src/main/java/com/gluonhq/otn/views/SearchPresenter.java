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
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.Exhibitor;
import com.gluonhq.otn.model.Note;
import com.gluonhq.otn.model.Searchable;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.model.Session;
import com.gluonhq.otn.model.Speaker;
import com.gluonhq.otn.model.Venue;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.util.OTNSearch;
import com.gluonhq.otn.views.cell.SearchCell;
import com.gluonhq.otn.views.cell.SearchHeaderCell;
import com.gluonhq.otn.views.helper.Placeholder;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javax.inject.Inject;

public class SearchPresenter extends GluonPresenter<OTNApplication> {

    private static final int MIN_CHARACTERS = 3;
    private Placeholder emptySearchPlaceholder = new Placeholder(OTNBundle.getString("OTN.SEARCH.PLACEHOLDER"), MaterialDesignIcon.SEARCH),
                        noResultsPlaceholder = new Placeholder(OTNBundle.getString("OTN.SEARCH.NO_RESULTS"), MaterialDesignIcon.SEARCH);

    @FXML
    private View search;
    
    @FXML
    private CharmListView<Searchable, String> searchListView;

    private TextField searchTextField;
    private Button clearButton;
    
    @Inject
    private Service service;

    @Inject
    private OTNSearch otnSearch;
    
    private ObservableList<Searchable> results;
    private ObservableList<Searchable> prefixResults;
    private String prefix;
    
    public void initialize() {
        results = FXCollections.observableArrayList();
        prefixResults = FXCollections.observableArrayList();
        searchTextField = new TextField();
        searchTextField.getStyleClass().add("search-text-field");
        searchTextField.setPromptText(OTNBundle.getString("OTN.SEARCH.PROMPT"));
        
        clearButton = MaterialDesignIcon.CLOSE.button(e -> {
            searchTextField.clear();
            searchListView.itemsProperty().clear();
        });
        clearButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                final String text = searchTextField.getText();
                if (text.isEmpty()) {
                    searchListView.itemsProperty().clear();
                } else if (text.length() == MIN_CHARACTERS) {
                    // initial search
                    prefix = text;
                    backgroundSearch().start();
                } else if (text.length() > MIN_CHARACTERS) {
                    // while typing more characters, use the initial results to refine
                    // the search over those results.
                    if (prefix.equals(text.substring(0, MIN_CHARACTERS))) {
                        refineBackgroundSearch().start();
                    } else {
                        // Whenever the prefix changes, do a full search again
                        prefix = text.substring(0, MIN_CHARACTERS);
                        backgroundSearch().start();
                    }
                }
                return text.isEmpty();
            }, searchTextField.textProperty()));
        
        search.setOnShowing(e -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setPrefHeight(appBar.getHeight());
            if (!appBar.getStyleClass().contains("search-app-bar")) {
                appBar.getStyleClass().add("search-app-bar");
            }
            appBar.setNavIcon(getApp().getNavMenuButton());
            appBar.setTitle(searchTextField);
            appBar.getActionItems().add(clearButton);
            setTextFieldWidth(appBar);
            appBar.widthProperty().addListener(o -> setTextFieldWidth(appBar));
            searchTextField.requestFocus();
            if (searchListView != null) {
                searchListView.setSelectedItem(null);
            }
            
        });
        search.setOnHiding(e -> {
            AppBar appBar = getApp().getAppBar();
            if (appBar.getStyleClass().contains("search-app-bar")) {
                appBar.getStyleClass().remove("search-app-bar");
            }
            appBar.setPrefHeight(-1);
        });

        searchListView.setPlaceholder(emptySearchPlaceholder);
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                searchListView.setPlaceholder(noResultsPlaceholder);
            } else {
                searchListView.setPlaceholder(emptySearchPlaceholder);
            }
        });
        searchListView.setHeadersFunction(item -> item.getClass().toString());
        searchListView.setCellFactory(p -> new SearchCell(service));
        searchListView.getStyleClass().add("search-list-view");
        searchListView.setItems(results);
        searchListView.setHeaderCellFactory(p -> new SearchHeaderCell());
        addViewChangeListener(searchListView);
    }

    private void setTextFieldWidth(AppBar appBar) {
        searchTextField.setPrefWidth(appBar.getWidth() - appBar.getNavIcon().prefWidth(-1) - 
                        clearButton.prefWidth(-1));
    }
    
    private Thread backgroundSearch() {
        Runnable task = () -> {
            ObservableList<Searchable> search1 = otnSearch.search(searchTextField.getText());
            Platform.runLater(() -> {
                results.setAll(search1);
                // Keep the initial results on a list to refine the search based on them
                prefixResults.setAll(results);
            });
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        return thread;
    }
    
    private Thread refineBackgroundSearch() {
        Runnable task = () -> {
            // Uses the initial results to refine the search based on them
            // We don't update the prefixResults list, as this will imply losing elements from it,
            // and in case the user hits the back key, there won't be the same items to perform the search
            // as there were before
            ObservableList<Searchable> refineSearch = otnSearch.refineSearch(searchTextField.getText(), prefixResults);
            Platform.runLater(() -> results.setAll(refineSearch));
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        return thread;
    }

    private void addViewChangeListener(CharmListView<Searchable, String> searchListView) {
        searchListView.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue instanceof Exhibitor) {
                    OTNView.EXHIBITOR.switchView().ifPresent(presenter ->
                            ((ExhibitorPresenter) presenter).setExhibitor((Exhibitor) newValue));
                } else if (newValue instanceof Note) {
                    service.findSession(((Note) newValue).getSessionUuid()).ifPresent(session ->
                        OTNView.SESSION.switchView().ifPresent(presenter ->
                            ((SessionPresenter) presenter).showSession(session, SessionPresenter.Pane.NOTE)));
                } else if (newValue instanceof Session) {
                    OTNView.SESSION.switchView().ifPresent(presenter ->
                            ((SessionPresenter) presenter).showSession((Session) newValue));
                } else if (newValue instanceof Speaker) {
                    OTNView.SPEAKER.switchView().ifPresent( presenter ->
                            ((SpeakerPresenter)presenter).setSpeaker((Speaker) newValue));
                } else if (newValue instanceof Venue) {
                    OTNView.VENUE.switchView().ifPresent( presenter ->
                            ((VenuePresenter)presenter).setVenue((Venue) newValue));
//                } if (newValue instanceof Sponsor) {
//                    OTNView.SPONSOR.switchView().ifPresent( presenter ->
//                            ((SponsorPresenter)presenter).setSponsor((Sponsor) newValue));
                }
            }
        });
    }

}
