package model.creatures;

public interface CreatureValue<T> {
    T getTarget();
    int getModifier();
    String getInfo();
}
