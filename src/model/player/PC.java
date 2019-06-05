package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.AttributeMod;
import model.abc.Ancestry;
import model.abc.Background;
import model.abc.PClass;
import model.abilities.Ability;
import model.abilities.SkillIncrease;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.Choice;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.enums.Attribute;
import model.enums.Language;
import model.enums.Slot;
import model.enums.Type;
import model.equipment.Armor;
import model.equipment.ItemTrait;
import model.equipment.RangedWeapon;
import model.equipment.Weapon;

import javax.script.ScriptException;
import java.util.*;

import static model.ability_scores.AbilityScore.*;

public class PC {
    public static final int MAX_LEVEL = 20;
    private ReadOnlyObjectWrapper<Ancestry> ancestry = new ReadOnlyObjectWrapper<>();
    private ReadOnlyObjectWrapper<Background> background = new ReadOnlyObjectWrapper<>();
    private ReadOnlyObjectWrapper<PClass> pClass = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Integer> level = new ReadOnlyObjectWrapper<>(0);
    private final Map<AbilityScore, ObservableList<AbilityMod>> abilityScores = new HashMap<>();
    private final Map<Type, List<AbilityMod>> abilityScoresByType = new HashMap<>();
    private final Eyeball abilityScoreChange = new Eyeball();
    private final Eyeball ancestryWatcher = new Eyeball();
    private final List<AbilityModChoice> abilityScoreChoices = new ArrayList<>();
    private String name;
    private final List<Language> languages = new ArrayList<>();
    private InventoryManager inventory = new InventoryManager();
    private ModManager modManager;
    private DecisionManager decisions = new DecisionManager();
    private AbilityManager abilities = new AbilityManager(ancestry.getReadOnlyProperty(), pClass.getReadOnlyProperty(), decisions);
    private AttributeManager attributes = new AttributeManager(level.getReadOnlyProperty(), decisions);

    {
        try {
            modManager = new ModManager(this, level.getReadOnlyProperty());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public PC() {
        abilityScores.computeIfAbsent(Int, (key)-> FXCollections.observableArrayList()).addListener((ListChangeListener<? super AbilityMod>) (event)->{
            if(getPClass() != null) {
                attributes.updateSkillCount(getPClass().getSkillIncrease() + getAbilityMod(Int));
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
        applyLevel(getPClass().getLevel(level.get()));
    }

    public void setAncestry(Ancestry ancestry) {
        if(getAncestry() != null) {
            remove(getAncestry().getAbilityMods());
            languages.removeAll(ancestry.getLanguages());
        }
        this.ancestry.set(ancestry);
        apply(ancestry.getAbilityMods());
        languages.addAll(ancestry.getLanguages());
        ancestryWatcher.wink();
    }

    public void setBackground(Background background) {
        if(getBackground() != null)
            remove(getBackground().getAbilityMods());
        this.background.set(background);
        apply(background.getAbilityMods());
        attributes.apply(background.getMod());
    }

    public void setClass(PClass newPClass) {
        if(!(getPClass().equals(PClass.NO_CLASS))) {
            remove(getPClass().getAbilityMods());
            for(int i=getLevel(); i>0; i--)
                removeLevel(getPClass().getLevel(i));
        }
        pClass.set(newPClass);
        apply(newPClass.getAbilityMods());
        level.set(1);
        applyLevel(getPClass().getLevel(1));
        attributes.updateSkillCount(getPClass().getSkillIncrease() + getAbilityMod(Int));
    }

    private void removeLevel(List<AbilitySlot> level) {
        for(AbilitySlot slot: level) {
            abilities.remove(slot);
        }
    }

    private void applyLevel(List<AbilitySlot> level) {
        for(AbilitySlot slot: level) {
            addSlot(slot);
        }
    }

    private void addSlot(AbilitySlot slot) {
        if(slot instanceof Choice)
            abilities.addDecision((Choice) slot);
        if(slot.isPreSet()) {
            apply(slot);
        }
    }

    private void removeSlot(AbilitySlot slot) {
        abilities.remove(slot);
        if(slot.getCurrentAbility() != null) {
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
            abilities.add(slot);
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
        }
    }

    public int getHP() {
        return ((getAncestry() != null) ? getAncestry().getHP() : 0) + (((getPClass() != null) ? getPClass().getHP() : 0) + getAbilityMod(Con)) * level.get() + modManager.get("hp");
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

    public <U , T extends Choice<U>> void choose(T slot, U selectedItem) {
        if(slot instanceof AbilitySlot && selectedItem instanceof Ability && meetsPrerequisites((Ability) selectedItem)) {
            remove((AbilitySlot) slot);
            slot.fill(selectedItem);
            apply((AbilitySlot) slot);
        }else{
            slot.fill(selectedItem);
        }
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
        return (getAncestry() != null) ? getAncestry().getSpeed() : 0;
    }

    public List<AbilityMod> getAbilityMods(Type type) {
        return Collections.unmodifiableList(abilityScoresByType.computeIfAbsent(type, (key)->new ArrayList<>()));
    }

    public List<AbilityModChoice> getAbilityScoreChoices() {
        return Collections.unmodifiableList(abilityScoreChoices);
    }

    public PClass currentClass() {
        return pClass.get();
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

    public boolean meetsPrerequisites(Ability ability) {
        for (AttributeMod requiredAttr : ability.getRequiredAttrs()) {
            if(attributes.getProficiency(requiredAttr.getAttr()).getValue().getMod() < requiredAttr.getMod().getMod())
                return false;
        }
        for (String s : ability.getPrerequisites()) {
            boolean found=false;
            for (Ability charAbility : abilities.getAbilities()) {
                if(charAbility != null && charAbility.toString().toLowerCase().trim().equals(s.toLowerCase().trim())) {
                    found=true;
                    break;
                }
            }
            if(!found) return false;
        }
        return true;
    }

    public Ancestry getAncestry() {
        return ancestry.get();
    }

    public PClass getPClass() {
        return (pClass.get() != null) ? pClass.get() : PClass.NO_CLASS;
    }

    public InventoryManager inventory() {return inventory;}

    public AttributeManager attributes() {
        return attributes;
    }

    public AbilityManager abilities() {
        return abilities;
    }

    public Background getBackground() {
        return background.get();
    }

    public ReadOnlyObjectProperty<Ancestry> getAncestryProperty() {
        return ancestry.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<Background> getBackgroundProperty() {
        return background.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<PClass> getPClassProperty() {
        return pClass.getReadOnlyProperty();
    }

    public DecisionManager decisions() {
        return decisions;
    }
}
