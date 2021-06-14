package model.creatures.scaling;

public class PointReplacementFunction implements ZRMapping {
    private final ZRMapping zrMapping;
    private final int point;
    private final double value;

    public PointReplacementFunction(ZRMapping zrMapping, int point, double value) {
        this.zrMapping = zrMapping;
        this.point = point;
        this.value = value;
    }

    @Override
    public double map(int input) {
        return (input == point) ? value : zrMapping.map(input);
    }

    @Override
    public boolean isInDomain(int input) {
        return input == point || zrMapping.isInDomain(input);
    }
}
