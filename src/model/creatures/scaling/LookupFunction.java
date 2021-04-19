package model.creatures.scaling;

import java.util.HashMap;
import java.util.Map;

public class LookupFunction implements ZRMapping {
    private final Map<Integer, Double> lookup = new HashMap<>();

    public static class LookupPair {
        private final int key;
        private final double value;
        public LookupPair(int key, double value) {
            this.key = key;
            this.value = value;
        }
    }

    public LookupFunction(LookupPair... pairs){
        for (LookupPair pair : pairs) {
            lookup.put(pair.key, pair.value);
        }
    }

    @Override
    public double map(int input) {
        return lookup.get(input);
    }

    @Override
    public boolean isInDomain(int input) {
        return lookup.containsKey(input);
    }
}
