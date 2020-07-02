package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import model.abc.Ancestry;
import model.abc.PClass;
import model.abilities.Ability;
import model.abilities.AbilitySetExtension;
import model.abilities.ArchetypeExtension;
import model.ability_slots.*;
import model.data_managers.sources.SourcesLoader;
import model.enums.Type;
import model.util.StringUtils;

import java.util.*;
import java.util.function.Function;

public class AbilityManager {
	private final ObservableList<Ability> abilities = FXCollections.observableArrayList();
	private final Map<String, Set<Ability>> prereqGivers = new HashMap<>();
	private final Map<String, Set<Ability>> needsPrereqStrings = new HashMap<>(); // For Prereq Strings
	private final Map<String, Set<Ability>> needsPrerequisites = new HashMap<>(); // For Prerequisite Abilities
	private final Map<Ability, Boolean> isApplied = new HashMap<>();
	private final SortedList<Ability> sortedAbilities;
	private final ObservableList<AbilitySetExtension> abilitySetExtensions = FXCollections.observableArrayList();
	private final Map<Type, Set<Ability>> abcTracker = new HashMap<>();
	private final DecisionManager decisions;
	private final Applier applier;
	private final ReadOnlyObjectProperty<Ancestry> ancestry;
	private final ReadOnlyObjectProperty<PClass> pClass;
	private final Function<Ability, Boolean> meetsPrereqs;
	private final Map<String, Set<Ability>> archetypeAbilities = new HashMap<>();
	private final Map<String, Ability> archetypeDedications = new HashMap<>();

	AbilityManager(DecisionManager decisions, ReadOnlyObjectProperty<Ancestry> ancestry,
	               ReadOnlyObjectProperty<PClass> pClass, Applier applier,
	               Function<Ability, Boolean> meetsPrereqs) {
		this.applier = applier;
		this.decisions = decisions;
		this.ancestry = ancestry;
		this.pClass = pClass;
		this.meetsPrereqs = meetsPrereqs;
		sortedAbilities = new SortedList<>(abilities);

		pClass.addListener((o, oldVal, newVal)->{
			Ability ability = archetypeDedications.get(StringUtils.clean(newVal.getName()));
			if(ability != null) {
				remove(ability, false);
			}
		});
	}

	// TODO: Replace this with a listener-type system, this is horribly slow
	private void checkAbilitySets() {
		boolean check = true;
		while (check) {
			check = false;
			for (AbilitySetExtension abilitySetExtension : abilitySetExtensions) {
				for (Ability ability : abilitySetExtension.getAbilities()) {
					if (!meetsPrereqs.apply(ability) && haveAbility(ability)) {
						check = true;
						remove(ability, true);
					}
					if (meetsPrereqs.apply(ability) && !haveAbility(ability)) {
						check = true;
						apply(ability, true);
					}
				}
			}
		}
	}

	public List<Ability> getOptions(SingleChoice<Ability> choice) {
		if (choice instanceof FeatSlot) {
			List<Ability> results = new ArrayList<>();
			for (String allowedType : ((FeatSlot) choice).getAllowedTypes()) {
				int end = allowedType.indexOf(" Feat");
				if(end == -1) end = allowedType.length();
				switch (allowedType.substring(0, end)) {
					case "Class":
						int bracket = allowedType.indexOf("(");
						if (bracket != -1) {
							int close = allowedType.indexOf(")");
							String className = allowedType.substring(bracket + 1, close);
							PClass specificClass = SourcesLoader.instance().classes().find(className);
							if(specificClass != null)
								results.addAll(specificClass.getFeats(((FeatSlot) choice).getLevel()));
						}else if (pClass.get() != null)
							results.addAll(pClass.get().getFeats(((FeatSlot) choice).getLevel()));
							results.addAll(SourcesLoader.instance().feats().getCategory("Archetype").values());
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
						results.addAll(SourcesLoader.instance().feats().getCategory(allowedType).values());
						break;
				}
			}
			return results;
		}
		return Collections.emptyList();
	}

	void apply(AbilitySlot slot) {
		apply(slot, false);
	}

