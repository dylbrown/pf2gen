package model.enums;

import model.abilityScores.AbilityScore;

import java.util.*;

import static model.abilityScores.AbilityScore.*;
import static model.abilityScores.AbilityScore.Cha;
import static model.abilityScores.AbilityScore.Wis;

public enum Attribute {
    Acrobatics(Dex), Arcana(Int), Athletics(Str), Crafting(Int), Deception(Cha), Diplomacy(Cha), Intimidation(Cha), Lore(Int), Medicine(Wis), Nature(Wis), Occultism(Int), Performance(Cha), Religion(Wis), Society(Int), Stealth(Dex), Survival(Wis), Thievery(Dex),

    Fortitude(Con), Reflex(Dex), Will(Wis), Perception(Wis),

    SimpleWeapons, MartialWeapons, LightArmor, MediumArmor, HeavyArmor, Shields;

    private static Map<AbilityScore, List<Attribute>> skillsByScore = new HashMap<>();

    static{
        skillsByScore.put(Str, new ArrayList<>(Collections.singletonList(Athletics)));
        skillsByScore.put(Dex, new ArrayList<>(Arrays.asList(Acrobatics, Stealth, Thievery)));
        skillsByScore.put(Con, new ArrayList<>(Collections.emptyList()));
        skillsByScore.put(Int, new ArrayList<>(Arrays.asList(Arcana, Crafting, Lore, Occultism, Society)));
        skillsByScore.put(Wis, new ArrayList<>(Arrays.asList(Medicine, Nature, Religion, Survival)));
        skillsByScore.put(Cha, new ArrayList<>(Arrays.asList(Deception, Diplomacy, Intimidation, Performance)));
    }

    private final AbilityScore keyAbility;

    Attribute(AbilityScore score) {
        this.keyAbility = score;
    }

    Attribute() {
        this.keyAbility = None;
    }

    public static List<Attribute> skillsByScore(AbilityScore score) {
        return Collections.unmodifiableList(skillsByScore.get(score));
    }

    public AbilityScore getKeyAbility() {
        return keyAbility;
    }
}
