package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.*;
import model.AttributeMod;
import model.abc.Ancestry;
import model.abc.Background;
import model.abc.Class;
import model.abilities.Ability;
import model.abilities.SkillIncrease;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.ChoiceSlot;
import model.abilities.abilitySlots.FeatSlot;
import model.abilities.abilitySlots.Choice;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.enums.*;
import model.equipment.*;

import javax.script.ScriptException;
import java.util.*;

import static model.ability_scores.AbilityScore.*;

public class PC {
    public static final int MAX_LEVEL = 20;
    private Ancestry ancestry;
    private Background background;
    private Class pClass;
    private final ReadOnlyObjectWrapper<Integer> level = new ReadOnlyObjectWrapper<>(0);
    private final ObservableList<Ability> abilities = FXCollections.observableArrayList();
    private final ObservableList<Choice> decisions = FXCollections.observableArrayList();
    private final Map<AbilityScore, ObservableList<AbilityMod>> abilityScores = new HashMap<>();
    private final Map<Type, List<AbilityMod>> abilityScoresByType = new HashMap<>();
    private final Eyeball abilityScoreChange = new Eyeball();
    private final Eyeball ancestryWatcher = new Eyeball();
    private final List<AbilityModChoice> abilityScoreChoices = new ArrayList<>();
    private String name;
    private final List<Language> languages = new ArrayList<>();
    private InventoryManager inventory = new InventoryManager();
    private AttributeManager attributes = new AttributeManager(this);
    private ModManager modManager;

