package model.creatures;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import model.creatures.scaling.ScaleMap;
import model.creatures.scaling.ScalingNumber;

import java.util.function.Function;

public class CustomCreatureValue<T> {
    public final T target;
    public final Function<T, String> stringConverter;
    public final ScalingNumber modifier;
    public final ReadOnlyStringWrapper info = new ReadOnlyStringWrapper("");
    private final CreatureValue<T> creatureValue;

    public CustomCreatureValue(T target, Function<T, String> stringConverter, ReadOnlyIntegerProperty level, ScaleMap scaleMap) {
        this.target = target;
        this.stringConverter = stringConverter;
        this.modifier = new ScalingNumber(level, scaleMap);
        this.creatureValue = new UnmodifiableCustomCreatureValue();
    }

    public CustomCreatureValue(T target, ReadOnlyIntegerProperty level, ScaleMap scaleMap) {
        this(target, T::toString, level, scaleMap);
    }

    public int getModifier() {
        return modifier.get();
    }

    public CreatureValue<T> getAsCreatureValue() {
        return creatureValue;
    }

    public String getTargetString() {
        return stringConverter.apply(target);
    }

    private class UnmodifiableCustomCreatureValue implements CreatureValue<T> {

        @Override
        public T getTarget() {
            return target;
        }

        @Override
        public int getModifier() {
            return modifier.get();
        }

        @Override
        public String getInfo() {
            return info.get();
        }
    }
}
