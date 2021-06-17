package model.player;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class Applier<T> {
	private final List<Consumer<T>> preApplyFns = new ArrayList<>();
	private final List<Consumer<T>> applyFns = new ArrayList<>();
	private final List<Consumer<T>> preRemoveFns = new ArrayList<>();
	private final List<Consumer<T>> removeFns = new ArrayList<>();
	private final BooleanProperty isLooping = new SimpleBooleanProperty(false);
	private T mostRecentT = null;

	void preApply(T t) {
		process(t, preApplyFns);
	}

	void apply(T t) {
		process(t, applyFns);
	}

	void preRemove(T t) {
		process(t, preRemoveFns);
	}

	void remove(T t) {
		process(t, removeFns);
	}

	private void process(T t, List<Consumer<T>> functions) {
		isLooping.setValue(true);
		for (Consumer<T> function : functions) {
			function.accept(t);
		}
		mostRecentT = t;
		isLooping.setValue(false);
	}

	void onPreApply(Consumer<T> consumer) {
		addListener(consumer, preApplyFns);
	}

	void onApply(Consumer<T> consumer) {
		addListener(consumer, applyFns);
	}

	void onPreRemove(Consumer<T> consumer) {
		addListener(consumer, preRemoveFns);
	}

	void onRemove(Consumer<T> consumer) {
		addListener(consumer, removeFns);
	}

	private void addListener(Consumer<T> consumer, List<Consumer<T>> functions) {
		if(!isLooping.get())
			functions.add(consumer);
		else {
			Property<ChangeListener<Boolean>> listener = new SimpleObjectProperty<>();
			listener.setValue((o, oldVal, newVal) -> {
				if (!newVal) {
					functions.add(consumer);
					consumer.accept(mostRecentT);
					isLooping.removeListener(listener.getValue());
				}
			});
			isLooping.addListener(listener.getValue());
		}
	}
}