    {
        try {
            modManager = new ModManager(this, level.getReadOnlyProperty());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public PC() {
        abilityScores.computeIfAbsent(Int, (key)-> FXCollections.observableArrayList()).addListener((ListChangeListener<? super AbilityMod>) (event)->{
            if(pClass != null) {
                attributes.updateSkillCount(pClass.getSkillIncrease() + getAbilityMod(Int));
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
        return (name != null) ? name : "Unnamed";
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
        attributes.apply(background.getMod());
    }

    public void setClass(Class newClass) {
        if(this.pClass != null) {
            remove(this.pClass.getAbilityMods());
            removeLevel(pClass.getLevel(1));
        }
        pClass = newClass;
        apply(newClass.getAbilityMods());
        level.set(1);
        applyLevel(pClass.getLevel(1));
        attributes.updateSkillCount(pClass.getSkillIncrease() + getAbilityMod(Int));
    }

    private void removeLevel(List<AbilitySlot> level) {
        for(AbilitySlot slot: level) {
            abilities.remove(slot.getCurrentAbility());
            if(slot instanceof FeatSlot || slot instanceof ChoiceSlot)
                decisions.remove(slot);
            if(slot.isPreSet()) {
                remove(slot);
            }
        }
    }

    private void applyLevel(List<AbilitySlot> level) {
        for(AbilitySlot slot: level) {
            addSlot(slot);
        }
    }

    private void addSlot(AbilitySlot slot) {
        if(slot instanceof Choice)
            decisions.add((Choice) slot);
        if(slot.isPreSet()) {
            abilities.add(slot.getCurrentAbility());
            apply(slot);
        }
    }

    private void removeSlot(AbilitySlot slot) {
        if(slot instanceof FeatSlot || slot instanceof ChoiceSlot)
            decisions.remove(slot);
        if(slot.getCurrentAbility() != null) {
            abilities.remove(slot.getCurrentAbility());
            remove(slot);
        }
    }

    private void apply(AbilitySlot slot) {
        if(slot.getCurrentAbility() != null) {
            if (slot.getCurrentAbility() instanceof SkillIncrease) {
                attributes.addSkillIncrease(slot.getCurrentAbility().getLevel());
            }

            for (AttributeMod mod : slot.getCurrentAbility().getModifiers()) {
                attributes.apply(mod);
            }
            for(AbilitySlot subSlot: slot.getCurrentAbility().getAbilitySlots()) {
                addSlot(subSlot);
            }
            apply(slot.getCurrentAbility().getAbilityMods());
            if(!slot.getCurrentAbility().getCustomMod().equals(""))
                modManager.jsApply(slot.getCurrentAbility().getCustomMod());
            abilities.add(slot.getCurrentAbility());
        }
    }

    private void remove(AbilitySlot slot) {
        if(slot.getCurrentAbility() != null) {
            if (slot.getCurrentAbility() instanceof SkillIncrease) {
                attributes.removeSkillIncrease(slot.getCurrentAbility().getLevel());
            }

            for (AttributeMod mod : slot.getCurrentAbility().getModifiers()) {
                attributes.remove(mod);
            }
            for(AbilitySlot subSlot: slot.getCurrentAbility().getAbilitySlots()) {
                removeSlot(subSlot);
            }

            remove(slot.getCurrentAbility().getAbilityMods());
            if(!slot.getCurrentAbility().getCustomMod().equals(""))
                modManager.jsRemove(slot.getCurrentAbility().getCustomMod());
            abilities.remove(slot.getCurrentAbility());
        }
    }

    public int getHP() {
        return ((ancestry != null) ? ancestry.getHP() : 0) + (((pClass != null) ? pClass.getHP() : 0) + getAbilityMod(Con)) * level.get() + modManager.get("hp");
    }

    public Integer getAbilityMod(AbilityScore ability) {
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
        abilityScoreChange.wink();
    }

    private void remove(List<AbilityMod> abilityMods) {
        for(AbilityMod mod: abilityMods) {
            List<AbilityMod> mods = abilityScores.get(mod.getTarget());
            mods.remove(mod);
            abilityScoresByType.get(mod.getSource()).remove(mod);
            if(mod instanceof AbilityModChoice)
                abilityScoreChoices.remove(mod);
        }
        abilityScoreChange.wink();
    }
    public void addAbilityObserver(Observer o) {
        abilityScoreChange.addObserver(o);
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

    public ReadOnlyObjectProperty<Integer> getLevelProperty() {
        return level.getReadOnlyProperty();
    }

    public int getLevel(){
        return level.get();
    }

    public ObservableList<Choice> getDecisions() {
        return FXCollections.unmodifiableObservableList(decisions);
    }

    public <U , T extends Choice<U>> void choose(T slot, U selectedItem) {
        if(slot instanceof AbilitySlot && selectedItem instanceof Ability && meetsPrerequisites((Ability) selectedItem)) {
            remove((AbilitySlot) slot);
            slot.fill(selectedItem);
            apply((AbilitySlot) slot);
        }else{
            slot.fill(selectedItem);
        }
    }

    public boolean meetsPrerequisites(Ability ability) {
        for (AttributeMod requiredAttr : ability.getRequiredAttrs()) {
            if(attributes.getProficiency(requiredAttr.getAttr()).getValue().getMod() < requiredAttr.getMod().getMod())
                return false;
        }
        for (String s : ability.getPrerequisites()) {
            boolean found=false;
            for (Ability charAbility : abilities) {
                if(charAbility != null && charAbility.toString().toLowerCase().trim().equals(s.toLowerCase().trim())) {
                    found=true;
                    break;
                }
            }
            if(!found) return false;
        }
        return true;
    }

    public int getAC() {
        Armor armor = (Armor) inventory.getEquipped(Slot.Armor);
        if(armor != null)
            return 10 + level.get() + armor.getAC() + Math.max(getAbilityMod(Dex), armor.getMaxDex());
        return 10 + level.get() + getAbilityMod(Dex);
    }

    public int getTAC() {
        Armor armor = (Armor) inventory.getEquipped(Slot.Armor);
        if(armor != null)
            return 10 + level.get() + armor.getTAC() + Math.max(getAbilityMod(Dex), armor.getMaxDex());
        return 10 + level.get() + getAbilityMod(Dex);
    }

    public int getTotalMod(Attribute attribute) {
        return level.get()+getAbilityMod(attribute.getKeyAbility())+attributes.getProficiency(attribute).getValue().getMod();
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

    public int getAttackMod(Weapon weapon) {
        int mod = attributes.getProficiency(Attribute.valueOf(weapon.getProficiency()), weapon.getGroup()).getMod()
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
        return (pClass != null) ? pClass : Class.NO_CLASS;
    }

    public ObservableList<Ability> getAbilities() {
        return FXCollections.unmodifiableObservableList(abilities);
    }

    public InventoryManager inventory() {return inventory;}

    public AttributeManager attributes() {
        return attributes;
    }

    public void addDecision(Choice choice) {
        decisions.add(choice);
    }
    public void removeDecision(Choice choice) {
        decisions.remove(choice);
        choice.empty();
    }
}
