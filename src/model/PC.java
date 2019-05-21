package model;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import model.abc.Ancestry;
import model.abc.Background;
import model.abc.Class;
import model.abilities.Ability;
import model.abilities.SkillIncrease;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.ChoiceSlot;
import model.abilities.abilitySlots.FeatSlot;
import model.abilities.abilitySlots.Pickable;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.enums.*;
import model.equipment.*;

import java.util.*;

import static model.ability_scores.AbilityScore.*;

public class PC {
    public static final int MAX_LEVEL = 20;
    private Ancestry ancestry;
    private Background background;
    private Class pClass;
    private ReadOnlyObjectWrapper<Integer> level = new ReadOnlyObjectWrapper<>(0);
    private Map<Attribute, ReadOnlyObjectWrapper<Proficiency>> proficiencies = new HashMap<>();
    private ObservableList<AbilitySlot> abilities = FXCollections.observableArrayList();
    private Map<AbilityScore, List<AbilityMod>> abilityScores = new HashMap<>();
    private List<AbilityMod> remaining = new ArrayList<>();
    private String name;
    private final SortedMap<Integer, Set<Attribute>> skillChoices = new TreeMap<>(); //Map from level to selected skills
    private final SortedMap<Integer, Integer> skillIncreases = new TreeMap<>(); // Maps from level to number of increases
    private final List<Language> languages = new ArrayList<>();
    private final ReadOnlyObjectWrapper<Double> money= new ReadOnlyObjectWrapper<>(150.0);
    private final ObservableMap<Equipment, Equipment> inventory = FXCollections.observableHashMap();
    private final Map<Slot, Equipment> equipped = new HashMap<>();

    public PC() {
        abilityScores.computeIfAbsent(Int, (key)-> FXCollections.observableArrayList()).addListener((ListChangeListener<? super AbilityMod>) (event)->{
            if(pClass != null) {
                skillIncreases.put(1, pClass.getSkillIncrease() + getAbilityMod(Int));
                proficiencyChange.wink();
            }
        });
        List<AbilityModChoice> choices = Arrays.asList(
                new AbilityModChoice(Type.Initial),
                new AbilityModChoice(Type.Initial),
                new AbilityModChoice(Type.Initial),
                new AbilityModChoice(Type.Initial)
        );
        abilityScoreChoices.addAll(choices);
        abilityScoresByType.put(Type.Initial, new ArrayList<>(choices));
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void levelUp(){
        if(level.get() == 0)
            return;
        level.set(level.get()+1);
        applyLevel(pClass.getLevel(level.get()));
    }

    public void setAncestry(Ancestry ancestry) {
        if(this.ancestry != null) {
            remove(this.ancestry.getAbilityMods());
            languages.removeAll(ancestry.getLanguages());
        }
        this.ancestry = ancestry;
        apply(ancestry.getAbilityMods());
        languages.addAll(ancestry.getLanguages());
        ancestryWatcher.wink();
    }

    public void setBackground(Background background) {
        if(this.background != null)
            remove(this.background.getAbilityMods());
        this.background = background;
        apply(background.getAbilityMods());
        apply(background.getMod());
        proficiencyChange.wink();
    }

    public void setClass(Class newClass) {
        pClass = newClass;
        applyLevel(pClass.getLevel(1));
        skillIncreases.put(1, pClass.getSkillIncrease() + getAbilityMod(Int));
        proficiencyChange.wink();
    }

    private void removeLevel(List<AbilitySlot> level) {
        for(AbilitySlot slot: level) {
            abilities.remove(slot);
            if(slot instanceof FeatSlot || slot instanceof ChoiceSlot)
                decisions.remove(slot);
            if(slot.isPreSet()) {
                remove(slot);
            }
        }
    }

    private void applyLevel(List<AbilitySlot> level) {
        for(AbilitySlot slot: level) {
            abilities.add(slot);
            if(slot instanceof FeatSlot || slot instanceof ChoiceSlot)
                decisions.add(slot);
            if(slot.isPreSet()) {
                apply(slot);
            }
        }
    }

    private void apply(AbilitySlot slot) {
        if(slot.getCurrentAbility() != null) {
            if (slot.getCurrentAbility() instanceof SkillIncrease) {
                skillIncreases.put(slot.getCurrentAbility().getLevel(), skillIncreases.computeIfAbsent(slot.getCurrentAbility().getLevel(), (key) -> 0) + 1);
                proficiencyChange.wink();
            }

            for (AttributeMod mod : slot.getCurrentAbility().getModifiers()) {
                apply(mod);
            }
            apply(slot.getCurrentAbility().getAbilityMods());
        }
    }

    private void remove(AbilitySlot slot) {
        if(slot.getCurrentAbility() != null) {
            if (slot.getCurrentAbility() instanceof SkillIncrease) {
                skillIncreases.put(slot.getCurrentAbility().getLevel(), skillIncreases.computeIfAbsent(slot.getCurrentAbility().getLevel(), (key) -> 1) - 1);
                proficiencyChange.wink();
            }

            for (AttributeMod mod : slot.getCurrentAbility().getModifiers()) {
                remove(mod);
            }

            remove(slot.getCurrentAbility().getAbilityMods());
        }
    }

    public Map<Attribute, ObservableValue<Proficiency>> getProficiencies() {
        return Collections.unmodifiableMap(proficiencies);
    }

    public ObservableValue<Proficiency> getProficiency(Attribute attr) {
        return proficiencies.computeIfAbsent(attr, (key) -> new ReadOnlyObjectWrapper<>(Proficiency.Untrained)).getReadOnlyProperty();
    }

    private void apply(AttributeMod mod) {
        ReadOnlyObjectWrapper<Proficiency> proficiency = proficiencies.get(mod.getAttr());
        proficienciesTracker.computeIfAbsent(mod.getAttr(), (key)->new HashMap<>()).computeIfAbsent(mod.getMod(), (key)->new ArrayList<>()).add(mod);
        if(proficiency == null) {
            proficiencies.put(mod.getAttr(), new ReadOnlyObjectWrapper<>(mod.getMod()));
        }else if(proficiency.getValue() == null || proficiency.getValue().getMod() < mod.getMod().getMod()) {
            proficiency.set(mod.getMod());
        }
    }

    private void remove(AttributeMod mod) {
        ReadOnlyObjectWrapper<Proficiency> proficiency = proficiencies.get(mod.getAttr());
        Map<Proficiency, List<AttributeMod>> proficiencyListMap = proficienciesTracker.computeIfAbsent(mod.getAttr(), (key) -> new HashMap<>());
        List<AttributeMod> attributeMods = proficiencyListMap.computeIfAbsent(mod.getMod(), (key) -> new ArrayList<>());
        attributeMods.remove(mod);
        if(proficiency.get().getMod() == mod.getMod().getMod() && attributeMods.size() == 0) {
            boolean doLoop=true;
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
                if(doLoop)
                    doLoop = removeHelper(proficiencyListMap, temp);
            }
            proficiency.set(temp);
        }
    }

