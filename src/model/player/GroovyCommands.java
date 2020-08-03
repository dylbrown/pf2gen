package model.player;

import groovy.lang.Closure;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import model.equipment.weapons.WeaponGroupMod;
import model.equipment.weapons.WeaponMod;
import model.abilities.Ability;
import model.abilities.ArchetypeExtension;
import model.abilities.SpellExtension;
import model.ability_slots.AbilitySlot;
import model.ability_slots.FeatSlot;
import model.attributes.Attribute;
import model.attributes.AttributeMod;
import model.data_managers.sources.MultiSourceLoader;
import model.data_managers.sources.SourcesLoader;
import model.enums.Proficiency;
import model.equipment.weapons.Damage;
import model.equipment.weapons.WeaponGroup;
import model.spells.Spell;
import model.util.StringUtils;
import model.xml_parsers.AbilityLoader;
import setting.Deity;
import setting.Domain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unused", "unchecked"})
class GroovyCommands {
    private final ObservableMap<String, Integer> mods = FXCollections.observableHashMap();
    private final BooleanProperty applying = new SimpleBooleanProperty(true);
    private final Map<String, AbilitySlot> slots = new HashMap<>();
    private final Map<String, ArbitraryChoice<Object>> arbitraryChoices = new HashMap<>();
    private final Map<String, List<Object>> choices = new HashMap<>();
    private final AttributeManager attributes;
    private final DecisionManager decisions;
    private final CombatManager combat;
    private final SpellListManager spells;
    private final ReadOnlyObjectProperty<Deity> deity;
    private final CustomGetter customGetter;
    private final AbilityManager abilities;

    GroovyCommands(CustomGetter customGetter, AbilityManager abilities, AttributeManager attributes, DecisionManager decisions, CombatManager combat, SpellListManager spells, ReadOnlyObjectProperty<Deity> deity, ReadOnlyObjectProperty<Integer> levelProperty) {
        this.abilities = abilities;
        this.attributes = attributes;
        this.decisions = decisions;
        this.combat = combat;
        this.spells = spells;
        this.deity = deity;
        this.customGetter = customGetter;
    }

