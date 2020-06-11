package model.player;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import model.WeaponGroupMod;
import model.WeaponMod;
import model.attributes.Attribute;
import model.attributes.AttributeMod;
import model.data_managers.sources.SourcesLoader;
import model.enums.Proficiency;
import model.equipment.weapons.Damage;
import model.util.StringUtils;
import org.codehaus.groovy.runtime.MethodClosure;

import java.util.*;
import java.util.stream.Collectors;

public class GroovyModManager {
    private final ObservableMap<String, Integer> mods = FXCollections.observableHashMap();
    private final Binding bindings = new Binding();
    private final GroovyShell shell = new GroovyShell(bindings);
    private final BooleanProperty applying = new SimpleBooleanProperty(true);
    private final Map<String, ArbitraryChoice> arbitraryChoices = new HashMap<>();
    private final Map<String, List<String>> choices = new HashMap<>();
    private final GroovyCommands commands;
    private final List<String> activeMods = new ArrayList<>();
    private final AttributeManager attributes;
    private final DecisionManager decisions;
    private final CombatManager combat;

    @SuppressWarnings({"rawtypes", "unused"})
    private class GroovyCommands {
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
        public void choose(String things, String name, Closure callback, String secondParam, int numSelections) {
            if(applying.get()) {
                if(arbitraryChoices.get(name) != null){
                    arbitraryChoices.get(name).increaseChoices(numSelections);
                    return;
                }
                List<String> selections = Collections.emptyList();
                String[] listSplit = things.split(" ?: ?");
                String[] words = listSplit[0].split(" ");
                switch (words[0].toLowerCase()){
                    case "weapongroup":
                        selections = SourcesLoader.instance().find("Core Rulebook").getWeapons()
                                .getWeaponsGroups().keySet()
                                .stream().map(StringUtils::camelCase).collect(Collectors.toList());
                        break;
                    case "skill":
                        if(words.length > 1){
                            Proficiency min = Proficiency.valueOf(words[1].replaceAll("[()]", ""));
                            selections = attributes.getMinList(min);

                        }else
                            selections = Arrays.stream(Attribute.getSkills()).map(
                                    Enum::toString).collect(Collectors.toCollection(ArrayList::new));
                        break;
                    case "attributes":
                        String[] items = listSplit[1].split("[, ]+");
                        selections = new ArrayList<>(Arrays.asList(items));
                        break;
                }
                ArbitraryChoice choice = new ArbitraryChoice(name, selections, (response) -> {
                    choices.computeIfAbsent(name, (key)->new ArrayList<>()).add(response);
                    applying.set(true);
                    callback.call(response, secondParam);
                },(response) -> {
                    choices.computeIfAbsent(name, (key)->new ArrayList<>()).remove(response);
                    applying.set(false);
                    callback.call(response, secondParam);
                }, numSelections, false);
                arbitraryChoices.put(name, choice);
                decisions.add(choice);
                if(choices.get(name) != null) {
                    List<String> strings = new ArrayList<>(choices.get(name));
                    choices.get(name).clear();
                    choice.addAll(strings);
                }
            } else {
                if(arbitraryChoices.get(name).getNumSelections() > numSelections){
                    arbitraryChoices.get(name).decreaseChoices(numSelections);
                    return;
                }
                if(choices.get(name) != null) {
                    List<String> strings = new ArrayList<>(choices.get(name));
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
                        SourcesLoader.instance().find("Core Rulebook").getWeapons()
                                .getWeaponsGroups().get(group.toLowerCase()),
                        Proficiency.valueOf(StringUtils.camelCaseWord(prof.trim()))
                ));
            } else {
                attributes.remove(new WeaponGroupMod(
                        SourcesLoader.instance().find("Core Rulebook").getWeapons()
                                .getWeaponsGroups().get(group.toLowerCase()),
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

    GroovyModManager(CustomGetter customGetter, AttributeManager attributes, DecisionManager decisions, CombatManager combat, ReadOnlyObjectProperty<Integer> levelProperty, Applier applier) {
        this.attributes = attributes;
        this.decisions = decisions;
        this.combat = combat;
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
        bindings.setVariable("get", new MethodClosure(customGetter, "get"));
        bindings.setVariable("log", new MethodClosure(System.out, "println"));
        addCommand("bonus");
        addCommand("proficiency");
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
