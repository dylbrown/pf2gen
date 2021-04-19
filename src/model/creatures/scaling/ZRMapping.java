package model.creatures.scaling;

public interface ZRMapping {
    double map(int input);
    boolean isInDomain(int input);
}
