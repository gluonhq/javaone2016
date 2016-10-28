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

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.model.Session;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.util.OTNSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

import static com.gluonhq.otn.OTNApplication.POPUP_FILTER_SESSIONS_MENU;

public class FilterSessionsPresenter extends GluonPresenter<OTNApplication> {

    @FXML private CheckBox day1;
    @FXML private CheckBox day2;
    @FXML private CheckBox day3;
    @FXML private CheckBox day4;
    @FXML private CheckBox day5;
    
    @FXML private CheckBox track1;
    @FXML private CheckBox track2;
    @FXML private CheckBox track3;
    @FXML private CheckBox track4;
    @FXML private CheckBox track5;
    @FXML private CheckBox track6;
    @FXML private CheckBox track7;
    
    @FXML private CheckBox type1;
    @FXML private CheckBox type2;
    @FXML private CheckBox type3;
    @FXML private CheckBox type4;
    @FXML private CheckBox type5;
    @FXML private CheckBox type6;
    @FXML private CheckBox type7;
    
    @FXML private Tab tabDay;
    @FXML private Tab tabTrack;
    @FXML private Tab tabType;

    @FXML private HBox buttonContainer;
    @FXML private Button applyButton;
    @FXML private Button resetButton;

    // A list to temporarily keep checkboxes whose selectedProperty has changed
    private final ObservableList<CheckBox> draftFilter = FXCollections.observableArrayList();
    private static final Predicate<Session> DEFAULT_PREDICATE = session -> true;

    public void initialize() {
        tabDay.setText(OTNBundle.getString("OTN.FILTER.TABDAY"));
        tabTrack.setText(OTNBundle.getString("OTN.FILTER.TABTRACK"));
        tabType.setText(OTNBundle.getString("OTN.FILTER.TABTYPE"));

        DateTimeFormatter formatter = OTNSettings.DATE_FORMATTER;
        day1.setText(formatter.format(Session.CONFERENCE_START_DATE));
        day2.setText(formatter.format(Session.CONFERENCE_START_DATE.plusDays(1)));
        day3.setText(formatter.format(Session.CONFERENCE_START_DATE.plusDays(2)));
        day4.setText(formatter.format(Session.CONFERENCE_START_DATE.plusDays(3)));
        day5.setText(formatter.format(Session.CONFERENCE_START_DATE.plusDays(4)));

        track1.setText(OTNBundle.getString("OTN.TRACK.CORE_JAVA_PLATFORM"));
        track2.setText(OTNBundle.getString("OTN.TRACK.EMERGING_LANGUAGES"));
        track3.setText(OTNBundle.getString("OTN.TRACK.JAVA_CLOUD_AND_SERVER-SIDE_DEVELOPMENT"));
        track4.setText(OTNBundle.getString("OTN.TRACK.JAVA_AND_DEVICES"));
        track5.setText(OTNBundle.getString("OTN.TRACK.JAVA_CLIENTS_AND_USER_INTERFACES"));
        track6.setText(OTNBundle.getString("OTN.TRACK.JAVA_DEVELOPMENT_TOOLS"));
        track7.setText(OTNBundle.getString("OTN.TRACK.JAVA_DEVOPS_AND_METHODOLOGIES"));

        type1.setText(OTNBundle.getString("OTN.SESSION.TYPE.BOF"));
        type2.setText(OTNBundle.getString("OTN.SESSION.TYPE.CONFERENCE"));
        type3.setText(OTNBundle.getString("OTN.SESSION.TYPE.HOL"));
        type4.setText(OTNBundle.getString("OTN.SESSION.TYPE.KEYNOTE"));
        type5.setText(OTNBundle.getString("OTN.SESSION.TYPE.TUTORIAL"));
        type6.setText(OTNBundle.getString("OTN.SESSION.TYPE.UGF"));
        type7.setText(OTNBundle.getString("OTN.SESSION.TYPE.UNIVERSITY"));

        applyButton.setText(OTNBundle.getString("OTN.BUTTON.APPLY"));
        resetButton.setText(OTNBundle.getString("OTN.FILTER.BUTTON.RESET"));

        showing.addListener((observable, oldValue, newValue) -> {
            if(!newValue) {
                for (CheckBox checkBox : draftFilter) {
                    checkBox.setSelected(!checkBox.isSelected());
                }
                draftFilter.clear();
            }
        });

        buttonContainer.setMaxWidth(Region.USE_PREF_SIZE);
        BorderPane.setAlignment(buttonContainer, Pos.BOTTOM_RIGHT);
    }

