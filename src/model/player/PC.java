package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import model.AttributeMod;
import model.abc.Ancestry;
import model.abc.Background;
import model.abc.PClass;
import model.abilities.Ability;
import model.abilities.AttackAbility;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.Choice;
import model.abilities.abilitySlots.SingleChoice;
import model.enums.*;
import model.equipment.Armor;
import model.equipment.CustomTrait;
import model.equipment.RangedWeapon;
import model.equipment.Weapon;
import model.spells.Spell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observer;

import static model.ability_scores.AbilityScore.*;

public class PC {
    public static final int MAX_LEVEL = 20;
    private final ReadOnlyObjectWrapper<Ancestry> ancestry = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Background> background = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<PClass> pClass = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Integer> level = new ReadOnlyObjectWrapper<>(0);
    private final Eyeball ancestryWatcher = new Eyeball();
    private final Applier applier = new Applier();
    private String name, height, weight, age, hair, eyes, gender;
    private String player;
    private Alignment alignment;
    private final List<Language> languages = new ArrayList<>();
    private final InventoryManager inventory = new InventoryManager();
    private final ModManager modManager;
    private final DecisionManager decisions = new DecisionManager();
    private final AbilityManager abilities = new AbilityManager(decisions, getAncestryProperty(),
            getPClassProperty(), getLevelProperty(), applier, this::meetsPrerequisites);
    private final AbilityScoreManager scores = new AbilityScoreManager(applier);
    private final AttributeManager attributes = new AttributeManager(level.getReadOnlyProperty(), decisions, applier);
    private final SpellManager spells = new SpellManager(applier);
    private List<Weapon> attacks = new ArrayList<>();

    {
        modManager = new ModManager(this, level.getReadOnlyProperty(), applier);
    }

    public PC() {
        scores.getScoreEyeball(Int).addObserver(((o, arg) -> {
            if(getPClass() != null) {
                attributes.updateSkillCount(getPClass().getSkillIncrease() + scores.getMod(Int));
            }
        }));

        applier.onApply(ability -> {
            if(ability instanceof AttackAbility) {
                addAttacks(((AttackAbility) ability).getAttacks());
            }
        });

        applier.onRemove(ability -> {
            if(ability instanceof AttackAbility) {
                removeAttacks(((AttackAbility) ability).getAttacks());
            }
        });
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return (name != null) ? name : "Unnamed";
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHair() {
        return hair;
    }

    public void setHair(String hair) {
        this.hair = hair;
    }

    public String getEyes() {
        return eyes;
    }

    public void setEyes(String eyes) {
        this.eyes = eyes;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
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
        if(level.get() == 20)
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
        ancestryWatcher.addObserver(o); // TODO: Remove deprecated observer use
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
                    (((Ability) selectedItem).isMultiple() || !abilities().haveAbility((Ability) selectedItem)))){
                if(slot.getChoice() != null) {
                    abilities.changeSlot((AbilitySlot) slot, null);
                    choose(slot, selectedItem);
                }else abilities.changeSlot((AbilitySlot) slot, (Ability) selectedItem);
            }
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
                return 10 + attributes().getProficiency(Attribute.valueOf(armor.getProficiency())).getValue().getMod(level.get()) + armor.getAC() + Math.max(scores.getMod(Dex), armor.getMaxDex());
        }
        return 10 + attributes().getProficiency(Attribute.Unarmored).getValue().getMod(level.get()) + scores.getMod(Dex);
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
        if(weapon.getTraits().contains(new CustomTrait("Finesse")))
            return mod+Math.max(scores.getMod(Str), scores.getMod(Dex));
        else if(weapon instanceof RangedWeapon)
            return mod+scores.getMod(Dex);
        else
            return mod+scores.getMod(Str);
    }

    public int getDamageMod(Weapon weapon) {
        if(weapon.getTraits().contains(new CustomTrait("Thrown")))
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
outerLoop:  for (String orClause : split) {
                if(orClause.matches("Spell\\(.*\\)")) {
                    String spellName = orClause.replaceAll("Spell\\((.*)\\)", "\\1");
                    for (Spell focusSpell : spells().getFocusSpells()) {
                        if(focusSpell.getName().equals(spellName)) {
                            found = true;
                            break outerLoop;
                        }
                    }
                }else {
                    found = abilities.meetsPrerequisite(orClause, true);
                    if(found) break;
                }
            }
            if(!found) return false;
        }
        for (String prereq : ability.getPrereqStrings()) {
            String[] split = prereq.split(" or ");
            boolean found=false;
            for (String orClause : split) {
                found = abilities.meetsPrerequisite(orClause, false);
                if(found) break;
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

    public SpellManager spells() {return spells;}

    ModManager mods() {
        return modManager;
    }

    public AbilityScoreManager scores() {
        return scores;
    }

    public void reset() {
        while(getLevel() > 1) levelDown();
    }

    private void addAttacks(List<Weapon> attacks) {
        this.attacks.addAll(attacks);
    }

    private void removeAttacks(List<Weapon> attacks) {
        this.attacks.removeAll(attacks);
    }

    public List<Weapon> getAttacks() {
        return Collections.unmodifiableList(attacks);
    }
}
