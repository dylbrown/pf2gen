package model.abilities.abilitySlots;

import javafx.beans.property.ReadOnlyObjectProperty;
import model.abilities.Ability;
import model.data_managers.FeatsManager;
import model.enums.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ui.Main.character;

public class FeatSlot extends AbilitySlot implements Choice<Ability> {
    private final List<Type> allowedTypes;

    public FeatSlot(String name, int level, List<Type> allowedTypes) {
        super(name, level);
        this.allowedTypes = allowedTypes;
    }

    public List<Type> getAllowedTypes() {
        return Collections.unmodifiableList(allowedTypes);
    }

    private List<Ability> getOptions(int level) {
        List<Ability> results = new ArrayList<>();
        for (Type allowedType : allowedTypes) {
            switch(allowedType) {
                case Class:
                    if(character.getPClass() != null)
                        results.addAll(character.getPClass().getFeats(level));
                    break;
                case Ancestry:
                    if(character.getAncestry() != null)
                        results.addAll(character.getAncestry().getFeats(level));
                    break;
                case Heritage:
                    if(character.getAncestry() != null)
                        results.addAll(character.getAncestry().getHeritages());
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

    @Override
    public List<Ability> getOptions() {
        return getOptions(getLevel());
    }

    @Override
    public void fill(Ability choice) {
        currentAbility.set(choice);
    }

    @Override
    public Ability getChoice() {
        return currentAbility.get();
    }

    @Override
    public ReadOnlyObjectProperty<Ability> getChoiceProperty() {
        return currentAbility.getReadOnlyProperty();
    }

    @Override
    public void empty() {
        currentAbility =null;
    }

}
