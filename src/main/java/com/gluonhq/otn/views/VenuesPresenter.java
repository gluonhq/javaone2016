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
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.model.Venue;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.views.cell.VenueCell;
import com.gluonhq.otn.views.helper.Placeholder;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;

import javax.inject.Inject;

public class VenuesPresenter extends GluonPresenter<OTNApplication> {
    private static final String PLACEHOLDER_MESSAGE = OTNBundle.getString("OTN.VENUES.PLACEHOLDER_MESSAGE");

    @FXML
    private View venues;
    @FXML
    private CharmListView<Venue, String> venuesListView;

    private FilteredList<Venue> filteredVenues;

    @Inject
    private Service service;

    public void initialize() {
        venues.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavMenuButton());
            appBar.setTitleText(OTNView.VENUES.getTitle());
            appBar.getActionItems().add(getApp().getSearchButton());
            venuesListView.setSelectedItem(null);
        });

        venuesListView.getStyleClass().add("venues-list-view");

        venuesListView.setPlaceholder(new Placeholder(PLACEHOLDER_MESSAGE, OTNView.VENUES.getMenuIcon()));

        filteredVenues = new FilteredList<>(service.retrieveVenues());
        venuesListView.setItems(filteredVenues);

        venuesListView.setCellFactory(p -> new VenueCell());
//        venuesListView.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                OTNView.VENUE.switchView().ifPresent( presenter ->
//                        ((VenuePresenter)presenter).setVenue(newValue));
//            }
//        });
    }

}