    public Object get(String path) {
        return customGetter.get(path);
    }
    public Object get(String arg1, String name) {
        String type = arg1.toLowerCase();
        for (Method method : SourcesLoader.class.getMethods()) {
            if(method.getParameterCount() != 0
                    || Modifier.isStatic(method.getModifiers())
                    || !method.canAccess(SourcesLoader.instance()))
                continue;
            if(method.getName().toLowerCase().equals(type)) {
                try {
                    Object loader = method.invoke(SourcesLoader.instance());
                    if(loader instanceof MultiSourceLoader) {
                        return ((MultiSourceLoader) loader).find(name);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    public String archetypeName(Ability ability) {
        return ability.getExtension(ArchetypeExtension.class).getArchetype();
    }
    public boolean archetypeHas(String archetype, String prereqString) {
        return abilities.hasPrereqString(archetype, prereqString);
    }
    public int archetypeCount(Ability ability) {
        return abilities.getArchetypeAbilities(ability.getExtension(ArchetypeExtension.class).getArchetype()).size();
    }
    public void featSlot(String name, Number level, String type) {
        if(applying.get()) {
            FeatSlot featSlot = new FeatSlot(name, level.intValue(), AbilityLoader.getTypes(type));
            slots.put(name, featSlot);
            abilities.apply(featSlot);
        } else {
            abilities.remove(slots.remove(name));
        }
    }
    public void bonus(String str, Integer num) {
        if(applying.get()) {
            mods.merge(str, num, Integer::sum);
        } else {
            mods.merge(str, num, (oldInt, newInt)->oldInt-newInt);
        }
    }
    public void proficiency(String attr, String prof) {
        proficiency(Attribute.valueOf(attr), prof);
    }
    public void proficiency(Attribute attr, String prof) {
        if(applying.get()) {
            attributes.apply(new AttributeMod(attr, Proficiency.valueOf(prof)));
        } else {
            attributes.remove(new AttributeMod(attr, Proficiency.valueOf(prof)));
        }
    }
    public void spell(String spellName, String spellListName) {
        Spell spell = SourcesLoader.instance().spells().find(spellName);
        if(spell != null) {
            spell(spell, spellListName);
        }
    }
    public void spell(Spell spell, String spellListName) {
        if(applying.get()) {
            spells.getSpellList(spellListName).addFocusSpell(spell);
        }else{
            spells.getSpellList(spellListName).removeFocusSpell(spell);
        }
    }
    public void spellSlot(int level, int count, String spellListName) {
        if(applying.get()) {
            spells.getSpellList(spellListName).addSlots(level, count);
        }else{
            spells.getSpellList(spellListName).removeSlots(level, count);
        }
    }
    public void spellSlot(int level, int count, Ability source) {
        spellSlot(level, count, source.getExtension(SpellExtension.class).getSpellListName());
    }
    public void choose(String things, String name, Closure callback, String secondParam, int numSelections) {
        if(applying.get()) {
            if(arbitraryChoices.get(name) != null){
                arbitraryChoices.get(name).increaseChoices(numSelections);
                return;
            }
            List<?> selections = Collections.emptyList();
            Class<?> optionsClass = Object.class;
            String[] listSplit = things.split(" ?: ?");
            String[] words = listSplit[0].split(" ");
            switch (words[0].toLowerCase()){
                case "deitydomain":
                    ObservableList<Domain> domains = FXCollections.observableArrayList();
                    if(deity.get() != null)
                        domains.addAll(deity.get().getDomains());

                    deity.addListener((o, oldVal, newVal)->{
                        domains.removeIf(d->!newVal.getDomains().contains(d));
                        for (Domain domain : newVal.getDomains()) {
                            if(!domains.contains(domain))
                                domains.add(domain);
                        }
                        domains.sort(Comparator.comparing(Object::toString));
                    });
                    selections = domains;
                    optionsClass = Domain.class;
                    break;
                case "divineskill":
                    ObservableList<Attribute> skills = FXCollections.observableArrayList();
                    if(deity.get() != null)
                        skills.addAll(deity.get().getDivineSkillChoices());

                    deity.addListener((o, oldVal, newVal)->{
                        skills.removeIf(s->!newVal.getDivineSkillChoices().contains(s));
                        for (Attribute skill : newVal.getDivineSkillChoices()) {
                            if(!skills.contains(skill))
                                skills.add(skill);
                        }
                        skills.sort(Comparator.comparing(Object::toString));
                    });
                    selections = skills;
                    optionsClass = Attribute.class;
                    break;
                case "weapongroup":
                    selections = new ArrayList<>(SourcesLoader.instance().weapons()
                            .getWeaponGroups().values());
                    optionsClass = WeaponGroup.class;
                    break;
                case "skill":
                    if(words.length > 1){
                        Proficiency min = Proficiency.valueOf(words[1].replaceAll("[()]", ""));
                        selections = attributes.getMinList(min);
                    }else
                        selections = Arrays.stream(Attribute.getSkills()).map(
                                Enum::toString).collect(Collectors.toCollection(ArrayList::new));
                    optionsClass = String.class;
                    break;
                case "saving throw":
                    if(words.length > 1){
                        Proficiency min = Proficiency.valueOf(words[1].replaceAll("[()]", ""));
                        selections = attributes.getMinSavesList(min);
                    }else
                        selections = Arrays.stream(Attribute.getSaves()).map(
                                Enum::toString).collect(Collectors.toCollection(ArrayList::new));
                    optionsClass = String.class;
                    break;
                case "attributes":
                    String[] items = listSplit[1].split("[, ]+");
                    selections = new ArrayList<>(Arrays.asList(items));
                    optionsClass = String.class;
                    break;
            }
            ArbitraryChoice choice = new ArbitraryChoice(name, selections, (response) -> {
                choices.computeIfAbsent(name, (key)->new ArrayList<>()).add(response);
                applying.set(true);
                if(secondParam != null)
                    callback.call(response, secondParam);
                else
                    callback.call(response);
            },(response) -> {
                choices.computeIfAbsent(name, (key)->new ArrayList<>()).remove(response);
                applying.set(false);
                if(secondParam != null)
                    callback.call(response, secondParam);
                else
                    callback.call(response);
            }, numSelections, false, optionsClass);
            arbitraryChoices.put(name, choice);
            decisions.add(choice);
            if(choices.get(name) != null) {
                List<Object> strings = new ArrayList<>(choices.get(name));
                choices.get(name).clear();
                choice.addAll(strings);
            }
        } else {
            if(arbitraryChoices.get(name).getMaxSelections() > numSelections){
                arbitraryChoices.get(name).decreaseChoices(numSelections);
                return;
            }
            if(choices.get(name) != null) {
                List<?> strings = new ArrayList<>(choices.get(name));
                arbitraryChoices.get(name).clear();
                choices.get(name).addAll(strings);
            }
            decisions.remove(arbitraryChoices.get(name));
            arbitraryChoices.remove(name);
        }
    }
    public void weaponGroupProficiency(String group, String prof) {
        if(applying.get()) {
            attributes.apply(new WeaponGroupMod(
                    SourcesLoader.instance().weapons().getWeaponGroups().get(group.toLowerCase()),
                    Proficiency.valueOf(StringUtils.camelCaseWord(prof.trim()))
            ));
        } else {
            attributes.remove(new WeaponGroupMod(
                    SourcesLoader.instance().weapons()
                            .getWeaponGroups().get(group.toLowerCase()),
                    Proficiency.valueOf(StringUtils.camelCaseWord(prof.trim()))
            ));
        }
    }
    public void weaponProficiency(String weaponName, String prof) {
        if(applying.get()) {
            attributes.apply(new WeaponMod(
                    weaponName,
                    Proficiency.valueOf(StringUtils.camelCaseWord(prof.trim()))
            ));
        } else {
            attributes.remove(new WeaponMod(
                    weaponName,
                    Proficiency.valueOf(StringUtils.camelCaseWord(prof.trim()))
            ));
        }
    }
    public void damageModifier(String name, Closure<Damage> modifier) {
        if(applying.get()) {
            combat.addDamageModifier(name, modifier::call);
        } else {
            combat.removeDamageModifier(name);
        }
    }

    void setApplying() {
        applying.set(true);
    }

    void setRemoving() {
        applying.set(false);
    }

    int getMod(String variable) {
        return mods.getOrDefault(variable, 0);
    }

    void reset() {
        choices.clear();
    }
}
