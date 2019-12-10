package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import model.abc.Ancestry;
import model.abc.PClass;
import model.abilities.Ability;
import model.abilities.AbilitySet;
import model.abilities.abilitySlots.AbilitySingleChoice;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.FeatSlot;
import model.abilities.abilitySlots.SingleChoice;
import model.data_managers.FeatsManager;
import model.enums.Type;

import java.util.*;

public class AbilityManager {
    private final ObservableList<Ability> abilities = FXCollections.observableArrayList();
    private final SortedList<Ability> sortedAbilities;
    private final ObservableList<AbilitySet> abilitySets = FXCollections.observableArrayList();
    private final Map<Type, Set<Ability>> abcTracker = new HashMap<>();
    private final DecisionManager decisions;
    private final Applier applier;
    private final ReadOnlyObjectProperty<Ancestry> ancestry;
    private final ReadOnlyObjectProperty<PClass> pClass;
    private final ReadOnlyObjectProperty<Integer> level;

    AbilityManager(DecisionManager decisions, ReadOnlyObjectProperty<Ancestry> ancestry,
                   ReadOnlyObjectProperty<PClass> pClass,
                   ReadOnlyObjectProperty<Integer> level, Applier applier) {
        this.applier = applier;
        this.decisions = decisions;
        this.ancestry = ancestry;
        this.pClass = pClass;
        this.level = level;
        sortedAbilities = new SortedList<>(abilities);

        level.addListener((observable -> {
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
                        if (pClass.get() != null)
                            results.addAll(pClass.get().getFeats(((FeatSlot) choice).getLevel()));
                        break;
                    case Ancestry:
                        if (ancestry.get() != null)
                            results.addAll(ancestry.get().getFeats(((FeatSlot) choice).getLevel()));
                        break;
                    case Heritage:
                        if (ancestry.get() != null)
                            results.addAll(ancestry.get().getHeritages());
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
            applier.apply(ability);
            if(ability.getType() != Type.None)
                abcTracker.computeIfAbsent(ability.getType(), (key)->new HashSet<>()).add(ability);
            if(ability instanceof AbilitySet) {
                abilitySets.add((AbilitySet) ability);
                List<Ability> subAbilities = ((AbilitySet) ability).getAbilities();
                for (Ability subAbility : subAbilities) {
                    if(subAbility.getLevel() <= level.get())
                        apply(subAbility);
                }
            }else {
                for (AbilitySlot subSlot : ability.getAbilitySlots()) {
                    apply(subSlot);
                }
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
                for(AbilitySlot subSlot: ability.getAbilitySlots()) {
                    remove(subSlot);
                }
            }
        }
    }

    public ObservableList<Ability> getAbilities() {
        return FXCollections.unmodifiableObservableList(abilities);
    }

    void removeAll(Type type) {
        if(abcTracker.get(type) != null) {
            Iterator<Ability> iterator = abcTracker.get(type).iterator();

            while(iterator.hasNext()){
                Ability ability = iterator.next();
                iterator.remove();
                remove(ability);
            }
        }
    }

    void changeSlot(AbilitySlot slot, Ability selectedItem) {
        Ability oldItem = slot.getCurrentAbility();
        if(oldItem != null)
            remove(oldItem);
        if(slot instanceof AbilitySingleChoice)
            ((AbilitySingleChoice) slot).fill(selectedItem);
        apply(selectedItem);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean haveAbility(Ability ability) {
        return sortedAbilities.contains(ability);
    }
}
