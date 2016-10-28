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
package com.gluonhq.otn.views.layer;

import com.gluonhq.charm.glisten.application.GlassPane;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.layout.Layer;
import javafx.animation.ScaleTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class ImageViewLayer extends Layer {
    private final Image image;
    private final ImageView imageView;

    private final GlassPane glassPane;

    public ImageViewLayer(Image image) {
        this.glassPane = MobileApplication.getInstance().getGlassPane();
        this.glassPane.getLayers().add(this);

        this.image = image;
        this.imageView = new ImageView(image);
        this.imageView.setScaleX(0);
        this.imageView.setScaleY(0);
        this.imageView.setVisible(false);
        this.imageView.setPreserveRatio(true);
        this.imageView.setOnMouseClicked(e -> {
            hide();
            dispose();
        });

        // if the image is wider than it is tall, then rotate 90 degrees so it displays better on
        // mobile phones
        configureImageView();

        this.getChildren().add(imageView);

        setBackgroundFade(Layer.DEFAULT_BACKGROUND_FADE_LEVEL);

        setAutoHide(true);
    }

    private void configureImageView() {
        final double glassPaneWidth = snapSize(glassPane.getWidth());
        final double glassPaneHeight = snapSize(glassPane.getHeight());

        final double imageWidth = snapSize(image.getWidth());
        final double imageHeight = snapSize(image.getHeight());

        // determine aspect ratios
        final double glassPaneAspectRatio = glassPaneWidth / glassPaneHeight;
        final double imageAspectRatio = imageWidth / imageHeight;

        // rotate the image in two situations
        boolean rotateImage = (glassPaneAspectRatio > 1 && imageAspectRatio < 1) || (glassPaneAspectRatio < 1 && imageAspectRatio > 1);
        imageView.setRotate(rotateImage ? 90 : 0);
        imageView.setFitWidth((rotateImage ? glassPaneHeight : glassPaneWidth) * 0.9);
        imageView.setFitHeight((rotateImage ? glassPaneWidth : glassPaneHeight) * 0.9);
    }

    /** {@inheritDoc} */
    @Override public void layoutChildren() {
        final boolean wasShowing = imageView.isVisible();
        final boolean isShowing = isShowing();

        imageView.setVisible(isShowing);
        if (!isShowing) {
            return;
        }

        final double glassPaneWidth = snapSize(glassPane.getWidth());
        final double glassPaneHeight = snapSize(glassPane.getHeight());

        final double imageWidth = imageView.getLayoutBounds().getWidth();
        final double imageHeight = imageView.getLayoutBounds().getHeight();

        imageView.relocate(glassPaneWidth / 2.0 - imageWidth / 2.0, glassPaneHeight / 2.0 - imageHeight / 2.0);

        if (!wasShowing && isShowing) {
            ScaleTransition st = new ScaleTransition(Duration.millis(250), imageView);
            st.setByX(1.0f);
            st.setByY(1.0f);
            st.setCycleCount(1);
            st.setAutoReverse(true);
            st.play();
        }
    }
}