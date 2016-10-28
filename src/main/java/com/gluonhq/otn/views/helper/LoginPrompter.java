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

import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.util.OTNBundle;
import javafx.scene.control.Button;

public class LoginPrompter extends Placeholder {
    private Button login;

    public LoginPrompter(Service service, String message, MaterialDesignIcon image, Runnable onAuthenticated) {
        this(service, OTNBundle.getString("OTN.LOGIN.TITLE"), message, image, onAuthenticated);
    }

    public LoginPrompter(Service service, String titleText, String messageText, MaterialDesignIcon image, Runnable onAuthenticated) {
        super(titleText, messageText, image);

        getStyleClass().add("login-prompter");

        this.login = new Button(OTNBundle.getString("OTN.BUTTON.LOGIN"));
        this.login.getStyleClass().add("login");
        this.login.setOnAction(e -> {
            if (service.authenticate()) {
                onAuthenticated.run();
            }
        });
        getChildren().add(login);
    }
}
