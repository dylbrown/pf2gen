package model.creatures.scaling;

public class ShiftedFunction implements ZRMapping {
    private final ZRMapping zrMapping;
    private final int shift;

    public ShiftedFunction(ZRMapping zrMapping, int shift) {
        this.zrMapping = zrMapping;
        this.shift = shift;
    }

    @Override
    public double map(int input) {
        return zrMapping.map(input) + shift;
    }

    @Override
    public boolean isInDomain(int input) {
        return zrMapping.isInDomain(input);
    }
}
