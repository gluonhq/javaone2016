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
import com.gluonhq.otn.model.Sponsor;
import com.gluonhq.otn.util.ImageCache;
import javafx.scene.image.ImageView;

public class SponsorCell extends CharmListCell<Sponsor> {

    private final ListTile tile;
    private final ImageView imageView;

    public SponsorCell() {
        tile = new ListTile();
        tile.setSecondaryGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());

        setText(null);

        imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        tile.setLeft(imageView);

        getStyleClass().add("sponsor-cell");
    }

    @Override
    public void updateItem(Sponsor sponsor, boolean empty) {
        super.updateItem(sponsor, empty);
        if (sponsor != null && !empty) {
            imageView.setImage(ImageCache.get(sponsor.getPicture()));
            tile.textProperty().setAll(sponsor.getName());
            setGraphic(tile);
        } else {
            setGraphic(null);
        }
    }
}
