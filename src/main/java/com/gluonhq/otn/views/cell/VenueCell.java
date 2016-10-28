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
package com.gluonhq.otn.views.cell;

import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.ListTile;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.Venue;
import com.gluonhq.otn.views.VenuePresenter;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class VenueCell extends CharmListCell<Venue> {

    private final ListTile tile;

    public VenueCell() {
        tile = new ListTile();
        tile.setSecondaryGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());

        setText(null);
        getStyleClass().add("venue-cell");
    }

    @Override
    public void updateItem(Venue venue, boolean empty) {
        super.updateItem(venue, empty);
        if (venue != null && !empty) {
            tile.textProperty().setAll(venue.getName());
            setGraphic(tile);

            // FIX for OTN-568
            tile.setOnMouseReleased(event -> {
                OTNView.VENUE.switchView().ifPresent(presenter ->
                        ((VenuePresenter)presenter).setVenue(venue));
            });
         } else {
            setGraphic(null);
         }
    }
}