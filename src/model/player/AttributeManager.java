package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import model.attributes.*;
import model.enums.Proficiency;
import model.enums.Type;
import model.enums.WeaponProficiency;
import model.items.Item;
import model.items.weapons.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author Dylan Brown
 */
public class AttributeManager implements PlayerState {
    private final Map<Attribute, ReadOnlyObjectWrapper<Proficiency>> proficiencies = new HashMap<>();
    private final Map<Attribute, Map<Proficiency, List<AttributeMod>>> trackers = new HashMap<>();
    private final Map<Attribute, SingleAttributeManager> singleAttributes = new HashMap<>();

    //Map from level to selected skills`
    private final SortedMap<Integer, Set<SkillIncrease>> skillChoices = new TreeMap<>();

    // Maps from level to number of increases
    private final SortedMap<Integer, Integer> innerSkillIncreases = new TreeMap<>();
    private final ObservableMap<Integer, Integer> skillIncreases = FXCollections.observableMap(innerSkillIncreases);

    private final Map<Proficiency, ObservableList<String>> minSkillLists = new HashMap<>();
    private final Map<Proficiency, ObservableList<String>> minSaveLists = new HashMap<>();
    private final ReadOnlyObjectProperty<Integer> level;
    private final DecisionManager decisions;
    private final Map<String, Proficiency> weaponProficiencies = new HashMap<>();
    private final List<String> customWeaponStrings = new ArrayList<>();
    private final List<WeaponProficiencyTranslator> weaponProficiencyTranslators = new ArrayList<>();
    private final Map<WeaponGroup, Proficiency> groupProficiencies = new HashMap<>();
    private final PropertyChangeSupport proficiencyChange = new PropertyChangeSupport(this);
    private final List<AttributeModSingleChoice> choices = new ArrayList<>();
    private final Map<Attribute, Map<Type, NavigableSet<AttributeBonus>>> attributeBonuses = new HashMap<>();
    private final CustomGetter customGetter;

    AttributeManager(CustomGetter customGetter, ReadOnlyObjectProperty<Integer> level, DecisionManager decisions, Applier applier){
        this.customGetter = customGetter;
        this.level = level;
        this.decisions = decisions;
        for (Attribute skill : Attribute.getSkills()) {
            proficiencies.put(skill, new ReadOnlyObjectWrapper<>(Proficiency.Untrained));
        }
        singleAttributes.put(Attribute.Lore, new SingleAttributeManager(Attribute.Lore));
        singleAttributes.put(Attribute.ClassDC, new SingleAttributeManager(Attribute.ClassDC));

        Predicate<String> isSkill = s -> {
            for (Attribute skill : Attribute.getSkills())
                if (skill.toString().equals(s))
                    return true;
            return false;
        };
        Predicate<String> isSave = s -> {
            for (Attribute save : Attribute.getSaves())
                if (save.toString().equals(s))
                    return true;
            return false;
        };
        for(Proficiency proficiency : Proficiency.notUntrained()) {
            minSkillLists.put(proficiency,
                    new FilteredList<>(
                            new MinimumProficiencyList(Collections.unmodifiableMap(proficiencies), proficiency),
                            isSkill
                    )
            );
            minSaveLists.put(proficiency,
                    new FilteredList<>(
                            new MinimumProficiencyList(Collections.unmodifiableMap(proficiencies), proficiency),
                            isSave
                    )
            );
        }

        applier.onApply((ability -> {
            addSkillIncreases(ability.getSkillIncreases(), ability.getLevel());
            for (AttributeMod mod : ability.getModifiers()) {
                apply(mod);
            }
        }));

        applier.onRemove((ability -> {
            removeSkillIncreases(ability.getSkillIncreases(), ability.getLevel());
            for (AttributeMod mod : ability.getModifiers()) {
                remove(mod);
            }
        }));
    }

