package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import model.abc.Ancestry;
import model.abc.Background;
import model.abc.PClass;
import model.abilities.Ability;
import model.abilities.AttackAbility;
import model.abilities.abilitySlots.AbilitySlot;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityModChoice;
import model.attributes.Attribute;
import model.attributes.AttributeMod;
import model.enums.Alignment;
import model.enums.Type;
import model.spells.Spell;
import setting.Deity;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import static model.ability_scores.AbilityScore.*;

public class PC {
    public static final int MAX_LEVEL = 20;
    private final ReadOnlyObjectWrapper<Ancestry> ancestry = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Background> background = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<PClass> pClass = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Deity> deity = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Integer> level = new ReadOnlyObjectWrapper<>(0);
    private final PropertyChangeSupport ancestryWatcher = new PropertyChangeSupport(ancestry);
    private final Applier applier = new Applier();
    private ReadOnlyObjectWrapper<Alignment> alignment = new ReadOnlyObjectWrapper<>();
    private final GroovyModManager modManager;
    private final DecisionManager decisions = new DecisionManager();
    private final AbilityManager abilities = new AbilityManager(decisions, getAncestryProperty(),
            getPClassProperty(), levelProperty(), applier, this::meetsPrerequisites);
    private final AbilityScoreManager scores = new AbilityScoreManager(applier, ()->{
        PClass currClass = pClass.get();
        if(currClass == null) return None;
        AbilityMod abilityMod = currClass.getAbilityMods().get(0);
        if(abilityMod instanceof AbilityModChoice &&
                ((AbilityModChoice) abilityMod).getChoices().size() > 0)
                return ((AbilityModChoice) abilityMod).getChoices().get(0);
        return abilityMod.getTarget();
    });
    private final CustomGetter customGetter = new CustomGetter(this);
    private final AttributeManager attributes =
            new AttributeManager(customGetter, level.getReadOnlyProperty(), decisions, applier);
    private final InventoryManager inventory = new InventoryManager(attributes);
    private final QualityManager qualities = new QualityManager(decisions::add, decisions::remove);
    private final SpellManager spells = new SpellManager(applier);
    private final CombatManager combat = new CombatManager(scores, attributes, inventory, level.getReadOnlyProperty());

    {
        modManager = new GroovyModManager(customGetter, attributes, decisions, combat, level.getReadOnlyProperty(), applier);
    }

    public PC() {
        scores.getScoreEyeball(Int).addPropertyChangeListener(((o) -> {
            attributes.updateSkillCount(getPClass().getSkillIncrease() + scores.getMod(Int));
            qualities.updateInt(scores.getMod(Int));
        }));

        applier.onApply(ability -> {
            if(ability instanceof AttackAbility) {
                combat.addAttacks(((AttackAbility) ability).getAttacks());
            }
        });

        applier.onRemove(ability -> {
            if(ability instanceof AttackAbility) {
                combat.removeAttacks(((AttackAbility) ability).getAttacks());
            }
        });
    }

    public Alignment getAlignment() {
        return (alignment.get() != null) ? alignment.get() : Alignment.N;
    }

    public ReadOnlyObjectProperty<Alignment> alignmentProperty() {
        return alignment.getReadOnlyProperty();
    }

    public void setAlignment(Alignment alignment) {
        this.alignment.set(alignment);
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
        Ancestry oldAncestry = getAncestry();
        if(oldAncestry != null) {
            scores.remove(oldAncestry.getAbilityMods());
            abilities.removeAll(Type.Ancestry);
        }
        this.ancestry.set(ancestry);
        scores.apply(ancestry.getAbilityMods());
        qualities.update(ancestry, oldAncestry);
        ancestryWatcher.firePropertyChange("ancestryChange", null, ancestry);
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

    public void setPClass(PClass newPClass) {
        if(newPClass == null) return;
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

    public void setDeity(Deity newDeity) {
        deity.set(newDeity);
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
    public void addAncestryObserver(PropertyChangeListener o) {
        ancestryWatcher.addPropertyChangeListener(o);
    }

    public ReadOnlyObjectProperty<Integer> levelProperty() {
        return level.getReadOnlyProperty();
    }

    public int getLevel(){
        return level.get();
    }

    public int getTotalMod(Attribute attribute, String data) {
        int acp = 0;
        if(attribute.hasACP() && combat.getArmor().getStrength() > scores.getScore(Str))
            acp -= combat.getArmor().getACP();
        return scores.getMod(attribute.getKeyAbility())
                + attributes.getProficiency(attribute, data).getValue().getMod(level.get())
                + attributes.getBonus(attribute) + acp;
    }

    public int getSpeed() {
        return ((getAncestry() != null) ? getAncestry().getSpeed() : 0) + modManager.get("speed");
    }

    public PClass currentClass() {
        return pClass.get();
    }

    public boolean meetsPrerequisites(Ability ability) {
        for (AttributeMod requiredAttr : ability.getRequiredAttrs()) {
            if(attributes.getProficiency(requiredAttr.getAttr(), requiredAttr.getData()).getValue().getMod() < requiredAttr.getMod().getMod())
                return false;
        }
        for (String prereq : ability.getPrerequisites()) {
            String[] split = prereq.split(" or ");
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
    public Deity getDeity() {
        return (deity.get() != null) ? deity.get() : Deity.NO_DEITY;
    }

    public InventoryManager inventory() {return inventory;}

    public AttributeManager attributes() {
        return attributes;
    }

    public AbilityManager abilities() {
        return abilities;
    }

    public QualityManager qualities() { return qualities; }

    public CombatManager combat() {
        return combat;
    }

    public Background getBackground() {
        return (background.get() != null) ? background.get() : Background.NO_BACKGROUND;
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

    public ReadOnlyObjectProperty<Deity> getDeityProperty() {
        return deity.getReadOnlyProperty();
    }

    public DecisionManager decisions() {
        return decisions;
    }

    public SpellManager spells() {return spells;}

    GroovyModManager mods() {
        return modManager;
    }

    public AbilityScoreManager scores() {
        return scores;
    }

    public void reset() {
        while(getLevel() > 1) levelDown();
    }
}