    private final ObjectProperty<Predicate<Session>> searchPredicateProperty = new SimpleObjectProperty<>(DEFAULT_PREDICATE);
    public final ObjectProperty<Predicate<Session>> searchPredicateProperty() {
        return searchPredicateProperty;
    }

    private final BooleanProperty filterApplied = new SimpleBooleanProperty();
    public final BooleanProperty filterAppliedProperty() {
        return filterApplied;
    }

    private final BooleanProperty showing = new SimpleBooleanProperty();
    public final BooleanProperty showingProperty() {
        return showing;
    }

    @FXML
    private void addToFilter(ActionEvent actionEvent) {
        draftFilter.add((CheckBox) actionEvent.getSource());
    }

    @FXML
    private void apply(ActionEvent actionEvent) {
        apply();
    }

    private void apply() {
        // update predicate with new search rules
        updateSearchPredicate();
        updateFilterApplied();
        draftFilter.clear();
        MobileApplication.getInstance().hideLayer(POPUP_FILTER_SESSIONS_MENU);
    }

    @FXML
    private void reset(ActionEvent actionEvent) {
        day1.setSelected(false);
        day2.setSelected(false);
        day3.setSelected(false);
        day4.setSelected(false);
        day5.setSelected(false);

        track1.setSelected(false);
        track2.setSelected(false);
        track3.setSelected(false);
        track4.setSelected(false);
        track5.setSelected(false);
        track6.setSelected(false);
        track7.setSelected(false);

        type1.setSelected(false);
        type2.setSelected(false);
        type3.setSelected(false);
        type4.setSelected(false);
        type5.setSelected(false);
        type6.setSelected(false);
        type7.setSelected(false);

        apply();
    }

    private boolean checkDay(Session session) {
        return (day1.isSelected() && session.getConferenceDayIndex() == 1) ||
               (day2.isSelected() && session.getConferenceDayIndex() == 2) ||
               (day3.isSelected() && session.getConferenceDayIndex() == 3) ||
               (day4.isSelected() && session.getConferenceDayIndex() == 4) ||
               (day5.isSelected() && session.getConferenceDayIndex() == 5);
    }

    private boolean considerDayFilter() {
        return day1.isSelected() || day2.isSelected() || day3.isSelected() || day4.isSelected() || day5.isSelected();
    }

    private boolean checkTrack(Session session) {
        if (session.getTrack()== null) {
            return false;
        }

        return (track1.isSelected() && session.getTrack().getTitle().equals(track1.getText())) ||
               (track2.isSelected() && session.getTrack().getTitle().equals(track2.getText())) ||
               (track3.isSelected() && session.getTrack().getTitle().equals(track3.getText())) ||
               (track4.isSelected() && session.getTrack().getTitle().equals(track4.getText())) ||
               (track5.isSelected() && session.getTrack().getTitle().equals(track5.getText())) ||
               (track6.isSelected() && session.getTrack().getTitle().equals(track6.getText())) ||
               (track7.isSelected() && session.getTrack().getTitle().equals(track7.getText()));
    }

    private boolean considerTrackFilter() {
        return track1.isSelected() || track2.isSelected() || track3.isSelected() || track4.isSelected() ||
               track5.isSelected() || track6.isSelected() || track7.isSelected();
    }

    private boolean checkType(Session session) {
        if (session.getType()== null || session.getType().isEmpty()) {
            return false;
        }

        return (type1.isSelected() && session.getType().equals(type1.getText())) ||
               (type2.isSelected() && session.getType().equals(type2.getText())) ||
               (type3.isSelected() && session.getType().equals(type3.getText())) ||
               (type4.isSelected() && session.getType().equals(type4.getText())) ||
               (type5.isSelected() && session.getType().equals(type5.getText())) ||
               (type6.isSelected() && session.getType().equals(type6.getText())) ||
               (type7.isSelected() && session.getType().equals(type7.getText()));
    }

    private boolean considerTypeFilter() {
        return type1.isSelected() || type2.isSelected() || type3.isSelected() || type4.isSelected() ||
               type5.isSelected() || type6.isSelected() || type7.isSelected();
    }

    private void updateSearchPredicate() {
        searchPredicateProperty.set(session ->
                session != null &&
                (!considerDayFilter() || checkDay(session)) &&
                (!considerTrackFilter() || checkTrack(session)) &&
                (!considerTypeFilter() || checkType(session)));
    }

    private void updateFilterApplied() {
        filterApplied.set(considerDayFilter() || considerTrackFilter() || considerTypeFilter());
    }


}
