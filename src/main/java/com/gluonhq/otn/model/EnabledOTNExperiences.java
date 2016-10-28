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

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class EnabledOTNExperiences {

    private BooleanProperty oneActive = new SimpleBooleanProperty(false);
    private BooleanProperty coffeeEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty badgeEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty gameEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty embroiderEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty vote3dEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty iotworkshopEnabled = new SimpleBooleanProperty(false);

    public EnabledOTNExperiences() {
        oneActive.bind(Bindings.createBooleanBinding(() -> coffeeEnabled.get() || badgeEnabled.get() || gameEnabled.get() || embroiderEnabled.get() || vote3dEnabled.get() || iotworkshopEnabled.get(),
                coffeeEnabled, badgeEnabled, gameEnabled, embroiderEnabled, vote3dEnabled, iotworkshopEnabled));
    }

    public BooleanProperty oneActiveProperty() {
        return oneActive;
    }

    public BooleanProperty coffeeEnabledProperty() {
        return coffeeEnabled;
    }

    public BooleanProperty badgeEnabledProperty() {
        return badgeEnabled;
    }

    public BooleanProperty gameEnabledProperty() {
        return gameEnabled;
    }

    public BooleanProperty embroiderEnabledProperty() {
        return embroiderEnabled;
    }

    public BooleanProperty vote3dEnabledProperty() {
        return vote3dEnabled;
    }
    
    public BooleanProperty iotworkshopEnabledProperty() {
        return iotworkshopEnabled;
    }

}
