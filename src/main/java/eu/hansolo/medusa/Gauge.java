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

import eu.hansolo.medusa.events.UpdateEvent;
import eu.hansolo.medusa.events.UpdateEventListener;
import eu.hansolo.medusa.tools.Helper;
import eu.hansolo.medusa.tools.SectionComparator;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.util.Duration;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by hansolo on 11.12.15.
 */
public class Gauge extends Control {


    public enum SkinType {
        GAUGE
    }

    private final ThresholdEvent EXCEEDED_EVENT      = new ThresholdEvent(ThresholdEvent.THRESHOLD_EXCEEDED);
    private final ThresholdEvent UNDERRUN_EVENT      = new ThresholdEvent(ThresholdEvent.THRESHOLD_UNDERRUN);
    private final UpdateEvent    RECALC_EVENT        = new UpdateEvent(Gauge.this, UpdateEvent.EventType.RECALC);
    private final UpdateEvent    REDRAW_EVENT        = new UpdateEvent(Gauge.this, UpdateEvent.EventType.REDRAW);
    private final UpdateEvent    RESIZE_EVENT        = new UpdateEvent(Gauge.this, UpdateEvent.EventType.RESIZE);
    private final UpdateEvent    VISIBILITY_EVENT    = new UpdateEvent(Gauge.this, UpdateEvent.EventType.VISIBILITY);
    private final UpdateEvent    FINISHED_EVENT      = new UpdateEvent(Gauge.this, UpdateEvent.EventType.FINISHED);
    private final UpdateEvent    SECTION_EVENT       = new UpdateEvent(Gauge.this, UpdateEvent.EventType.SECTION);


    // Update events
    private List<UpdateEventListener> listenerList = new CopyOnWriteArrayList<>();

    // Data related
    private DoubleProperty                       value;
    private DoubleProperty                       oldValue;      // last value
    private DoubleProperty                       currentValue;
    private DoubleProperty                       formerValue;   // last current value
    private double                               _minValue;
    private DoubleProperty                       minValue;
    private double                               _maxValue;
    private DoubleProperty                       maxValue;
    private double                               _range;
    private DoubleProperty                       range;
    private double                               _threshold;
    private DoubleProperty                       threshold;
    private String                               _title;
    private StringProperty                       title;
    private ObservableList<Section>              sections;
    private ObservableList<Section>              areas;
    // UI related
    private boolean                              _animated;
    private BooleanProperty                      animated;
    private long                                 animationDuration;
    private double                               _startAngle;
    private DoubleProperty                       startAngle;
    private double                               _angleRange;
    private DoubleProperty                       angleRange;
    private double                               _angleStep;
    private DoubleProperty                       angleStep;
    private Locale                               _locale;
    private ObjectProperty<Locale>               locale;
    private boolean                              _checkThreshold;
    private BooleanProperty                      checkThreshold;

    // others
    private double   originalMinValue;
    private double   originalMaxValue;
    private double   originalThreshold;
    private Timeline timeline;
    private Instant  lastCall;
    private boolean  withinSpeedLimit;


