package model.ability_scores;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum AbilityScore {
    Str, Dex, Con, Int, Wis, Cha, None, Free;

    private static final List<AbilityScore> scores = new ArrayList<>(Arrays.asList(Str, Dex, Con, Int, Wis, Cha));
    public static List<AbilityScore> scores() {
        return Collections.unmodifiableList(scores);
    }
}
