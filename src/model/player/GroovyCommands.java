package model.player;

import groovy.lang.Closure;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import model.enums.WeaponProficiency;
import model.items.weapons.WeaponGroupMod;
import model.items.weapons.WeaponMod;
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
import model.items.weapons.Damage;
import model.items.weapons.WeaponGroup;
import model.spells.Spell;
import model.util.ObjectNotFoundException;
import model.util.StringUtils;
import model.xml_parsers.AbilityLoader;
import model.setting.Deity;
import model.setting.Domain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked"})
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
    private final SourcesManager sources;

    GroovyCommands(CustomGetter customGetter, SourcesManager sources, AbilityManager abilities, AttributeManager attributes, DecisionManager decisions, CombatManager combat, SpellListManager spells, ReadOnlyObjectProperty<Deity> deity, ReadOnlyObjectProperty<Integer> levelProperty) {
        this.sources = sources;
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
                } catch (IllegalAccessException | InvocationTargetException | ObjectNotFoundException e) {
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
        Spell spell = null;
        try {
            spell(sources.spells().find(spellName), spellListName);
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
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
    public void choose(String things, String name, Closure callback, String secondParam, int maxSelections) {
        if(applying.get()) {
            if(arbitraryChoices.get(name) != null){
                arbitraryChoices.get(name).increaseChoices(maxSelections);
                return;
            }
            String[] listSplit = things.split(" ?: ?");
            String[] words = listSplit[0].split(" ");
            ArbitraryChoice.Builder<?> builder = null;
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
                    ArbitraryChoice.Builder<Domain> domain = new ArbitraryChoice.Builder<>();
                    domain.setChoices(domains);
                    domain.setOptionsClass(Domain.class);
                    builder = domain;
                    break;
                case "divineskill":
                    ObservableList<Attribute> skills = FXCollections.observableArrayList();
                    if(deity.get() != null)
                        skills.addAll(deity.get().getDivineSkillChoices());

                    deity.addListener((o, oldVal, newVal)->{
                        if(newVal == null) {
                            skills.clear();
                        }else {
                            skills.removeIf(s -> !newVal.getDivineSkillChoices().contains(s));
                            for (Attribute skill : newVal.getDivineSkillChoices()) {
                                if (!skills.contains(skill))
                                    skills.add(skill);
                            }
                            skills.sort(Comparator.comparing(Object::toString));
                        }
                    });
                    ArbitraryChoice.Builder<Attribute> attribute = new ArbitraryChoice.Builder<>();
                    attribute.setChoices(skills);
                    attribute.setOptionsClass(Attribute.class);
                    builder = attribute;
                    break;
                case "weapongroup":
                    ArbitraryChoice.Builder<WeaponGroup> groups = new ArbitraryChoice.Builder<>();
                    groups.setChoices(FXCollections.observableArrayList(sources.weapons()
                            .getWeaponGroups().values()));
                    groups.setOptionsClass(WeaponGroup.class);
                    builder = groups;
                    break;
                case "skill":
                    ArbitraryChoice.Builder<String> stringChoice = new ArbitraryChoice.Builder<>();
                    if(words.length > 1){
                        Proficiency min = Proficiency.valueOf(words[1].replaceAll("[()]", ""));
                        stringChoice.setChoices(attributes.getMinList(min));
                    }else
                        stringChoice.setChoices(Arrays.stream(Attribute.getSkills()).map(
                                Enum::toString).collect(Collectors.toCollection(FXCollections::observableArrayList)));
                    stringChoice.setOptionsClass(String.class);
                    builder = stringChoice;
                    break;
                case "saving throw":
                    stringChoice = new ArbitraryChoice.Builder<>();
                    if(words.length > 1){
                        Proficiency min = Proficiency.valueOf(words[1].replaceAll("[()]", ""));
                        stringChoice.setChoices(attributes.getMinSavesList(min));
                    }else
                        stringChoice.setChoices(Arrays.stream(Attribute.getSaves()).map(
                                Enum::toString).collect(Collectors.toCollection(FXCollections::observableArrayList)));
                    stringChoice.setOptionsClass(String.class);
                    builder = stringChoice;
                    break;
                case "attributes":
                    String[] items = listSplit[1].split("[, ]+");
                    stringChoice = new ArbitraryChoice.Builder<>();
                    stringChoice.setChoices(FXCollections.observableArrayList(items));
                    stringChoice.setOptionsClass(String.class);
                    builder = stringChoice;
                    break;
            }
            if(builder == null)
                builder = new ArbitraryChoice.Builder<String>();
            builder.setName(name);
            builder.setFillFunction((response) -> {
                choices.computeIfAbsent(name, (key)->new ArrayList<>()).add(response);
                applying.set(true);
                if(secondParam != null)
                    callback.call(response, secondParam);
                else
                    callback.call(response);
            });
            builder.setEmptyFunction((response) -> {
                choices.computeIfAbsent(name, (key)->new ArrayList<>()).remove(response);
                applying.set(false);
                if(secondParam != null)
                    callback.call(response, secondParam);
                else
                    callback.call(response);
            });
            builder.setMaxSelections(maxSelections);

            ArbitraryChoice choice = builder.build();
            arbitraryChoices.put(name, choice);
            decisions.add(choice);
            if(choices.get(name) != null) {
                List<Object> strings = new ArrayList<>(choices.get(name));
                choices.get(name).clear();
                choice.addAll(strings);
            }
        } else {
            if(arbitraryChoices.get(name).getMaxSelections() > maxSelections){
                arbitraryChoices.get(name).decreaseChoices(maxSelections);
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
        weaponGroupProficiency(sources.weapons().getWeaponGroups().get(group),
                prof);
    }
    public void weaponGroupProficiency(WeaponGroup weaponGroup, String prof) {
        if(applying.get()) {
            attributes.apply(new WeaponGroupMod(
                    weaponGroup,
                    Proficiency.valueOf(StringUtils.camelCaseWord(prof.trim()))
            ));
        } else {
            attributes.remove(new WeaponGroupMod(
                    weaponGroup,
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
    public void weaponProficiencyTranslator(Closure<WeaponProficiency> translator) {
        if(applying.get()) {
            attributes.apply(translator::call);
        } else {
            attributes.remove(translator::call);
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
