package model.player;

import model.abilities.Ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.function.Consumer;

class Applier extends Observable {
	private List<Consumer<Ability>> applyFns = new ArrayList<>();
	private List<Consumer<Ability>> removeFns = new ArrayList<>();

	void apply(Ability ability) {
		for (Consumer<Ability> applyFn : applyFns) {
			applyFn.accept(ability);
		}
	}

	void remove(Ability ability) {
		for (Consumer<Ability> removeFn : removeFns) {
			removeFn.accept(ability);
		}
	}

	void onApply(Consumer<Ability> consumer) {
		applyFns.add(consumer);
	}

	void onRemove(Consumer<Ability> consumer) {
		removeFns.add(consumer);
	}
}
