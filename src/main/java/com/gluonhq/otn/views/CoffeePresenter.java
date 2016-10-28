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

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.ProgressBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.otn.OTNApplication;
import com.gluonhq.otn.OTNView;
import com.gluonhq.otn.model.OTNCoffee;
import com.gluonhq.otn.model.Service;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.views.helper.OrderHandler;
import eu.hansolo.fx.qualitygauge.QualityGauge;
import java.util.Iterator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public class CoffeePresenter extends GluonPresenter<OTNApplication> {
    private static final String ANONYMOUS_MESSAGE = OTNBundle.getString("OTN.COFFEE.ANONYMOUS_MESSAGE");

    @FXML
    private View coffeeView;

    @FXML
    private VBox instructionsContainer;

    @FXML
    private HBox submitContainer;

    @FXML
    private VBox coffeeBox;

    @FXML
    private Label instructions;

    @FXML
    private Label typeLabel;

    @FXML
    private Label strengthLabel;

    @FXML
    private VBox coffeeTypes;

    @FXML
    private Button placeOrder;

    @FXML
    private QualityGauge strengthGauge;

    @Inject
    private Service service;
    
    @FXML
    private ProgressBar indicator;

    private static final String QR_MESSAGE = OTNBundle.getString("OTN.COFFEE.QR_MESSAGE");
    private final BooleanProperty processing = new SimpleBooleanProperty();
    private OrderHandler ordering;
    
    public void initialize() {
        coffeeView.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavBackButton());
            appBar.setTitleText(OTNView.COFFEE.getTitle());

            loadView();

        });
        
        coffeeView.setOnHiding(event -> {
            if (ordering != null) {
                ordering.cancel();
            }
        });

        instructions.setText(OTNBundle.getString("OTN.COFFEE.INSTRUCTION"));
        typeLabel.setText(OTNBundle.getString("OTN.COFFEE.CHOOSE_A_COFFEE_TYPE"));
        strengthLabel.setText(OTNBundle.getString("OTN.COFFEE.STRENGTH_OF_COFFEE"));
        
        strengthGauge.disableProperty().bind(Bindings.isEmpty(coffeeTypes.getChildren()));

        ToggleGroup coffeeType = new ToggleGroup();
        createCoffeeTypes(coffeeType);
        configureOrderPlacement(coffeeType);
        
        indicator.visibleProperty().bind(processing);
        indicator.progressProperty().bind(Bindings.when(processing).then(-1).otherwise(0));
    }

    private void configureOrderPlacement(ToggleGroup group) {
        placeOrder.setText(OTNBundle.getString("OTN.BUTTON.PLACE_ORDER"));
        ordering = new OrderHandler(() -> service.orderOTNCoffee((OTNCoffee) group.getSelectedToggle().getUserData(), strengthGauge.getValue()),
                                            QR_MESSAGE);
        processing.bind(ordering.processingProperty());
        
        placeOrder.setOnAction(ordering);
        placeOrder.disableProperty().bind(group.selectedToggleProperty().isNull().or(processing));
        coffeeTypes.disableProperty().bind(processing);
        strengthGauge.disableProperty().bind(processing);
    }

    private void createCoffeeTypes(ToggleGroup toggleGroup) {
        service.retrieveOTNCoffees().addListener((ListChangeListener.Change<? extends OTNCoffee> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (OTNCoffee coffee : c.getAddedSubList()) {
                        RadioButton button = new RadioButton(coffee.getName());
                        button.setUserData(coffee);
                        button.setToggleGroup(toggleGroup);
                        coffeeTypes.getChildren().add(button);
                    }
                } 
                if (c.wasRemoved()) {
                    for (OTNCoffee coffee : c.getRemoved()) {
                        Iterator i = coffeeTypes.getChildren().iterator();
                        while (i.hasNext()) {
                            RadioButton button = (RadioButton) i.next();
                            if (button.getUserData().equals(coffee)) {
                                toggleGroup.getToggles().remove(button);
                                coffeeTypes.getChildren().remove(button);
                            }
                        }
                    }
                }
            }
            if (!toggleGroup.getToggles().isEmpty()) {
                toggleGroup.getToggles().get(0).setSelected(true);
            }
        });
    }

    private void loadView() {
        coffeeView.setTop(instructionsContainer);
        coffeeView.setCenter(coffeeBox);
        coffeeView.setBottom(submitContainer);
    }

}
