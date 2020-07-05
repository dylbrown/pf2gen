package model.player;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import model.abilities.Ability;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class Applier {
	private final List<Consumer<Ability>> applyFns = new ArrayList<>();
	private final List<Consumer<Ability>> removeFns = new ArrayList<>();
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	private BooleanProperty isLooping = new SimpleBooleanProperty(false);
	private Ability mostRecentAbility = null;

	void apply(Ability ability) {
		isLooping.setValue(true);
		for (Consumer<Ability> applyFn : applyFns) {
			applyFn.accept(ability);
		}
		mostRecentAbility = ability;
		isLooping.setValue(false);
	}

	void remove(Ability ability) {
		isLooping.setValue(true);
		for (Consumer<Ability> removeFn : removeFns) {
			removeFn.accept(ability);
		}
		mostRecentAbility = ability;
		isLooping.setValue(false);
	}

	void onApply(Consumer<Ability> consumer) {
		if(!isLooping.get())
			applyFns.add(consumer);
		else {
			Property<ChangeListener<Boolean>> listener = new SimpleObjectProperty<>();
			listener.setValue((o, oldVal, newVal) -> {
				if (!newVal) {
					applyFns.add(consumer);
					consumer.accept(mostRecentAbility);
					isLooping.removeListener(listener.getValue());
				}
			});
			isLooping.addListener(listener.getValue());
		}
	}

	void onRemove(Consumer<Ability> consumer) {
		if(!isLooping.get())
			removeFns.add(consumer);
		else {
			Property<ChangeListener<Boolean>> listener = new SimpleObjectProperty<>();
			listener.setValue((o, oldVal, newVal) -> {
				if (!newVal) {
					removeFns.add(consumer);
					consumer.accept(mostRecentAbility);
					isLooping.removeListener(listener.getValue());
				}
			});
			isLooping.addListener(listener.getValue());
		}
	}
}
