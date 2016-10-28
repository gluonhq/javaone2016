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
package com.gluonhq.otn.model.dummy;

import com.gluonhq.charm.glisten.afterburner.GluonView;
import com.gluonhq.otn.model.BaseService;
import com.gluonhq.otn.model.EnabledOTNExperiences;
import com.gluonhq.otn.model.Exhibitor;
import com.gluonhq.otn.model.LatestClearThreeDModelVotes;
import com.gluonhq.otn.model.News;
import com.gluonhq.otn.model.Note;
import com.gluonhq.otn.model.OTN3DModel;
import com.gluonhq.otn.model.OTNCarvedBadgeOrder;
import com.gluonhq.otn.model.OTNCoffee;
import com.gluonhq.otn.model.OTNCoffeeOrder;
import com.gluonhq.otn.model.OTNEmbroidery;
import com.gluonhq.otn.model.OTNGame;
import com.gluonhq.otn.model.PushNotification;
import com.gluonhq.otn.model.Session;
import com.gluonhq.otn.model.Speaker;
import com.gluonhq.otn.model.Sponsor;
import com.gluonhq.otn.model.Venue;
import com.gluonhq.otn.model.Vote;
import com.gluonhq.otn.util.ServiceUtils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;

public class DummyService extends BaseService {

    private static final Logger LOG = Logger.getLogger(DummyService.class.getName());

    private static final JsonReaderFactory readerFactory = Json.createReaderFactory(null);

    private GluonView authenticationView;

    // system wide data
    private ReadOnlyListWrapper<Exhibitor> exhibitors;
    private ReadOnlyListWrapper<Session> sessions;
    private ReadOnlyListWrapper<Speaker> speakers;
    private ReadOnlyListWrapper<Sponsor> sponsors;
    private ReadOnlyListWrapper<Venue> venues;
    private ReadOnlyListWrapper<News> news;
    private ReadOnlyObjectWrapper<EnabledOTNExperiences> enabledOTNExperiences;
    private ReadOnlyObjectWrapper<LatestClearThreeDModelVotes> latestClearVotes = new ReadOnlyObjectWrapper<>(new LatestClearThreeDModelVotes());
    private ReadOnlyListWrapper<OTNCoffee> otnCoffees;
    private ReadOnlyListWrapper<OTNGame> otnGames;
    private ReadOnlyListWrapper<OTNEmbroidery> otnEmbroideries;
    private ReadOnlyListWrapper<OTN3DModel> otn3DModels;

    private boolean voted;

    // user specific data
    private final ObservableList<Session> favoriteSessions = FXCollections.observableArrayList();
    private final ObservableList<Session> scheduledSessions = FXCollections.observableArrayList();
    private final ObservableList<Note> notes = FXCollections.observableArrayList();
    private final ObservableList<Vote> votes = FXCollections.observableArrayList();
    private String surveyAnswers;

    public DummyService() {
    }
    
    @PostConstruct
    public void postConstruct() {
        retrieveAuthenticatedUser();
    }

    @Override
    public GluonView getAuthenticationView() {
        if (authenticationView == null) {
            authenticationView = new GluonView(DummyAuthenticationPresenter.class);
        }
        return authenticationView;
    }

