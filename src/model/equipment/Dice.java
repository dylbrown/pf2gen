package model.equipment;

import java.util.HashMap;
import java.util.Map;

public class Dice {
    private final int count;
    private final int size;
    private static final Map<Integer, Map<Integer, Dice>> dice = new HashMap<>();

    private Dice(int count, int size) {
        this.count = count;
        this.size = size;
    }

    public static Dice get(int count, int size) {
        return dice.computeIfAbsent(count, (key)->new HashMap<>()).computeIfAbsent(size, (key)->new Dice(count, size));
    }

    public int getCount() {
        return count;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return count+"d"+size;
    }
}
