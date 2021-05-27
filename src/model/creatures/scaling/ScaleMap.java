package model.creatures.scaling;

import model.util.TransformationMap;
import model.util.Triple;

import java.util.TreeMap;
import java.util.function.Function;

public class ScaleMap {
    public static ScaleMap ABILITY_MODIFIER_SCALES, PERCEPTION_AND_SAVES_SCALES, SKILL_SCALES, AC_SCALES;

    static {
        ABILITY_MODIFIER_SCALES = new ScaleMap(
            new PiecewiseFunction( // Extreme
                new PolynomialFunction(i->i<=20, 0.324812030075, 4.48947368421),
                new FloorFunction(new PolynomialFunction(i->i>=20, 1d/3, 5.0 - 20d/3), 2, 1)),
            new PiecewiseFunction( // High
                new FloorFunction(new PolynomialFunction(i->i>=20, 1d/3, 5.0 - 20d/3), 2, 0),
                new PolynomialFunction(0.332015810277, 3.33201581028)),
            new PiecewiseFunction( // Moderate
                new PolynomialFunction(i->i<=20, 0.212450592885, 2.44071146245),
                new PolynomialFunction(i->i>=20, .5, -3.75)),
            new PiecewiseFunction( // Low
                new PolynomialFunction(i->i<=1, .5, .25),
                new PolynomialFunction(i->i>=1, 0.251304347826, 0.608695652174))
        );

        PiecewiseFunction terriblePerceptionScale = new PiecewiseFunction( // Terrible
                new PolynomialFunction(i -> i <= 1, 1, 1),
                new RepeatingStepFunction(2, 1, 20, 1, 1, 2),
                new PolynomialFunction(i -> i >= 20, 1.3, 1));
        RepeatingStepFunction extremePerceptionCenter = new RepeatingStepFunction(11, 1, 20, 1, 2);
        RepeatingStepFunction modPerceptionCenter = new RepeatingStepFunction(7, 1, 20, 1, 1, 2, 1, 2);
        PERCEPTION_AND_SAVES_SCALES = new ScaleMap(
            new PiecewiseFunction( // Extreme
                new PolynomialFunction(i->i<=1, 1, 10),
                extremePerceptionCenter,
                new PolynomialFunction(i->i>=20, 1.7, 5.2)),
            new PiecewiseFunction( // High
                new PolynomialFunction(i -> i <= 1, 1, 9),
                new PolynomialFunction(i -> i >= 1 && i <= 20, 1.376623377, 8.369242424),
                new PolynomialFunction(i -> i >= 20, 1.4, 8.2)),
            new PiecewiseFunction( // Moderate
                new PolynomialFunction(i->i<=1, 1, 6),
                    modPerceptionCenter,
                new PolynomialFunction(i->i>=20, 1.7, 5.2)),
            new PiecewiseFunction( // Low
                new PolynomialFunction(i->i<=1, 1, 3),
                new ShiftedFunction(modPerceptionCenter, -3),
                new PolynomialFunction(i->i>=20, 1.4, 2.2)),
            terriblePerceptionScale
        );

        PiecewiseFunction extremeSkillScale = new PiecewiseFunction( // Extreme
                new PolynomialFunction(i -> i <= 1, 1, 9),
                new RepeatingStepFunction(10, 1, 20, 1, 2, 2),
                new PolynomialFunction(i -> i >= 20, 1.7, 7.2));
        PiecewiseFunction moderateSkillScale = new PiecewiseFunction( // Moderate
                new PolynomialFunction(i -> i <= 1, 1, 5),
                new ShiftedFunction(extremePerceptionCenter, -5),
                new PolynomialFunction(i -> i >= 20, 1.4, 6.2));
        SKILL_SCALES = new ScaleMap(
            extremeSkillScale,
            new ShiftedFunction(extremeSkillScale, -3),
            moderateSkillScale,
            new ShiftedFunction(moderateSkillScale, -2),
            new ShiftedFunction(terriblePerceptionScale, +1)
        );

        PiecewiseFunction acScale = new PiecewiseFunction(
                new PolynomialFunction(i -> i <= 1, .5, 18.666666),
                new PolynomialFunction(1.5, 17.75)
        );

        AC_SCALES = new ScaleMap(
            acScale,
            new ShiftedFunction(acScale, -3),
            new ShiftedFunction(acScale, -4),
            new ShiftedFunction(acScale, -6)
        );
    }

