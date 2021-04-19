package model.creatures.scaling;

import java.util.function.Predicate;

public class PolynomialFunction implements ZRMapping {
    private final Predicate<Integer> isInDomain;
    private final double[] coefficients;

    public PolynomialFunction(Predicate<Integer> isInDomain, double... coefficients) {
        this.isInDomain = isInDomain;
        this.coefficients = coefficients;
    }

    public PolynomialFunction(double... coefficients) {
        this(i->true, coefficients);
    }

    @Override
    public double map(int x) {
        double value = 0;
        for (int i = 0; i < coefficients.length; i++) {
            value += Math.pow(x, coefficients.length - i - 1) * coefficients[i];
        }
        return value;
    }

    @Override
    public boolean isInDomain(int input) {
        return isInDomain.test(input);
    }
}
