package model.ability_scores;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum AbilityScore {
    Str, Dex, Con, Int, Wis, Cha, None, Free, KeyAbility, CastingAbility;

    private static final List<AbilityScore> scores = new ArrayList<>(Arrays.asList(Str, Dex, Con, Int, Wis, Cha));
    public static List<AbilityScore> scores() {
        return Collections.unmodifiableList(scores);
    }

    public static AbilityScore robustValueOf(String name) {
        switch(name.toLowerCase()) {
            case "str":
            case "strength":
                return Str;
            case "dex":
            case "dexterity":
                return Dex;
            case "con":
            case "constitution":
                return Con;
            case "int":
            case "intelligence":
                return Int;
            case "wis":
            case "wisdom":
                return Wis;
            case "cha":
            case "charisma":
                return Cha;
            case "free":
                return Free;
            case "keyability":
            case "key ability":
                return KeyAbility;
            case "castingability":
            case "casting ability":
                return CastingAbility;
        }
        return None;
    }
}
