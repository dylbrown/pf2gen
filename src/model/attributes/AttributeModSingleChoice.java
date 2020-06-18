package model.attributes;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.abilities.abilitySlots.SingleChoiceList;
import model.enums.Proficiency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AttributeModSingleChoice extends AttributeMod implements SingleChoiceList<Attribute> {
    private final List<Attribute> choices = new ArrayList<>();
    private final ReadOnlyObjectWrapper<Attribute> choiceProperty = new ReadOnlyObjectWrapper<>();
    private final ObservableList<Attribute> list = FXCollections.observableArrayList();

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
        if(!choices.contains(choice)) return;
        this.choiceProperty.set(choice);
        list.clear();
        if(choice != null)
            list.add(choice);
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
    public String toNiceAttributeString() {
        return choices.stream().map(Attribute::name).collect(Collectors.joining(" or "));
    }

    @Override
    public String getName() {
        return "Attribute Mod Choice";
    }

    @Override
    public void add(Attribute choice) {
        if(list.size() == 0) fill(choice);
    }

    @Override
    public void remove(Attribute choice) {
        if(list.contains(choice)) fill(null);
    }

    @Override
    public void empty() {
        fill(null);
    }

    private ObservableList<Attribute> unmodifiable = FXCollections.unmodifiableObservableList(list);
    @Override
    public ObservableList<Attribute> getSelections() {
        return unmodifiable;
    }

    @Override
    public String toString() {
        return "Skill Choice";
    }


}
