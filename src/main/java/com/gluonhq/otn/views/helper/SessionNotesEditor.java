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

import com.gluonhq.otn.model.Note;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.util.OTNSettings;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SessionNotesEditor extends VBox {
    private static final int WAIT_TIME = 3000; // In milliseconds

    private final Timeline timer;
    private final TextArea textArea;
    private final String sessionUuid;
    private final Service service;
    private boolean textChanged;
    private Note currentNote;

    public SessionNotesEditor(String sessionUuid, Service service) {
        this.sessionUuid = sessionUuid;
        this.service = service;

        Label title = new Label(OTNBundle.getString("OTN.NOTES.SESSION_NOTES"));
        title.getStyleClass().add("title");
        VBox.setVgrow(title, Priority.NEVER);

        timer = new Timeline();
        timer.setCycleCount(1);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(WAIT_TIME), event -> {
            if (textChanged) {
                saveNotes();
            }
        });
        timer.getKeyFrames().add(keyFrame);

        textArea = new TextArea();
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            textChanged = true;
            timer.playFromStart();
        });

        VBox.setVgrow(textArea, Priority.ALWAYS);

        getStyleClass().add("session-notes-editor");
        getChildren().addAll(title, textArea);

        findAndSetNote();
    }

    private void findAndSetNote() {
        if (service.isAuthenticated() || !OTNSettings.USE_REMOTE_NOTES) {
            ObservableList<Note> notes = service.retrieveNotes();
            for (Note note : notes) {
                if (note.getSessionUuid().equals(sessionUuid)) {
                    currentNote = note;
                    textArea.setText(note.getContent());
                    break;
                }
            }
        }
    }

    public void saveNotes() {
        if (!textChanged) {
            return;
        }

        boolean isNewNote = currentNote == null;
        if (isNewNote) {
            currentNote = new Note(sessionUuid);
        }
        // Update the content irrespective of whether its a new Note
        currentNote.setContent(textArea.getText());

        // If there is no text, remove it from the list
        // If it is not yet added to the list, it will be NO-OP
        if(currentNote.getContent().isEmpty()) {
            service.retrieveNotes().remove(currentNote);
        } else {
            if(isNewNote) {
                ObservableList<Note> notes = service.retrieveNotes();
                notes.add(currentNote);
            }
        }
        textChanged = false;
    }

}
