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
package com.gluonhq.otn.util;

import com.gluonhq.otn.model.Exhibitor;
import com.gluonhq.otn.model.FeedType;
import com.gluonhq.otn.model.News;
import com.gluonhq.otn.model.Session;
import com.gluonhq.otn.model.Speaker;
import com.gluonhq.otn.model.Sponsor;
import com.gluonhq.otn.model.Track;
import com.gluonhq.otn.model.Venue;
import com.gluonhq.otn.views.helper.SponsorCategory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.ArrayList;
import java.util.List;

public class ServiceUtils {

    public static Exhibitor mapJsonToExhibitor(JsonObject jsonObject) {
        String uuid = jsonObject.containsKey("uuid") ? jsonObject.getString("uuid") : "";
        String name = jsonObject.containsKey("name") ? jsonObject.getString("name") : "";
        String location = jsonObject.containsKey("location") ? jsonObject.getString("location") : "";
        String summary = jsonObject.containsKey("summary") ? jsonObject.getString("summary") : "";
        String description = jsonObject.containsKey("description") ? jsonObject.getString("description") : "";
        String picture = jsonObject.containsKey("picture") ? jsonObject.getString("picture") : "";
        String url = jsonObject.containsKey("url") ? jsonObject.getString("url") : "";
        String booth = jsonObject.containsKey("booth") ? jsonObject.getString("booth") : "";
        return new Exhibitor(uuid, name, location, summary, description, picture, url, booth);
    }

    public static Session mapJsonToSession(JsonObject jsonObject) {
        int capacity = jsonObject.containsKey("capacity") ? jsonObject.getInt("capacity") : 0;
        long endTime = jsonObject.containsKey("endTime") ? jsonObject.getJsonNumber("endTime").longValue() : 0L;
        String experienceLevel = jsonObject.containsKey("experienceLevel") ? jsonObject.getString("experienceLevel") : "";
        String location = jsonObject.containsKey("location") ? jsonObject.getString("location") : "";
        int registered = jsonObject.containsKey("registered") ? jsonObject.getInt("registered") : 0;
        List<String> speakersUuid = new ArrayList<>();
        if (jsonObject.containsKey("speakersUuid")) {
            JsonObject speakersObject = jsonObject.getJsonObject("speakersUuid");
            for (JsonString jsonString : speakersObject.getJsonArray("items").getValuesAs(JsonString.class)) {
                speakersUuid.add(jsonString.getString());
            }
        }
        long startTime = jsonObject.containsKey("startTime") ? jsonObject.getJsonNumber("startTime").longValue() : 0L;
        String summary = jsonObject.containsKey("summary") ? jsonObject.getString("summary") : "";
        String title = jsonObject.containsKey("title") ? jsonObject.getString("title") : "";
        Track track = jsonObject.containsKey("track") ? Track.valueOf(jsonObject.getString("track")) : null;
        String type = jsonObject.containsKey("type") ? jsonObject.getString("type") : "";
        String uuid = jsonObject.containsKey("uuid") ? jsonObject.getString("uuid") : "";
        return new Session(uuid, title, summary, location, capacity, registered, startTime, endTime,
                track, type, experienceLevel, speakersUuid);
    }

    public static Speaker mapJsonToSpeaker(JsonObject jsonObject) {
        String company = jsonObject.containsKey("company") ? jsonObject.getString("company") : "";
        String firstName = jsonObject.containsKey("firstName") ? jsonObject.getString("firstName") : "";
        String fullName = jsonObject.containsKey("fullName") ? jsonObject.getString("fullName") : "";
        String jobTitle = jsonObject.containsKey("jobTitle") ? jsonObject.getString("jobTitle") : "";
        String lastName = jsonObject.containsKey("lastName") ? jsonObject.getString("lastName") : "";
        String picture = jsonObject.containsKey("picture") ? jsonObject.getString("picture") : "";
        String summary = jsonObject.containsKey("summary") ? jsonObject.getString("summary") : "";
        String thumbnail = jsonObject.containsKey("thumbnail") ? jsonObject.getString("thumbnail") : "";
        String uuid = jsonObject.containsKey("uuid") ? jsonObject.getString("uuid") : "";
        return new Speaker(uuid, fullName, firstName, lastName, summary, picture, company, jobTitle, thumbnail);
    }

    public static Sponsor mapJsonToSponsor(JsonObject jsonObject) {
        boolean banner = jsonObject.containsKey("banner") && jsonObject.getBoolean("banner");
        String description = jsonObject.containsKey("description") ? jsonObject.getString("description") : "";
        String name = jsonObject.containsKey("name") ? jsonObject.getString("name") : "";
        String picture = jsonObject.containsKey("picture") ? jsonObject.getString("picture") : "";
        SponsorCategory section = jsonObject.containsKey("section") ? SponsorCategory.valueOf(jsonObject.getString("section")) : null;
        boolean splash = jsonObject.containsKey("splash") && jsonObject.getBoolean("splash");
        String summary = jsonObject.containsKey("summary") ? jsonObject.getString("summary") : "";
        String uuid = jsonObject.containsKey("uuid") ? jsonObject.getString("uuid") : "";
        return new Sponsor(uuid, section, name, summary, description, picture, splash, banner);
    }

    public static Venue mapJsonToVenue(JsonObject jsonObject) {
        String location = jsonObject.containsKey("location") ? jsonObject.getString("location") : "";
        String name = jsonObject.containsKey("name") ? jsonObject.getString("name") : "";
        String uuid = jsonObject.containsKey("uuid") ? jsonObject.getString("uuid") : "";
        double latitude = jsonObject.containsKey("latitude") ? jsonObject.getJsonNumber("latitude").doubleValue() : 0.0;
        double longitude = jsonObject.containsKey("longitude") ? jsonObject.getJsonNumber("longitude").doubleValue() : 0.0;
        String phoneNumber = jsonObject.containsKey("phoneNumber") ? jsonObject.getString("phoneNumber") : "";
        String url = jsonObject.containsKey("url") ? jsonObject.getString("url") : "";
        return new Venue(uuid, name, location, latitude, longitude, null, null, null, phoneNumber, url);
    }

    public static News mapJsonToNews(JsonObject jsonObject) {
        String content = jsonObject.containsKey("content") ? jsonObject.getString("content") : "";
        long creationDate = jsonObject.containsKey("creationDate") ? jsonObject.getJsonNumber("creationDate").longValue() : 0L;
        List<String> thumbnails = new ArrayList<>();
        if (jsonObject.containsKey("thumbnails")) {
            JsonArray thumbnailsArrray = jsonObject.getJsonArray("thumbnails");
            for (JsonString jsonString : thumbnailsArrray.getValuesAs(JsonString.class)) {
                thumbnails.add(jsonString.getString());
            }
        }
        String title = jsonObject.containsKey("title")? jsonObject.getString("title"): "";
        FeedType type = jsonObject.containsKey("type")? FeedType.valueOf(jsonObject.getString("type")) : null;
        String uuid = jsonObject.containsKey("uuid")? jsonObject.getString("uuid") : "";
        return new News(uuid, creationDate, type, title, content, thumbnails);
    }
}
