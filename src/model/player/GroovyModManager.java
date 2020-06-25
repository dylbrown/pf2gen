package model.player;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import model.WeaponGroupMod;
import model.WeaponMod;
import model.attributes.Attribute;
import model.attributes.AttributeMod;
import model.data_managers.sources.MultiSourceLoader;
import model.data_managers.sources.SourcesLoader;
import model.enums.Proficiency;
import model.equipment.weapons.Damage;
import model.equipment.weapons.WeaponGroup;
import model.spells.Spell;
import model.util.StringUtils;
import org.codehaus.groovy.runtime.MethodClosure;
import setting.Deity;
import setting.Domain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class GroovyModManager {
    private final ObservableMap<String, Integer> mods = FXCollections.observableHashMap();
    private final Binding bindings = new Binding();
    private final GroovyShell shell = new GroovyShell(bindings);
    private final BooleanProperty applying = new SimpleBooleanProperty(true);
    private final Map<String, ArbitraryChoice<Object>> arbitraryChoices = new HashMap<>();
    private final Map<String, List<Object>> choices = new HashMap<>();
    private final GroovyCommands commands;
    private final List<String> activeMods = new ArrayList<>();
    private final AttributeManager attributes;
    private final DecisionManager decisions;
    private final CombatManager combat;
    private final SpellManager spells;
    private final ReadOnlyObjectProperty<Deity> deity;
    private final CustomGetter customGetter;

    @SuppressWarnings({"rawtypes", "unused", "unchecked"})
    private class GroovyCommands {
        public Object get(String arg1, String name) {
            if(name == null) return customGetter.get(arg1);
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
        public void bonus(String str, Integer num) {
            if(applying.get()) {
                mods.merge(str, num, Integer::sum);
            } else {
                mods.merge(str, num, (oldInt, newInt)->oldInt-newInt);
            }
        }
        public void proficiency(String attr, String prof) {
            if(applying.get()) {
                attributes.apply(new AttributeMod(Attribute.valueOf(attr), Proficiency.valueOf(prof)));
            } else {
                attributes.remove(new AttributeMod(Attribute.valueOf(attr), Proficiency.valueOf(prof)));
            }
        }
        public void spell(String spellName) {
            Spell spell = SourcesLoader.instance().spells().find(spellName);
            if(spell != null) {
               spell(spell);
            }
        }
        public void spell(Spell spell) {
            if(applying.get()) {
                spells.addFocusSpell(spell);
            }else{
                spells.removeFocusSpell(spell);
            }
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
                        if(deity.get() != null) {
                            domains.addAll(deity.get().getDomains());
                        }
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
                if(arbitraryChoices.get(name).getNumSelections() > numSelections){
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
    }

    GroovyModManager(CustomGetter customGetter, AttributeManager attributes, DecisionManager decisions, CombatManager combat, SpellManager spells, ReadOnlyObjectProperty<Deity> deity, ReadOnlyObjectProperty<Integer> levelProperty, Applier applier) {
        this.attributes = attributes;
        this.decisions = decisions;
        this.combat = combat;
        this.spells = spells;
        this.deity = deity;
        this.customGetter = customGetter;
        applier.onApply(ability -> {
            if (!ability.getCustomMod().equals("")) {
                apply(ability.getCustomMod());
                activeMods.add(ability.getCustomMod());
            }
        });

        applier.onRemove(ability -> {
            if(!ability.getCustomMod().equals("")) {
                remove(ability.getCustomMod());
                activeMods.remove(ability.getCustomMod());
            }
        });

        this.commands = new GroovyCommands();
        bindings.setVariable("log", new MethodClosure(System.out, "println"));
        addCommand("get");
        addCommand("bonus");
        addCommand("proficiency");
        addCommand("spell");
        addCommand("choose");
        addCommand("weaponGroupProficiency");
        addCommand("weaponProficiency");
        addCommand("damageModifier");

        bindings.setVariable("level", levelProperty.getValue());
        levelProperty.addListener((event)-> {
            for (String activeMod : activeMods) {
                remove(activeMod);
            }
            bindings.setVariable("level", levelProperty.get());
            for (String activeMod : activeMods) {
                apply(activeMod);
            }
        });
    }

    private void addCommand(String commandName) {
        bindings.setVariable(commandName, new MethodClosure(commands, commandName));
    }

    private void apply(String customMod) {
        applying.set(true);
        shell.parse(customMod).run();
    }

    private void remove(String customMod) {
        applying.set(false);
        shell.parse(customMod).run();
    }

    public int get(String variable) {
        return mods.getOrDefault(variable, 0);
    }
}
