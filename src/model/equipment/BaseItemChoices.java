package model.equipment;

import model.ability_slots.Choice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseItemChoices extends ItemExtension {
    private final List<Choice<?>> choices;
    protected BaseItemChoices(Builder builder, Item baseItem) {
        super(baseItem);
        this.choices = builder.choices;
    }

    public List<Choice<?>> getChoices() {
        return Collections.unmodifiableList(choices);
    }

    public static class Builder extends ItemExtension.Builder {
        private List<Choice<?>> choices = Collections.emptyList();

        public <T> void addChoice(Choice<T> choice) {
            if(choices.size() == 0) choices = new ArrayList<>();
            choices.add(choice);
        }

        @Override
        public ItemExtension build(Item baseItem) {
            return new BaseItemChoices(this, baseItem);
        }
    }
}
