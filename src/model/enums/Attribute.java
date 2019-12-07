package model.enums;

import model.ability_scores.AbilityScore;

import java.util.*;

import static model.ability_scores.AbilityScore.*;
import static model.util.StringUtils.camelCaseWord;

public enum Attribute {
    Acrobatics(Dex), Arcana(Int), Athletics(Str), Crafting(Int), Deception(Cha), Diplomacy(Cha), Intimidation(Cha), Lore(Int), Medicine(Wis), Nature(Wis), Occultism(Int), Performance(Cha), Religion(Wis), Society(Int), Stealth(Dex), Survival(Wis), Thievery(Dex),

    Fortitude(Con), Reflex(Dex), Will(Wis), Perception(Wis),

    SimpleWeapons, MartialWeapons, AdvancedWeapons, Unarmed,

    LightArmor, MediumArmor, HeavyArmor, Unarmored, Shields,

    SpellAttacks, SpellDcs, ClassDc,

    None;

    private static final Map<AbilityScore, List<Attribute>> skillsByScore = new HashMap<>();

    public static Attribute[] getSkills() {
        return skills;
    }

    private static final Attribute[] skills = {Acrobatics, Arcana, Athletics, Crafting, Deception, Diplomacy, Intimidation, Lore, Medicine, Nature, Occultism, Performance, Religion, Society, Stealth, Survival, Thievery};

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
        this.keyAbility = AbilityScore.None;
    }

    public static List<Attribute> skillsByScore(AbilityScore score) {
        return Collections.unmodifiableList(skillsByScore.get(score));
    }

    public static Attribute valueOf(WeaponProficiency proficiency) {
        switch(proficiency) {
            default:
                return Unarmed;
            case Simple:
                return SimpleWeapons;
            case Martial:
                return MartialWeapons;
            case Advanced:
                return AdvancedWeapons;
        }
    }

        public static Attribute valueOf(ArmorProficiency proficiency) {
            switch(proficiency) {
                default:
                    return Unarmored;
                case Light:
                    return LightArmor;
                case Medium:
                    return MediumArmor;
                case Heavy:
                    return HeavyArmor;
                case Shield:
                    return Shields;
            }
        }

        @SuppressWarnings("SpellCheckingInspection")
        public static Attribute robustValueOf(String s) {
            try {
                return Attribute.valueOf(camelCaseWord(s));
            }catch(IllegalArgumentException e) {
                switch (s.toLowerCase()) {
                    case "simpleweapons": return SimpleWeapons;
                    case "martialweapons": return MartialWeapons;
                    case "advancedweapons": return AdvancedWeapons;
                    case "lightarmor": return LightArmor;
                    case "mediumarmor": return MediumArmor;
                    case "heavyarmor": return HeavyArmor;
                    case "spellattacks": return SpellAttacks;
                    case "spelldcs": return SpellDcs;
                    case "classdc": return ClassDc;
                }
            }
            throw new IllegalArgumentException();
        }

        public AbilityScore getKeyAbility() {
        return keyAbility;
    }
}