    private boolean removeHelper(Map<Proficiency, List<AttributeMod>> proficiencyListMap, Proficiency proficiency) {
        return proficiencyListMap.computeIfAbsent(proficiency, (key) -> new ArrayList<>()).size() > 0;
    }

    public int getHP() {
        return ((ancestry != null) ? ancestry.getHP() : 0) + (((pClass != null) ? pClass.getHP() : 0) + getAbilityMod(Con)) * level.get();
    }

    public int getAbilityMod(AbilityScore ability) {
        return getAbilityScore(ability) / 2  - 5;
    }

    public int getAbilityScore(AbilityScore ability) {
        int score = 10;
        List<Type> stackingCheck = new ArrayList<>();
        for(AbilityMod mod: abilityScores.computeIfAbsent(ability, (key)-> FXCollections.observableArrayList())) {
            if(mod.isPositive() && !stackingCheck.contains(mod.getSource())) {
                score += (score < 18) ? 2 : 1;
                stackingCheck.add(mod.getSource());
            }else if(!mod.isPositive()){
                score -= 2;
            }
        }
        return score;
    }

    private void apply(List<AbilityMod> abilityMods) {
        for(AbilityMod mod: abilityMods) {
            abilityScores.computeIfAbsent(mod.getTarget(), (key)-> FXCollections.observableArrayList()).add(mod);
            abilityScoresByType.computeIfAbsent(mod.getSource(), (key)->new ArrayList<>()).add(mod);
            if(mod instanceof AbilityModChoice)
                abilityScoreChoices.add((AbilityModChoice) mod);
        }
    }

    private void remove(List<AbilityMod> abilityMods) {
        for(AbilityMod mod: abilityMods) {
            List<AbilityMod> mods = abilityScores.get(mod.getTarget());
            mods.remove(mod);
            abilityScoresByType.get(mod.getSource()).remove(mod);
            if(mod instanceof AbilityModChoice)
                abilityScoreChoices.remove(mod);
        }
    }
    public void addProficiencyObserver(Observer o) {
        proficiencyChange.addObserver(o);
    }
    public void addAncestryObserver(Observer o) {
        ancestryWatcher.addObserver(o);
    }