    // ******************** Constructors **************************************
    public Gauge() {
        this(SkinType.GAUGE);
    }
    public Gauge(final SkinType SKIN) {
        getStyleClass().add("gauge");

        init();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        _minValue                           = 0;
        _maxValue                           = 100;
        value                               = new DoublePropertyBase(_minValue) {
            @Override protected void invalidated() {
                final double VALUE = get();
                withinSpeedLimit = !(Instant.now().minusMillis(getAnimationDuration()).isBefore(lastCall));
                lastCall = Instant.now();
                if (isAnimated() && withinSpeedLimit) {
                    long animationDuration = getAnimationDuration();
                    timeline.stop();

                    final KeyValue KEY_VALUE;

                    double ov  = getOldValue();
                    double min = getMinValue();
                    double max = getMaxValue();
                    double cv  = getCurrentValue();
                    double tmpValue;
                    if (Math.abs(VALUE - ov) > getRange() * 0.5) {
                        if (ov < VALUE) {
                            tmpValue = min - max + VALUE;
                        } else {
                            tmpValue = ov + max - ov + min + VALUE - getRange();
                        }
                        KEY_VALUE = new KeyValue(currentValue, tmpValue, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
                    } else {
                        if (cv < min) currentValue.set(max + cv);
                        KEY_VALUE = new KeyValue(currentValue, VALUE, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
                    }

                    final KeyFrame KEY_FRAME = new KeyFrame(Duration.millis(animationDuration), KEY_VALUE);
                    timeline.getKeyFrames().setAll(KEY_FRAME);
                    timeline.play();
                } else {
                    currentValue.set(VALUE);
                    fireUpdateEvent(FINISHED_EVENT);
                }
                oldValue.set(get());
            }
            @Override public Object getBean() { return Gauge.this; }
            @Override public String getName() { return "value"; }
        };
        oldValue                            = new SimpleDoubleProperty(Gauge.this, "oldValue", value.get());
        currentValue                        = new DoublePropertyBase(value.get()) {
            @Override protected void invalidated() {
                final double VALUE = get();
                if (isCheckThreshold()) {
                    double thrshld = getThreshold();
                    if (formerValue.get() < thrshld && VALUE > thrshld) {
                        fireEvent(EXCEEDED_EVENT);
                    } else if (formerValue.get() > thrshld && VALUE < thrshld) {
                        fireEvent(UNDERRUN_EVENT);
                    }
                }
                formerValue.set(VALUE);
            }
            @Override public void set(final double VALUE) { super.set(VALUE); }
            @Override public Object getBean() { return Gauge.this; }
            @Override public String getName() { return "currentValue";}
        };
        formerValue                         = new SimpleDoubleProperty(Gauge.this, "formerValue", value.get());
        _range                              = _maxValue - _minValue;
        _threshold                          = _maxValue;
        _title                              = "";
        sections                            = FXCollections.observableArrayList();
        areas                               = FXCollections.observableArrayList();
        _startAngle                         = 320;
        _angleRange                         = 280;
        _angleStep                          = _angleRange / _range;
        _locale                             = Locale.US;

        originalMinValue                    = -Double.MAX_VALUE;
        originalMaxValue                    = Double.MAX_VALUE;
        originalThreshold                   = Double.MAX_VALUE;
        lastCall                            = Instant.now();
        timeline                            = new Timeline();
        timeline.setOnFinished(e -> {
            if (Double.compare(currentValue.get(), 0d) != 0d) {
                final KeyValue KEY_VALUE2 = new KeyValue(value, 0, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
                final KeyFrame KEY_FRAME2 = new KeyFrame(Duration.millis((long) (0.8 * getAnimationDuration())), KEY_VALUE2);
                timeline.getKeyFrames().setAll(KEY_FRAME2);
                timeline.play();
            }
            fireUpdateEvent(FINISHED_EVENT);
        });
    }

    private void registerListeners() { disabledProperty().addListener(o -> setOpacity(isDisabled() ? 0.4 : 1)); }


    // ******************** Data related methods ******************************

    /**
     * Returns the value of the Gauge. If animated == true this value represents
     * the value at the end of the animation. Where currentValue represents the
     * current value during the animation.
     *
     * @return the value of the gauge
     */
    public double getValue() { return value.get(); }
    /**
     * Sets the value of the Gauge to the given double. If animated == true this
     * value will be the end value after the animation is finished.
     *
     * @param VALUE
     */
    public void setValue(final double VALUE) { value.set(VALUE); }
    public DoubleProperty valueProperty() { return value; }

    /**
     * Returns the current value of the Gauge. If animated == true this value
     * represents the current value during the animation. Otherwise it's returns
     * the same value as the getValue() method.
     *
     * @return the current value of the gauge
     */
    public double getCurrentValue() { return currentValue.get(); }
    public ReadOnlyDoubleProperty currentValueProperty() { return currentValue; }

    /**
     * Returns the last value of the Gauge. This will not be the last value during
     * an animation but the final last value after the animation was finished.
     * If you need to get the last value during an animation you should use
     * formerValue instead.
     *
     * @return the last value of the gauge
     */
    public double getOldValue() { return oldValue.get(); }
    public ReadOnlyDoubleProperty oldValueProperty() { return oldValue; }

    /**
     * Returns the last value of the Gauge. This will be the last value during
     * an animation.
     * If you need to get the last value after the animation is finished or if
     * you don't use animation at all (when using real values) you should use
     * oldValue instead.
     *
     * @return the last value of the gauge during an animation
     */
    public double getFormerValue() { return formerValue.get(); }
    public ReadOnlyDoubleProperty formerValueProperty() { return formerValue; }

    /**
     * Returns the minimum value of the scale. This value represents the lower
     * limit of the visible gauge values.
     *
     * @return the minimum value of the gauge scale
     */
    public double getMinValue() { return null == minValue ? _minValue : minValue.get(); }
    /**
     * Sets the minimum value of the gauge scale to the given value
     *
     * @param VALUE
     */
    public void setMinValue(final double VALUE) {
        if (null == minValue) {
            if (VALUE > getMaxValue()) { setMaxValue(VALUE); }
            _minValue = Helper.clamp(-Double.MAX_VALUE, getMaxValue(), VALUE).doubleValue();
            setRange(getMaxValue() - _minValue);
            if (Double.compare(originalMinValue, -Double.MAX_VALUE) == 0) originalMinValue = _minValue;
            if (Double.compare(originalThreshold, getThreshold()) < 0) { setThreshold(Helper.clamp(_minValue, getMaxValue(), originalThreshold)); }
            fireUpdateEvent(RECALC_EVENT);
            if (!valueProperty().isBound()) Gauge.this.setValue(Helper.clamp(getMinValue(), getMaxValue(), Gauge.this.getValue()));
        } else {
            minValue.set(VALUE);
        }
    }
    public DoubleProperty minValueProperty() {
        if (null == minValue) {
            minValue = new DoublePropertyBase(_minValue) {
                @Override protected void invalidated() {
                    final double VALUE = get();
                    if (VALUE > getMaxValue()) setMaxValue(VALUE);
                    setRange(getMaxValue() - VALUE);
                    if (Double.compare(originalMinValue, -Double.MAX_VALUE) == 0) originalMinValue = VALUE;
                    if (Double.compare(originalThreshold, getThreshold()) < 0) { setThreshold(Helper.clamp(VALUE, getMaxValue(), originalThreshold)); }
                    fireUpdateEvent(RECALC_EVENT);
                    if (!valueProperty().isBound()) Gauge.this.setValue(Helper.clamp(getMinValue(), getMaxValue(), Gauge.this.getValue()));
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "minValue";}
            };
        }
        return minValue;
    }

    /**
     * Returns the maximum value of the scale. This value represents the upper limit
     * of the visible gauge values.
     *
     * @return the maximum value of the gauge scale
     */
    public double getMaxValue() { return null == maxValue ? _maxValue : maxValue.get(); }
    /**
     * Sets the maximum value of the gauge scale to the given value
     *
     * @param VALUE
     */
    public void setMaxValue(final double VALUE) {
        if (null == maxValue) {
            if (VALUE < getMinValue()) { setMinValue(VALUE); }
            _maxValue = Helper.clamp(getMinValue(), Double.MAX_VALUE, VALUE).doubleValue();
            setRange(_maxValue - getMinValue());
            if (Double.compare(originalMaxValue, Double.MAX_VALUE) == 0) originalMaxValue = _maxValue;
            if (Double.compare(originalThreshold, getThreshold()) > 0) { setThreshold(Helper.clamp(getMinValue(), _maxValue, originalThreshold)); }
            fireUpdateEvent(RECALC_EVENT);
            if (!valueProperty().isBound()) Gauge.this.setValue(Helper.clamp(getMinValue(), getMaxValue(), Gauge.this.getValue()));
        } else {
            maxValue.set(VALUE);
        }
    }
    public DoubleProperty maxValueProperty() {
        if (null == maxValue) {
            maxValue = new DoublePropertyBase(_maxValue) {
                @Override protected void invalidated() {
                    final double VALUE = get();
                    if (VALUE < getMinValue()) setMinValue(VALUE);
                    setRange(VALUE - getMinValue());
                    if (Double.compare(originalMaxValue, Double.MAX_VALUE) == 0) originalMaxValue = VALUE;
                    if (Double.compare(originalThreshold, getThreshold()) > 0) { setThreshold(Helper.clamp(getMinValue(), VALUE, originalThreshold)); }
                    fireUpdateEvent(RECALC_EVENT);
                    if (!valueProperty().isBound()) Gauge.this.setValue(Helper.clamp(getMinValue(), getMaxValue(), Gauge.this.getValue()));
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "maxValue"; }
            };
        }
        return maxValue;
    }

    /**
     * Always returns the range of the gauge scale (maxValue - minValue).
     * This value will be automatically calculated each time
     * the min- or maxValue will change.
     *
     * @return the range of the gauge scale
     */
    public double getRange() { return null == range ? _range : range.get(); }
    /**
     * This is a private method that sets the range to the given value
     * which is always (maxValue - minValue).
     *
     * @param RANGE
     */
    private void setRange(final double RANGE) {
        if (null == range) {
            _range = RANGE;
            setAngleStep(getAngleRange() / RANGE);
        } else {
            range.set(RANGE);
        }
    }
    public ReadOnlyDoubleProperty rangeProperty() {
        if (null == range) {
            range = new DoublePropertyBase((getMaxValue() - getMinValue())) {
                @Override protected void invalidated() { setAngleStep(getAngleRange() / get()); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "range"; }
            };
        }
        return range;
    }

    /**
     * Returns the threshold value that can be used to visualize a
     * threshold value on the scale. There are also events that will
     * be fired if the threshold was exceeded or underrun.
     * The value will be clamped to range of the gauge.
     *
     * @return the threshold value of the gauge
     */
    public double getThreshold() { return null == threshold ? _threshold : threshold.get(); }
    /**
     * Sets the threshold of the gauge to the given value. The value
     * will be clamped to the range of the gauge.
     *
     * @param THRESHOLD
     */
    public void setThreshold(final double THRESHOLD) {
        originalThreshold = THRESHOLD;
        if (null == threshold) {
            _threshold = Helper.clamp(getMinValue(), getMaxValue(), THRESHOLD).doubleValue();
            fireUpdateEvent(RESIZE_EVENT);
        } else {
            threshold.set(THRESHOLD);
        }
    }
    public DoubleProperty tresholdProperty() {
        if (null == threshold) {
            threshold = new DoublePropertyBase(_threshold) {
                @Override protected void invalidated() {
                    final double THRESHOLD = get();
                    if (THRESHOLD < getMinValue() || THRESHOLD > getMaxValue()) set(Helper.clamp(getMinValue(), getMaxValue(), THRESHOLD).doubleValue());
                    fireUpdateEvent(RESIZE_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "threshold"; }
            };
        }
        return threshold;
    }

    /**
     * Returns the title of the gauge. This title will usually
     * only be visible if it is not empty.
     *
     * @return the title of the gauge
     */
    public String getTitle() { return null == title ? _title : title.get(); }
    /**
     * Sets the title of the gauge. This title will only be visible
     * if it is not empty.
     *
     * @param TITLE
     */
    public void setTitle(final String TITLE) {
        if (null == title) {
            _title = TITLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            title.set(TITLE);
        }
    }
    public StringProperty titleProperty() {
        if (null == title) {
            title  = new StringPropertyBase(_title) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "title"; }
            };
            _title = null;
        }
        return title;
    }

    /**
     * Returns an observable list of Section objects. The sections
     * will be used to colorize areas with a special meaning such
     * as the red area in a rpm gauge. Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @return an observable list of Section objects
     */
    public ObservableList<Section> getSections() { return sections; }
    /**
     * Sets the sections to the given list of Section objects. The
     * sections will be used to colorize areas with a special
     * meaning such as the red area in a rpm gauge.
     * Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @param SECTIONS
     */
    public void setSections(final List<Section> SECTIONS) {
        sections.setAll(SECTIONS);
        Collections.sort(sections, new SectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Sets the sections to the given array of Section objects. The
     * sections will be used to colorize areas with a special
     * meaning such as the red area in a rpm gauge.
     *
     * @param SECTIONS
     */
    public void setSections(final Section... SECTIONS) { setSections(Arrays.asList(SECTIONS)); }
    /**
     * Adds the given Section to the list of sections.
     * Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @param SECTION
     */
    public void addSection(final Section SECTION) {
        if (null == SECTION) return;
        sections.add(SECTION);
        Collections.sort(sections, new SectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Removes the given Section from the list of sections.
     * Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @param SECTION
     */
    public void removeSection(final Section SECTION) {
        if (null == SECTION) return;
        sections.remove(SECTION);
        Collections.sort(sections, new SectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Clears the list of sections.
     */
    public void clearSections() {
        sections.clear();
        fireUpdateEvent(SECTION_EVENT);
    }

    // ******************** UI related methods ********************************


    /**
     * Returns true if setting the value of the gauge will be animated
     * using the duration defined in animationDuration [ms].
     * Keep in mind that it only makes sense to animate the setting if
     * the data rate is low (more than 1 value per second). If you use real
     * live measured data you should set animated to false.
     *
     * @return true if setting the value of the gauge will be animated
     */
    public boolean isAnimated() { return null == animated ? _animated : animated.get(); }
    /**
     * Defines if setting the value of the gauge should be animated using
     * the duration defined in animationDuration [ms].
     * Keep in mind that it only makes sense to animate the setting if
     * the data rate is low (more than 1 value per second). If you use real
     * live measured data you should set animated to false.
     *
     * @param ANIMATED
     */
    public void setAnimated(final boolean ANIMATED) {
        if (null == animated) {
            _animated = ANIMATED;
        } else {
            animated.set(ANIMATED);
        }
    }
    public BooleanProperty animatedProperty() {
        if (null == animated) { animated = new SimpleBooleanProperty(Gauge.this, "animated", _animated); }
        return animated;
    }

    /**
     * Returns the duration in milliseconds that will be used to animate
     * the needle/bar of the gauge from the last value to the next value.
     * This will only be used if animated == true. This value will be
     * clamped in the range of 10ms - 10s.
     *
     * @return the duration in ms that will be used to animate the needle/bar
     */
    public long getAnimationDuration() { return animationDuration; }
    /**
     * Defines the duration ín milliseconds that will be used to animate
     * the needle/bar of the gauge from the last value to the next value.
     * This will only be used if animated == true. This value will be
     * clamped in the range of 10ms - 10s.
     *
     * @param ANIMATION_DURATION
     */
    public void setAnimationDuration(final long ANIMATION_DURATION) { animationDuration = Helper.clamp(10l, 10000l, ANIMATION_DURATION); }

    /**
     * Returns the angle in degree that defines the start of the scale with
     * it's minValue in a radial gauge. If set to 0 the scale will start at
     * the bottom center and the direction of counting is mathematical correct
     * counter-clockwise.
     * Means if you would like to start the scale on the left side in the
     * middle of the gauge height the startAngle should be set to 270 degrees.
     *
     * @return the angle in degree that defines the start of the scale
     */
    public double getStartAngle() { return null == startAngle ? _startAngle : startAngle.get(); }
    /**
     * Defines the angle in degree that defines the start of the scale with
     * it's minValue in a radial gauge. If set to 0 the scale will start at
     * the bottom center and the direction of counting is mathematical correct
     * counter-clockwise.
     * Means if you would like to start the scale on the left side in the
     * middle of the gauge height the startAngle should be set to 270 degrees.
     *
     * @param ANGLE
     */
    public void setStartAngle(final double ANGLE) {
        if (null == startAngle) {
            _startAngle = Helper.clamp(0d, 360d, ANGLE);
            fireUpdateEvent(RECALC_EVENT);
        } else {
            startAngle.set(ANGLE);
        }
    }
    public DoubleProperty startAngleProperty() {
        if (null == startAngle) {
            startAngle = new DoublePropertyBase(_startAngle) {
                @Override protected void invalidated() {
                    final double ANGLE = get();
                    if (ANGLE < 0 || ANGLE > 360 ) set(Helper.clamp(0d, 360d, ANGLE));
                    fireUpdateEvent(RECALC_EVENT);
                }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "startAngle"; }
            };
        }
        return startAngle;
    }

    /**
     * Returns the angle range in degree that will be used to draw the scale
     * of the radial gauge. The given range will be clamped in the range of
     * 0 - 360 degrees and will be drawn in the direction dependent on the
     * scaleDirection.
     *
     * @return the angle range in degree that will be used to draw the scale
     */
    public double getAngleRange() { return null == angleRange ? _angleRange : angleRange.get(); }
    /**
     * Defines the angle range in degree that will be used to draw the scale
     * of the radial gauge. The given range will be clamped in the range of
     * 0 - 360 degrees. The range will start at the startAngle and will be
     * drawn in the direction dependent on the scaleDirection.
     *
     * @param RANGE
     */
    public void setAngleRange(final double RANGE) {
        double tmpAngleRange = Helper.clamp(0d, 360d, RANGE);
        if (null == angleRange) {
            _angleRange = tmpAngleRange;
            setAngleStep(tmpAngleRange / getRange());
            fireUpdateEvent(RECALC_EVENT);
        } else {
            angleRange.set(tmpAngleRange);
        }
    }
    public DoubleProperty angleRangeProperty() {
        if (null == angleRange) {
            angleRange = new DoublePropertyBase(_angleRange) {
                @Override protected void invalidated() {
                    final double ANGLE_RANGE = get();
                    if (ANGLE_RANGE < 0 || ANGLE_RANGE > 360) set(Helper.clamp(0d, 360d, ANGLE_RANGE));
                    setAngleStep(get() / getRange());
                    fireUpdateEvent(RECALC_EVENT);
                }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "angleRange"; }
            };
        }
        return angleRange;
    }

    /**
     * Returns the value that is calculated by dividing the angleRange
     * by the range. The angleStep will always be recalculated when changing
     * the min-, maxValue or angleRange.
     * E.g. angleRange = 180 degrees, range = 0 - 100 will lead to angleStep = 180/100 = 1.8
     *
     * @return the value that is calculated by dividing the angleRange by the range
     */
    public double getAngleStep() { return null == angleStep ? _angleStep : angleStep.get(); }
    /**
     * Private method that will be used to set the angleStep
     *
     * @param STEP
     */
    private void setAngleStep(final double STEP) {
        if (null == angleStep) {
            _angleStep = STEP;
        } else {
            angleStep.set(STEP);
        }
    }
    public ReadOnlyDoubleProperty angleStepProperty() {
        if (null == angleStep) { angleStep = new SimpleDoubleProperty(Gauge.this, "angleStep", _angleStep); }
        return angleStep;
    }

    public Locale getLocale() { return null == locale ? _locale : locale.get(); }
    public void setLocale(final Locale LOCALE) {
        if (null == locale) {
            _locale = null == LOCALE ? Locale.US : LOCALE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            locale.set(LOCALE);
        }
    }
    public ObjectProperty<Locale> localeProperty() {
        if (null == locale) {
            locale  = new ObjectPropertyBase<Locale>(_locale) {
                @Override protected void invalidated() {
                    if (null == get()) set(Locale.US);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "locale"; }
            };
            _locale = null;
        }
        return locale;
    }

    /**
     * Returns true if the value of the gauge should be checked against
     * the threshold. If a value crosses the threshold it will fire an
     * event (EXCEEDED and UNDERRUN. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @return true if the value of the gauge should be checked against the threshold
     */
    public boolean isCheckThreshold() { return null == checkThreshold ? _checkThreshold : checkThreshold.get(); }
    /**
     * Defines if the value of the gauge should be checked against
     * the threshold. If a value crosses the threshold it will fire an
     * event (EXCEEDED and UNDERRUN. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @param CHECK
     */
    public void setCheckThreshold(final boolean CHECK) {
        if (null == checkThreshold) {
            _checkThreshold = CHECK;
        } else {
            checkThreshold.set(CHECK);
        }
    }
    public BooleanProperty checkThresholdProperty() {
        if (null == checkThreshold) { checkThreshold = new SimpleBooleanProperty(Gauge.this, "checkThreshold", _checkThreshold); }
        return checkThreshold;
    }


    @Override public String toString() {
        return new StringBuilder("{")
            .append("\"title\":").append("\"").append(getTitle()).append("\",")
            .append("\"subTitle\":").append("\"").append(getTitle()).append("\",")
            .append("\"value\":").append(getValue()).append(",")
            .append("\"minValue\":").append(getMinValue()).append(",")
            .append("\"maxValue\":").append(getMaxValue()).append(",")
            .append("\"threshold\":").append(getThreshold()).append(",")
            .append("}").toString();
    }

    // ******************** Event handling ************************************
    public void setOnUpdate(final UpdateEventListener LISTENER) { addUpdateEventListener(LISTENER); }
    public void addUpdateEventListener(final UpdateEventListener LISTENER) { if (!listenerList.contains(LISTENER)) listenerList.add(LISTENER); }
    public void removeUpdateEventListener(final UpdateEventListener LISTENER) { if (listenerList.contains(LISTENER)) listenerList.remove(LISTENER); }

    public void fireUpdateEvent(final UpdateEvent EVENT) {
        int listSize = listenerList.size();
        for (int i = 0; i < listSize; i++) { listenerList.get(i).onUpdateEvent(EVENT); }
    }

    public static class ThresholdEvent extends Event {
        public static final EventType<ThresholdEvent> THRESHOLD_EXCEEDED = new EventType(ANY, "THRESHOLD_EXCEEDED");
        public static final EventType<ThresholdEvent> THRESHOLD_UNDERRUN = new EventType(ANY, "THRESHOLD_UNDERRUN");


        // ******************** Constructors **************************************
        public ThresholdEvent(final EventType<ThresholdEvent> TYPE) { super(TYPE); }
        public ThresholdEvent(final Object SOURCE, final EventTarget TARGET, EventType<ThresholdEvent> TYPE) { super(SOURCE, TARGET, TYPE); }
    }
}
