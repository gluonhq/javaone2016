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
import com.gluonhq.otn.model.Sponsor;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.views.cell.SponsorCell;
import com.gluonhq.otn.views.cell.SponsorHeaderCell;
import com.gluonhq.otn.views.helper.Placeholder;
import com.gluonhq.otn.views.helper.SponsorCategory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javax.inject.Inject;

public class SponsorsPresenter extends GluonPresenter<OTNApplication> {
    private static final String PLACEHOLDER_MESSAGE = OTNBundle.getString("OTN.SPONSORS.PLACEHOLDER_MESSAGE");

    @FXML
    private View sponsors;
    @FXML
    private CharmListView<Sponsor, SponsorCategory> sponsorListView;

    @Inject
    private Service service;

    public void initialize() {
        sponsors.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavMenuButton());
            appBar.setTitleText(OTNView.SPONSORS.getTitle());
            appBar.getActionItems().add(getApp().getSearchButton());
            sponsorListView.setSelectedItem(null);
        });

        sponsorListView.getStyleClass().add("sponsor-list-view");

        sponsorListView.setPlaceholder(new Placeholder(PLACEHOLDER_MESSAGE, OTNView.SPONSORS.getMenuIcon()));

        ObservableList<Sponsor> sponsorsList = FXCollections.observableArrayList(service.retrieveSponsors());
        sponsorListView.setItems(sponsorsList);

        sponsorListView.setHeadersFunction(Sponsor::getSection);
        sponsorListView.setHeaderComparator((category1, category2) -> Integer.compare(category1.getValue(), category2.getValue()));

        sponsorListView.setCellFactory(p -> new SponsorCell());
        sponsorListView.setHeaderCellFactory(p -> new SponsorHeaderCell());

        sponsorListView.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                OTNView.SPONSOR.switchView().ifPresent( presenter ->
                        ((SponsorPresenter)presenter).setSponsor(newValue)
                );
            }
        });
    }

}