    public List<AbilityModChoice> getFreeScores() {
        return Collections.unmodifiableList(abilityScoreChoices);
    }

    public void choose(AbilityModChoice choice, AbilityScore value) {
        AbilityScore old = choice.getTarget();
        if(choice.pick(value)) {
            abilityScores.computeIfAbsent(old, (key)->FXCollections.observableArrayList()).remove(choice);
            if(value != None && value != Free)
                abilityScores.computeIfAbsent(value, (key)->FXCollections.observableArrayList()).add(choice);
            abilityScoreChange.wink();
        }
    }

    public boolean advanceSkill(Attribute skill) {
        if(!Arrays.asList(Attribute.getSkills()).contains(skill))
            return false;
        ReadOnlyObjectWrapper<Proficiency> prof = proficiencies.get(skill);
        if(prof.getValue() == Proficiency.Legendary) return false;
        for (Map.Entry<Integer, Integer> entry : skillIncreases.entrySet()) {
            if(prof.getValue() == Proficiency.Expert && entry.getKey() < 7)
                continue;
            if(prof.getValue() == Proficiency.Master && entry.getKey() < 15)
                continue;
            Set<Attribute> choices = skillChoices.computeIfAbsent(entry.getKey(), (key) -> new HashSet<>());
            if(entry.getValue() - choices.size() <= 0)
                continue;
            if(choices.contains(skill))
                continue;
            choices.add(skill);
            prof.setValue(Proficiency.values()[Arrays.asList(Proficiency.values()).indexOf(prof.getValue())+1]);
            proficiencyChange.wink();
            return true;
        }
        return false;
    }

    public boolean canAdvanceSkill(Attribute skill) {
        if(!Arrays.asList(Attribute.getSkills()).contains(skill))
            return false;
        ReadOnlyObjectWrapper<Proficiency> prof = proficiencies.get(skill);
        if(prof.getValue() == Proficiency.Legendary) return false;
        for (Map.Entry<Integer, Integer> entry : skillIncreases.entrySet()) {
            if(prof.getValue() == Proficiency.Expert && entry.getKey() < 7)
                continue;
            if(prof.getValue() == Proficiency.Master && entry.getKey() < 15)
                continue;
            Set<Attribute> choices = skillChoices.computeIfAbsent(entry.getKey(), (key) -> new HashSet<>());
            if(entry.getValue() - choices.size() <= 0)
                continue;
            if(choices.contains(skill))
                continue;
            return true;
        }
        return false;
    }

    public ReadOnlyObjectProperty<Integer> getLevel() {
        return level.getReadOnlyProperty();
    }

    public boolean regressSkill(Attribute skill) {
        if(!Arrays.asList(Attribute.getSkills()).contains(skill))
            return false;
        ReadOnlyObjectWrapper<Proficiency> prof = proficiencies.get(skill);
        if(prof.getValue() == Proficiency.Untrained) return false;
        Stack<Map.Entry<Integer, Integer>> reverser = new Stack<>();
        for (Map.Entry<Integer, Integer> entry : skillIncreases.entrySet()) {
            reverser.push(entry);
        }
        for (Map.Entry<Integer, Integer> entry : reverser) {
            Set<Attribute> choices = skillChoices.computeIfAbsent(entry.getKey(), (key) -> new HashSet<>());
            if(!choices.contains(skill))
                continue;
            choices.remove(skill);
            prof.setValue(Proficiency.values()[Arrays.asList(Proficiency.values()).indexOf(prof.getValue())-1]);
            proficiencyChange.wink();
            return true;
        }

        return false;
    }

    public boolean canRegressSkill(Attribute skill) {
        if(!Arrays.asList(Attribute.getSkills()).contains(skill))
            return false;
        ReadOnlyObjectWrapper<Proficiency> prof = proficiencies.get(skill);
        if(prof.getValue() == Proficiency.Untrained) return false;
        for (Map.Entry<Integer, Integer> entry : skillIncreases.entrySet()) {
            Set<Attribute> choices = skillChoices.computeIfAbsent(entry.getKey(), (key) -> new HashSet<>());
            if(!choices.contains(skill))
                continue;
            return true;
        }
        return false;
    }

    public ObservableList<AbilitySlot> getDecisions() {
        return FXCollections.unmodifiableObservableList(decisions);
    }

