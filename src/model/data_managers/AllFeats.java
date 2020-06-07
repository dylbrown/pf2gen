package model.data_managers;

import model.abilities.Ability;
import model.xml_parsers.FeatsLoader;

import java.util.*;

public class AllFeats {
    private static final SortedMap<String, Ability> allFeatsMap;
    private static final FeatsLoader feats = new FeatsLoader("data/feats");

    private AllFeats(){}

    static{
        allFeatsMap = new TreeMap<>();
        for (Ability feat : feats.parse()) {
            allFeatsMap.put(feat.toString().toLowerCase(), feat);
        }
    }

    public static List<Ability> getFeats(String allowedType) {
        return feats.getFeats(allowedType);
    }

    public static Ability find(String contents) {
        return allFeatsMap.get(contents.toLowerCase());
    }
}
