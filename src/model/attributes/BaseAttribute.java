package model.attributes;

import model.ability_scores.AbilityScore;
import model.util.StringUtils;

import java.util.*;

import static model.ability_scores.AbilityScore.*;

public enum BaseAttribute implements Attribute {
    Acrobatics(Dex), Arcana(Int), Athletics(Str), Crafting(Int), Deception(Cha), Diplomacy(Cha), Intimidation(Cha), Lore(Int), Medicine(Wis), Nature(Wis), Occultism(Int), Performance(Cha), Religion(Wis), Society(Int), Stealth(Dex), Survival(Wis), Thievery(Dex),

    Fortitude(Con), Reflex(Dex), Will(Wis), Perception(Wis),

    SimpleWeapons, MartialWeapons, AdvancedWeapons, Unarmed,

    LightArmor, MediumArmor, HeavyArmor, Unarmored, Shields,

    ArcaneSpellAttacks(CastingAbility), ArcaneSpellDCs(CastingAbility),
    DivineSpellAttacks(CastingAbility), DivineSpellDCs(CastingAbility),
    OccultSpellAttacks(CastingAbility), OccultSpellDCs(CastingAbility),
    PrimalSpellAttacks(CastingAbility), PrimalSpellDCs(CastingAbility),

    ClassDC(KeyAbility),

    None;

    private static final Map<AbilityScore, List<BaseAttribute>> skillsByScore = new HashMap<>();

    public static BaseAttribute[] getSkills() {
        return skills;
    }

    private static final BaseAttribute[] skills = {Acrobatics, Arcana, Athletics, Crafting, Deception, Diplomacy, Intimidation, Lore, Medicine, Nature, Occultism, Performance, Religion, Society, Stealth, Survival, Thievery};
    private static final BaseAttribute[] saves = {Fortitude, Reflex, Will};

    static{
        skillsByScore.put(Str, new ArrayList<>(Collections.singletonList(Athletics)));
        skillsByScore.put(Dex, new ArrayList<>(Arrays.asList(Acrobatics, Stealth, Thievery)));
        skillsByScore.put(Con, new ArrayList<>(Collections.emptyList()));
        skillsByScore.put(Int, new ArrayList<>(Arrays.asList(Arcana, Crafting, Lore, Occultism, Society)));
        skillsByScore.put(Wis, new ArrayList<>(Arrays.asList(Medicine, Nature, Religion, Survival)));
        skillsByScore.put(Cha, new ArrayList<>(Arrays.asList(Deception, Diplomacy, Intimidation, Performance)));
    }

    private final AbilityScore keyAbility;

    BaseAttribute(AbilityScore score) {
        this.keyAbility = score;
    }

    BaseAttribute() {
        this.keyAbility = AbilityScore.None;
    }

    public static List<BaseAttribute> skillsByScore(AbilityScore score) {
        return Collections.unmodifiableList(skillsByScore.get(score));
    }

    public static BaseAttribute[] getSaves() {
        return saves;
    }

    public boolean hasACP() {
        return this.getKeyAbility().equals(Str) || this.getKeyAbility().equals(Dex);
    }



    @SuppressWarnings("SpellCheckingInspection")
    public static BaseAttribute robustValueOf(String s) {
        String formatted = org.apache.commons.lang3.StringUtils.
                deleteWhitespace(StringUtils.capitalize(s));
        try {
            return BaseAttribute.valueOf(formatted);
        }catch(IllegalArgumentException e) {
            switch (formatted.toLowerCase()) {
                case "simpleweapons": return SimpleWeapons;
                case "martialweapons": return MartialWeapons;
                case "advancedweapons": return AdvancedWeapons;
                case "lightarmor": return LightArmor;
                case "mediumarmor": return MediumArmor;
                case "heavyarmor": return HeavyArmor;
                case "unarmedattacks": return Unarmed;
                case "unarmoreddefense": return Unarmored;
                case "arcanespellattacks": return ArcaneSpellAttacks;
                case "arcanespelldcs": return ArcaneSpellDCs;
                case "divinespellattacks": return DivineSpellAttacks;
                case "divinespelldcs": return DivineSpellDCs;
                case "occultspellattacks": return OccultSpellAttacks;
                case "occultspelldcs": return OccultSpellDCs;
                case "primalspellattacks": return PrimalSpellAttacks;
                case "primalspelldcs": return PrimalSpellDCs;
                case "classdc": return ClassDC;
                case "fort": return Fortitude;
                case "ref": return Reflex;
                case "will": return Will;
            }
        }
        System.out.println(s);
        throw new IllegalArgumentException();
    }

    public AbilityScore getKeyAbility() {
    return keyAbility;
}

    @Override
    public BaseAttribute getBase() {
        return this;
    }
}
