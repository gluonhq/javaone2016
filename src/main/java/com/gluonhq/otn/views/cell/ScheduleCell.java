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
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.model.Session;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.util.OTNSettings;
import com.gluonhq.otn.views.SessionPresenter;
import com.gluonhq.otn.views.helper.SessionVisuals;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

public class ScheduleCell extends CharmListCell<Session> {

    private final Service service;
    private final ListTile listTile;
    private Session session;
    private final SecondaryGraphic secondaryGraphic;
    private boolean showDate;
    
    private PseudoClass oldPseudoClass;

    public ScheduleCell(Service service) {
        this(service, false);
    }

    public ScheduleCell(Service service, boolean showDate) {
        this.service = service;
        this.showDate = showDate;


        secondaryGraphic = new SecondaryGraphic();

        listTile = new ListTile();
        listTile.setWrapText(true);
        listTile.setSecondaryGraphic(secondaryGraphic);

        setText(null);
        getStyleClass().add("schedule-cell");
    }

    @Override
    public void updateItem(Session item, boolean empty) {
        super.updateItem(item, empty);
        session = item;
        if (item != null && !empty) {
            updateVBox();
            secondaryGraphic.updateGraphic(session);
            setGraphic(listTile);

            // FIX for OTN-568
            listTile.setOnMouseReleased(event -> {
                OTNView.SESSION.switchView().ifPresent(presenter ->
                        ((SessionPresenter) presenter).showSession(session));
            });
        } else {
            setGraphic(null);
        }
    }
    
    private void updateVBox() {
        listTile.textProperty().clear();
        listTile.textProperty().add(session.getTitle());

        String trackTitle = session.getTrack().getTitle();
        if (trackTitle == null || trackTitle.isEmpty()) {
            listTile.textProperty().add(session.getType());
        } else {
            listTile.textProperty().add(trackTitle);
        }

        listTile.textProperty().add(OTNBundle.getString("OTN.SCHEDULE.IN_AT", session.getLocation(),
                        OTNSettings.TIME_FORMATTER.format(session.getStartDate()))
                        + (showDate? "\n" + OTNSettings.DATE_FORMATTER.format(session.getStartDate()) : ""));

        changePseudoClass(session.getTrack().getPseudoClass());


    }

    private void changePseudoClass(PseudoClass pseudoClass) {
        pseudoClassStateChanged(oldPseudoClass, false);
        pseudoClassStateChanged(pseudoClass, true);
        oldPseudoClass = pseudoClass;
    }
    
    private class SecondaryGraphic extends Pane {

        private final Node chevron;
        private StackPane indicator;

        public SecondaryGraphic() {
            chevron = MaterialDesignIcon.CHEVRON_RIGHT.graphic();
            indicator = createIndicator(SessionVisuals.SessionListType.SCHEDULED, true);
            getChildren().addAll(chevron, indicator);
        }

        @Override protected void layoutChildren() {
            final double w = getWidth();
            final double h = getHeight();

            double indicatorWidth = indicator.prefWidth(-1);
            double indicatorHeight = indicator.prefHeight(-1);
            indicator.resizeRelocate(w - indicatorWidth, 0, indicatorWidth, indicatorHeight);

            double chevronWidth = chevron.prefWidth(-1);
            double chevronHeight = chevron.prefHeight(-1);
            chevron.resizeRelocate(0, h / 2.0 - chevronHeight / 2.0, chevronWidth, chevronHeight);
        }

        public void updateGraphic(Session session) {
            // FIXME this doesn't work as the retrieve* calls return empty lists on first call, resulting in graphics
            // not being shown.

            final boolean authenticated = service.isAuthenticated();

            if ( authenticated && service.retrieveScheduledSessions().contains(session)) {
                resetIndicator( indicator, SessionVisuals.SessionListType.SCHEDULED);
                indicator.setVisible(true);
            } else if ( authenticated && service.retrieveFavoriteSessions().contains(session)) {
                resetIndicator( indicator, SessionVisuals.SessionListType.FAVORITES);
                indicator.setVisible(true);
            } else {
                indicator.setVisible(false);
            }
        }

        private void resetIndicator(StackPane indicator, SessionVisuals.SessionListType style) {
            if (!indicator.getStyleClass().contains(style.getStyleClass()) ) {
                final Node graphic = style.getOnGraphic();
                StackPane.setAlignment(graphic, Pos.TOP_RIGHT);
                indicator.getChildren().set(0, graphic);

                indicator.getStyleClass().remove( style.other().getStyleClass());
                indicator.getStyleClass().add( style.getStyleClass());
            }
        }

        private StackPane createIndicator(SessionVisuals.SessionListType style, boolean topRight) {
            Node graphic = style.getOnGraphic();
            StackPane node = new StackPane(graphic);
            StackPane.setAlignment(graphic, topRight ? Pos.TOP_RIGHT : Pos.BOTTOM_RIGHT);

            node.setVisible(false);
            node.getStyleClass().addAll("indicator", style.getStyleClass());

            GridPane.setVgrow(node, Priority.ALWAYS);
            GridPane.setHalignment(node, HPos.RIGHT);

            return node;
        }
    }
}