    /**
     * Update the number of initial skill increases
     * @param numSkills the number of initial skill increases
     */
    void updateSkillCount(int numSkills) {
        skillIncreases.put(1,numSkills);
        proficiencyChange.firePropertyChange("skillCount", null, null);
    }


    /**
     * Get Proficiency Wrapper for attribute
     * Note: Internal function to reduce repeated computeIfAbsent
     * @param attribute The attribute to get the wrapper for
     * @return The Attribute's proficiency wrapper
     */
    private ReadOnlyObjectWrapper<Proficiency> get(Attribute attribute) {
        return proficiencies.computeIfAbsent(attribute, t->new ReadOnlyObjectWrapper<>(Proficiency.Untrained));
    }

    /**
     * apply an attribute mod to the character's proficiencies
     * <p>
     *     If the mod is a choice, it adds it to the decisions.
     *     If the mod would increase the character's current proficiency, do so.
     *     Otherwise store it (only if it is a Trained mod) and increase the number of free skill increases by 1
     * </p>
     * @param mod the mod to apply
     */
    void apply(AttributeMod mod) {
        if(mod.getAttr() == null) return;
        if(mod instanceof AttributeModSingleChoice) {
            AttributeModSingleChoice choice = (AttributeModSingleChoice) mod;
            if(!choices.contains(mod)) {
                choices.add(choice);
                choice.getChoiceProperty().addListener((observable, oldVal, newVal)->{
                    remove(new AttributeMod(oldVal, mod.getMod()));
                    apply(new AttributeMod(newVal, mod.getMod()));
                });
            }
            decisions.add(choice);
            return;
        }
        SingleAttributeManager singleManager = singleAttributes.get(mod.getAttr());
        boolean used = false;
        if(singleManager != null)
            used = singleManager.apply(mod);
        else{
            ReadOnlyObjectWrapper<Proficiency> proficiency = get(mod.getAttr());
            trackers
                    .computeIfAbsent(mod.getAttr(), (key) -> new HashMap<>())
                    .computeIfAbsent(mod.getMod(), (key) -> new ArrayList<>())
                    .add(mod);
            if (proficiency.getValue() == null || proficiency.getValue().getMod() < mod.getMod().getMod()) {
                proficiency.set(mod.getMod());
                used = true;
            }
        }
        // Granted proficiencies instead give you a free skill increase if they overlap
        if(!used && mod.getMod().equals(Proficiency.Trained)){
            SkillIncrease skillIncrease = null;
            for (AttributeMod attributeMod : trackers.getOrDefault(mod.getAttr(), Collections.emptyMap())
                    .getOrDefault(Proficiency.Trained, Collections.emptyList())) {
                if(attributeMod instanceof SkillIncrease) {
                    skillIncrease = (SkillIncrease) attributeMod;
                    break;
                }
            }
            if(skillIncrease != null) {
                skillChoices.get(skillIncrease.getLevel()).remove(skillIncrease);
                remove(skillIncrease);
            }
            else
                addSkillIncreases(1, 1);
        }
        proficiencyChange.firePropertyChange("addAttributeMod", null, null);
    }

