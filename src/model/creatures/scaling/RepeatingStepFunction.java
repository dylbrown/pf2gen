package model.creatures.scaling;

public class RepeatingStepFunction implements ZRMapping {
    private final double[] pattern;
    private final double firstValue;
    private final int firstLevel, lastLevel;

    public RepeatingStepFunction(double firstValue, int firstLevel, int lastLevel, double... pattern) {
        this.firstValue = firstValue;
        this.firstLevel = firstLevel;
        this.lastLevel = lastLevel;
        this.pattern = pattern;
    }

    @Override
    public double map(int input) {
        if(input < firstLevel)
            throw new NotInDomainException();
        double value = firstValue;
        for(int i = 0; i < input - firstLevel; i++) {
            value += pattern[i % pattern.length];
        }
        return value;
    }

    @Override
    public boolean isInDomain(int input) {
        return firstLevel <= input && input <= lastLevel;
    }
}
