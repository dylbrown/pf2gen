package model.player;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import javafx.beans.property.ReadOnlyObjectProperty;
import model.abilities.Ability;
import model.items.Item;
import org.codehaus.groovy.runtime.MethodClosure;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class GroovyModManager implements PlayerState {
    private final Binding bindings = new Binding();
    private final GroovyShell shell = new GroovyShell(bindings);
    private final GroovyCommands commands;
    private final Set<Ability> activeLevelAbilities = new HashSet<>();
    private final Set<Ability> activeAlwaysRecalculateAbilities = new HashSet<>();
    private final Set<Item> activeLevelItems = new HashSet<>();
    private final Set<Item> activeAlwaysRecalculateItems = new HashSet<>();

    GroovyModManager(GroovyCommands commands,
                     Applier<Ability> abilityApplier,
                     Applier<Item> itemApplier,
                     ReadOnlyObjectProperty<Integer> levelProperty) {
        abilityApplier.onPreApply(preAbility -> prepareToChange(null, null));
        abilityApplier.onApply(newAbility -> {
            if (!newAbility.getCustomMod().equals("")) {
                bindings.setProperty("ability", newAbility);
                apply(newAbility.getCustomMod());
                switch (newAbility.getRecalculate()) {
                    case Always:
                        activeAlwaysRecalculateAbilities.add(newAbility);
                    case OnLevel:
                        activeLevelAbilities.add(newAbility);
                }
            }
            finishChanging(newAbility, null);
        });

        abilityApplier.onPreRemove(preAbility->prepareToChange(preAbility, null));
        abilityApplier.onRemove(oldAbility -> {
            if(!oldAbility.getCustomMod().equals("")) {
                bindings.setProperty("ability", oldAbility);
                remove(oldAbility.getCustomMod());
                switch (oldAbility.getRecalculate()) {
                    case Always:
                        activeAlwaysRecalculateAbilities.remove(oldAbility);
                    case OnLevel:
                        activeLevelAbilities.add(oldAbility);
                }
            }
            finishChanging(null, null);
        });

        itemApplier.onPreApply(preItem -> prepareToChange(null, null));
        itemApplier.onApply(newItem -> {
            if (!newItem.getCustomMod().equals("")) {
                bindings.setProperty("item", newItem);
                apply(newItem.getCustomMod());
                switch (newItem.getRecalculate()) {
                    case Always:
                        activeAlwaysRecalculateItems.add(newItem);
                    case OnLevel:
                        activeLevelItems.add(newItem);
                }
            }
            finishChanging(null, newItem);
        });

        itemApplier.onPreRemove(preItem->prepareToChange(null, preItem));
        itemApplier.onRemove(oldItem -> {
            if(!oldItem.getCustomMod().equals("")) {
                bindings.setProperty("item", oldItem);
                remove(oldItem.getCustomMod());
                switch (oldItem.getRecalculate()) {
                    case Always:
                        activeAlwaysRecalculateItems.remove(oldItem);
                    case OnLevel:
                        activeLevelItems.add(oldItem);
                }
            }
            finishChanging(null, null);
        });

        this.commands = commands;
        bindings.setVariable("log", new MethodClosure(System.out, "println"));
        for (Method method : commands.getClass().getMethods()) {
            String commandName = method.getName();
            bindings.setVariable(commandName, new MethodClosure(commands, commandName));
        }

        bindings.setVariable("level", levelProperty.getValue());
        levelProperty.addListener((event)-> {
            for (Ability ability : activeLevelAbilities) {
                bindings.setProperty("ability", ability);
                remove(ability.getCustomMod());
            }
            for (Item item : activeLevelItems) {
                bindings.setProperty("item", item);
                remove(item.getCustomMod());
            }
            bindings.setVariable("level", levelProperty.get());
            for (Ability ability : activeLevelAbilities) {
                bindings.setProperty("ability", ability);
                apply(ability.getCustomMod());
            }
            for (Item item : activeLevelItems) {
                bindings.setProperty("item", item);
                apply(item.getCustomMod());
            }
        });
    }

    private void prepareToChange(Ability changingAbility, Item changingItem) {
        for (Ability ability : activeAlwaysRecalculateAbilities) {
            if(ability == changingAbility) continue;
            bindings.setProperty("ability", ability);
            remove(ability.getCustomMod());
        }
        for (Item item : activeAlwaysRecalculateItems) {
            if(item == changingItem) continue;
            bindings.setProperty("item", item);
            remove(item.getCustomMod());
        }
    }

    private void finishChanging(Ability changingAbility, Item changingItem) {
        for (Ability ability : activeAlwaysRecalculateAbilities) {
            if(ability == changingAbility) continue;
            bindings.setProperty("ability", ability);
            apply(ability.getCustomMod());
        }
        for (Item item : activeAlwaysRecalculateItems) {
            if(item == changingItem) continue;
            bindings.setProperty("item", item);
            apply(item.getCustomMod());
        }
    }

    private void apply(String customMod) {
        commands.setApplying();
        bindings.setVariable("applying", true);
        shell.parse(customMod).run();
    }

    private void remove(String customMod) {
        commands.setRemoving();
        bindings.setVariable("applying", false);
        shell.parse(customMod).run();
    }

    public int get(String variable) {
        return commands.getMod(variable);
    }

    public void refreshAlways() {
        for (Ability ability : activeAlwaysRecalculateAbilities) {
            bindings.setProperty("ability", ability);
            remove(ability.getCustomMod());
        }
        for (Ability ability : activeAlwaysRecalculateAbilities) {
            bindings.setProperty("ability", ability);
            apply(ability.getCustomMod());
        }
    }

    @Override
    public void reset(PC.ResetEvent resetEvent) {
        commands.reset();
    }
}
