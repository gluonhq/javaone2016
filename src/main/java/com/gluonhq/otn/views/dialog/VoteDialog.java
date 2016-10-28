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
import com.gluonhq.charm.glisten.control.Rating;
import com.gluonhq.otn.model.Vote;
import com.gluonhq.otn.util.OTNBundle;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class VoteDialog extends Dialog<Vote> {

    private Vote vote;
    private Rating rating;
    private TextArea feedbackInput;

    public VoteDialog(String title) {
        this.rootNode.getStyleClass().add("vote-dialog");

        setTitleText(OTNBundle.getString("OTN.VOTEDIALOG.VOTE"));
        VBox content = new VBox();
        Label contentTitle = new Label(OTNBundle.getString("OTN.VOTEDIALOG.RATE_THE_FOLLOWING_SESSION"));
        Label sessionTitleLabel = new Label(title);
        StackPane stackPane = new StackPane();
        rating = new Rating();
        stackPane.getChildren().add(rating);

        Label shareLabel = new Label(OTNBundle.getString("OTN.VOTEDIALOG.SHARE_YOUR_THOUGHTS"));
        feedbackInput = new TextArea();
        VBox shareContainer = new VBox();
        shareContainer.getChildren().addAll(shareLabel, feedbackInput);

        content.getChildren().addAll(contentTitle, sessionTitleLabel, stackPane, shareContainer);

        setContent(content);

        Button cancel = new Button(OTNBundle.getString("OTN.BUTTON.CANCEL"));
        Button submit = new Button(OTNBundle.getString("OTN.BUTTON.SUBMIT_CAPS"));
        getButtons().addAll(cancel, submit);

        cancel.setOnAction(event -> {
            hide();
        });
        submit.setOnAction(event -> {
            vote.setComment(feedbackInput.getText());
            vote.setValue((int) rating.getRating());
            setResult(vote);
            hide();
        });

        content.getStyleClass().add("content");
        contentTitle.getStyleClass().add("title");
        sessionTitleLabel.getStyleClass().add("session-title");
        shareLabel.getStyleClass().add("share-text");
        feedbackInput.getStyleClass().add("feedback-input");
        shareContainer.getStyleClass().add("share-container");

    }

    public void setVote(Vote vote) {
        this.vote = vote;
        rating.setRating(vote.getValue());
        feedbackInput.setText(vote.getComment());
    }


}