    /**
     * remove an attribute mod from the character's proficiencies
     * <p>
     *     If the mod is a choice, it removes it from the decisions.
     *     If removing the mod would decrease the character's current proficiency, do so.
     *     Otherwise remove it (only if it is a Trained mod) and decrease the number of free skill increases by 1
     * </p>
     * @param mod the mod to remove
     */
    void remove(AttributeMod mod) {
        if (mod.getAttr() == null) return;
        if (mod instanceof AttributeModSingleChoice) {
            decisions.remove((AttributeModSingleChoice) mod);
            Attribute choice = ((AttributeModSingleChoice) mod).getChoice();
            if (choice != null)
                remove(new AttributeMod(choice, mod.getMod()));
            return;
        }
        SingleAttributeManager singleManager = singleAttributes.get(mod.getAttr());
        boolean changed, haveTrainedMod;
        if (singleManager != null) {
            changed = singleManager.remove(mod);
            haveTrainedMod = singleManager.hasMod(mod.getData(), Proficiency.Trained);
        }else {
            ReadOnlyObjectWrapper<Proficiency> proficiency = get(mod.getAttr());
            Map<Proficiency, List<AttributeMod>> tracker = trackers
                    .computeIfAbsent(mod.getAttr(), (key) -> new HashMap<>());
            List<AttributeMod> attributeMods = tracker
                    .computeIfAbsent(mod.getMod(), (key) -> new ArrayList<>());
            if (!attributeMods.contains(mod)) return;
            attributeMods.remove(mod);
            changed = updateProficiencyToMatchTracker(tracker, proficiency, mod);
            haveTrainedMod = attributeMods.size() > 0;
        }
        if(!changed && mod.getMod().equals(Proficiency.Trained) && haveTrainedMod) {
            if(!(mod instanceof SkillIncrease))
                removeSkillIncreases(1, 1);
        }
        proficiencyChange.firePropertyChange("removeAttributeMod", null, null);
    }

    private void downScale() {
        boolean canSpend = false;
        int minSpendLevel = 0;
        for (Map.Entry<Integer, Integer> entry : skillIncreases.entrySet()) {
            if(getSkillIncreasesRemaining(entry.getKey()) > 0 & !canSpend) {
                canSpend = true;
                minSpendLevel = entry.getKey();
                continue;
            }
            if(canSpend) {
                SkillIncrease increase = null;
outer:            for(int i = minSpendLevel; i < entry.getKey(); i++) {
                    for (SkillIncrease skillIncrease : skillChoices.get(entry.getKey())) {
                        if(skillIncrease.getMod().getMinLevel() <= minSpendLevel) {
                            increase = skillIncrease;
                            minSpendLevel = i;
                            break outer;
                        }
                    }
                }
                if(increase != null) {
                    skillChoices.get(increase.getLevel()).remove(increase);
                    remove(increase);
                    SkillIncrease skillIncrease = new SkillIncrease(increase.getAttr(), increase.getMod(), minSpendLevel, increase.getData());
                    skillChoices.get(minSpendLevel).add(skillIncrease);
                    apply(skillIncrease);
                    downScale();
                    return;
                }
            }
        }
    }

    /**
     * Reduces the proficiency until we have an applied mod for the proficiency level in the tracker
     * @param tracker Map from proficiency to currently applied mods of that proficiency
     * @param proficiency The wrapper which stores the current proficiency
     * @param mod The mod which we have just removed for reference
     * @return returns true if we reduce the proficiency
     */
    static boolean updateProficiencyToMatchTracker(Map<Proficiency, List<AttributeMod>> tracker, ReadOnlyObjectWrapper<Proficiency> proficiency, AttributeMod mod) {
        Proficiency removingMod = mod.getMod();
        if (proficiency.get().getMod() != removingMod.getMod() || haveProf(tracker, removingMod))
            return false;
        boolean doLoop = true;
        Proficiency temp = proficiency.get();
        while (doLoop) {
            switch (temp) {
                case Trained:
                    temp = Proficiency.Untrained;
                    doLoop = false;
                    break;
                case Expert:
                    temp = Proficiency.Trained;
                    break;
                case Master:
                    temp = Proficiency.Expert;
                    break;
                case Legendary:
                    temp = Proficiency.Master;
                    break;
            }
            if (doLoop)
                doLoop = !haveProf(tracker, temp);
        }
        proficiency.set(temp);
        return true;
    }

    /**
     * Check if the map contains the given proficiency
     * @param proficiencyListMap the sub-map of the proficiency tracker
     * @param proficiency the desired proficiency
     * @return true if do not have the desired proficiency
     */
    private static boolean haveProf(Map<Proficiency, List<AttributeMod>> proficiencyListMap, Proficiency proficiency) {
        return proficiencyListMap.computeIfAbsent(proficiency, (key) -> new ArrayList<>()).size() > 0;
    }


