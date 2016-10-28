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

import java.util.Locale;

public class Speaker implements Searchable, Mergeable<Speaker> {
    private String uuid;
    private String fullName;
    private String firstName;
    private String lastName;
    private String summary;
    private String picture;
    private String company;
    private String jobTitle;

    public Speaker() {}

    public Speaker(String uuid, String fullName, String firstName, String lastName, String summary, String picture,
                   String company, String jobTitle, String thumbnail) {
        this.uuid = uuid;
        this.fullName = fullName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.summary = summary;
        this.picture = picture;
        this.company = company;
        this.jobTitle = jobTitle;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSummary() {
        return summary;
    }

    public String getPicture() {
        return picture;
    }

    public String getCompany() {
        return company;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    @Override
    public boolean contains(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return false;
        } 
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return ((getFullName() != null && getFullName().toLowerCase(Locale.ROOT).contains(lowerKeyword)) || 
                (getJobTitle() != null && getJobTitle().toLowerCase(Locale.ROOT).contains(lowerKeyword)) || 
                (getCompany()!= null && getCompany().toLowerCase(Locale.ROOT).contains(lowerKeyword)) ||
                (getSummary() != null && getSummary().toLowerCase(Locale.ROOT).contains(lowerKeyword)));
    }

    @Override
    public boolean merge(Speaker other) {
        boolean changed = false;
        if ((other.fullName == null && this.fullName != null) ||
                (other.fullName != null && this.fullName == null) ||
                (other.fullName != null && !other.fullName.equals(this.fullName))) {
            changed = true;
            this.fullName = other.fullName;
        }
        if ((other.firstName == null && this.firstName != null) ||
                (other.firstName != null && this.firstName == null) ||
                (other.firstName != null && !other.firstName.equals(this.firstName))) {
            changed = true;
            this.firstName = other.firstName;
        }
        if ((other.lastName == null && this.lastName != null) ||
                (other.lastName != null && this.lastName == null) ||
                (other.lastName != null && !other.lastName.equals(this.lastName))) {
            changed = true;
            this.lastName = other.lastName;
        }
        if ((other.summary == null && this.summary != null) ||
                (other.summary != null && this.summary == null) ||
                (other.summary != null && !other.summary.equals(this.summary))) {
            changed = true;
            this.summary = other.summary;
        }
        if ((other.picture == null && this.picture != null) ||
                (other.picture != null && this.picture == null) ||
                (other.picture != null && !other.picture.equals(this.picture))) {
            changed = true;
            this.picture = other.picture;
        }
        if ((other.company == null && this.company != null) ||
                (other.company != null && this.company == null) ||
                (other.company != null && !other.company.equals(this.company))) {
            changed = true;
            this.company = other.company;
        }
        if ((other.jobTitle == null && this.jobTitle != null) ||
                (other.jobTitle != null && this.jobTitle == null) ||
                (other.jobTitle != null && !other.jobTitle.equals(this.jobTitle))) {
            changed = true;
            this.jobTitle = other.jobTitle;
        }
        return changed;
    }
}
