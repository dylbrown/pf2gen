package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.ability_slots.ChoiceList;

public class ArbitraryListChoice<T> extends ArbitraryChoice<T> implements ChoiceList<T> {
    private final ObservableList<T> choices;

    private ArbitraryListChoice(Builder<T> builder) {
        super(builder);
        this.choices = builder.choices;
        choices.addListener((ListChangeListener<T>) change->{
            while(change.next()) {
                for (T t : change.getRemoved()) {
                    remove(t);
                }
            }
        });
    }

    @Override
    public ObservableList<T> getOptions() {
        return FXCollections.unmodifiableObservableList(choices);
    }

    @Override
    public void add(T choice) {
        if(this.choices.contains(choice)) {
            super.add(choice);
        }
    }

    @Override
    public ArbitraryListChoice<T> copy() {
        return new Builder<>(this).build();
    }

    public static class Builder<T> extends ArbitraryChoice.Builder<T> {
        private ObservableList<T> choices;

        public Builder(){}

        public Builder(ArbitraryListChoice<T> choice) {
            super(choice);
            choices = choice.choices;
        }

        public void setChoices(ObservableList<T> choices) {
            this.choices = choices;
        }

        public ArbitraryListChoice<T> build() {
            return new ArbitraryListChoice<>(this);
        }
    }
}
