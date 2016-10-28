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
package com.gluonhq.otn.views.cell;

import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.ListTile;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.Exhibitor;
import com.gluonhq.otn.util.ImageCache;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.views.ExhibitorPresenter;
import javafx.event.EventHandler;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class ExhibitorCell extends CharmListCell<Exhibitor> {

    private final ListTile tile;
    private final ImageView imageView;

    public ExhibitorCell() {
        tile = new ListTile();
        tile.setSecondaryGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());

        imageView = new ImageView();
        imageView.setFitWidth(97);
        imageView.setPreserveRatio(true);
        tile.setLeft(imageView);

        setText(null);
        getStyleClass().add("exhibitor-cell");


    }

    @Override public void updateItem(Exhibitor exhibitor, boolean empty) {
        super.updateItem(exhibitor, empty);
        if (exhibitor != null && !empty) {
            imageView.setImage(ImageCache.get(exhibitor.getPicture()));
            tile.textProperty().setAll(exhibitor.getName());
            String booth = exhibitor.getBooth();
            if(booth != null && !booth.isEmpty()) {
                tile.textProperty().add(OTNBundle.getString("OTN.EXHIBITOR.BOOTH_NUMBER", booth));
            }
            setGraphic(tile);

            // FIX for OTN-568
            tile.setOnMouseReleased(event -> {
                OTNView.EXHIBITOR.switchView().ifPresent(presenter ->
                        ((ExhibitorPresenter) presenter).setExhibitor(exhibitor));
            });
        } else {
            setGraphic(null);
        }
    }
}