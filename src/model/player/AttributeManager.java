package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import model.WeaponGroupMod;
import model.WeaponMod;
import model.abilities.MinimumProficiencyList;
import model.attributes.*;
import model.enums.Proficiency;
import model.enums.Type;
import model.equipment.weapons.Weapon;
import model.equipment.weapons.WeaponGroup;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * @author Dylan Brown
 */
public class AttributeManager {
    private final Map<Attribute, ReadOnlyObjectWrapper<Proficiency>> proficiencies = new HashMap<>();
    private final Map<Attribute, Map<Proficiency, List<AttributeMod>>> proficienciesTracker = new HashMap<>();
    private final Map<String, ReadOnlyObjectWrapper<Proficiency>> loreProficiencies = new HashMap<>();
    private final Map<String, Map<Proficiency, List<AttributeMod>>> loreProficienciesTracker = new HashMap<>();

    //Map from level to selected skills`
    private final SortedMap<Integer, Set<SkillIncrease>> skillChoices = new TreeMap<>();

    // Maps from level to number of increases
    private final SortedMap<Integer, Integer> innerSkillIncreases = new TreeMap<>();
    private final ObservableMap<Integer, Integer> skillIncreases = FXCollections.observableMap(innerSkillIncreases);

    private final Map<Proficiency, MinimumProficiencyList> minLists = new HashMap<>();
    private final ReadOnlyObjectProperty<Integer> level;
    private final DecisionManager decisions;
    private final Map<String, Proficiency> weaponProficiencies = new HashMap<>();
    private final List<String> customWeaponStrings = new ArrayList<>();
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

