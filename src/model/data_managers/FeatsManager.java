package model.data_managers;

import model.abilities.Ability;
import model.xml_parsers.FeatsLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatsManager {
    private static List<Ability> allFeats;
    private static FeatsLoader generalFeats = new FeatsLoader("data/feats/general.pfdyl");
    private static FeatsLoader skillFeats = new FeatsLoader("data/feats/skill.pfdyl");
    public static List<Ability> getFeats() {
        if(allFeats == null) {
            allFeats = new ArrayList<>();
            allFeats.addAll(getGeneralFeats());
            allFeats.addAll(getSkillFeats());
        }
        return Collections.unmodifiableList(allFeats);
    }

    public static List<Ability> getGeneralFeats() {
        return generalFeats.parse();
    }

    public static List<Ability> getSkillFeats() {
        return skillFeats.parse();
    }
}
