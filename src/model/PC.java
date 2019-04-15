package model;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.abc.Ancestry;
import model.abc.Background;
import model.abc.Class;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilityScores.AbilityMod;
import model.abilityScores.AbilityScore;
import model.enums.Attribute;
import model.enums.Proficiency;

import java.util.*;

import static model.abilityScores.AbilityScore.Con;

public class PC {
    private Ancestry ancestry;
    private Background background;
    private Class pClass;
    private int level = 1;
    private Map<Attribute, ReadOnlyObjectWrapper<Proficiency>> proficiencies = new HashMap<>();
    private ObservableList<AbilitySlot> abilities = FXCollections.observableArrayList();
    private Map<AbilityScore, List<AbilityMod>> abilityScores = new HashMap<>();
    private List<AbilityMod> remaining = new ArrayList<>();
    private String name;


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAncestry(Ancestry ancestry) {
        if(this.ancestry != null)
            remove(this.ancestry.getAbilityMods());
        this.ancestry = ancestry;
        apply(ancestry.getAbilityMods());
    }

    public void setBackground(Background background) {
        if(this.background != null)
            remove(this.background.getAbilityMods());
        this.background = background;
        apply(background.getAbilityMods());
        apply(background.getMod());
    }

    public void setClass(Class newClass) {
        pClass = newClass;
        applyLevel(pClass.getLevel(1));
    }

    private void applyLevel(List<AbilitySlot> level) {
        for(AbilitySlot slot: level) {
            abilities.add(slot);
            if(slot.isPreSet()) {
                apply(slot);
            }
        }
    }

    private void apply(AbilitySlot slot) {
        if(slot.getCurrentAbility() != null)
            for(AttributeMod mod: slot.getCurrentAbility().getModifiers()){
                apply(mod);
            }
    }

    public Map<Attribute, ObservableValue<Proficiency>> getProficiencies() {
        return Collections.unmodifiableMap(proficiencies);
    }

    public ObservableValue<Proficiency> getProficiency(Attribute attr) {
        return proficiencies.computeIfAbsent(attr, (key) -> new ReadOnlyObjectWrapper<>(null));
    }

    private void apply(AttributeMod mod) {
        ReadOnlyObjectWrapper<Proficiency> proficiency = proficiencies.get(mod.getAttr());
        if(proficiency == null) {
            proficiencies.put(mod.getAttr(), new ReadOnlyObjectWrapper<>(mod.getMod()));
        }else if(proficiency.getValue() == null || proficiency.getValue().getMod() < mod.getMod().getMod()) {
            proficiency.set(mod.getMod());
        }
    }

    public int getHP() {
        return ((ancestry != null) ? ancestry.getHP() : 0) + (((pClass != null) ? pClass.getHP() : 0) + getAbilityMod(Con)) * level;
    }

    public int getAbilityMod(AbilityScore ability) {
        return getAbilityScore(ability) / 2  - 5;
    }

    public int getAbilityScore(AbilityScore ability) {
        int score = 10;
        for(AbilityMod mod: abilityScores.computeIfAbsent(ability, (key)->new ArrayList<>())) {
            if(mod.isPositive()) {
                score += (score < 18) ? 2 : 1;
            }else{
                score -= 2;
            }
        }
        return score;
    }

    private void apply(List<AbilityMod> abilityMods) {
        for(AbilityMod mod: abilityMods) {
            abilityScores.computeIfAbsent(mod.getTarget(), (key)-> new ArrayList<>()).add(mod);
        }
    }

    private void remove(List<AbilityMod> abilityMods) {
        for(AbilityMod mod: abilityMods) {
            List<AbilityMod> mods = abilityScores.get(mod.getTarget());
            mods.remove(mod);
        }
    }
}
