package model.creatures.scaling;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.value.ObservableIntegerValue;


public class ScalingNumber extends ReadOnlyIntegerWrapper {

    @SuppressWarnings("FieldCanBeLocal")
    private final ObservableIntegerValue level;

    public ScalingNumber(ObservableIntegerValue level, int currentValue, ScaleMap scaleMap) {
        super(currentValue);
        this.level = level;
        level.addListener((o, oldVal, newVal) ->
                set(scaleMap.scale(get(), oldVal.intValue(), newVal.intValue())));
    }

    public ScalingNumber(ObservableIntegerValue level, ScaleMap scaleMap) {
        this(level, scaleMap.getMin(level.get()), scaleMap);
    }
}
