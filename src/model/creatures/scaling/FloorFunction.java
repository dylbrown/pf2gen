package model.creatures.scaling;

public class FloorFunction implements ZRMapping {
    private final ZRMapping zrMapping;
    private final double slope;
    private final double intercept;

    public FloorFunction(ZRMapping zrMapping, double slope, double intercept) {
        this.zrMapping = zrMapping;
        this.slope = slope;
        this.intercept = intercept;
    }

    @Override
    public double map(int input) {
        return slope * Math.floor(zrMapping.map(input)) + intercept;
    }

    @Override
    public boolean isInDomain(int input) {
        return zrMapping.isInDomain(input);
    }
}
