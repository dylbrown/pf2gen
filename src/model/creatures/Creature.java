package model.creatures;

import model.NamedObject;
import model.abilities.Ability;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.enums.Language;
import model.enums.Trait;

import java.util.List;
import java.util.Map;

public interface Creature extends NamedObject {
    CreatureFamily getFamily();
    List<Trait> getTraits();
    Map<Attribute, Integer> getModifiers();
    Map<Attribute, String> getModifierSpecialInfo();
    Map<AbilityScore, Integer> getAbilityModifiers();
    List<Language> getLanguages();
    String getSpecialLanguages();
    List<CreatureItem> getItems();
    int getLevel();
    int getAC();
    int getHP();
    String getSpeed();
    String getACMods();
    String getSaveMods();
    String getHealthMods();
    String getImmunities();
    String getResistances();
    String getWeaknesses();
    String getSenses();
    List<Ability> getMiscAbilities();
    List<Ability> getDefensiveAbilities();
    List<Ability> getOffensiveAbilities();
    List<Attack> getAttacks();
    List<CreatureSpellList> getSpells();
}
