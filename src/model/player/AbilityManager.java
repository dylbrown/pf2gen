package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.abc.Ancestry;
import model.abc.PClass;
import model.abilities.Ability;
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
    private final ReadOnlyObjectProperty<Ancestry> ancestry;
    private final ReadOnlyObjectProperty<PClass> pClass;
    private final DecisionManager decisions;

    public AbilityManager(ReadOnlyObjectProperty<Ancestry> ancestry, ReadOnlyObjectProperty<PClass> pClass, DecisionManager decisions) {
        this.ancestry = ancestry;
        this.pClass = pClass;
        this.decisions = decisions;
    }

    public List<Ability> getOptions(Choice<Ability> choice) {
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

    public void add(AbilitySlot slot) {
        abilities.add(slot.getCurrentAbility());
    }

    public void remove(AbilitySlot slot) {
        abilities.remove(slot.getCurrentAbility());
        if(slot instanceof Choice)
            decisions.remove((Choice) slot);
        if(slot.isPreSet()) {
            remove(slot);
        }
    }

    public void addDecision(Choice slot) {
        decisions.add(slot);
    }

    public void removeDecision(Choice slot) {
        decisions.remove(slot);
        slot.empty();
    }

    public ObservableList<Ability> getAbilities() {
        return FXCollections.unmodifiableObservableList(abilities);
    }
}