	private void apply(AbilitySlot slot, boolean isNestedCall) {
		if (slot instanceof AbilitySingleChoice) {
			decisions.add((AbilitySingleChoice) slot);
		}

		slot.currentAbilityProperty().addListener((o, oldVal, newVal)->{
			remove(oldVal, false);
			apply(newVal, isNestedCall);
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
			if (isApplied.get(ability)) apply(ability, isNestedCall);
		} else apply(ability, isNestedCall);
	}

	private void apply(Ability ability, boolean isNestedCall) {
		if (ability != null) {
			applier.apply(ability);
			for (String s : ability.getGivenPrerequisites()) {
				prereqGivers.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).add(ability);
				checkAPrereq(s, needsPrereqStrings);
			}
			checkAPrereq(ability.getName(), needsPrerequisites);
			if (ability.getType() != Type.None)
				abcTracker.computeIfAbsent(ability.getType(), (key) -> new HashSet<>()).add(ability);

			ArchetypeExtension archetypeExt = ability.getExtension(ArchetypeExtension.class);
			if(archetypeExt != null) {
				String archetypeName = StringUtils.clean(archetypeExt.getArchetype());
				if(archetypeExt.isDedication()) {
				archetypeDedications.put(archetypeName, ability);
				}else {
					archetypeAbilities.computeIfAbsent(
							archetypeName,
							s -> new HashSet<>())
							.add(ability);
				}
			}

			AbilitySetExtension abilitySetExt = ability.getExtension(AbilitySetExtension.class);
			if (abilitySetExt != null) {
				abilitySetExtensions.add(abilitySetExt);
				List<Ability> subAbilities = abilitySetExt.getAbilities();
				for (Ability subAbility : subAbilities) {
					if (meetsPrereqs.apply(subAbility))
						apply(subAbility, true);
				}
			}

			for (AbilitySlot subSlot : ability.getAbilitySlots()) {
				apply(subSlot, true);
			}
			if(ability.toString() == null)
				System.out.println("Warning - Ability with no name");
			abilities.add(ability);
			if(!isNestedCall)
				checkAbilitySets();
		}
	}

	private void checkAPrereq(String prereq, Map<String, Set<Ability>> mapOfChoice) {
		if(prereq == null || mapOfChoice == null) return;
		if (mapOfChoice.get(prereq.toLowerCase()) == null) return;
		for (Ability ability : mapOfChoice.get(prereq.toLowerCase())) {
			if (isApplied.get(ability)) {
				if (!meetsPrereqs.apply(ability)) {
					isApplied.put(ability, false);
					remove(ability, true);
				}
			} else {
				if (meetsPrereqs.apply(ability)) {
					isApplied.put(ability, true);
					apply(ability, true);
				}
			}
		}
		checkAbilitySets();
	}

	void remove(AbilitySlot slot) {
		remove(slot, false);
	}

	private void remove(AbilitySlot slot, boolean isNestedCall) {
		Ability ability = slot.getCurrentAbility();
		if (slot instanceof FilledSlot) {
			for (String s : ability.getPrereqStrings()) {
				needsPrereqStrings.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).remove(ability);
			}
			for (String s : ability.getPrerequisites()) {
				needsPrerequisites.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).remove(ability);
			}
			if (isApplied.get(ability) != null && isApplied.get(ability)) remove(ability, isNestedCall);
			isApplied.remove(ability);
		} else remove(ability, isNestedCall);

		if (slot instanceof SingleChoice) {
			//noinspection rawtypes
			decisions.remove((SingleChoice) slot);
			//noinspection rawtypes
			((SingleChoice) slot).empty();
		}
	}

	private void remove(Ability ability, boolean isNestedCall) {
		if (ability != null) {
			applier.remove(ability);
			for (String s : ability.getGivenPrerequisites()) {
				prereqGivers.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).remove(ability);
				checkAPrereq(s, needsPrereqStrings);
			}
			checkAPrereq(ability.getName(), needsPrerequisites);
			if (ability.getType() != Type.None)
				abcTracker.computeIfAbsent(ability.getType(), (key) -> new HashSet<>()).remove(ability);


			ArchetypeExtension archetypeExt = ability.getExtension(ArchetypeExtension.class);
			if(archetypeExt != null) {
				String archetypeName = StringUtils.clean(archetypeExt.getArchetype());
				if(archetypeExt.isDedication()) {
					archetypeDedications.remove(archetypeName);
				}else {
					archetypeAbilities.computeIfAbsent(
							archetypeName,
							s -> new HashSet<>())
							.remove(ability);
				}
			}

			AbilitySetExtension abilitySetExt = ability.getExtension(AbilitySetExtension.class);
			if (abilitySetExt != null) {
				abilitySetExtensions.remove(abilitySetExt);
				List<Ability> subAbilities = (abilitySetExt).getAbilities();
				for (Ability subAbility : subAbilities) {
					if (haveAbility(subAbility))
						remove(subAbility, true);
				}
			}

			abilities.remove(ability);
			for (AbilitySlot subSlot : ability.getAbilitySlots()) {
				remove(subSlot, true);
			}
			if(!isNestedCall)
				checkAbilitySets();
		}
	}
	private final ObservableList<Ability> abilitiesUnmod = FXCollections.unmodifiableObservableList(abilities);
	public ObservableList<Ability> getAbilities() {
		return abilitiesUnmod;
	}

	void removeAll(Type type) {
		if (abcTracker.get(type) != null) {
			Iterator<Ability> iterator = abcTracker.get(type).iterator();

			while (iterator.hasNext()) {
				Ability ability = iterator.next();
				iterator.remove();
				remove(ability, true);
				checkAbilitySets();
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

	public boolean meetsPrerequisites(ArchetypeExtension archetypeExt) {
		if(archetypeExt.isDedication()) {
			for (Set<Ability> abilities : archetypeAbilities.values()) {
				if(abilities.size() < 2) return false;
			}
			return !StringUtils.clean(pClass.get().getName()).equals(
					StringUtils.clean(archetypeExt.getArchetype()));
		}
		return true;
	}

	public void reset() {

	}
}
