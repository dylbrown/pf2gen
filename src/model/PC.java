package model;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.abc.Ancestry;
import model.abc.Background;
import model.abc.Class;
import model.abilities.Ability;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.ChoiceSlot;
import model.abilities.abilitySlots.FeatSlot;
import model.abilities.abilitySlots.Pickable;
import model.abilityScores.AbilityMod;
import model.abilityScores.AbilityModChoice;
import model.abilityScores.AbilityScore;
import model.enums.Attribute;
import model.enums.Proficiency;
import model.enums.Type;

import java.util.*;

import static model.abilityScores.AbilityScore.*;

public class PC {
    public static final int MAX_LEVEL = 20;
    private Ancestry ancestry;
    private Background background;
    private Class pClass;
    private int level = 1;
    private Map<Attribute, ReadOnlyObjectWrapper<Proficiency>> proficiencies = new HashMap<>();
    private ObservableList<AbilitySlot> abilities = FXCollections.observableArrayList();
    private Map<AbilityScore, List<AbilityMod>> abilityScores = new HashMap<>();
    private List<AbilityMod> remaining = new ArrayList<>();
    private String name;
    private SortedMap<Integer, Set<Attribute>> skillChoices = new TreeMap<>();
    private SortedMap<Integer, Integer> skillIncreases = new TreeMap<>();

    public PC() {
        abilityScores.computeIfAbsent(Int, (key)-> FXCollections.observableArrayList()).addListener((ListChangeListener<? super AbilityMod>) (event)->{
            if(pClass != null) {
                skillIncreases.put(1, pClass.getSkillIncreases() + getAbilityMod(Int));
                proficiencyChange.wink();
            }
        });
        List<AbilityModChoice> choices = Arrays.asList(
                new AbilityModChoice(Type.Initial),
                new AbilityModChoice(Type.Initial),
                new AbilityModChoice(Type.Initial),
                new AbilityModChoice(Type.Initial)
        );//TODO: Add to advancement table
        abilityScoreChoices.addAll(choices);
        abilityScoresByType.put(Type.Initial, new ArrayList<>(choices));
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAncestry(Ancestry ancestry) {
        if(this.ancestry != null)
            remove(this.ancestry.getAbilityMods());
        this.ancestry = ancestry;
        apply(ancestry.getAbilityMods());
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
        skillIncreases.put(1, pClass.getSkillIncreases() + getAbilityMod(Int));
        proficiencyChange.wink();
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
        if(slot.getCurrentAbility() != null)
            for(AttributeMod mod: slot.getCurrentAbility().getModifiers()){
                apply(mod);
            }
    }

    public Map<Attribute, ObservableValue<Proficiency>> getProficiencies() {
        return Collections.unmodifiableMap(proficiencies);
    }

    public ObservableValue<Proficiency> getProficiency(Attribute attr) {
        return proficiencies.computeIfAbsent(attr, (key) -> new ReadOnlyObjectWrapper<>(Proficiency.Untrained));
    }

    private void apply(AttributeMod mod) {
        ReadOnlyObjectWrapper<Proficiency> proficiency = proficiencies.get(mod.getAttr());
        if(proficiency == null) {
            proficiencies.put(mod.getAttr(), new ReadOnlyObjectWrapper<>(mod.getMod()));
        }else if(proficiency.getValue() == null || proficiency.getValue().getMod() < mod.getMod().getMod()) {
            proficiency.set(mod.getMod());
        }
    }

    public int getHP() {
        return ((ancestry != null) ? ancestry.getHP() : 0) + (((pClass != null) ? pClass.getHP() : 0) + getAbilityMod(Con)) * level;
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

    public int getLevel() {
        return level;
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

    public List<Ability> getFeatSet(List<Type> allowedTypes, int level) {
        List<Ability> results = new ArrayList<>();
        for (Type allowedType : allowedTypes) {
            switch(allowedType) {
                case Class:
                    if(pClass != null)
                        results.addAll(pClass.getFeats(level));
                    break;
                case Ancestry:
                    if(ancestry != null)
                        results.addAll(ancestry.getFeats(level));
                    break;
                case Heritage:
                    if(ancestry != null)
                        results.addAll(ancestry.getHeritages());
                    break;
            }
        }
        return results;
    }

    public void choose(AbilitySlot slot, Ability selectedItem) {
        if(slot instanceof Pickable) {
            ((Pickable) slot).fill(selectedItem);
            apply(slot);
        }
    }

    public int getAC() {
        return 10 + level + getAbilityMod(Dex);
    }

    public int getTAC() {
        return 10 + level + getAbilityMod(Dex);
    }

    public int getTotalMod(Attribute attribute) {
        return level+getAbilityMod(attribute.getKeyAbility())+getProficiency(attribute).getValue().getMod();
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
}
