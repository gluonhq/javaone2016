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


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class OTN3DModel {
    private String uuid;
    private String name;
    private String image;
    private boolean currentModel;
    private IntegerProperty currentVotes = new SimpleIntegerProperty(0);

    public OTN3DModel() {
    }

    public OTN3DModel(String uuid, String name, String image, boolean currentModel, int currentVotes) {
        this.uuid = uuid;
        this.name = name;
        this.image = image;
        this.currentModel = currentModel;
        this.currentVotes.set(currentVotes);
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public boolean isCurrentModel() {
        return currentModel;
    }

    public int getCurrentVotes() {
        return currentVotes.get();
    }

    public IntegerProperty currentVotesProperty() {
        return currentVotes;
    }

    public void vote() {
        currentVotes.set(currentVotes.get() + 1);
    }
}
