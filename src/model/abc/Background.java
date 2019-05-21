package model.abc;

import model.abilities.Ability;
import model.AttributeMod;
import model.ability_scores.AbilityMod;
import model.enums.Attribute;
import model.enums.Proficiency;

import java.util.List;

public class Background extends ABC {
    //Farmhand(Con, Wis, "Farming", null), Gladiator(Str, Cha, "Gladiatorial", null), Hunter(Dex, Wis, "Hunting", null), Laborer(Str, Con, "Labor", null), Merchant(Int, Cha, "Mercantile", null), Noble(Int, Cha, "Nobility", null), Nomad(Con, Wis, "A Terrain", null), Sailor(Str, Dex, "Sailing", null), ScholarArcana(Int, Wis, Arcana, null), ScholarNature(Int, Wis, Nature, null), ScholarOccultism(Int, Wis, Occultism, null), ScholarReligion(Int, Wis, Religion, null), Scout(Dex, Wis, "Scouting", null), StreetUrchin(Dex, Int, "Underworld", null), Warrior(Str, Con, "Warfare", null);

    private final AttributeMod mod;
    private final Ability freeFeat;
    private final String modString;

    public Background(String name, String modString, String desc, List<AbilityMod> abilityMods, Attribute skill) {
        super(name, desc, abilityMods);
        this.modString = modString;
        this.mod = new AttributeMod(skill, Proficiency.Trained);
        this.freeFeat = null;
    }

    public Background(String name, String modString, String desc, List<AbilityMod> abilityMods, Attribute skill, String data) {
        super(name, desc, abilityMods);
        this.modString = modString;
        this.mod = new AttributeMod(skill, Proficiency.Trained, data);
        this.freeFeat = null;
    }

    public AttributeMod getMod() {
        return mod;
    }

    public String getModString() {
        return modString;
    }
}
