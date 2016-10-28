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
package com.gluonhq.otn.model;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;

import javax.inject.Singleton;
import java.util.Optional;

/**
 * A service class that provides the data from a back end system. Some methods require an authenticated
 * user to be available.
 */
@Singleton
public interface Service {

    /**
     * Return the list of news items on the conference.
     *
     * @return
     */
    ReadOnlyListProperty<News> retrieveNews();

    /**
     * Returns an instance of PushNotification of which the fields will be updated by
     * the OTN back end. You can listen for changes on the id property to see if a new
     * notification is being pushed.
     *
     * @return
     */
    PushNotification retrievePushNotification();

    /**
     * Returns a list of sessions at the conference.
     *
     * @return
     */
    ReadOnlyListProperty<Session> retrieveSessions();

    /**
     * Returns a list of speakers at the conference.
     *
     * @return
     */
    ReadOnlyListProperty<Speaker> retrieveSpeakers();

    /**
     * Returns a list of exhibitors at the conference.
     *
     * @return
     */
    ReadOnlyListProperty<Exhibitor> retrieveExhibitors();

    /**
     * Returns a list of sponsors of the conference.
     *
     * @return
     */
    ReadOnlyListProperty<Sponsor> retrieveSponsors();

    /**
     * Returns a list of available venues at the conference.
     *
     * @return
     */
    ReadOnlyListProperty<Venue> retrieveVenues();

    /**
     * Starts the authentication process. This will show a Dialog in which the user can authenticate
     * himself. When the authentication process completed successfully, <code>true</code> will be
     * returned.
     *
     * @return <code>true</code> if the user is authenticated at the end of this method
     */
    boolean authenticate();

    /**
     * Returns a boolean indicating whether there is an authenticated user or not.
     *
     * @return true if there is an authenticated user, false otherwise.
     */
    boolean isAuthenticated();

    /**
     * Starts the log out process. This will show a Dialog in which the user can confirm
     * he wants to log out. When the log out process completed successfully, 
     * <code>true</code> will be returned.
     *
     * @return <code>true</code> if the user is logged out at the end of this method
     */
    boolean logOut();

    /**
     * Returns a list of sessions that the authenticated user marked as favorite.
     *
     * @return
     * @throws IllegalStateException when no user is currently authenticated
     */
    ObservableList<Session> retrieveFavoriteSessions();

    /**
     * Returns a list of sessions that the authenticated user has scheduled.
     *
     * @return
     * @throws IllegalStateException when no user is currently authenticated
     */
    ObservableList<Session> retrieveScheduledSessions();

    /**
     * Returns a list of notes that the authenticated user has written. Each note belongs to a specific session.
     *
     * @return
     * @throws IllegalStateException when no user is currently authenticated
     */
    ObservableList<Note> retrieveNotes();

    /**
     * Returns a list of votes that the authenticated user has cast. Each vote belongs to a specific session.
     *
     * @return
     * @throws IllegalStateException when no user is currently authenticated
     */
    ObservableList<Vote> retrieveVotes();

    /**
     * Store the answers to the survey as a JSON object encoded as a string. The answers will be mapped to the
     * currently authenticated user. If no user is currently authenticated, this method will do nothing.
     *
     * @param answers the string representing a JSON object that contains all the answers for the survey.
     */
    void storeSurveyAnswers(String answers);

    /**
     * Returns <code>true</code> if the authenticated user completed the survey, <code>false</code> otherwise.
     *
     * @return
     */
    boolean isSurveyCompleted();

    /**
     * Get a list of Strings that represent the OTN experiences that should currently
     * be enabled in the UI.
     *
     * @return the list of enabled OTN experiences.
     */
    ReadOnlyObjectProperty<EnabledOTNExperiences> retrieveEnabledOTNExperiences();

    /**
     * Retrieves a list of available OTN coffees for the OTN Get-a-coffee experience.
     * @return list of OTNCoffees
     */
    ReadOnlyListProperty<OTNCoffee> retrieveOTNCoffees();

    /**
     * Orders the coffee and returns the ID of the order.
     *
     * @param coffee the type of coffee ordered
     * @param strength int from 0 to 10
     * @return the ID of the order
     */
    OTNCoffeeOrder orderOTNCoffee(OTNCoffee coffee, int strength);

    /**
     * Orders the shape and returns the ID of the order.
     *
     * @param shape TODO: the format of the shape must still be defined.
     * @return the ID of the order
     */
    OTNCarvedBadgeOrder orderOTNCarveABadge(String shape);

    /**
     * Retrieves a list of available OTN Games for the OTN Play-a-game experience.
     * @return list of OTNGames
     */
    ReadOnlyListProperty<OTNGame> retrieveOTNGames();
    
    /**
     * Retrieves a list of available OTN embroderies for the OTN embroider-a-duke experience.
     * @return list of OTNGames
     */
    ReadOnlyListProperty<OTNEmbroidery> retrieveOTNEmbroideries();
    
    /**
     * Retrieves a list of OTN 3D Models, including the current model and the current votes for the other models.
     * @return list of OTN3DModels
     */
    ReadOnlyListProperty<OTN3DModel> retrieveOTN3DModels();

    /**
     * Checks if the user can cast a vote for the next OTN 3D Model.
     *
     * @return true if voting is possible, false otherwise
     */
    boolean canVoteForOTN3DModel();

    ReadOnlyObjectProperty<LatestClearThreeDModelVotes> retrieveLatestClearVotes();

    /**
     * Cast a vote for the next OTN 3D Model by id
     * @param id ID of the OTN3DModel
     */
    void voteForOTN3DModel(String id);

    /**
     * Retrieves the Session for a specific session uuid.
     * @param uuid The uuid assigned to the session
     * @return Session with the specific uuid
     */
    Optional<Session> findSession(String uuid);
    
}
