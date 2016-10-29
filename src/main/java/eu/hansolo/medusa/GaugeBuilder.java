/*
 * Copyright (c) 2015 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.hansolo.medusa;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * Created by hansolo on 13.12.15.
 */
public class GaugeBuilder<B extends GaugeBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected GaugeBuilder() {}


    // ******************** Methods *******************************************
    public static final GaugeBuilder create() {
        return new GaugeBuilder();
    }

    public final B value(final double VALUE) {
        properties.put("value", new SimpleDoubleProperty(VALUE));
        return (B) this;
    }

    public final B minValue(final double VALUE) {
        properties.put("minValue", new SimpleDoubleProperty(VALUE));
        return (B) this;
    }

    public final B maxValue(final double VALUE) {
        properties.put("maxValue", new SimpleDoubleProperty(VALUE));
        return (B) this;
    }

    public final B title(final String TITLE) {
        properties.put("title", new SimpleStringProperty(TITLE));
        return (B)this;
    }

    public final B startAngle(final double ANGLE) {
        properties.put("startAngle", new SimpleDoubleProperty(ANGLE));
        return (B)this;
    }

    public final B angleRange(final double RANGE) {
        properties.put("angleRange", new SimpleDoubleProperty(RANGE));
        return (B)this;
    }

    public final B locale(final Locale LOCALE) {
        properties.put("locale", new SimpleObjectProperty<>(LOCALE));
        return (B)this;
    }

    public final B style(final String STYLE) {
        properties.put("style", new SimpleStringProperty(STYLE));
        return (B)this;
    }

    public final B styleClass(final String... STYLES) {
        properties.put("styleClass", new SimpleObjectProperty<>(STYLES));
        return (B)this;
    }

    public final B sections(final Section... SECTIONS) {
        properties.put("sectionsArray", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B sections(final List<Section> SECTIONS) {
        properties.put("sectionsList", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B sectionsVisible(final boolean VISIBLE) {
        properties.put("sectionsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final Gauge build() {
        final Gauge CONTROL = new Gauge();

        // Make sure that sections, areas and markers will be added first
        if (properties.keySet().contains("sectionsArray")) {
            CONTROL.setSections(((ObjectProperty<Section[]>) properties.get("sectionsArray")).get());
        }
        if(properties.keySet().contains("sectionsList")) {
            CONTROL.setSections(((ObjectProperty<List<Section>>) properties.get("sectionsList")).get());
        }

        if (properties.keySet().contains("minValue")) {
            CONTROL.setMinValue(((DoubleProperty) properties.get("minValue")).get());
        }
        if (properties.keySet().contains("maxValue")) {
            CONTROL.setMaxValue(((DoubleProperty) properties.get("maxValue")).get());
        }

        for (String key : properties.keySet()) {
            if("styleClass".equals(key)) {
                CONTROL.getStyleClass().setAll("gauge");
                CONTROL.getStyleClass().addAll(((ObjectProperty<String[]>) properties.get(key)).get());
            } else if("value".equals(key)) {
                CONTROL.setValue(((DoubleProperty) properties.get(key)).get());
            } else if("title".equals(key)) {
                CONTROL.setTitle(((StringProperty) properties.get(key)).get());
            } else if("startAngle".equals(key)) {
                CONTROL.setStartAngle(((DoubleProperty) properties.get(key)).get());
            } else if("angleRange".equals(key)) {
                CONTROL.setAngleRange(((DoubleProperty) properties.get(key)).get());
            } else if ("style".equals(key)) {
                CONTROL.setStyle(((StringProperty) properties.get(key)).get());
            }
        }
        return CONTROL;
    }
}
