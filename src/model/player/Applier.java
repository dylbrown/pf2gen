package model.player;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import model.abilities.Ability;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class Applier {
	private final List<Consumer<Ability>> preApplyFns = new ArrayList<>();
	private final List<Consumer<Ability>> applyFns = new ArrayList<>();
	private final List<Consumer<Ability>> preRemoveFns = new ArrayList<>();
	private final List<Consumer<Ability>> removeFns = new ArrayList<>();
	private final BooleanProperty isLooping = new SimpleBooleanProperty(false);
	private Ability mostRecentAbility = null;

	void preApply(Ability ability) {
		process(ability, preApplyFns);
	}

	void apply(Ability ability) {
		process(ability, applyFns);
	}

	void remove(Ability ability) {
		process(ability, removeFns);
	}

	private void process(Ability ability, List<Consumer<Ability>> functions) {
		isLooping.setValue(true);
		for (Consumer<Ability> function : functions) {
			function.accept(ability);
		}
		mostRecentAbility = ability;
		isLooping.setValue(false);
	}

	void onPreApply(Consumer<Ability> consumer) {
		addListener(consumer, preApplyFns);
	}

	void onApply(Consumer<Ability> consumer) {
		addListener(consumer, applyFns);
	}

	void onPreRemove(Consumer<Ability> consumer) {
		addListener(consumer, preRemoveFns);
	}

	void onRemove(Consumer<Ability> consumer) {
		addListener(consumer, removeFns);
	}

	private void addListener(Consumer<Ability> consumer, List<Consumer<Ability>> functions) {
		if(!isLooping.get())
			functions.add(consumer);
		else {
			Property<ChangeListener<Boolean>> listener = new SimpleObjectProperty<>();
			listener.setValue((o, oldVal, newVal) -> {
				if (!newVal) {
					functions.add(consumer);
					consumer.accept(mostRecentAbility);
					isLooping.removeListener(listener.getValue());
				}
			});
			isLooping.addListener(listener.getValue());
		}
	}
}
