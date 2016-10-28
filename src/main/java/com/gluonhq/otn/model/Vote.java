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

import java.util.UUID;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Vote implements Identifiable {

    private String uuid;
    private String sessionUuid;

    @SuppressWarnings("unused")
    public Vote() {
    }

    public Vote(String sessionUuid) {
        this(sessionUuid, -1, null);
    }

    public Vote(String sessionUuid, int value, String comment) {
        this.uuid = UUID.randomUUID().toString();
        this.sessionUuid = sessionUuid;
        this.value.set(value);
        this.comment.set(comment);
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public String getSessionUuid() {
        return sessionUuid;
    }

    private final IntegerProperty value = new SimpleIntegerProperty(0);
    public void setValue(int value) { this.value.set(value); }
    public int getValue() {
        return value.get();
    }
    public IntegerProperty valueProperty() { return value; }

    private final StringProperty comment = new SimpleStringProperty();
    public String getComment() {
        return comment.get();
    }
    public void setComment(String comment) { this.comment.set(comment); }
    public StringProperty commentProperty() { return comment; }

    @Override
    public String toString() {
        return "Vote{" +
                "sessionUuid='" + sessionUuid + '\'' +
                ", value=" + value +
                ", comment='" + comment + '\'' +
                '}';
    }
}