        minLists.put(Proficiency.Trained, new MinimumProficiencyList(Collections.unmodifiableMap(proficiencies),
                Proficiency.Trained));
        minLists.put(Proficiency.Expert, new MinimumProficiencyList(Collections.unmodifiableMap(proficiencies),
                Proficiency.Expert));
        minLists.put(Proficiency.Master, new MinimumProficiencyList(Collections.unmodifiableMap(proficiencies),
                Proficiency.Master));
        minLists.put(Proficiency.Legendary, new MinimumProficiencyList(Collections.unmodifiableMap(proficiencies),
                Proficiency.Legendary));

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
            decisions.add(choice);
            if(!choices.contains(mod)) {
                choices.add(choice);
                choice.getChoiceProperty().addListener((observable, oldVal, newVal)->{
                    remove(new AttributeMod(oldVal, mod.getMod()));
                    apply(new AttributeMod(newVal, mod.getMod()));
                });
            }
            return;
        }
        boolean lore = mod.getAttr().equals(Attribute.Lore) && mod.getData() != null && mod.getData().length() > 0;
        ReadOnlyObjectWrapper<Proficiency> proficiency = (lore) ? loreProficiencies.get(mod.getData()) : proficiencies.get(mod.getAttr());
        Map<Proficiency, List<AttributeMod>> listMap;
        if(!lore) {
            listMap = proficienciesTracker.computeIfAbsent(mod.getAttr(), (key) -> new HashMap<>());
        }else{
            listMap = loreProficienciesTracker.computeIfAbsent(mod.getData(), (key) -> new HashMap<>());
        }
        listMap.computeIfAbsent(mod.getMod(), (key) -> new ArrayList<>())
                .add(mod);
        if (proficiency == null) {
            if(lore) loreProficiencies.put(mod.getData(), new ReadOnlyObjectWrapper<>(mod.getMod()));
            else proficiencies.put(mod.getAttr(), new ReadOnlyObjectWrapper<>(mod.getMod()));
        } else if (proficiency.getValue() == null || proficiency.getValue().getMod() < mod.getMod().getMod()) {
            proficiency.set(mod.getMod());
        } else if (mod.getMod().equals(Proficiency.Trained)){
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
        if(mod.getAttr() == null) return;
        if(mod instanceof AttributeModSingleChoice) {
            decisions.remove((AttributeModSingleChoice)mod);
            Attribute choice = ((AttributeModSingleChoice) mod).getChoice();
            if(choice != null)
                remove(new AttributeMod(choice, mod.getMod()));
            return;
        }
        boolean lore = mod.getAttr().equals(Attribute.Lore) && mod.getData() != null && mod.getData().length() > 0;
        ReadOnlyObjectWrapper<Proficiency> proficiency = (lore) ? loreProficiencies.get(mod.getData()) : proficiencies.get(mod.getAttr());
        Map<Proficiency, List<AttributeMod>> listMap;
        if(!lore) {
            listMap = proficienciesTracker.computeIfAbsent(mod.getAttr(), (key) -> new HashMap<>());
        }else{
            listMap = loreProficienciesTracker.computeIfAbsent(mod.getData(), (key) -> new HashMap<>());
        }
        List<AttributeMod> attributeMods = listMap.computeIfAbsent(mod.getMod(), (key) -> new ArrayList<>());

        if(!attributeMods.contains(mod)) return;

        attributeMods.remove(mod);
        if (proficiency.get().getMod() == mod.getMod().getMod() && attributeMods.size() == 0) {
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
                    doLoop = doNotHaveProf(listMap, temp);
            }
            proficiency.set(temp);
        } else if(mod.getMod().equals(Proficiency.Trained) && attributeMods.size() > 0) {
            removeSkillIncreases(1, 1);
        }
    }

    /**
     * Check if the map contains the given proficiency
     * @param proficiencyListMap the sub-map of the proficiency tracker
     * @param proficiency the desired proficiency
     * @return true if do not have the desired proficiency
     */
    private boolean doNotHaveProf(Map<Proficiency, List<AttributeMod>> proficiencyListMap, Proficiency proficiency) {
        return proficiencyListMap.computeIfAbsent(proficiency, (key) -> new ArrayList<>()).size() == 0;
    }


    public Map<Attribute, ObservableValue<Proficiency>> getProficiencies() {
        return Collections.unmodifiableMap(proficiencies);
    }

    public ObservableValue<Proficiency> getProficiency(Attribute attr) {
        return getProficiency(attr, "");
    }

    public ObservableValue<Proficiency> getProficiency(Attribute attr, String data) {
        if(attr.equals(Attribute.Lore) && data != null && data.length() > 0)
            return loreProficiencies
                    .computeIfAbsent(data, (key) -> new ReadOnlyObjectWrapper<>(Proficiency.Untrained))
                    .getReadOnlyProperty();
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
        boolean lore = skill.equals(Attribute.Lore) && data != null && data.length() > 0;

        ReadOnlyObjectWrapper<Proficiency> prof = (lore) ?
                loreProficiencies.computeIfAbsent(data, (key)->new ReadOnlyObjectWrapper<>(Proficiency.Untrained))
                : proficiencies.computeIfAbsent(skill, (key)->new ReadOnlyObjectWrapper<>(Proficiency.Untrained));

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
            choices.add(new SkillIncrease(skill, value, entry.getKey(), data));
            prof.setValue(value);
            proficiencyChange.firePropertyChange("skillAdvance", null, null);
            return true;
        }
        return false;
    }

    public boolean canAdvanceSkill(Attribute skill, String data) {
        if(!Arrays.asList(Attribute.getSkills()).contains(skill))
            return false;
        boolean lore = skill.equals(Attribute.Lore) && data != null && data.length() > 0;

        ReadOnlyObjectWrapper<Proficiency> prof = (lore) ?
                loreProficiencies.computeIfAbsent(data, (key)->new ReadOnlyObjectWrapper<>(Proficiency.Untrained))
                : proficiencies.computeIfAbsent(skill, (key)->new ReadOnlyObjectWrapper<>(Proficiency.Untrained));

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
        boolean lore = skill.equals(Attribute.Lore) && data != null && data.length() > 0;

        ReadOnlyObjectWrapper<Proficiency> prof = (lore) ?
                loreProficiencies.computeIfAbsent(data, (key)->new ReadOnlyObjectWrapper<>(Proficiency.Untrained))
                : proficiencies.computeIfAbsent(skill, (key)->new ReadOnlyObjectWrapper<>(Proficiency.Untrained));

        if(prof.getValue() == Proficiency.Untrained) return false;
        Stack<Map.Entry<Integer, Integer>> reverser = new Stack<>();
        for (Map.Entry<Integer, Integer> entry : skillIncreases.entrySet()) {
            reverser.push(entry);
        }
        for (Map.Entry<Integer, Integer> entry : reverser) {
            Set<SkillIncrease> choices = skillChoices.computeIfAbsent(entry.getKey(), (key) -> new HashSet<>());
            if(!choices.contains(new SkillIncrease(skill, prof.getValue(), entry.getKey(), data)))
                continue;
            choices.remove(new SkillIncrease(skill, prof.getValue(), entry.getKey(), data));
            prof.setValue(Proficiency.values()[Arrays.asList(Proficiency.values()).indexOf(prof.getValue())-1]);
            proficiencyChange.firePropertyChange("skillRegress", null, null);
            return true;
        }

        return false;
    }

    public boolean canRegressSkill(Attribute skill, String data) {
        if(!Arrays.asList(Attribute.getSkills()).contains(skill))
            return false;
        boolean lore = skill.equals(Attribute.Lore) && data != null && data.length() > 0;

        ReadOnlyObjectWrapper<Proficiency> prof = (lore) ?
                loreProficiencies.computeIfAbsent(data, (key)->new ReadOnlyObjectWrapper<>(Proficiency.Untrained))
                : proficiencies.computeIfAbsent(skill, (key)->new ReadOnlyObjectWrapper<>(Proficiency.Untrained));

        if(prof.getValue() == Proficiency.Untrained) return false;
        for (Map.Entry<Integer, Integer> entry : skillIncreases.entrySet()) {
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
        weaponProficiencies.put(weaponMod.getWeaponName().toLowerCase(), null);
        if(weaponMod.getWeaponName().contains("{")) {
            customWeaponStrings.remove(weaponMod.getWeaponName());
        }
    }

    void apply(WeaponGroupMod weaponGroupMod) {
        groupProficiencies.merge(weaponGroupMod.getGroup(), weaponGroupMod.getProficiency(), Proficiency::max);
    }
    void remove(WeaponGroupMod weaponGroupMod) {
        groupProficiencies.put(weaponGroupMod.getGroup(), null);
    }

    Proficiency getProficiency(Attribute attr, Weapon weapon) {
        return Proficiency.max(getProficiency(attr, "").getValue(),
                groupProficiencies.getOrDefault(weapon.getGroup(), Proficiency.Untrained),
                getSpecificWeaponProficiency(weapon));
    }

    private Proficiency getSpecificWeaponProficiency(Weapon weapon) {
        for (String customWeaponString : customWeaponStrings) {
            Object o = customGetter.get(customWeaponString);
            if(o instanceof Weapon && weapon.getName().equals(((Weapon) o).getName())) {
                return Proficiency.max(
                        weaponProficiencies.getOrDefault(customWeaponString.toLowerCase(), Proficiency.Untrained),
                        weaponProficiencies.getOrDefault(weapon.getName().toLowerCase(), Proficiency.Untrained)
                );
            }
        }
        return weaponProficiencies.getOrDefault(weapon.getName().toLowerCase(), Proficiency.Untrained);
    }

    public SortedMap<Integer, Set<SkillIncrease>> getSkillChoices() {
        return Collections.unmodifiableSortedMap(skillChoices);
    }

    /**
     * Unmake all skill choices
     */
    public void resetSkills() {
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
        return minLists.get(min);
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
        return loreProficiencies.keySet();
    }
}