    public Map<Attribute, ObservableValue<Proficiency>> getProficiencies() {
        return Collections.unmodifiableMap(proficiencies);
    }

    public ObservableValue<Proficiency> getProficiency(Attribute attr) {
        return getProficiency(attr, "");
    }

    public ObservableValue<Proficiency> getProficiency(Attribute attr, String data) {
        SingleAttributeManager singleAttribute = singleAttributes.get(attr);
        if(singleAttribute != null)
            return singleAttribute.getProficiency(data);
        else return proficiencies
                .computeIfAbsent(attr, (key) -> new ReadOnlyObjectWrapper<>(Proficiency.Untrained))
                .getReadOnlyProperty();
    }

    public void addListener(PropertyChangeListener l) {
        proficiencyChange.addPropertyChangeListener(l);
    }

    public boolean advanceSkill(Attribute skill, String data) {
        if(!Arrays.asList(Attribute.getSkills()).contains(skill))
            return false;

        SingleAttributeManager singleAttribute = singleAttributes.get(skill);

        ObservableValue<Proficiency> prof = (singleAttribute != null) ?
                singleAttribute.getProficiency(data) : getProficiency(skill);

        if(prof.getValue() == Proficiency.Legendary) return false;
        for (Map.Entry<Integer, Integer> entry : skillIncreases.entrySet()) {
            if(prof.getValue() == Proficiency.Trained && entry.getKey() < Proficiency.getMinLevel(Proficiency.Expert))
                continue;
            if(prof.getValue() == Proficiency.Expert && entry.getKey() < Proficiency.getMinLevel(Proficiency.Master))
                continue;
            if(prof.getValue() == Proficiency.Master && entry.getKey() < Proficiency.getMinLevel(Proficiency.Legendary))
                continue;
            Set<SkillIncrease> choices = skillChoices.computeIfAbsent(entry.getKey(), (key) -> new HashSet<>());
            if(entry.getValue() - choices.size() <= 0)
                continue;
            Proficiency value = Proficiency.values()[Arrays.asList(Proficiency.values()).indexOf(prof.getValue()) + 1];
            SkillIncrease increase = new SkillIncrease(skill, value, entry.getKey(), data);
            choices.add(increase);
            apply(increase);
            return true;
        }
        return false;
    }

    public boolean canAdvanceSkill(Attribute skill, String data) {
        if(!Arrays.asList(Attribute.getSkills()).contains(skill))
            return false;

        SingleAttributeManager singleAttribute = singleAttributes.get(skill);

        ObservableValue<Proficiency> prof = (singleAttribute != null) ?
                singleAttribute.getProficiency(data) : getProficiency(skill);

        if(prof.getValue() == Proficiency.Legendary) return false;
        for (Map.Entry<Integer, Integer> entry : skillIncreases.entrySet()) {
            if(prof.getValue() == Proficiency.Trained && entry.getKey() < Proficiency.getMinLevel(Proficiency.Expert))
                continue;
            if(prof.getValue() == Proficiency.Expert && entry.getKey() < Proficiency.getMinLevel(Proficiency.Master))
                continue;
            if(prof.getValue() == Proficiency.Master && entry.getKey() < Proficiency.getMinLevel(Proficiency.Legendary))
                continue;
            Set<SkillIncrease> choices = skillChoices.computeIfAbsent(entry.getKey(), (key) -> new HashSet<>());
            if(entry.getValue() - choices.size() <= 0)
                continue;
            return true;
        }
        return false;
    }