    private final TreeMap<Scale, ZRMapping> fixedPoints = new TreeMap<>();
    private TransformationMap<Scale, ZRMapping, Function<Integer, Double>> map =
            new TransformationMap<>(fixedPoints, f -> f::map);

    private ScaleMap(ZRMapping extreme, ZRMapping high, ZRMapping moderate, ZRMapping low) {
        fixedPoints.put(Scale.Extreme, extreme);
        fixedPoints.put(Scale.High, high);
        fixedPoints.put(Scale.Moderate, moderate);
        fixedPoints.put(Scale.Low, low);
    }

    private ScaleMap(ZRMapping extreme, ZRMapping high, ZRMapping moderate, ZRMapping low, ZRMapping terrible) {
        this(extreme, high, moderate, low);
        fixedPoints.put(Scale.Terrible, terrible);
    }

    public TransformationMap<Scale, ZRMapping, Function<Integer, Double>> getMap() {
        return map;
    }

    /**
     * @param scale Fixed scale point to evaluate at
     * @param level Level to evaluate at
     * @return The value of the scale point at the given level
     */
    public int get(Scale scale, int level) {
        return (int) Math.round(fixedPoints.get(scale).map(level));
    }


    /**
     * @param level Level to compute at
     * @return Lowest existing point's value at the given level
     */
    public int getMin(int level) {
        return get(getLowerBound(0), level);
    }

    /**
     * @param value The value at oldLevel
     * @param oldLevel The level represented in value
     * @param newLevel The level to convert to
     * @return value, scaled from oldLevel to newLevel
     */
    public int scale(int value, int oldLevel, int newLevel) {
        Triple<Double, Scale, Scale> position = getPosition(value, oldLevel);
        double offset = position.first - position.second.ordinal();
        int newLowerValue = get(position.second, newLevel);
        if(offset == 0)
            return newLowerValue;
        int newUpperValue = get(position.third, newLevel);
        double slope =
                ((double) newUpperValue - newLowerValue) /
                ((double) position.third.ordinal() - position.second.ordinal());
        return (int) Math.round(newLowerValue + offset * slope);
    }

    /**
     * @param value The value at the given level
     * @param level The level of the creature
     * @return Triple with the position, LB, UB. LB and UB were used for slope calculations
     */
    private Triple<Double, Scale, Scale> getPosition(int value, int level) {
        Scale upperBound = map.keySet().iterator().next();
        int upperBoundValue = get(upperBound, level);
        while(upperBound != Scale.Extreme && upperBoundValue < value) {
            upperBound = getNext(upperBound);
            upperBoundValue = get(upperBound, level);
        }
        if(upperBoundValue == value && upperBound != null)
            return new Triple<>((double) upperBound.ordinal(), upperBound, upperBound);
        Scale lowerBound = getPrevious(upperBound);
        if(lowerBound == null) {
            lowerBound = upperBound;
            upperBound = getNext(upperBound);
            upperBoundValue = get(upperBound, level);
        }
        assert lowerBound != null;
        assert upperBound != null;
        int lowerBoundValue = get(lowerBound, level);
        double inverseSlope =  ((double) upperBound.ordinal() - lowerBound.ordinal()) /
                ((double) upperBoundValue - lowerBoundValue);
        return new Triple<>(
                (value - lowerBoundValue) * inverseSlope + lowerBound.ordinal(),
                lowerBound,
                upperBound);
    }

    /**
     * @param position fractional position representing distance between Scale ordinals
     * @return Greatest Lower Bound if it exists, otherwise the lowest existing Scale
     */
    private Scale getLowerBound(double position) {
        Scale scale = Scale.getNearest(position);
        if(map.containsKey(scale))
            return scale;
        if(scale == Scale.Terrible)
            return map.keySet().iterator().next();
        return getLowerBound(position - 1);
    }


    /**
     * @param scale Scale to start from
     * @return The next scale that exists in the map, or null if none such exists
     */
    private Scale getNext(Scale scale) {
        if(scale == Scale.Extreme)
            return null;
        Scale next = Scale.getNearest(scale.ordinal() + 1);
        return (map.containsKey(next)) ? next : getNext(next);
    }

    /**
     * @param scale Scale to start from
     * @return The previous scale that exists in the map, or null if none such exists
     */
    private Scale getPrevious(Scale scale) {
        if(scale == Scale.Terrible)
            return null;
        Scale previous = Scale.getNearest(scale.ordinal() - 1);
        return (map.containsKey(previous)) ? previous : getPrevious(previous);
    }
}
