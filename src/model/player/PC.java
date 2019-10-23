package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import model.AttributeMod;
import model.abc.Ancestry;
import model.abc.Background;
import model.abc.PClass;
import model.abilities.Ability;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.Choice;
import model.abilities.abilitySlots.SingleChoice;
import model.enums.Attribute;
import model.enums.Language;
import model.enums.Slot;
import model.enums.Type;
import model.equipment.Armor;
import model.equipment.ItemTrait;
import model.equipment.RangedWeapon;
import model.equipment.Weapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observer;

import static model.ability_scores.AbilityScore.*;

public class PC {
    public static final int MAX_LEVEL = 20;
    private ReadOnlyObjectWrapper<Ancestry> ancestry = new ReadOnlyObjectWrapper<>();
    private ReadOnlyObjectWrapper<Background> background = new ReadOnlyObjectWrapper<>();
    private ReadOnlyObjectWrapper<PClass> pClass = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Integer> level = new ReadOnlyObjectWrapper<>(0);
    private final Eyeball ancestryWatcher = new Eyeball();
    private String name;
    private String player;
    private final List<Language> languages = new ArrayList<>();
    private final InventoryManager inventory = new InventoryManager();
    private ModManager modManager;
    private DecisionManager decisions = new DecisionManager();
    private AbilityManager abilities = new AbilityManager(this);
    private AbilityScoreManager scores = new AbilityScoreManager();
    private AttributeManager attributes = new AttributeManager(level.getReadOnlyProperty(), decisions);

    {
        modManager = new ModManager(this, level.getReadOnlyProperty());
    }

    public PC() {
        scores.getScoreEyeball(Int).addObserver(((o, arg) -> {
            if(getPClass() != null) {
                attributes.updateSkillCount(getPClass().getSkillIncrease() + scores.getMod(Int));
            }
        }));
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return (name != null) ? name : "Unnamed";
    }

    public void setPlayer(String name) {
        this.player = name;
    }

    public String getPlayer() {
        return (player != null) ? player : "Unknown";
    }

    public void levelUp(){
        if(level.get() == 0)
            return;
        level.set(level.get()+1);
        applyLevel(getPClass().getLevel(level.get()));
    }

    public void levelDown(){
        if(level.get() == 1)
            return;
        removeLevel(getPClass().getLevel(level.get()));
        level.set(level.get()-1);
    }

    public void setAncestry(Ancestry ancestry) {
        if(ancestry == null) return;
        if(getAncestry() != null) {
            scores.remove(getAncestry().getAbilityMods());
            languages.removeAll(getAncestry().getLanguages());
            abilities.removeAll(Type.Ancestry);
        }
        this.ancestry.set(ancestry);
        scores.apply(ancestry.getAbilityMods());
        languages.addAll(ancestry.getLanguages());
        ancestryWatcher.wink();
    }

    public void setBackground(Background background) {
        if (background == null) return;
        if(getBackground() != null){
            scores.remove(getBackground().getAbilityMods());
            attributes.remove(background.getMods().get(0));
            attributes.remove(background.getMods().get(1));
            abilities.removeAll(Type.Background);
        }
        this.background.set(background);
        scores.apply(background.getAbilityMods());
        attributes.apply(background.getMods().get(0));
        attributes.apply(background.getMods().get(1));
        abilities.apply(background.getFreeFeat());
    }

    public void setClass(PClass newPClass) {
        if(!(getPClass().equals(PClass.NO_CLASS))) {
            scores.remove(getPClass().getAbilityMods());
            for(int i=getLevel(); i>0; i--)
                removeLevel(getPClass().getLevel(i));
            abilities.removeAll(Type.Class);
        }
        scores.apply(newPClass.getAbilityMods());
        applyLevel(newPClass.getLevel(1));
        attributes.updateSkillCount(newPClass.getSkillIncrease() + scores.getMod(Int));
        pClass.set(newPClass);
        level.set(1);
    }

