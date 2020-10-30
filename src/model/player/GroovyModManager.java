package model.player;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import javafx.beans.property.ReadOnlyObjectProperty;
import model.abilities.Ability;
import org.codehaus.groovy.runtime.MethodClosure;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class GroovyModManager implements PlayerState {
    private final Binding bindings = new Binding();
    private final GroovyShell shell = new GroovyShell(bindings);
    private final GroovyCommands commands;
    private final Set<Ability> activeMods = new HashSet<>();
    private final Set<Ability> activeAlwaysRecalculateMods = new HashSet<>();

    GroovyModManager(GroovyCommands commands, Applier applier, ReadOnlyObjectProperty<Integer> levelProperty) {
        applier.onPreApply(preAbility -> {
            for (Ability ability : activeAlwaysRecalculateMods) {
                bindings.setProperty("ability", ability);
                remove(ability.getCustomMod());
            }
        });
        applier.onApply(newAbility -> {
            if (!newAbility.getCustomMod().equals("")) {
                bindings.setProperty("ability", newAbility);
                apply(newAbility.getCustomMod());
                switch (newAbility.getRecalculate()) {
                    case Always:
                        activeAlwaysRecalculateMods.add(newAbility);
                    case OnLevel:
                        activeMods.add(newAbility);
                }
            }
            for (Ability ability : activeAlwaysRecalculateMods) {
                if(ability == newAbility) continue;
                bindings.setProperty("ability", ability);
                apply(ability.getCustomMod());
            }
        });

        applier.onPreRemove(preAbility -> {
            for (Ability ability : activeAlwaysRecalculateMods) {
                if(ability == preAbility) continue;
                bindings.setProperty("ability", ability);
                remove(ability.getCustomMod());
            }
        });
        applier.onRemove(oldAbility -> {
            if(!oldAbility.getCustomMod().equals("")) {
                bindings.setProperty("ability", oldAbility);
                remove(oldAbility.getCustomMod());
                switch (oldAbility.getRecalculate()) {
                    case Always:
                        activeAlwaysRecalculateMods.remove(oldAbility);
                    case OnLevel:
                        activeMods.add(oldAbility);
                }
            }
            for (Ability ability : activeAlwaysRecalculateMods) {
                bindings.setProperty("ability", ability);
                apply(ability.getCustomMod());
            }
        });

        this.commands = commands;
        bindings.setVariable("log", new MethodClosure(System.out, "println"));
        for (Method method : commands.getClass().getMethods()) {
            String commandName = method.getName();
            bindings.setVariable(commandName, new MethodClosure(commands, commandName));
        }

        bindings.setVariable("level", levelProperty.getValue());
        levelProperty.addListener((event)-> {
            for (Ability ability : activeMods) {
                bindings.setProperty("ability", ability);
                remove(ability.getCustomMod());
            }
            bindings.setVariable("level", levelProperty.get());
            for (Ability ability : activeMods) {
                bindings.setProperty("ability", ability);
                apply(ability.getCustomMod());
            }
        });
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

    @Override
    public void reset(PC.ResetEvent resetEvent) {
        commands.reset();
    }
}