    @Override
    public ReadOnlyListProperty<News> retrieveNews() {
        if (news == null) {
            news = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(readNews()));
        }
        return news.getReadOnlyProperty();
    }

    @Override
    public PushNotification retrievePushNotification() {
        return new PushNotification();
    }

    @Override
    public ReadOnlyListProperty<Session> retrieveSessions() {
        if (sessions == null) {
            sessions = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(readSessions()));
        }
        return sessions.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyListProperty<Speaker> retrieveSpeakers() {
        if (speakers == null) {
            speakers = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(readSpeakers()));
        }
        return speakers.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyListProperty<Exhibitor> retrieveExhibitors() {
        if (exhibitors == null) {
            exhibitors = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(readExhibitors()));
        }
        return exhibitors.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyListProperty<Sponsor> retrieveSponsors() {
        if (sponsors == null) {
            sponsors = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(readSponsors()));
        }
        return sponsors.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyListProperty<Venue> retrieveVenues() {
        if (venues == null) {
            venues = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(readVenues()));
        }
        return venues.getReadOnlyProperty();
    }

    @Override
    public ObservableList<Session> internalRetrieveFavoriteSessions() {
        return favoriteSessions;
    }

    @Override
    public ObservableList<Session> internalRetrieveScheduledSessions(Runnable onStateSucceeded) {
        return scheduledSessions;
    }

    @Override
    public ObservableList<Note> internalRetrieveNotes() {
        return notes;
    }

    @Override
    public ObservableList<Vote> internalRetrieveVotes() {
        return votes;
    }

    @Override
    public void storeSurveyAnswers(String answers) {
        if (isAuthenticated() && rootDir != null) {
            try {
                File surveyAnswersFile = new File(rootDir, "authenticationUser.surveyAnswers.json");
                try (FileWriter fileWriter = new FileWriter(surveyAnswersFile)) {
                    fileWriter.write(answers);
                }

                this.surveyAnswers = answers;
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Could not store authenticated user to private storage.", ex);
            }
        }
    }

    @Override
    public boolean isSurveyCompleted() {
        return surveyAnswers != null;
    }

    @Override
    public void retrieveSurveyAnswers() {
        if (rootDir != null) {
            try {
                File surveyAnswersFile = new File(rootDir, "authenticationUser.surveyAnswers.json");
                if (surveyAnswersFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(surveyAnswersFile))) {
                        surveyAnswers = reader.readLine();
                    }
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Could not retrieve survey answers from private storage.", ex);
            }
        }
    }

    @Override
    public ReadOnlyObjectProperty<EnabledOTNExperiences> retrieveEnabledOTNExperiences() {
        if (enabledOTNExperiences == null) {
            EnabledOTNExperiences otnExperiences = new EnabledOTNExperiences();
            otnExperiences.badgeEnabledProperty().set(true);
            otnExperiences.coffeeEnabledProperty().set(true);
            otnExperiences.embroiderEnabledProperty().set(true);
            otnExperiences.gameEnabledProperty().set(true);
            otnExperiences.vote3dEnabledProperty().set(true);
            otnExperiences.iotworkshopEnabledProperty().set(true);
            enabledOTNExperiences = new ReadOnlyObjectWrapper<>(otnExperiences);
        }
        return enabledOTNExperiences.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyListProperty<OTNCoffee> retrieveOTNCoffees() {
        if (otnCoffees == null) {
            List<OTNCoffee> coffees = new LinkedList<>();
            coffees.add(new OTNCoffee("1", "Brazilian Coffee"));
            coffees.add(new OTNCoffee("2", "Ethiopian Coffee"));
            coffees.add(new OTNCoffee("3", "Taiwan Tea"));
            otnCoffees = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(coffees));
        }
        return otnCoffees;
    }

    @Override
    public OTNCoffeeOrder orderOTNCoffee(OTNCoffee coffee, int strength) {
        OTNCoffeeOrder order = new OTNCoffeeOrder(coffee.getType(), strength);
        Platform.runLater(() -> order.responseProperty().set("OK: " + UUID.randomUUID().toString()));
        return order;
    }

    @Override
    public OTNCarvedBadgeOrder orderOTNCarveABadge(String shape) {
        OTNCarvedBadgeOrder order = new OTNCarvedBadgeOrder(shape);
        Platform.runLater(() -> order.responseProperty().set("OK: " + UUID.randomUUID().toString()));
        return order;
    }

    @Override
    public ReadOnlyListProperty<OTNGame> retrieveOTNGames() {
        if (otnGames == null) {
            List<OTNGame> games = new LinkedList<>();
            games.add(new OTNGame("Tetris", "http://javahub.com/game/id1", "http://lorempixel.com/400/200"));
            games.add(new OTNGame("PacMan", "http://javahub.com/game/id2", "http://lorempixel.com/400/200"));
            games.add(new OTNGame("Brick Breaker", "http://javahub.com/game/id3", "http://lorempixel.com/400/200"));
            otnGames = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(games));
        }
        return otnGames;
    }

    @Override
    public ReadOnlyListProperty<OTNEmbroidery> retrieveOTNEmbroideries() {
        if (otnEmbroideries == null) {
            List<OTNEmbroidery> embroideries = new LinkedList<>();
            embroideries.add(new OTNEmbroidery("OTNEmbroidery 1", "http://javahub.com/embroidery/id1", "http://lorempixel.com/400/200"));
            embroideries.add(new OTNEmbroidery("OTNEmbroidery 2", "http://javahub.com/embroidery/id2", "http://lorempixel.com/400/200"));
            embroideries.add(new OTNEmbroidery("OTNEmbroidery 3", "http://javahub.com/embroidery/id3", "http://lorempixel.com/400/200"));
            otnEmbroideries = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(embroideries));
        }
        return otnEmbroideries;
    }

    @Override
    public ReadOnlyListProperty<OTN3DModel> retrieveOTN3DModels() {
        if (otn3DModels == null) {
            List<OTN3DModel> models = new LinkedList<>();
            models.add(new OTN3DModel("1", "Model 1", "http://lorempixel.com/400/200", true, 0));
            models.add(new OTN3DModel("2", "Model 2", "http://lorempixel.com/400/200", false, 4));
            models.add(new OTN3DModel("3", "Model 3", "http://lorempixel.com/400/200", false, 7));
            models.add(new OTN3DModel("4", "Model 4", "http://lorempixel.com/400/200", false, 19));
            otn3DModels = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(models));
        }
        return otn3DModels;
    }

    @Override
    public boolean canVoteForOTN3DModel() {
        return voted;
    }

    @Override
    public ReadOnlyObjectProperty<LatestClearThreeDModelVotes> retrieveLatestClearVotes() {
        return latestClearVotes.getReadOnlyProperty();
    }

    @Override
    public void voteForOTN3DModel(String id) {
        if (otn3DModels != null) {
            for (OTN3DModel model : otn3DModels) {
                if (model.getUuid().equals(id)) {
                    voted = true;

                    model.vote();
                    break;
                }
            }
        }
    }

    private List<Exhibitor> readExhibitors() {
        List<Exhibitor> exhibitors = new ArrayList<>();
        try (JsonReader reader = readerFactory.createReader(new InputStreamReader(DummyService.class.getResourceAsStream("/data/exhibitors.json")))) {
            JsonArray array = reader.readArray();
            for (JsonObject object : array.getValuesAs(JsonObject.class)) {
                exhibitors.add(ServiceUtils.mapJsonToExhibitor(object));
            }
        }
        return exhibitors;
    }

    private List<Session> readSessions() {
        List<Session> sessions = new ArrayList<>();
        try (JsonReader reader = readerFactory.createReader(new InputStreamReader(DummyService.class.getResourceAsStream("/data/sessions.json")))) {
            JsonArray array = reader.readArray();
            for (JsonObject object : array.getValuesAs(JsonObject.class)) {
                sessions.add(ServiceUtils.mapJsonToSession(object));
            }
        }
        return sessions;
    }

    private List<Speaker> readSpeakers() {
        List<Speaker> speakers = new ArrayList<>();
        try (JsonReader reader = readerFactory.createReader(new InputStreamReader(DummyService.class.getResourceAsStream("/data/speakers.json")))) {
            JsonArray array = reader.readArray();
            for (JsonObject object : array.getValuesAs(JsonObject.class)) {
                speakers.add(ServiceUtils.mapJsonToSpeaker(object));
            }
        }
        return speakers;
    }

    private List<Sponsor> readSponsors() {
        List<Sponsor> sponsors = new ArrayList<>();
        try (JsonReader reader = readerFactory.createReader(new InputStreamReader(DummyService.class.getResourceAsStream("/data/sponsors.json")))) {
            JsonArray array = reader.readArray();
            for (JsonObject object : array.getValuesAs(JsonObject.class)) {
                sponsors.add(ServiceUtils.mapJsonToSponsor(object));
            }
        }
        return sponsors;
    }

    private List<Venue> readVenues() {
        List<Venue> venues = new ArrayList<>();
        try (JsonReader reader = readerFactory.createReader(new InputStreamReader(DummyService.class.getResourceAsStream("/data/venues.json")))) {
            JsonArray array = reader.readArray();
            for (JsonObject object : array.getValuesAs(JsonObject.class)) {
                venues.add(ServiceUtils.mapJsonToVenue(object));
            }
        }
        return venues;
    }

    private List<News> readNews() {
        List<News> newsContainer = new ArrayList<>();
        try (JsonReader reader = readerFactory.createReader(new InputStreamReader(DummyService.class.getResourceAsStream("/data/activityFeed.json")))) {
            JsonArray array = reader.readArray();
            for (JsonObject object : array.getValuesAs(JsonObject.class)) {
                newsContainer.add(ServiceUtils.mapJsonToNews(object));
            }
        }
        return newsContainer;
    }

}
