package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;
import model.AttributeMod;
import model.WeaponGroupMod;
import model.data_managers.EquipmentManager;
import model.enums.Attribute;
import model.enums.Proficiency;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

class ModManager {
    private ScriptEngine engine;
    private ObservableMap<String, Integer> mods = FXCollections.observableHashMap();
    private List<String> jsStrings = new ArrayList<>();
    private final Map<String, List<String>> choices = new HashMap<>();
    private Map<String, ArbitraryChoice> arbitraryChoices = new HashMap<>();
    private String currentlyChanging = "";
    private ObservableBindings bindings = new ObservableBindings();

    @FunctionalInterface
    public interface QuinConsumer<T, U, V, W, X> {
        void apply(T t, U u, V v, W w, X x);
    }

    private class SpecialFunction extends AbstractJSObject {

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
    }

    ModManager(PC character, ReadOnlyObjectProperty<Integer> levelProperty) {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("js");
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        engine.put("add",
                new SpecialFunction((str, num) -> mods.merge((String)str, ((Number)num).intValue(), Integer::sum)));
        engine.put("subtract",
                new SpecialFunction((str, num) -> mods.merge((String)str, ((Number)num).intValue(), (oldInt, newInt)->oldInt-newInt)));
        engine.put("addChoose", (QuinConsumer<String, String, JSObject, String, Integer>)
            (things, name, callback, secondParam, numSelections)->{
                List<String> selections = Collections.emptyList();
                String[] split = things.replaceAll(":", "").split("[, ]+");
                switch (split[0].toLowerCase()){
                    case "weapongroup":
                        selections = new ArrayList<>(EquipmentManager.getWeaponGroups().keySet());
                        break;
                    case "skill":
                        selections = Arrays.stream(Attribute.getSkills()).map(
                                Enum::toString).collect(Collectors.toCollection(ArrayList::new));
                        break;
                    case "attributes":
                        selections = new ArrayList<>(Arrays.asList(split).subList(1, split.length));
                        break;
                }
                ArbitraryChoice choice = new ArbitraryChoice(name, selections, (response) -> {
                    choices.computeIfAbsent(name, (key)->new ArrayList<>()).add(response);
                    try {
                        setAdd();
                        callback.call(null, response, secondParam);
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                },(response) -> {
                    choices.computeIfAbsent(name, (key)->new ArrayList<>()).remove(response);
                    try {
                        setRemove();
                        callback.call(null, response, secondParam);
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                }, numSelections);
                arbitraryChoices.put(name, choice);
                character.decisions().add(choice);
                if(choices.get(name) != null)
                    for(String selection: choices.get(name))
                        choice.add(selection);
            }
        );
        engine.put("removeChoose", (QuinConsumer<String, String, JSObject, String, Integer>)
                (things, name, callback, secondParam, numSelections)->{
                    character.decisions().remove(arbitraryChoices.get(name));
                    arbitraryChoices.remove(name);
                }
        );
        AttributeManager attributes = character.attributes();
        engine.put("applyProf", new SpecialFunction((attr, prof)->
                attributes.apply(new AttributeMod(Attribute.valueOf((String)attr), Proficiency.valueOf((String)prof)))));
        engine.put("removeProf", new SpecialFunction((attr, prof)->
                attributes.remove(new AttributeMod(Attribute.valueOf((String)attr), Proficiency.valueOf((String)prof)))));
        engine.put("applyGroupProf", new SpecialFunction((group, prof)->
                attributes.apply(new WeaponGroupMod(EquipmentManager.getWeaponGroups().get(((String)group).toLowerCase()),
                        Proficiency.valueOf((String)prof)))));
        engine.put("removeGroupProf", new SpecialFunction((group, prof)->
                attributes.remove(new WeaponGroupMod(EquipmentManager.getWeaponGroups().get(((String)group).toLowerCase()),
                        Proficiency.valueOf((String)prof)))));
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
    }
    void jsApply(String jsString) {
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

    void jsRemove(String jsString) {
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