    public boolean regressSkill(Attribute skill, String data) {
        if(!Arrays.asList(Attribute.getSkills()).contains(skill))
            return false;

        SingleAttributeManager singleAttribute = singleAttributes.get(skill);

        ObservableValue<Proficiency> prof = (singleAttribute != null) ?
                singleAttribute.getProficiency(data)
                : getProficiency(skill);

        if(prof.getValue() == Proficiency.Untrained) return false;
        Stack<Map.Entry<Integer, Integer>> reverser = new Stack<>();
        for (Map.Entry<Integer, Integer> entry : skillIncreases.entrySet()) {
            reverser.push(entry);
        }
        for (Map.Entry<Integer, Integer> entry : reverser) {
            Set<SkillIncrease> choices = skillChoices.computeIfAbsent(entry.getKey(), (key) -> new HashSet<>());
            SkillIncrease increase = new SkillIncrease(skill, prof.getValue(), entry.getKey(), data);
            if(!choices.contains(increase))
                continue;
            choices.remove(increase);
            remove(increase);
            downScale();
            return true;
        }

        return false;
    }

    public boolean canRegressSkill(Attribute skill, String data) {
        if(!Arrays.asList(Attribute.getSkills()).contains(skill))
            return false;

        SingleAttributeManager singleAttribute = singleAttributes.get(skill);

        ObservableValue<Proficiency> prof = (singleAttribute != null) ?
                singleAttribute.getProficiency(data)
                : getProficiency(skill);

        if(prof.getValue() == Proficiency.Untrained) return false;
        Stack<Map.Entry<Integer, Integer>> reverser = new Stack<>();
        for (Map.Entry<Integer, Integer> entry : skillIncreases.entrySet()) {
            reverser.push(entry);
        }
        for (Map.Entry<Integer, Integer> entry : reverser) {
            Set<SkillIncrease> choices = skillChoices.computeIfAbsent(entry.getKey(), (key) -> new HashSet<>());
            if(!choices.contains(new SkillIncrease(skill, prof.getValue(), entry.getKey(), data)))
                continue;
            return true;
        }

        return false;
    }

    void addSkillIncreases(int amount, int level) {
        skillIncreases.put(level, skillIncreases.computeIfAbsent(level, (key) -> 0) + amount);
        proficiencyChange.firePropertyChange("addSkillIncrease", null, null);
    }

    void removeSkillIncreases(int amount, int level) {
        skillIncreases.put(level, skillIncreases.computeIfAbsent(level, (key) -> 1) - amount);
        while(getSkillIncreasesRemaining(level) < 0) {
            SkillIncrease next = skillChoices.get(level).iterator().next();
            regressSkill(next.getAttr(), next.getData());
        }
        proficiencyChange.firePropertyChange("removeSkillIncrease", null, null);
    }

    void apply(WeaponMod weaponMod) {
        weaponProficiencies.merge(weaponMod.getWeaponName().toLowerCase(),
                weaponMod.getProficiency(), Proficiency::max);
        if(weaponMod.getWeaponName().contains("{")) {
            customWeaponStrings.add(weaponMod.getWeaponName());
        }
    }
    void remove(WeaponMod weaponMod) {
        weaponProficiencies.remove(weaponMod.getWeaponName().toLowerCase());
        if(weaponMod.getWeaponName().contains("{")) {
            customWeaponStrings.remove(weaponMod.getWeaponName());
        }
    }

    void apply(WeaponProficiencyTranslator translator) {
        weaponProficiencyTranslators.add(translator);
    }

    void remove(WeaponProficiencyTranslator translator) {
        weaponProficiencyTranslators.remove(translator);
    }

    void apply(WeaponGroupMod weaponGroupMod) {
        groupProficiencies.merge(weaponGroupMod.getGroup(), weaponGroupMod.getProficiency(), Proficiency::max);
    }
    void remove(WeaponGroupMod weaponGroupMod) {
        groupProficiencies.put(weaponGroupMod.getGroup(), null);
    }

    Proficiency getProficiency(WeaponProficiency attr, Item weapon) {
        for (WeaponProficiencyTranslator translator : weaponProficiencyTranslators) {
            attr = translator.apply(weapon, attr);
        }

        return getProficiency(Attribute.valueOf(attr), weapon);
    }

