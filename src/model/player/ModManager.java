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
    private final Map<String, String> choices = new HashMap<>();
    private String currentlyChanging = "";
    private ObservableBindings bindings = new ObservableBindings();

    @FunctionalInterface
    public interface QuadConsumer<T, U, V, W> {
        void apply(T t, U u, V v, W w);
    }

    private class SpecialFunction extends AbstractJSObject {

        private final BiConsumer<Object, Object> consumer;

        public SpecialFunction(BiConsumer<Object, Object> consumer) {
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

    ModManager(PC character, ReadOnlyObjectProperty<Integer> levelProperty) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("js");
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        engine.put("add",
                new SpecialFunction((str, num) -> mods.merge((String)str, ((Number)num).intValue(), Integer::sum)));
        engine.put("subtract",
                new SpecialFunction((str, num) -> mods.merge((String)str, ((Number)num).intValue(), (oldInt, newInt)->oldInt-newInt)));
        engine.put("addChoose", (QuadConsumer<String, String, JSObject, String>)
            (things, name, callback, secondParam)->{
                List<String> selections = Collections.emptyList();
                switch (things.toLowerCase()){
                    case "weapongroup":
                        selections = new ArrayList<>(EquipmentManager.getWeaponGroups().keySet());
                        break;
                    case "skill":
                        selections = Arrays.stream(Attribute.getSkills()).map(
                                Enum::toString).collect(Collectors.toCollection(ArrayList::new));
                        break;
                }
                ArbitraryChoice choice = new ArbitraryChoice(name, selections, (response) -> {
                    choices.put(name, response);
                    callback.call(null, response, secondParam);
                });
                character.addDecision(choice);
                if(choices.get(name) != null)
                    choice.fill(choices.get(name));
            }
        );
        engine.put("removeChoose", (QuadConsumer<String, String, JSObject, String>)
                (things, name, callback, secondParam)->{
                    List<String> selections = Collections.emptyList();
                    switch (things.toLowerCase()){
                        case "weapongroup":
                            selections = new ArrayList<>(EquipmentManager.getWeaponGroups().keySet());
                            break;
                        case "skill":
                            selections = Arrays.stream(Attribute.getSkills()).map(
                                    Enum::toString).collect(Collectors.toCollection(ArrayList::new));
                            break;
                    }
                    character.removeDecision(new ArbitraryChoice(name, selections, (response)-> {
                        callback.call(null, response, secondParam);
                    }));
                }
        );
        AttributeManager attributes = character.attributes();
        engine.put("applySkillProf", new SpecialFunction((attr, prof)->
                attributes.apply(new AttributeMod(Attribute.valueOf((String)attr), Proficiency.valueOf((String)prof)))));
        engine.put("removeSkillProf", new SpecialFunction((attr, prof)->
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
            engine.eval("bonus = add;");
            engine.eval("skillProficiency = applySkillProf;");
            engine.eval("weaponGroupProficiency = applyGroupProf;");
            engine.eval("choose = addChoose;");
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
            engine.eval("bonus = subtract;");
            engine.eval("proficiency = removeSkillProf;");
            engine.eval("weaponGroupProficiency = removeGroupProf;");
            engine.eval("choose = removeChoose;");
            engine.eval(jsString.trim());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public int get(String variable) {
        return mods.getOrDefault(variable, 0);
    }
}
