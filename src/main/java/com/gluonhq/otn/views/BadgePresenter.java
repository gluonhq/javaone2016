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
package com.gluonhq.otn.views;

import com.gluonhq.charm.down.Platform;
import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.SettingsService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.control.ProgressBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.views.helper.OrderHandler;
import com.gluonhq.otn.views.helper.badge.BadgeOutline;
import com.gluonhq.otn.views.helper.badge.InterpolateBezier;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class BadgePresenter extends GluonPresenter<OTNApplication> {
    private static final String ANONYMOUS_MESSAGE = OTNBundle.getString("OTN.BADGE.ANONYMOUS_MESSAGE");
    private static final String DONT_SHOW_AGAIN_KEY = "BADGE.DIALOG.DONT_SHOW_AGAIN";

    private final static double MINIMUM_AREA = 3000;
    private final static int MINIMUM_SIZE = 5;
    private final static double MARGIN = 30;
    
    // Minimum distance from previous point to create a line. Lower values require more memory and 
    // verify algorithm is slower.
    // After path is created it will be replaced with splines that will generate a smoother figure
    private final static double MIN_DISTANCE = 30;
    
    @FXML
    private View badge;

    @FXML
    private HBox submitContainer;

    private StackPane pane;
    private Pane drawPane;
    
    @FXML
    private Button reset;
    @FXML
    private Button placeOrder;

    private Label errorLabel;
    
    private GridPane gridPane;
    
    @FXML
    private ProgressBar indicator;

    @Inject
    private Service service;
    
    private Path drawPath;
    private boolean lock = false;
    private BooleanProperty validated;
    private DoubleProperty area;
    private ObservableList<Point2D> list;
    private Affine affine;
    private BadgeOutline badgeOutline;
    private SVGPath svg;
    
    private static final String QR_MESSAGE = OTNBundle.getString("OTN.BADGE.QR_MESSAGE");
    private final PseudoClass validatedPane = PseudoClass.getPseudoClass("error");
    private ObjectProperty<Bounds> bounds;
    
    private OrderHandler ordering;
    private final BooleanProperty processing = new SimpleBooleanProperty();
    
    public void initialize() {
        createAuthenticatedView();
        
        badgeOutline = new BadgeOutline();

        drawPath = new Path();
        drawPath.getStyleClass().add("path");
        drawPath.setStrokeWidth(10);
        svg = new SVGPath();
        svg.getStyleClass().add("path");
        
        drawPane.getChildren().add(drawPath);
        pane.getChildren().add(drawPane);
        
        bounds = new SimpleObjectProperty<>(drawPane.getLayoutBounds());
        bounds.bind(drawPane.layoutBoundsProperty());
        
        area = new SimpleDoubleProperty();
        list = FXCollections.observableArrayList();
        validated = new SimpleBooleanProperty() {
            @Override
            protected void invalidated() {
                drawPane.pseudoClassStateChanged(validatedPane, !get());
            }
        };
        
        if (Platform.isDesktop()) {
            applyMouseInput();
        } else {
            applyTouchInput();
        }
        
        badge.setOnShowing(e -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavBackButton());
            appBar.setTitleText(OTNView.BADGE.getTitle());
            badge.prefWidthProperty().bind(badge.getScene().widthProperty());
            badge.prefHeightProperty().bind(badge.getScene().heightProperty().subtract(appBar.getHeight()));


            loadView();
            pane.heightProperty().addListener(o -> {
                if (pane.getHeight() > 0) {
                    double size = Math.min(pane.getWidth(), pane.getHeight());
                    drawPane.setMinSize(size, size);
                    drawPane.setPrefSize(size, size);
                    drawPane.setMaxSize(size, size);
                }
            });
                showInstructionsDialog();
        });
        
        badge.setOnHiding(e -> {
            badge.prefWidthProperty().unbind();
            badge.prefHeightProperty().unbind();
            if (ordering != null) {
                ordering.cancel();
            }
        });
        
        reset.setOnAction(e -> {
            list.clear();
            drawPath.getElements().clear();
            drawPath.getTransforms().clear();
            drawPane.getChildren().setAll(drawPath);
            drawPath.setStrokeWidth(10);
            lock = false;
            validated.set(true);
        });

        reset.disableProperty().bind(Bindings.size(list).isEqualTo(0).or(processing));
        
        placeOrder.disableProperty().bind(
                validated.not()
                .or(Bindings.size(list).lessThan(MINIMUM_SIZE)
                .or(area.lessThan(MINIMUM_AREA)))
                .or(processing));
        
        indicator.visibleProperty().bind(processing);
        indicator.progressProperty().bind(Bindings.when(processing).then(-1).otherwise(0));
        
        ordering = new OrderHandler(() -> service.orderOTNCarveABadge(svg.getContent()), QR_MESSAGE);
        processing.bind(ordering.processingProperty());
        placeOrder.setOnAction(ordering);
        
        errorLabel.visibleProperty().bind(validated.not());
        //errorLabel.managedProperty().bind(errorLabel.visibleProperty());
        validated.set(true);
        lock = false;
    }

    private void showInstructionsDialog() {
        boolean show = Services.get(SettingsService.class)
                .map(s -> !"checked".equals(s.retrieve(DONT_SHOW_AGAIN_KEY)))
                .orElse(true);

        if (show) {
            Label title = new Label(OTNBundle.getString("OTN.BADGE.DIALOG.TITLE"));
            Label titleDescription = new Label(OTNBundle.getString("OTN.BADGE.DIALOG.TITLE_DESCRIPTION"));
            Label listenTitle = new Label(OTNBundle.getString("OTN.BADGE.DIALOG.LISTEN_TITLE"));
            Label listenDescription = new Label(OTNBundle.getString("OTN.BADGE.DIALOG.LISTEN_DESCRIPTION"));
            Label drawTitle = new Label(OTNBundle.getString("OTN.BADGE.DIALOG.DRAW_TITLE"));
            Label drawDescription = new Label(OTNBundle.getString("OTN.BADGE.DIALOG.DRAW_DESCRIPTION"));
            Label cutTitle = new Label(OTNBundle.getString("OTN.BADGE.DIALOG.CUT_TITLE"));
            Label cutDescription = new Label(OTNBundle.getString("OTN.BADGE.DIALOG.CUT_DESCRIPTION"));

            ImageView listenImageView = new ImageView(BadgePresenter.class.getResource("EAR_Listen.png").toExternalForm());
            ImageView drawImageView = new ImageView(BadgePresenter.class.getResource("FINGER_Draw.png").toExternalForm());
            ImageView cutImageView = new ImageView(BadgePresenter.class.getResource("QRCODE_Cut.png").toExternalForm());

            // FIXME: Get rid of VBox's and use only GridPane
            VBox mainContainer = new VBox();
            GridPane contentContainer = new GridPane();
            VBox titleContainer = new VBox();
            VBox listenContainer = new VBox();
            VBox drawContainer = new VBox();
            VBox cutContainer = new VBox();

            CheckBox checkBox = new CheckBox(OTNBundle.getString("OTN.DIALOG.DONT_SHOW_AGAIN"));

            titleContainer.getChildren().addAll(title, titleDescription);
            listenContainer.getChildren().addAll(listenTitle, listenDescription);
            drawContainer.getChildren().addAll(drawTitle, drawDescription);
            cutContainer.getChildren().addAll(cutTitle, cutDescription);

            contentContainer.add(listenImageView, 0, 0);
            contentContainer.add(listenContainer, 1, 0);
            contentContainer.add(drawImageView, 0, 1);
            contentContainer.add(drawContainer, 1, 1);
            contentContainer.add(cutImageView, 0, 2);
            contentContainer.add(cutContainer, 1, 2);

            mainContainer.getChildren().addAll(contentContainer, checkBox);

            title.getStyleClass().add("dialog-badge-title");
            titleDescription.getStyleClass().add("dialog-badge-description");
            listenTitle.getStyleClass().add("dialog-badge-title");
            listenDescription.getStyleClass().add("dialog-badge-description");
            drawTitle.getStyleClass().add("dialog-badge-title");
            drawDescription.getStyleClass().add("dialog-badge-description");
            cutTitle.getStyleClass().add("dialog-badge-title");
            cutDescription.getStyleClass().add("dialog-badge-description");
            titleContainer.getStyleClass().add("dialog-badge-container");
            listenContainer.getStyleClass().add("dialog-badge-container");
            drawContainer.getStyleClass().add("dialog-badge-container");
            cutContainer.getStyleClass().add("dialog-badge-container");
            contentContainer.getStyleClass().add("dialog-badge-grid");
            checkBox.getStyleClass().add("dialog-badge-checkbox");
            mainContainer.getStyleClass().add("dialog-badge-main-container");

            Dialog instructionsDialog = new Dialog();
            instructionsDialog.setTitle(titleContainer);
            instructionsDialog.setContent(mainContainer);

            Button okButton = new Button(OTNBundle.getString("OTN.BUTTON.OK"));
            okButton.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    Services.get(SettingsService.class).ifPresent(s -> s.store(DONT_SHOW_AGAIN_KEY, "checked"));
                }
                instructionsDialog.hide();
            });
            instructionsDialog.getButtons().add(okButton);

            instructionsDialog.showAndWait();
        }
    }

    private void createAuthenticatedView() {
        Label instructions = new Label(OTNBundle.getString("OTN.BADGE.DRAW_A_SHAPE"));
        instructions.getStyleClass().add("instructions");
        GridPane.setHalignment(instructions, HPos.CENTER);

        pane = new StackPane();
        pane.getStyleClass().add("badge-pane");
        GridPane.setVgrow(pane, Priority.ALWAYS);
        
        drawPane = new Pane();
        drawPane.getStyleClass().add("draw-pane");
        
        errorLabel = new Label();
        errorLabel.getStyleClass().add("errorLabel");
        errorLabel.setWrapText(true);

        // Keep the error label directly under the drawing pane
        errorLabel.translateXProperty().bind(drawPane.layoutXProperty().subtract(badge.layoutXProperty()));
        errorLabel.translateYProperty().bind(pane.heightProperty().subtract(drawPane.heightProperty()).divide(2).negate().add(10));
        errorLabel.prefWidthProperty().bind(drawPane.prefWidthProperty());

        reset.setText(OTNBundle.getString("OTN.BUTTON.RESET"));
//        GridPane.setHalignment(reset, HPos.RIGHT);

        placeOrder .setText(OTNBundle.getString("OTN.BUTTON.SUBMIT"));
//        GridPane.setHalignment(placeOrder, HPos.RIGHT);

        Region spacer = new Region();
        GridPane.setHgrow(spacer, Priority.ALWAYS);

        gridPane = new GridPane();
        gridPane.getStyleClass().add("container");
        gridPane.add(instructions, 0, 0, 3, 1);
        gridPane.add(pane, 0, 1, 3, 1);
        gridPane.add(errorLabel, 0, 2, 3, 1);
        gridPane.add(spacer, 0, 3);
//        gridPane.add(reset, 1, 3);
//        gridPane.add(placeOrder, 2, 3);
    }

    private void loadView() {
        badge.setCenter(gridPane);
        badge.setBottom(submitContainer);
    }

    interface PathAction {
        void path(double x, double y);
    }
    
    private Point2D anchorPt = Point2D.ZERO;
    private Point2D oldLinePt = Point2D.ZERO;
    
    private final PathAction start = (x, y) -> {
        if (!lock) {
            anchorPt = new Point2D(x, y);
            drawPath.getElements().add(new MoveTo(anchorPt.getX(), anchorPt.getY()));
            list.add(anchorPt);
            oldLinePt = anchorPt;
        }
    };
    private final PathAction draw = (x, y) -> {
        if (!lock && validated.get()) { 
            Point2D linePt = new Point2D(clamp(bounds.get().getMinX() + MARGIN, x, bounds.get().getMaxX() - MARGIN), 
                    clamp(bounds.get().getMinY() + MARGIN, y, bounds.get().getMaxY() - MARGIN));
            if (linePt.distance(oldLinePt) > MIN_DISTANCE) {
                drawPath.getElements().add(new LineTo(linePt.getX(), linePt.getY()));
                list.add(linePt);
                oldLinePt = linePt;

                /**
                 * Warning: highly resource-intensive, maybe we need to skip it:
                 */
                validated.set(verify(true));
            }
        } else if (!validated.get()) {
            lock = true;
        }
    };
    private final PathAction end = (x, y) -> {
        if (!lock && validated.get()) { 
            validated.set(verify(false));
            if (validated.get()) {
                list.add(new Point2D(x, y));
                final List<Point2D> scaledPoints = maxScale(list, 2);
                
                drawPath.getElements().setAll(new MoveTo(scaledPoints.get(0).getX(), scaledPoints.get(0).getY()));
                
                InterpolateBezier interpolate = new InterpolateBezier(scaledPoints);
                for (InterpolateBezier.Bezier s : interpolate.getSplines()) {
                    final List<Point2D> points = s.getPoints();
                    drawPath.getElements().add(new CubicCurveTo(
                            points.get(1).getX(), points.get(1).getY(), 
                            points.get(2).getX(), points.get(2).getY(), 
                            points.get(3).getX(), points.get(3).getY()));
                }
                boolean validSVG = badgeOutline.generateOutline(drawPath, svg);
                drawPane.getChildren().setAll(svg);
                if (!validSVG) {
                    errorLabel.setText(OTNBundle.getString("OTN.BADGE.ERROR_LINES_TOO_CLOSE"));
                    validated.set(false);
                }
            }
            lock = true;
        }
    };
    
    private static double clamp(double min, double value, double max) {
        if (value <= min) return min;
        if (value >= max) return max;
        return value;
    }
    
    private void applyMouseInput() {
        drawPane.setOnMousePressed(mouseEvent ->
            start.path(mouseEvent.getX(), mouseEvent.getY()));

        drawPane.setOnMouseDragged(mouseEvent ->
            draw.path(mouseEvent.getX(), mouseEvent.getY()));

        drawPane.setOnMouseReleased(mouseEvent ->
            end.path(mouseEvent.getX(), mouseEvent.getY()));
    }
    
    private void applyTouchInput() {
        drawPane.setOnTouchPressed(touchEvent ->
            start.path(touchEvent.getTouchPoint().getX(), touchEvent.getTouchPoint().getY()));
        drawPane.setOnTouchMoved(touchEvent ->
            draw.path(touchEvent.getTouchPoint().getX(), touchEvent.getTouchPoint().getY()));
        drawPane.setOnTouchReleased( touchEvent ->
            end.path(touchEvent.getTouchPoint().getX(), touchEvent.getTouchPoint().getY()));
    }
    
    private List<Point2D> maxScale(List<Point2D> points, double factor) {
        List<Point2D> scaledList = new ArrayList<>();
        if (!drawPath.getElements().isEmpty()) {
            // scale and center
            Bounds b0 = drawPath.getBoundsInParent();
            if (b0.getWidth() > 0 && b0.getHeight() > 0) {
                final double w = drawPane.getWidth() - 2 * factor * MARGIN; 
                final double h = drawPane.getHeight() - 2 * factor * MARGIN;
                final double scale = Math.min(w / b0.getWidth(), h / b0.getHeight());
                affine = new Affine(new Scale(scale, scale, factor * MARGIN, factor * MARGIN));
                affine.append(new Translate(factor * MARGIN - b0.getMinX() + (w - scale * b0.getWidth()) / 2d / scale, 
                                  factor * MARGIN - b0.getMinY() + (h - scale * b0.getHeight()) / 2d / scale));

                for (Point2D p : points) {
                    scaledList.add(affine.transform(p));
                }
            }
        }
        return scaledList;
    }
    
    
    private boolean verify(boolean isOpen) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        
        if (isOpen) {
            // check only latest segment added
            int i = list.size() - 2;
            for (int j = 0; j < list.size() - 3; j++) {
                if (checkCollision(i, j)) {
                    showError(i, j);
                    errorLabel.setText(OTNBundle.getString("OTN.BADGE.ERROR_LINES_CANNOT_OVERLAP") + " " + OTNBundle.getString("OTN.BADGE.ERROR_TRY_AGAIN"));
                    return false;
                }
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                for (int j = i + 2; j < i + list.size() - 1; j++) {
                    if (checkCollision(i, j)) {
                        showError(i, j);
                        errorLabel.setText(OTNBundle.getString("OTN.BADGE.ERROR_LINES_CANNOT_OVERLAP"));
                    return false;
                    }
                }
            }
            area.set(calculateArea());
            if (area.get() < MINIMUM_AREA) {
                errorLabel.setText(OTNBundle.getString("OTN.BADGE.ERROR_SHAPE_IS_TOO_SMALL"));
                return false;
            }
            
            if (list.size() < MINIMUM_SIZE) {
                errorLabel.setText(OTNBundle.getString("OTN.BADGE.ERROR_SHAPE_IS_TOO_SMALL"));
                return false;
            }
        }
        return true;
    }
    
    private void showError(int i, int j) {
        Path collisionPath1 = new Path();
        collisionPath1.getElements().add(new MoveTo(getPoint(i).getX(), getPoint(i).getY()));
        collisionPath1.getElements().add(new LineTo(getPoint(i + 1).getX(), getPoint(i + 1).getY()));
        collisionPath1.setStroke(Color.RED);
        collisionPath1.setStrokeWidth(5);
        drawPane.getChildren().add(collisionPath1);
        Path collisionPath2 = new Path();
        collisionPath2.getElements().add(new MoveTo(getPoint(j).getX(), getPoint(j).getY()));
        collisionPath2.getElements().add(new LineTo(getPoint(j + 1).getX(), getPoint(j + 1).getY()));
        collisionPath2.setStroke(Color.RED);
        collisionPath2.setStrokeWidth(5);
        drawPane.getChildren().add(collisionPath2);
    }
    
    private Point2D getPoint(int i) {
        return list.get(i >= list.size() ? i - list.size() : i);
    }
    
    private boolean checkCollision(int segment1, int segment2) {
        Point2D A = getPoint(segment1);
        Point2D B = getPoint(segment1 + 1);
        Point2D C = getPoint(segment2);
        Point2D D = getPoint(segment2 + 1);
        Point2D CmP = C.subtract(A);
        Point2D r = B.subtract(A);
        Point2D s = D.subtract(C);
        
        double rxs = r.crossProduct(s).getZ();

        if (rxs == 0d) {
            return false; // Lines are parallel.
        }
        double CmPxr = CmP.crossProduct(r).getZ();
        if (CmPxr == 0d) {
            // Lines are collinear, and so intersect if they have any overlap
            return ((C.getX() - A.getX() < 0d) != (C.getX() - B.getX() < 0d))
                    || ((C.getY() - A.getY() < 0d) != (C.getY() - B.getY() < 0d));
        }
        
        double CmPxs = CmP.crossProduct(s).getZ();
			
        double t = CmPxs / rxs;
        double u = CmPxr / rxs;

        return (t >= 0d) && (t <= 1d) && (u >= 0d) && (u <= 1d);
    }
    
    private double calculateArea(){
        double a = 0d;
        for (int i = 0; i < list.size(); i++) {
            a += getPoint(i).crossProduct(getPoint(i + 1)).getZ();
        } 
        return Math.abs(a/2d);
    }
}