    Proficiency getProficiency(Attribute attr, Item weapon) {
        return Proficiency.max(getProficiency(attr, "").getValue(),
                groupProficiencies.getOrDefault(weapon.getExtension(Weapon.class).getGroup(), Proficiency.Untrained),
                getSpecificWeaponProficiency(weapon));
    }

    private Proficiency getSpecificWeaponProficiency(Item weapon) {
        for (String customWeaponString : customWeaponStrings) {
            Object o = customGetter.get(customWeaponString);
            if(o instanceof Item && weapon.getRawName().equalsIgnoreCase(((Item) o).getRawName())) {
                return Proficiency.max(
                        weaponProficiencies.getOrDefault(customWeaponString.toLowerCase(), Proficiency.Untrained),
                        weaponProficiencies.getOrDefault(weapon.getRawName().toLowerCase(), Proficiency.Untrained)
                );
            }
        }
        return weaponProficiencies.getOrDefault(weapon.getRawName().toLowerCase(), Proficiency.Untrained);
    }

    public SortedMap<Integer, Set<SkillIncrease>> getSkillChoices() {
        return Collections.unmodifiableSortedMap(skillChoices);
    }

    /**
     * Unmake all skill choices
     * @param resetEvent prevents this from being called just anywhere
     */
    public void reset(PC.ResetEvent resetEvent) {
        if(!resetEvent.isActive()) return;
        for(int i=level.get(); i>0; i--) {
            Set<SkillIncrease> attributes = skillChoices.get(i);
            if (attributes != null) {
                while(attributes.size() > 0) {
                    SkillIncrease next = attributes.iterator().next();
                    regressSkill(next.getAttr(), next.getData());
                }
            }
        }
    }

    public ObservableMap<Integer, Integer> getSkillIncreases() {
        return FXCollections.unmodifiableObservableMap(skillIncreases);
    }

    public int getSkillIncreasesRemaining(int level) {
        return skillIncreases.getOrDefault(level, 0)
                - skillChoices.getOrDefault(level, Collections.emptySet()).size();
    }

    ObservableList<String> getMinList(Proficiency min) {
        return minSkillLists.get(min);
    }

    public ObservableList<String> getMinSavesList(Proficiency min) {
        return minSaveLists.get(min);
    }

    public void apply(List<? extends AttributeBonus> bonuses) {
        for (AttributeBonus bonus : bonuses) {
            attributeBonuses
                    .computeIfAbsent(bonus.getTarget(), k->new HashMap<>())
                    .computeIfAbsent(bonus.getSource(), k->new TreeSet<>())
                    .add(bonus);
        }
        proficiencyChange.firePropertyChange("addBonuses", null, null);
    }

    public void remove(List<? extends AttributeBonus> bonuses) {
        for (AttributeBonus bonus : bonuses) {
            Map<Type, NavigableSet<AttributeBonus>> typeMap = attributeBonuses.get(bonus.getTarget());
            if(typeMap == null) continue;
            NavigableSet<AttributeBonus> bonusSet = typeMap.get(bonus.getSource());
            bonusSet.remove(bonus);
            if(bonusSet.size() == 0) typeMap.remove(bonus.getSource());
            if(typeMap.size() == 0) attributeBonuses.remove(bonus.getTarget());
        }
        proficiencyChange.firePropertyChange("removeBonuses", null, null);
    }

    public int getBonus(Attribute attribute) {
        int bonus = 0;
        for (NavigableSet<AttributeBonus> bonusSet : attributeBonuses.getOrDefault(attribute, Collections.emptyMap()).values()) {
            bonus += bonusSet.descendingIterator().next().getBonus();
        }
        return bonus;
    }

    public Set<String> lores() {
        return singleAttributes.get(Attribute.Lore).getDataStrings();
    }
}
