package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import model.AttributeMod;
import model.abilities.Ability;
import model.abilities.AbilitySet;
import model.abilities.SkillIncrease;
import model.abilities.abilitySlots.AbilitySingleChoice;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.SingleChoice;
import model.abilities.abilitySlots.FeatSlot;
import model.data_managers.FeatsManager;
import model.enums.Type;

import java.util.*;

public class AbilityManager {
    private final ObservableList<Ability> abilities = FXCollections.observableArrayList();
    private final SortedList<Ability> sortedAbilities;
    private final ObservableList<AbilitySet> abilitySets = FXCollections.observableArrayList();
    private final Map<Type, Set<Ability>> abcTracker = new HashMap<>();
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

    public List<Ability> getOptions(SingleChoice<Ability> choice) {
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
        if(slot instanceof SingleChoice)
            decisions.add((SingleChoice) slot);

        Ability ability = slot.getCurrentAbility();

        apply(ability);
    }

    private void apply(Ability ability) {
        if(ability != null) {
            if(ability.getType() != Type.None)
                abcTracker.computeIfAbsent(ability.getType(), (key)->new HashSet<>()).add(ability);
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

        if(slot instanceof SingleChoice){
            decisions.remove((SingleChoice) slot);
            ((SingleChoice) slot).empty();
        }

        Ability ability = slot.getCurrentAbility();
        remove(ability);
    }

    private void remove(Ability ability) {
        if(ability != null) {
            if(ability.getType() != Type.None)
                abcTracker.computeIfAbsent(ability.getType(), (key)->new HashSet<>()).remove(ability);
            if(ability instanceof AbilitySet){
                abilitySets.remove(ability);
                List<Ability> subAbilities = ((AbilitySet) ability).getAbilities();
                for (Ability subAbility : subAbilities) {
                    if(sortedAbilities.contains(subAbility))
                        remove(subAbility);
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

    public void removeAll(Type type) {
        if(abcTracker.get(type) != null) {
            Iterator<Ability> iterator = abcTracker.get(type).iterator();

            while(iterator.hasNext()){
                Ability ability = iterator.next();
                iterator.remove();
                remove(ability);
            }
        }
    }

    public void changeSlot(AbilitySlot slot, Ability selectedItem) {
        Ability oldItem = slot.getCurrentAbility();
        if(oldItem != null)
            remove(oldItem);
        if(slot instanceof AbilitySingleChoice)
            ((AbilitySingleChoice) slot).fill(selectedItem);
        apply(selectedItem);
    }
}
