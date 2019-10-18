package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import model.AttributeMod;
import model.AttributeModSingleChoice;
import model.WeaponGroupMod;
import model.abilities.MinimumProficiencyList;
import model.enums.Attribute;
import model.enums.Proficiency;
import model.equipment.WeaponGroup;

import java.util.*;

public class AttributeManager {
    private final Map<Attribute, ReadOnlyObjectWrapper<Proficiency>> proficiencies = new HashMap<>();
    private final Map<Attribute, Map<Proficiency, List<AttributeMod>>> proficienciesTracker = new HashMap<>();

    //Map from level to selected skills`
    private final SortedMap<Integer, Set<Attribute>> skillChoices = new TreeMap<>();

    // Maps from level to number of increases
    private final SortedMap<Integer, Integer> innerSkillIncreases = new TreeMap<>();
    private final ObservableMap<Integer, Integer> skillIncreases = FXCollections.observableMap(innerSkillIncreases);

    private final Map<Proficiency, MinimumProficiencyList> minLists = new HashMap<>();
    private final ReadOnlyObjectProperty<Integer> level;
    private DecisionManager decisions;
    private Map<WeaponGroup, Proficiency> groupProficiencies = new HashMap<>();
    private final Eyeball proficiencyChange = new Eyeball();
    private List<AttributeModSingleChoice> choices = new ArrayList<>();

    AttributeManager(ReadOnlyObjectProperty<Integer> level, DecisionManager decisions){
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
    }

    void updateSkillCount(int numSkills) {
        skillIncreases.put(1,numSkills);
        proficiencyChange.wink();
    }

    void apply(AttributeMod mod) {
        if(mod.getAttr() == null) return;
        if(mod instanceof AttributeModSingleChoice) {
            decisions.add((AttributeModSingleChoice)mod);
            if(!choices.contains(mod)) {
                choices.add((AttributeModSingleChoice) mod);
                ((AttributeModSingleChoice) mod).getChoiceProperty().addListener((observable, oldVal, newVal)->{
                    remove(new AttributeMod(oldVal, mod.getMod()));
                    apply(new AttributeMod(newVal, mod.getMod()));
                });
            }
            return;
        }
        ReadOnlyObjectWrapper<Proficiency> proficiency = proficiencies.get(mod.getAttr());
        proficienciesTracker.computeIfAbsent(mod.getAttr(), (key) -> new HashMap<>()).computeIfAbsent(mod.getMod(), (key) -> new ArrayList<>()).add(mod);
        if (proficiency == null) {
            proficiencies.put(mod.getAttr(), new ReadOnlyObjectWrapper<>(mod.getMod()));
        } else if (proficiency.getValue() == null || proficiency.getValue().getMod() < mod.getMod().getMod()) {
            proficiency.set(mod.getMod());
        }
        proficiencyChange.wink();
    }

    void remove(AttributeMod mod) {
        if(mod.getAttr() == null) return;
        if(mod instanceof AttributeModSingleChoice) {
            decisions.remove((AttributeModSingleChoice)mod);
            Attribute choice = ((AttributeModSingleChoice) mod).getChoice();
            if(choice != null)
                remove(new AttributeMod(choice, mod.getMod()));
            return;
        }
        ReadOnlyObjectWrapper<Proficiency> proficiency = proficiencies.get(mod.getAttr());
        Map<Proficiency, List<AttributeMod>> proficiencyListMap = proficienciesTracker.computeIfAbsent(mod.getAttr(), (key) -> new HashMap<>());
        List<AttributeMod> attributeMods = proficiencyListMap.computeIfAbsent(mod.getMod(), (key) -> new ArrayList<>());
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
                    doLoop = removeHelper(proficiencyListMap, temp);
            }
            proficiency.set(temp);
        }
    }
    private boolean removeHelper(Map<Proficiency, List<AttributeMod>> proficiencyListMap, Proficiency proficiency) {
        return proficiencyListMap.computeIfAbsent(proficiency, (key) -> new ArrayList<>()).size() == 0;
    }


    public Map<Attribute, ObservableValue<Proficiency>> getProficiencies() {
        return Collections.unmodifiableMap(proficiencies);
    }

    public ObservableValue<Proficiency> getProficiency(Attribute attr) {
        return proficiencies.computeIfAbsent(attr, (key) -> new ReadOnlyObjectWrapper<>(Proficiency.Untrained)).getReadOnlyProperty();
    }

    public void addObserver(Observer o) {
        proficiencyChange.addObserver(o);
    }

    public boolean advanceSkill(Attribute skill) {
        if(!Arrays.asList(Attribute.getSkills()).contains(skill))
            return false;
        ReadOnlyObjectWrapper<Proficiency> prof = proficiencies.computeIfAbsent(skill, (key)->new ReadOnlyObjectWrapper<>(Proficiency.Untrained));
        if(prof.getValue() == Proficiency.Legendary) return false;
        for (Map.Entry<Integer, Integer> entry : skillIncreases.entrySet()) {
            if(prof.getValue() == Proficiency.Trained && entry.getKey() < Proficiency.getMinLevel(Proficiency.Expert))
                continue;
            if(prof.getValue() == Proficiency.Expert && entry.getKey() < Proficiency.getMinLevel(Proficiency.Master))
                continue;
            if(prof.getValue() == Proficiency.Master && entry.getKey() < Proficiency.getMinLevel(Proficiency.Legendary))
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
            if(prof.getValue() == Proficiency.Trained && entry.getKey() < Proficiency.getMinLevel(Proficiency.Expert))
                continue;
            if(prof.getValue() == Proficiency.Expert && entry.getKey() < Proficiency.getMinLevel(Proficiency.Master))
                continue;
            if(prof.getValue() == Proficiency.Master && entry.getKey() < Proficiency.getMinLevel(Proficiency.Legendary))
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

    void addSkillIncrease(int level) {
        skillIncreases.put(level, skillIncreases.computeIfAbsent(level, (key) -> 0) + 1);
        proficiencyChange.wink();
    }

    void removeSkillIncrease(int level) {
        skillIncreases.put(level, skillIncreases.computeIfAbsent(level, (key) -> 1) - 1);
        proficiencyChange.wink();
    }

    void apply(WeaponGroupMod weaponGroupMod) {
        groupProficiencies.merge(weaponGroupMod.getGroup(), weaponGroupMod.getProficiency(), Proficiency::max);
    }
    void remove(WeaponGroupMod weaponGroupMod) {
        groupProficiencies.put(weaponGroupMod.getGroup(), null);
    }

    Proficiency getProficiency(Attribute attr, WeaponGroup group) {
        return Proficiency.max(getProficiency(attr).getValue(), groupProficiencies.getOrDefault(group, Proficiency.Untrained));
    }

    public SortedMap<Integer, Set<Attribute>> getSkillChoices() {
        return Collections.unmodifiableSortedMap(skillChoices);
    }

    public void resetSkills() {
        for(int i=level.get(); i>0; i--) {
            Set<Attribute> attributes = skillChoices.get(i);
            if (attributes != null) {
                while(attributes.size() > 0)
                    regressSkill(attributes.iterator().next());
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

    public ObservableList<String> getMinList(Proficiency min) {
        return minLists.get(min);
    }
}