    private void removeLevel(List<AbilitySlot> level) {
        for(AbilitySlot slot: level) {
            abilities.remove(slot);
        }
    }

    private void applyLevel(List<AbilitySlot> level) {
        for(AbilitySlot slot: level) {
            abilities.apply(slot);
        }
    }

    public int getHP() {
        return ((getAncestry() != null) ? getAncestry().getHP() : 0) + (((getPClass() != null) ? getPClass().getHP() : 0) + scores.getMod(Con)) * level.get() + modManager.get("hp");
    }
    public void addAncestryObserver(Observer o) {
        ancestryWatcher.addObserver(o);
    }

    public ReadOnlyObjectProperty<Integer> getLevelProperty() {
        return level.getReadOnlyProperty();
    }

    public int getLevel(){
        return level.get();
    }

    public <U , T extends SingleChoice<U>> void choose(T slot, U selectedItem) {
        if(slot instanceof AbilitySlot && (selectedItem == null || selectedItem instanceof Ability)) {
            if(selectedItem == null || (meetsPrerequisites((Ability) selectedItem) &&
                    ((Ability) selectedItem).isMultiple() || !abilities().haveAbility((Ability) selectedItem)))
                abilities.changeSlot((AbilitySlot) slot, (Ability) selectedItem);
        }else{
            slot.fill(selectedItem);
        }
    }



    public <T> void addSelection(Choice<T> slot, T selectedItem) {
        if(slot instanceof SingleChoice){
            choose((SingleChoice<T>) slot, selectedItem);
        }else{
            if(slot.getSelections().size() >= slot.getNumSelections()) return;
            slot.add(selectedItem);
        }
    }

    public <T> void removeSelection(Choice<T> slot, T selectedItem) {
        slot.remove(selectedItem);
    }

    public int getAC() {
        if(inventory.getEquipped(Slot.Armor) != null) {
            Armor armor = (Armor) inventory.getEquipped(Slot.Armor).stats();
            if(armor != null)
                return 10 + level.get() + armor.getAC() + Math.max(scores.getMod(Dex), armor.getMaxDex());
        }
        return 10 + level.get() + scores.getMod(Dex);
    }

    public int getTotalMod(Attribute attribute) {
        return scores.getMod(attribute.getKeyAbility())+attributes.getProficiency(attribute).getValue().getMod(level.get());
    }

    public int getSpeed() {
        return ((getAncestry() != null) ? getAncestry().getSpeed() : 0) + modManager.get("speed");
    }

    public PClass currentClass() {
        return pClass.get();
    }

    public List<Language> getLanguages() {
        return Collections.unmodifiableList(languages);
    }

    public int getAttackMod(Weapon weapon) {
        int mod = attributes.getProficiency(Attribute.valueOf(weapon.getProficiency()), weapon.getGroup()).getMod(level.get());
        if(weapon.getTraits().contains(new ItemTrait("Finesse")))
            return mod+Math.max(scores.getMod(Str), scores.getMod(Dex));
        else if(weapon instanceof RangedWeapon)
            return mod+scores.getMod(Dex);
        else
            return mod+scores.getMod(Str);
    }

    public int getDamageMod(Weapon weapon) {
        if(weapon.getTraits().contains(new ItemTrait("Thrown")))
            return scores.getMod(Str);
        else if(weapon instanceof RangedWeapon)
            return 0;
        else if(weapon.getHands() == 2)
            return (int) (scores.getMod(Str) * 1.5);
        else
            return scores.getMod(Str);
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
        return (ancestry.get() != null) ? ancestry.get() : Ancestry.NO_ANCESTRY;
    }

    public PClass getPClass() {
        return (pClass.get() != null) ? pClass.get() : PClass.NO_CLASS;
    }

    public InventoryManager inventory   () {return inventory;}

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

    ModManager mods() {
        return modManager;
    }

    public AbilityScoreManager scores() {
        return scores;
    }

    public void reset() {
        while(getLevel() > 1) levelDown();
    }
}
