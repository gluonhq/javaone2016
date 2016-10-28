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

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.DisplayService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.EnabledOTNExperiences;
import com.gluonhq.otn.model.Experience;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.views.helper.Placeholder;
import com.gluonhq.otn.views.helper.PlaceholderBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import javax.inject.Inject;

public class ExperiencesPresenter extends GluonPresenter<OTNApplication> {
    private static final String PLACEHOLDER_MESSAGE = OTNBundle.getString("OTN.EXPERIENCES.PLACEHOLDER_MESSAGE");

    @FXML
    private View experiences;
    
    private GridPane experiencesGridPane;

    @Inject
    private Service service;
    
    private ReadOnlyObjectProperty<EnabledOTNExperiences> enabledOTNExperiences;
    private EnabledOTNExperiences otnExperiences;
    private int maxCol;

    public void initialize() {
        maxCol = Services.get(DisplayService.class)
                    .map(d -> d.isTablet() ? 3 : 2)
                    .orElse(2);

        experiences.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavMenuButton());
            appBar.setTitleText(OTNView.EXPERIENCES.getTitle());
            appBar.getActionItems().add(getApp().getSearchButton());
        });
        experiences.getStyleClass().add("experiences-view");
        
        experiencesGridPane = new GridPane();
        experiencesGridPane.getStyleClass().add("experiences-grid-pane");
        experiencesGridPane.setAlignment(Pos.TOP_CENTER);
        enabledOTNExperiences = service.retrieveEnabledOTNExperiences();
        enabledOTNExperiences.addListener(o -> setExperiences());

        experiences.setCenter(new Placeholder(PLACEHOLDER_MESSAGE, MaterialDesignIcon.ANNOUNCEMENT));
    }
    
    private void setExperiences() {
        otnExperiences = enabledOTNExperiences.get();
        if (otnExperiences != null) {
            otnExperiences.oneActiveProperty().addListener((obs, ov, nv) -> {
                if (nv) {
                    ScrollPane sp = new ScrollPane(experiencesGridPane);
                    sp.setFitToWidth(true);
                    experiences.setCenter(sp);
                } else {
                    experiences.setCenter(new Placeholder(PLACEHOLDER_MESSAGE, MaterialDesignIcon.ANNOUNCEMENT));
                }
            });

            otnExperiences.badgeEnabledProperty().addListener(o -> layout());
            otnExperiences.coffeeEnabledProperty().addListener(o -> layout());
            otnExperiences.embroiderEnabledProperty().addListener(o -> layout());
            otnExperiences.gameEnabledProperty().addListener(o -> layout());
            otnExperiences.vote3dEnabledProperty().addListener(o -> layout());
            otnExperiences.iotworkshopEnabledProperty().addListener(o -> layout());

            layout();
        }

        if (experiencesGridPane.getChildren().isEmpty()) {
            experiences.setCenter(new Placeholder(PLACEHOLDER_MESSAGE, MaterialDesignIcon.ANNOUNCEMENT));
        } else {
            ScrollPane sp = new ScrollPane(experiencesGridPane);
            sp.setFitToWidth(true);
            experiences.setCenter(sp);
        }
    }

    private void layout() {
        int col = 0;
        int row = 0;
        experiencesGridPane.getChildren().clear();
        for (Experience experience : Experience.values()) {
            ExperienceHolder experienceHolder = new ExperienceHolder(experience);
            switch (experience) {
                case BADGE:
                    if (otnExperiences.badgeEnabledProperty().get()) {
                        experiencesGridPane.add(experienceHolder, col++, row);
                    }
                    break;
                case COFFEE:
                    if (otnExperiences.coffeeEnabledProperty().get()) {
                        experiencesGridPane.add(experienceHolder, col++, row);
                    }
                    break;
                case EMBROIDER:
                    if (otnExperiences.embroiderEnabledProperty().get()) {
                        experiencesGridPane.add(experienceHolder, col++, row);
                    }
                    break;
                case GAME:
                    if (otnExperiences.gameEnabledProperty().get()) {
                        experiencesGridPane.add(experienceHolder, col++, row);
                    }
                    break;
                case VOTE3D:
                    if (otnExperiences.vote3dEnabledProperty().get()) {
                        experiencesGridPane.add(experienceHolder, col++, row);
                    }
                    break;
                case IOT_WORKSHOP:
                    if (otnExperiences.iotworkshopEnabledProperty().get()) {
                        experiencesGridPane.add(experienceHolder, col++, row);
                    }
                    break;
            }
            if (col >= maxCol) {
                row +=1;
                col = 0;
            }
        }

        experiencesGridPane.getColumnConstraints().clear();
        for (int i = 0; i < maxCol; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100d / (double) maxCol);
            experiencesGridPane.getColumnConstraints().add(column);
        }
    }
    
    private class ExperienceHolder extends PlaceholderBase {

        public ExperienceHolder(Experience experience) {
            getStyleClass().add("experience-holder");

            OTNView.registry.getView(experience.name())
                    .ifPresent(v -> getChildren().add(getNodeFromIcon(v.getMenuIcon())));
            this.message.setText(experience.toString());
            getChildren().add(message);
            setOnMouseClicked(e -> experience.switchView());
        }

    }
}
