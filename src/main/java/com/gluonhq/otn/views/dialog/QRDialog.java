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
package com.gluonhq.otn.views.dialog;

import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.util.QRGenerator;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class QRDialog extends Dialog {

    private QRDialog(String id, String message) {
        this.rootNode.getStyleClass().add("qr-dialog");
        setTitleText(OTNBundle.getString("OTN.EXPERIENCE.QR_DIALOG_TITLE"));
        Image image = QRGenerator.createQR(id);
        ImageView imageView = new ImageView(image);
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("message");

        VBox qrContent = new VBox(imageView, messageLabel);
        qrContent.getStyleClass().add("content");
        setContent(qrContent);

        Button okButton = new Button(OTNBundle.getString("OTN.BUTTON.CLOSE"));
        okButton.setOnAction(event -> hide());
        getButtons().add(okButton);
    }

    public static void show(String id, String message) {
        new QRDialog(id, message).showAndWait();
    }
}
