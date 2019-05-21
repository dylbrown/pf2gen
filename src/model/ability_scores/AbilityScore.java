package model.ability_scores;

public enum AbilityScore {
    Str, Dex, Con, Int, Wis, Cha, None, Free;

    public static AbilityScore[] scores() {
        return new AbilityScore[]{Str, Dex, Con, Int, Wis, Cha};
    }
}
