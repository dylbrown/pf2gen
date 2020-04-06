package model.attributes;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import model.abilities.abilitySlots.SingleChoiceList;
import model.enums.Proficiency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AttributeModSingleChoice extends AttributeMod implements SingleChoiceList<Attribute> {
    private final List<Attribute> choices = new ArrayList<>();
    private final ReadOnlyObjectWrapper<Attribute> choiceProperty = new ReadOnlyObjectWrapper<>();

    public AttributeModSingleChoice(Attribute first, Attribute second, Proficiency prof) {
        super(Attribute.None,prof);
        this.choices.add(first);
        this.choices.add(second);
    }

    public AttributeModSingleChoice(Attribute[] options, Proficiency prof) {
        super(Attribute.None,prof);
        this.choices.addAll(Arrays.asList(options));
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
    public int getLevel() {
        return 0;
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
