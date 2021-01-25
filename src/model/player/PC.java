package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import model.abc.Ancestry;
import model.abc.Background;
import model.abc.PClass;
import model.abilities.Ability;
import model.abilities.ArchetypeExtension;
import model.abilities.AttackExtension;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.ability_slots.AbilitySlot;
import model.attributes.Attribute;
import model.enums.Alignment;
import model.enums.Proficiency;
import model.enums.Type;
import model.items.Item;
import model.items.armor.Armor;
import model.items.weapons.Weapon;
import model.setting.Deity;
import model.spells.Spell;
import model.spells.SpellList;
import model.util.ObjectNotFoundException;
import model.util.Pair;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static model.ability_scores.AbilityScore.*;

public class PC {
    private final SourcesManager sources;

    public static final int MAX_LEVEL = 20;
    private final ReadOnlyObjectWrapper<Ancestry> ancestry = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Background> background = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<PClass> pClass = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Deity> deity = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Integer> level = new ReadOnlyObjectWrapper<>(0);
    private final PropertyChangeSupport ancestryWatcher = new PropertyChangeSupport(ancestry);
    private final Applier applier = new Applier();
    private final ReadOnlyObjectWrapper<Alignment> alignment = new ReadOnlyObjectWrapper<>();

    private final DecisionManager decisions = new DecisionManager();
    private final SpellListManager spells = new SpellListManager(applier);
    private final AbilityManager abilities;
    private final AbilityScoreManager scores = new AbilityScoreManager(applier, ()->{
        PClass currClass = pClass.get();
        if(currClass == null) return None;
        AbilityMod abilityMod = currClass.getAbilityMods().get(0);
        if(abilityMod instanceof AbilityModChoice &&
                ((AbilityModChoice) abilityMod).getChoices().size() > 0)
                return ((AbilityModChoice) abilityMod).getChoices().get(0);
        return abilityMod.getTarget();
    }, (data)-> spells.getSpellList(data).getCastingAbility().get());
    private final CustomGetter customGetter = new CustomGetter(this);
    private final AttributeManager attributes =
            new AttributeManager(customGetter, level.getReadOnlyProperty(), decisions, applier);
    private final InventoryManager inventory = new InventoryManager(attributes, applier);
    private final QualityManager qualities = new QualityManager(decisions::add, decisions::remove);
    private final CombatManager combat = new CombatManager(scores, attributes, inventory, levelProperty());
    private final GroovyModManager modManager;
    private final List<PlayerState> stateManagers = new ArrayList<>(Arrays.asList(
            decisions, spells, scores, attributes, inventory, qualities, combat
    ));

