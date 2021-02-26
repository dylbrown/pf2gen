package model.items.weapons;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Dice {
    private final int count;
    private final int size;
    private static final Map<Integer, Map<Integer, Dice>> dice = new ConcurrentHashMap<>();

    private Dice(int count, int size) {
        this.count = count;
        this.size = size;
    }

    public static Dice get(int count, int size) {
        return dice.computeIfAbsent(count, (key)->new HashMap<>()).computeIfAbsent(size, (key)->new Dice(count, size));
    }

    public static Dice valueOf(String s) {
        int d = s.indexOf("d");
        if(d == -1) return null;
        return get(Integer.parseInt(s.substring(0, d)), Integer.parseInt(s.substring(d+1)));
    }

    public int getCount() {
        return count;
    }

    public int getSize() {
        return size;
    }

    public static Dice increase(Dice source) {
        int size = getNextSize(source.size);
        return Dice.get(source.count, size);
    }

    private static int getNextSize(int size) {
        switch (size) {
            case 4:
                return 6;
            case 6:
                return 8;
            case 8:
                return 10;
            case 10:
                return 12;
            case 12:
            default:
                return size;
        }
    }

    @Override
    public String toString() {
        return count+"d"+size;
    }
}
