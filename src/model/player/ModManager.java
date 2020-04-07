package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import model.WeaponGroupMod;
import model.attributes.Attribute;
import model.attributes.AttributeMod;
import model.data_managers.EquipmentManager;
import model.enums.Proficiency;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

class ModManager {
    private final ScriptEngine engine;
    private final ObservableMap<String, Integer> mods = FXCollections.observableHashMap();
    private final List<String> jsStrings = new ArrayList<>();
    private final Map<String, List<String>> choices = new HashMap<>();
    private final Map<String, ArbitraryChoice> arbitraryChoices = new HashMap<>();
    private String currentlyChanging = "";

    @FunctionalInterface
    interface QuinConsumer<T, U, V, W, X> {
        void apply(T t, U u, V v, W w, X x);
    }

    /*private class SpecialFunction extends AbstractJSObject {

        private final BiConsumer<Object, Object> consumer;

        SpecialFunction(BiConsumer<Object, Object> consumer) {
            this.consumer = consumer;
        }

        @Override
        public Object call(Object thiz, Object... args) {
            consumer.accept(args[0], args[1]);
            return null;
        }

        @Override
        public boolean isFunction() {
            return true;
        }
    }*/

    ModManager(PC character, ReadOnlyObjectProperty<Integer> levelProperty, Applier applier) {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("graal.js");
        ObservableBindings bindings = new ObservableBindings();
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowAllAccess", true);

        engine.put("add", (BiConsumer<String, Number>)
                (str, num) -> mods.merge(str, num.intValue(), Integer::sum));

        engine.put("subtract",(BiConsumer<String, Number>)
                (str, num) -> mods.merge(str, num.intValue(), (oldInt, newInt)->oldInt-newInt));

        engine.put("addChoose", (QuinConsumer<String, String, BiConsumer<Object, Object>, String, Integer>)
            (things, name, callback, secondParam, numSelections)->{
                if(arbitraryChoices.get(name) != null){
                    add(arbitraryChoices.get(name), numSelections);
                    return;
                }
                List<String> selections = Collections.emptyList();
                String[] listSplit = things.split(" ?: ?");
                String[] words = listSplit[0].split(" ");
                switch (words[0].toLowerCase()){
                    case "weapongroup":
                        selections = new ArrayList<>(EquipmentManager.getWeaponGroups().keySet());
                        break;
                    case "skill":
                        if(words.length > 1){
                            Proficiency min = Proficiency.valueOf(words[1].replaceAll("[()]", ""));
                            selections = character.attributes().getMinList(min);

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
                    try {
                        setAdd();
                        callback.accept(response, secondParam);
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                },(response) -> {
                    choices.computeIfAbsent(name, (key)->new ArrayList<>()).remove(response);
                    try {
                        setRemove();
                        callback.accept(response, secondParam);
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                }, numSelections);
                arbitraryChoices.put(name, choice);
                character.decisions().add(choice);
                if(choices.get(name) != null)
                        choice.addAll(choices.get(name));
            }
        );
        engine.put("removeChoose", (QuinConsumer<String, String, BiConsumer<Object, Object>, String, Integer>)
                (things, name, callback, secondParam, numSelections)->{
                    if(arbitraryChoices.get(name).getNumSelections() > numSelections){
                        subtract(arbitraryChoices.get(name), numSelections);
                        return;
                    }
                    character.decisions().remove(arbitraryChoices.get(name));
                    arbitraryChoices.remove(name);
                }
        );
        AttributeManager attributes = character.attributes();
        engine.put("applyProf", (BiConsumer<String, String>)(attr, prof)->
                attributes.apply(new AttributeMod(Attribute.valueOf(attr), Proficiency.valueOf(prof))));
        engine.put("removeProf", (BiConsumer<String, String>)(attr, prof)->
                attributes.remove(new AttributeMod(Attribute.valueOf(attr), Proficiency.valueOf(prof))));
        engine.put("applyGroupProf", (BiConsumer<String, String>)(group, prof)->
                attributes.apply(new WeaponGroupMod(EquipmentManager.getWeaponGroups().get(group.toLowerCase()),
                        Proficiency.valueOf(prof))));
        engine.put("removeGroupProf", (BiConsumer<String, String>)(group, prof)->
                attributes.remove(new WeaponGroupMod(EquipmentManager.getWeaponGroups().get(group.toLowerCase()),
                        Proficiency.valueOf(prof))));
        // TODO: Support Individual Weapon Proficiencies
        // TODO: Support Weapon Specialization
        engine.put("level", levelProperty.getValue());
        levelProperty.addListener((event)-> {
            for (String jsString : jsStrings) {
                remove(jsString);
            }
            engine.put("level", levelProperty.get());
            for (String jsString : jsStrings) {
                apply(jsString);
            }
        });

        applier.onApply(ability -> {
            if (!ability.getCustomMod().equals(""))
                jsApply(ability.getCustomMod());
        });

        applier.onRemove(ability -> {
            if(!ability.getCustomMod().equals(""))
                jsRemove(ability.getCustomMod());
        });
    }

    private void add(ArbitraryChoice arbitraryChoice, int amount) {
        arbitraryChoice.increaseChoices(amount);
    }

    private void subtract(ArbitraryChoice arbitraryChoice, int amount) {
        arbitraryChoice.decreaseChoices(amount);
    }
    private void jsApply(String jsString) {
        jsStrings.add(jsString);
        apply(jsString);
    }

    private void apply(String jsString){
        try {
            setAdd();
            engine.eval(jsString.trim());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private void jsRemove(String jsString) {
        remove(jsString);
        jsStrings.remove(jsString);
    }
    private void remove(String jsString) {
        try {
            setRemove();
            engine.eval(jsString.trim());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private void setAdd() throws ScriptException {
        engine.eval("bonus = add;");
        engine.eval("proficiency = applyProf;");
        engine.eval("weaponGroupProficiency = applyGroupProf;");
        engine.eval("choose = addChoose;");
    }

    private void setRemove() throws ScriptException {
        engine.eval("bonus = subtract;");
        engine.eval("proficiency = removeProf;");
        engine.eval("weaponGroupProficiency = removeGroupProf;");
        engine.eval("choose = removeChoose;");
    }

    public int get(String variable) {
        return mods.getOrDefault(variable, 0);
    }
}
