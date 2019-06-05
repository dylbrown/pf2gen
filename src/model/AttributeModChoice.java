package model;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import model.abilities.abilitySlots.ChoiceList;
import model.enums.Attribute;
import model.enums.Proficiency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttributeModChoice extends AttributeMod implements ChoiceList<Attribute> {
    private final List<Attribute> choices = new ArrayList<>();
    private ReadOnlyObjectWrapper<Attribute> choiceProperty = new ReadOnlyObjectWrapper<>();

    public AttributeModChoice(Attribute first, Attribute second, Proficiency prof) {
        super(first,prof);
        this.choices.add(first);
        this.choices.add(second);
    }

    public List<Attribute> getOptions() {
        return Collections.unmodifiableList(choices);
    }

    @Override
    public void fill(Attribute choice) {
        this.choiceProperty.set(choice);
    }

    @Override
    public Attribute getChoice() {
        return choiceProperty.get();
    }

    public ReadOnlyObjectProperty<Attribute> getChoiceProperty() {
        return choiceProperty.getReadOnlyProperty();
    }

    @Override
    public void empty() {
        choiceProperty.set(null);
    }

    @Override
    public String toString() {
        return "Skill Choice";
    }
}
