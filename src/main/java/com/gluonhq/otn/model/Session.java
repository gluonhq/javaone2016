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


import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class Session implements Searchable, Mergeable<Session> {
    //TODO Eventually we have to replace it with the data coming from the API
    private static final ZoneId CONFERENCE_ZONE_ID = ZoneId.of("America/Los_Angeles");
    private static final int CONFERENCE_DAYS_NUMBER = 5;
    private static final ZonedDateTime[] CONFERENCE_DATES = new ZonedDateTime[CONFERENCE_DAYS_NUMBER];
    // 2016 September 18th
    public static final ZonedDateTime CONFERENCE_START_DATE = ZonedDateTime.of(2016, 9, 18, 0, 0, 0, 0, CONFERENCE_ZONE_ID);

    private String uuid;
    private String title;
    private String summary;
    private String location;
    private int capacity;
    private long startTime;
    private long endTime;
    private Track track;
    private String type;
    private String experienceLevel;
    private List<String> speakersUuid;

    static {
        for (int i = 0; i < CONFERENCE_DAYS_NUMBER; ++i) {
            CONFERENCE_DATES[i] = CONFERENCE_START_DATE.plusDays(i);
        }
    }

    public Session() {}

    public Session(String uuid, String title, String summary, String location, int capacity, int registered, long startTime,
                   long endTime, Track track, String type, String experienceLevel, List<String> speakersUuid) {
        this.uuid = uuid;
        this.title = title;
        this.summary = summary;
        this.location = location;
        this.capacity = capacity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.track = track;
        this.type = type;
        this.experienceLevel = experienceLevel;
        this.speakersUuid = speakersUuid;
    }

    public static ZoneId getConferenceZoneId() {
        return CONFERENCE_ZONE_ID;
    }

    private static ZonedDateTime dayOnly(ZonedDateTime dateTime) {
        return ZonedDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), 0, 0, 0, 0, CONFERENCE_ZONE_ID);
    }

    public static int getConferenceDayIndex(ZonedDateTime dateTime) {
        return Arrays.binarySearch(CONFERENCE_DATES, dayOnly(dateTime)) + 1;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getLocation() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public Track getTrack() {
        return track;
    }

    public String getType() {
        return type;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public List<String> getSpeakersUuid() {
        return speakersUuid;
    }

    public ZonedDateTime getStartDate() {
        return timeToZonedDateTime(getStartTime());
    }

    public ZonedDateTime getEndDate() {
        return timeToZonedDateTime(getEndTime());
    }

    public int getConferenceDayIndex() {
        return getConferenceDayIndex(getStartDate());
    }

    private static ZonedDateTime timeToZonedDateTime( long time ) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time * 1000), CONFERENCE_ZONE_ID);
    }

    public boolean isOverlappingWith(Session otherSession ) {
        if (otherSession == null || this.equals(otherSession)) return false;

        return dateInRange( otherSession.getEndDate(),   getStartDate(), getEndDate() ) ||
                dateInRange( otherSession.getStartDate(), getStartDate(), getEndDate() );
    }

    private static boolean dateInRange( ZonedDateTime dateTime, ZonedDateTime rangeStart, ZonedDateTime rangeEnd ) {
        return dateTime.compareTo(rangeStart) >= 0 && dateTime.compareTo(rangeEnd) <= 0;
    }

    @Override
    public boolean contains(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return false;
        } 
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return ((getTitle() != null && getTitle().toLowerCase(Locale.ROOT).contains(lowerKeyword)) || 
                (getTrack() != null && getTrack().toString().toLowerCase(Locale.ROOT).contains(lowerKeyword)) || 
                (getLocation() != null && getLocation().toLowerCase(Locale.ROOT).contains(lowerKeyword)) ||
                (getExperienceLevel() != null && getExperienceLevel().toLowerCase(Locale.ROOT).contains(lowerKeyword)) ||
                (getSummary() != null && getSummary().toLowerCase(Locale.ROOT).contains(lowerKeyword)));
    }

    @Override
    public boolean merge(Session other) {
        boolean changed = false;
        return changed;
    }
}