    public PC(SourcesManager sources) {
        this.sources = sources;
        abilities = new AbilityManager(sources, decisions, ancestryProperty(),
                pClassProperty(), applier, this::meetsPrerequisites);
        stateManagers.add(abilities);
        GroovyCommands groovyCommands = new GroovyCommands(
                customGetter, sources, abilities, attributes, decisions, combat,
                spells, deity.getReadOnlyProperty(), level.getReadOnlyProperty()
        );
        modManager = new GroovyModManager(groovyCommands, applier, level.getReadOnlyProperty());
        stateManagers.add(modManager);
        scores.getScoreEyeball(Int).addPropertyChangeListener(((o) -> {
            attributes.updateSkillCount(getPClass().getSkillIncrease() + scores.getMod(Int));
            qualities.updateInt(scores.getMod(Int));
        }));

        applier.onApply(ability -> {
            AttackExtension attackExt = ability.getExtension(AttackExtension.class);
            if(attackExt != null) {
                combat.addAttacks(attackExt.getAttacks());
            }
        });

        applier.onRemove(ability -> {
            AttackExtension attackExt = ability.getExtension(AttackExtension.class);
            if(attackExt != null) {
                combat.removeAttacks(attackExt.getAttacks());
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
        if(level.get() <= 1)
            return;
        removeLevel(getPClass().getLevel(level.get()));
        level.set(level.get()-1);
    }

    public void setAncestry(Ancestry ancestry) {
        if(ancestry == null) return;
        Ancestry oldAncestry = getAncestry();
        if(oldAncestry != Ancestry.NO_ANCESTRY) {
            scores.remove(oldAncestry.getAbilityMods());
            abilities.removeAll(Type.Ancestry);
        }
        this.ancestry.set(ancestry);
        if(ancestry != Ancestry.NO_ANCESTRY) {
            scores.apply(ancestry.getAbilityMods());
            qualities.update(ancestry, oldAncestry);
        }
        ancestryWatcher.firePropertyChange("ancestryChange", null, ancestry);
    }

    public void setBackground(Background background) {
        if (background == null) return;
        if(getBackground() != Background.NO_BACKGROUND){
            scores.remove(getBackground().getAbilityMods());
            attributes.remove(getBackground().getMods().get(0));
            attributes.remove(getBackground().getMods().get(1));
            abilities.removeAll(Type.Background);
        }
        this.background.set(background);
        if(background != Background.NO_BACKGROUND) {
            scores.apply(background.getAbilityMods());
            attributes.apply(background.getMods().get(0));
            attributes.apply(background.getMods().get(1));
            abilities.apply(background.getFreeFeat());
        }
    }

    public void setPClass(PClass newPClass) {
        if(newPClass == null) return;
        if(!(getPClass().equals(PClass.NO_CLASS))) {
            scores.remove(getPClass().getAbilityMods());
            for(int i=getLevel(); i>0; i--)
                removeLevel(getPClass().getLevel(i));
            abilities.removeAll(Type.Class);
        }
        if(!(newPClass.equals(PClass.NO_CLASS))) {
            scores.apply(newPClass.getAbilityMods());
            pClass.set(newPClass);
            level.set(1);
            applyLevel(newPClass.getLevel(1));
            attributes.updateSkillCount(newPClass.getSkillIncrease() + scores.getMod(Int));
        }else{
            pClass.set(newPClass);
            level.set(1);
        }
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
        if(attribute.hasACP() && combat.getArmor().getExtension(Armor.class).getStrength() > scores.getScore(Str))
            acp -= combat.getArmor().getExtension(Armor.class).getACP();
        return scores.getMod(attribute.getKeyAbility(), data)
                + attributes.getProficiency(attribute, data).getValue().getMod(level.get())
                + attributes.getBonus(attribute) + acp;
    }

    public int getSpeed() {
        return ((getAncestry() != null) ? getAncestry().getSpeed() : 0) + modManager.get("speed");
    }

    public boolean meetsPrerequisites(Ability ability) {
        if(ability.getLevel() > getLevel()) return false;
        if(!ability.getRequiredAttrs().test(ca->
                attributes.getProficiency(ca.getAttribute(), ca.getData()).getValue()))
            return false;
        if(!ability.getRequiredWeapons().test(s->{
            try {
                Item item = sources.weapons().find(s);
                Weapon weapon = item.getExtension(Weapon.class);
                if(weapon != null)
                    return attributes.getProficiency(weapon.getProficiency(), item);
            } catch (ObjectNotFoundException e) {
                e.printStackTrace();
            }
            return Proficiency.Untrained;
        }))
            return false;
        for (Pair<AbilityScore, Integer> requiredScore : ability.getRequiredScores()) {
            if(scores.getScore(requiredScore.first) < requiredScore.second)
                return false;
        }
        ArchetypeExtension archetypeExt = ability.getExtension(ArchetypeExtension.class);
        if(archetypeExt != null) {
            if(!abilities.meetsPrerequisites(archetypeExt))
                return false;
        }

        for (String prereq : ability.getPrerequisites()) {
            String[] split = prereq.split(" or ");
            boolean found=false;
outerLoop:  for (String orClause : split) {
                if(orClause.matches("Spell\\(.*\\)")) {
                    String spellName = orClause.replaceAll("Spell\\((.*)\\)", "\\1");
                    for (SpellList spellList : spells.getSpellLists().values()) {
                        for (Spell focusSpell : spellList.getFocusSpells()) {
                            if(focusSpell.getName().equals(spellName)) {
                                found = true;
                                break outerLoop;
                            }
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
        try {
            return (deity.get() != null) ? deity.get() : sources.deities().find("no deity");
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SourcesManager sources() {
        return sources;
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

    public ReadOnlyObjectProperty<Ancestry> ancestryProperty() {
        return ancestry.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<Background> backgroundProperty() {
        return background.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<PClass> pClassProperty() {
        return pClass.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<Deity> deityProperty() {
        return deity.getReadOnlyProperty();
    }

    public DecisionManager decisions() {
        return decisions;
    }

    public SpellListManager spells() {return spells;}

    public GroovyModManager mods() {
        return modManager;
    }

    public AbilityScoreManager scores() {
        return scores;
    }

    public void reset() {
        while(getLevel() > 1) levelDown();
        setAncestry(Ancestry.NO_ANCESTRY);
        setBackground(Background.NO_BACKGROUND);
        setPClass(PClass.NO_CLASS);
        deity.set(null);
        ResetEvent resetEvent = new ResetEvent();
        for (PlayerState stateManager : stateManagers) {
            stateManager.reset(resetEvent);
        }
        resetEvent.active = false;
    }

    public static class ResetEvent {
        private boolean active = true;
        private ResetEvent() {}

        public boolean isActive() {
            return active;
        }
    }

    @Override
    public String toString() {
        String name = qualities.get("name");
        if(name == null || name.isBlank())
            name = "Unnamed PC";
        return name;
    }
}
