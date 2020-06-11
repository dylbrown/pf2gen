package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import model.abc.Ancestry;
import model.abc.PClass;
import model.abilities.Ability;
import model.abilities.AbilitySet;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.FeatSlot;
import model.abilities.abilitySlots.FilledSlot;
import model.abilities.abilitySlots.SingleChoice;
import model.data_managers.sources.SourcesLoader;
import model.enums.Type;

import java.util.*;
import java.util.function.Function;

public class AbilityManager {
	private final ObservableList<Ability> abilities = FXCollections.observableArrayList();
	private final Map<String, Set<Ability>> prereqGivers = new HashMap<>();
	private final Map<String, Set<Ability>> needsPrereqStrings = new HashMap<>(); // For Prereq Strings
	private final Map<String, Set<Ability>> needsPrerequisites = new HashMap<>(); // For Prerequisite Abilities
	private final Map<Ability, Boolean> isApplied = new HashMap<>();
	private final SortedList<Ability> sortedAbilities;
	private final ObservableList<AbilitySet> abilitySets = FXCollections.observableArrayList();
	private final Map<Type, Set<Ability>> abcTracker = new HashMap<>();
	private final DecisionManager decisions;
	private final Applier applier;
	private final ReadOnlyObjectProperty<Ancestry> ancestry;
	private final ReadOnlyObjectProperty<PClass> pClass;
	private final ReadOnlyObjectProperty<Integer> level;
	private final Function<Ability, Boolean> meetsPrereqs;

	AbilityManager(DecisionManager decisions, ReadOnlyObjectProperty<Ancestry> ancestry,
	               ReadOnlyObjectProperty<PClass> pClass,
	               ReadOnlyObjectProperty<Integer> level, Applier applier,
	               Function<Ability, Boolean> meetsPrereqs) {
		this.applier = applier;
		this.decisions = decisions;
		this.ancestry = ancestry;
		this.pClass = pClass;
		this.level = level;
		this.meetsPrereqs = meetsPrereqs;
		sortedAbilities = new SortedList<>(abilities);

		level.addListener(((o, newVal, oldVal) -> {
			for (AbilitySet abilitySet : abilitySets) {
				for (Ability ability : abilitySet.getAbilities()) {
					if (!meetsPrereqs.apply(ability) && haveAbility(ability))
						remove(ability);
					if (meetsPrereqs.apply(ability) && !haveAbility(ability))
						apply(ability);
				}

			}
		}));
	}

	public List<Ability> getOptions(SingleChoice<Ability> choice) {
		if (choice instanceof FeatSlot) {
			List<Ability> results = new ArrayList<>();
			for (String allowedType : ((FeatSlot) choice).getAllowedTypes()) {
				switch (allowedType) {
					case "Class":
						if (pClass.get() != null)
							results.addAll(pClass.get().getFeats(((FeatSlot) choice).getLevel()));
						break;
					case "Ancestry":
						if (ancestry.get() != null)
							results.addAll(ancestry.get().getFeats(((FeatSlot) choice).getLevel()));
						break;
					case "Heritage":
						if (ancestry.get() != null)
							results.addAll(ancestry.get().getHeritages());
						break;
					default:
						results.addAll(SourcesLoader.instance().find("Core Rulebook").getFeats().getCategory(allowedType).values());
						break;
				}
			}
			return results;
		}
		return Collections.emptyList();
	}

