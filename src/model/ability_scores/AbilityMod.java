package model.ability_scores;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import model.enums.Type;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

public class AbilityMod implements Serializable {
    ReadOnlyObjectWrapper<AbilityScore> target;
    private boolean positive;

    public Type getSource() {
        return source;
    }

    private Type source;
    private final int level = 1;

    public AbilityMod(AbilityScore target, boolean positive, Type source) {
        this.target = new ReadOnlyObjectWrapper<>(target);
        this.positive = positive;
        this.source = source;
    }

    public boolean isPositive() {
        return positive;
    }

    public AbilityScore getTarget() {
        return target.get();
    }

    public ReadOnlyObjectProperty<AbilityScore> getTargetProperty() {
        return target.getReadOnlyProperty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbilityMod that = (AbilityMod) o;
        return positive == that.positive &&
                level == that.level &&
                target == that.target &&
                source == that.source;
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, positive, source, level);
    }

    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
    {
        target = new ReadOnlyObjectWrapper<>((AbilityScore) aInputStream.readObject());
        positive = aInputStream.readBoolean();
        source = (Type) aInputStream.readObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException
    {
        aOutputStream.writeObject(target.get());
        aOutputStream.writeBoolean(positive);
        aOutputStream.writeObject(source);
    }
}