    public void choose(AbilitySlot slot, Ability selectedItem) {
        if(slot instanceof Pickable) {
            ((Pickable) slot).fill(selectedItem);
            apply(slot);
        }
    }

    public int getAC() {
        Armor armor = (Armor) equipped.get(Slot.Armor);
        if(armor != null)
            return 10 + level.get() + armor.getAC() + Math.max(getAbilityMod(Dex), armor.getMaxDex());
        return 10 + level.get() + getAbilityMod(Dex);
    }

    public int getTAC() {
        Armor armor = (Armor) equipped.get(Slot.Armor);
        if(armor != null)
            return 10 + level.get() + armor.getTAC() + Math.max(getAbilityMod(Dex), armor.getMaxDex());
        return 10 + level.get() + getAbilityMod(Dex);
    }

    public int getTotalMod(Attribute attribute) {
        return level.get()+getAbilityMod(attribute.getKeyAbility())+getProficiency(attribute).getValue().getMod();
    }

    public int getSpeed() {
        return (ancestry != null) ? ancestry.getSpeed() : 0;
    }

    public List<AbilityMod> getAbilityMods(Type type) {
        return Collections.unmodifiableList(abilityScoresByType.computeIfAbsent(type, (key)->new ArrayList<>()));
    }

    public List<AbilityModChoice> getAbilityScoreChoices() {
        return Collections.unmodifiableList(abilityScoreChoices);
    }

    public Class currentClass() {
        return pClass;
    }

    public List<Language> getLanguages() {
        return Collections.unmodifiableList(languages);
    }

    public ReadOnlyObjectProperty<Double> getMoneyProperty() {
        return money.getReadOnlyProperty();
    }

    public boolean buy(Equipment item, int count) {
        Equipment newItem = item.copy();
        newItem.setCount(count);
        if(newItem.getValue() * count > money.get()) return false;
        money.set(money.get() - newItem.getValue() * count);
        inventory.put(item, newItem);
        return true;
    }

    public boolean sell(Equipment item, int count) {
        Equipment equipment = inventory.get(item);
        if(equipment == null) return false;
        int remaining = equipment.getCount();
        if(remaining <= 0) return false;
        money.set(money.get() + item.getValue() * count);
        equipment.remove(count);
        unequip(item, count);
        remaining -= count;
        if(remaining <= 0) {
            inventory.remove(item);
        }
        return true;
    }

    public double getTotalValue() {
        return inventory.values().stream().mapToDouble(equipment -> equipment.getValue() * equipment.getCount()).sum();
    }

    public int getCount(Equipment item) {
        Equipment equipment = inventory.get(item);
        if(equipment == null) return 0;
        return equipment.getCount();
    }

    public void addInventoryListener(MapChangeListener<Equipment, Equipment> listener) {
        inventory.addListener(listener);
    }

    public boolean equip(Equipment item, int count) {
        Slot slot = item.getSlot();
        Equipment slotContents = equipped.get(slot);
        if(slotContents == null) {
            equipped.put(slot, item);
            return true;
        }else if(slotContents.equals(item)) {
            slotContents.add(count);
            return true;
        }else return false;
    }

    private boolean unequip(Equipment item, int count) {
        Slot slot = item.getSlot();
        Equipment slotContents = equipped.get(slot);
        if(slotContents != null && slotContents.equals(item) && slotContents.getCount() >= count) {
            slotContents.remove(count);
            if(slotContents.getCount() == 0) {
                equipped.remove(slot);
            }
            return true;
        }
        return false;
    }

    public ObservableMap<Equipment, Equipment> getInventory() {
        return FXCollections.unmodifiableObservableMap(inventory);
    }

    public int getAttackMod(Weapon weapon) {
        int mod = getProficiency(Attribute.valueOf(weapon.getProficiency())).getValue().getMod()
                +level.get();
        if(weapon.getTraits().contains(new ItemTrait("Finesse")))
            return mod+Math.max(getAbilityMod(Str), getAbilityMod(Dex));
        else if(weapon instanceof RangedWeapon)
            return mod+getAbilityMod(Dex);
        else
            return mod+getAbilityMod(Str);
    }

    public int getDamageMod(Weapon weapon) {
        if(weapon.getTraits().contains(new ItemTrait("Thrown")))
            return getAbilityMod(Str);
        else if(weapon instanceof RangedWeapon)
            return 0;
        else if(weapon.getHands() == 2)
            return (int) (getAbilityMod(Str) * 1.5);
        else
            return getAbilityMod(Str);
    }

    public Ancestry getAncestry() {
        return ancestry;
    }

    public Class getPClass() {
        return pClass;
    }
}