	void apply(AbilitySlot slot) {
		if (slot instanceof SingleChoice)
			//noinspection rawtypes
			decisions.add((SingleChoice) slot);

		slot.currentAbilityProperty().addListener((o, oldVal, newVal)->{
			remove(oldVal);
			apply(newVal);
		});

		Ability ability = slot.getCurrentAbility();

		if (slot instanceof FilledSlot) {
			for (String s : ability.getPrereqStrings()) {
				needsPrereqStrings.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).add(ability);
			}
			for (String s : ability.getPrerequisites()) {
				needsPrerequisites.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).add(ability);
			}
			isApplied.put(ability, meetsPrereqs.apply(ability));
			if (isApplied.get(ability)) apply(ability);
		} else apply(ability);
	}

	private void apply(Ability ability) {
		if (ability != null) {
			applier.apply(ability);
			for (String s : ability.getGivenPrerequisites()) {
				prereqGivers.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).add(ability);
				checkAPrereq(s, needsPrereqStrings);
			}
			checkAPrereq(ability.getName(), needsPrerequisites);
			if (ability.getType() != Type.None)
				abcTracker.computeIfAbsent(ability.getType(), (key) -> new HashSet<>()).add(ability);
			if (ability instanceof AbilitySet) {
				abilitySets.add((AbilitySet) ability);
				List<Ability> subAbilities = ((AbilitySet) ability).getAbilities();
				for (Ability subAbility : subAbilities) {
					if (subAbility.getLevel() <= level.get())
						apply(subAbility);
				}
			} else {
				for (AbilitySlot subSlot : ability.getAbilitySlots()) {
					apply(subSlot);
				}
				abilities.add(ability);
			}
		}
	}

	private void checkAPrereq(String prereq, Map<String, Set<Ability>> mapOfChoice) {
		if(prereq == null || mapOfChoice == null) return;
		if (mapOfChoice.get(prereq.toLowerCase()) == null) return;
		for (Ability ability : mapOfChoice.get(prereq.toLowerCase())) {
			if (isApplied.get(ability)) {
				if (!meetsPrereqs.apply(ability)) {
					isApplied.put(ability, false);
					remove(ability);
				}
			} else {
				if (meetsPrereqs.apply(ability)) {
					isApplied.put(ability, true);
					apply(ability);
				}
			}
		}
	}

	void remove(AbilitySlot slot) {
		Ability ability = slot.getCurrentAbility();
		if (slot instanceof FilledSlot) {
			for (String s : ability.getPrereqStrings()) {
				needsPrereqStrings.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).remove(ability);
			}
			for (String s : ability.getPrerequisites()) {
				needsPrerequisites.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).remove(ability);
			}
			if (isApplied.get(ability) != null && isApplied.get(ability)) remove(ability);
			isApplied.remove(ability);
		} else remove(ability);

		if (slot instanceof SingleChoice) {
			//noinspection rawtypes
			decisions.remove((SingleChoice) slot);
			//noinspection rawtypes
			((SingleChoice) slot).empty();
		}
	}

	private void remove(Ability ability) {
		if (ability != null) {
			applier.remove(ability);
			for (String s : ability.getGivenPrerequisites()) {
				prereqGivers.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).remove(ability);
				checkAPrereq(s, needsPrereqStrings);
			}
			checkAPrereq(ability.getName(), needsPrerequisites);
			if (ability.getType() != Type.None)
				abcTracker.computeIfAbsent(ability.getType(), (key) -> new HashSet<>()).remove(ability);
			if (ability instanceof AbilitySet) {
				abilitySets.remove(ability);
				List<Ability> subAbilities = ((AbilitySet) ability).getAbilities();
				for (Ability subAbility : subAbilities) {
					if (haveAbility(subAbility))
						remove(subAbility);
				}
			} else {
				abilities.remove(ability);
				for (AbilitySlot subSlot : ability.getAbilitySlots()) {
					remove(subSlot);
				}
			}
		}
	}

	public ObservableList<Ability> getAbilities() {
		return FXCollections.unmodifiableObservableList(abilities);
	}

	void removeAll(Type type) {
		if (abcTracker.get(type) != null) {
			Iterator<Ability> iterator = abcTracker.get(type).iterator();

			while (iterator.hasNext()) {
				Ability ability = iterator.next();
				iterator.remove();
				remove(ability);
			}
		}
	}

	public boolean haveAbility(Ability ability) {
		return sortedAbilities.contains(ability);
	}

	boolean meetsPrerequisite(String prereq, boolean isAbilityName) {
		if (isAbilityName) {
			for (Ability charAbility : getAbilities()) {
				if (charAbility != null && charAbility.toString().toLowerCase().trim().equals(
						prereq.toLowerCase().trim())) {
					return true;
				}
			}
			return false;
		} else {
			return prereqGivers.get(prereq.toLowerCase()) != null && prereqGivers.get(prereq.toLowerCase()).size() > 0;
		}
	}
}
