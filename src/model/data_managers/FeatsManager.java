package model.data_managers;

import model.abilities.Ability;
import model.xml_parsers.FeatsLoader;

import java.util.*;

public class FeatsManager {
    private static SortedMap<String, Ability> allFeatsMap;
    private static List<Ability> allFeats;
    private static FeatsLoader generalFeats = new FeatsLoader("data/feats/general.pfdyl");
    private static FeatsLoader skillFeats = new FeatsLoader("data/feats/skill.pfdyl");

    static{
        allFeats = new ArrayList<>();
        allFeatsMap = new TreeMap<>();
        allFeats.addAll(getGeneralFeats());
        allFeats.addAll(getSkillFeats());
        for (Ability feat : allFeats) {
            allFeatsMap.put(feat.toString().toLowerCase(), feat);
        }
    }

    public static List<Ability> getFeats() {
        return Collections.unmodifiableList(allFeats);
    }

    public static List<Ability> getGeneralFeats() {
        return generalFeats.parse();
    }

    public static List<Ability> getSkillFeats() {
        return skillFeats.parse();
    }

    public static Ability find(String contents) {
        return allFeatsMap.get(contents.toLowerCase());
    }
}
