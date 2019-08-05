package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import model.AttributeMod;
import model.abilities.Ability;
import model.abilities.AbilitySet;
import model.abilities.SkillIncrease;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.Choice;
import model.abilities.abilitySlots.FeatSlot;
import model.data_managers.FeatsManager;
import model.enums.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbilityManager {
    private final ObservableList<Ability> abilities = FXCollections.observableArrayList();
    private final SortedList<Ability> sortedAbilities;
    private final ObservableList<AbilitySet> abilitySets = FXCollections.observableArrayList();
    private final PC pc;
    private final DecisionManager decisions;

    AbilityManager(PC pc) {
        this.pc = pc;
        this.decisions = pc.decisions();
        sortedAbilities = new SortedList<>(abilities);

        pc.getLevelProperty().addListener((observable -> {
            for (AbilitySet abilitySet : abilitySets) {
                remove(abilitySet);
                apply(abilitySet);
            }

        }));
    }

    public List<Ability> getOptions(Choice<Ability> choice) {
        if (choice instanceof FeatSlot){
            List<Ability> results = new ArrayList<>();
            for (Type allowedType : ((FeatSlot) choice).getAllowedTypes()) {
                switch (allowedType) {
                    case Class:
                        if (pc.getPClass() != null)
                            results.addAll(pc.getPClass().getFeats(((FeatSlot) choice).getLevel()));
                        break;
                    case Ancestry:
                        if (pc.getAncestry() != null)
                            results.addAll(pc.getAncestry().getFeats(((FeatSlot) choice).getLevel()));
                        break;
                    case Heritage:
                        if (pc.getAncestry() != null)
                            results.addAll(pc.getAncestry().getHeritages());
                        break;
                    case General:
                        results.addAll(FeatsManager.getGeneralFeats());
                    case Skill:
                        results.addAll(FeatsManager.getSkillFeats());
                        break;
                }
            }
            return results;
        }
        return Collections.emptyList();
    }

    void apply(AbilitySlot slot) {
        if(slot instanceof Choice)
            decisions.add((Choice) slot);

        Ability ability = slot.getCurrentAbility();

        apply(ability);
    }

    private void apply(Ability ability) {
        if(ability != null) {
            if(ability instanceof AbilitySet) {
                abilitySets.add((AbilitySet) ability);
                List<Ability> subAbilities = ((AbilitySet) ability).getAbilities();
                int level = pc.getLevel();
                for (Ability subAbility : subAbilities) {
                    if(subAbility.getLevel() <= level)
                        apply(subAbility);
                }
            }else {
                if (ability instanceof SkillIncrease) {
                    pc.attributes().addSkillIncrease(ability.getLevel());
                }

                for (AttributeMod mod : ability.getModifiers()) {
                    pc.attributes().apply(mod);
                }
                for (AbilitySlot subSlot : ability.getAbilitySlots()) {
                    apply(subSlot);
                }
                pc.scores().apply(ability.getAbilityMods());
                if (!ability.getCustomMod().equals(""))
                    pc.mods().jsApply(ability.getCustomMod());
                abilities.add(ability);
            }
        }
    }

    void remove(AbilitySlot slot) {

        if(slot instanceof Choice){
            decisions.remove((Choice) slot);
            ((Choice) slot).empty();
        }

        Ability ability = slot.getCurrentAbility();
        remove(ability);
    }

    private void remove(Ability ability) {
        if(ability != null) {
            if(ability instanceof AbilitySet){
                abilitySets.remove(ability);
                List<Ability> subAbilities = ((AbilitySet) ability).getAbilities();
                for (Ability subAbility : subAbilities) {
                    if(sortedAbilities.contains(subAbility))
                        apply(subAbility);
                }
            }else{
                abilities.remove(ability);

                if (ability instanceof SkillIncrease) {
                    pc.attributes().removeSkillIncrease(ability.getLevel());
                }

                for (AttributeMod mod : ability.getModifiers()) {
                    pc.attributes().remove(mod);
                }
                for(AbilitySlot subSlot: ability.getAbilitySlots()) {
                    remove(subSlot);
                }

                pc.scores().remove(ability.getAbilityMods());
                if(!ability.getCustomMod().equals(""))
                    pc.mods().jsRemove(ability.getCustomMod());
            }
        }
    }

    public ObservableList<Ability> getAbilities() {
        return FXCollections.unmodifiableObservableList(abilities);
    }
}
