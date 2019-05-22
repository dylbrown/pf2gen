package model.player;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.TextInputDialog;
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
import java.util.function.Function;

class ModManager {
    private ScriptEngine engine;
    private ObservableMap<String, Integer> mods = FXCollections.observableHashMap();
    private List<String> jsStrings = new ArrayList<>();
    private Map<String, List<String>> variables = new HashMap<>();
    private String currentlyChanging = "";
    private ObservableBindings bindings = new ObservableBindings();

    ModManager(AttributeManager attributes, ReadOnlyObjectProperty<Integer> levelProperty) {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("js");
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        bindings.getMap().addListener((MapChangeListener<String, Object>) change -> {
            if(change.wasAdded() && !currentlyChanging.equals("")) {
                variables.computeIfAbsent(currentlyChanging, (key)->new ArrayList<>()).add(change.getKey());
            }
        });
        BiConsumer<String, Number> add = (str, num) -> mods.merge(str, num.intValue(), Integer::sum);
        engine.put("add", add);
        BiConsumer<String, Number> subtract = (str, num) -> mods.merge(str, num.intValue(), (oldInt, newInt)->oldInt-newInt);
        engine.put("subtract", subtract);
        engine.put("doPrompt", (Function<String, String>)(type) -> {
            TextInputDialog dialog = new TextInputDialog();
            switch(type.toLowerCase()) {
                case "skill":
                    dialog.setContentText("Type in a Skill Name");
                    while (true) {
                        Optional<String> s = dialog.showAndWait();
                        if (s.isPresent()) {
                            try {
                                if (Arrays.asList(Attribute.getSkills()).contains(Attribute.valueOf(s.get())))
                                    return s.get();
                            } catch (Exception ignored) {}
                        }
                    }
                case "weapongroup":
                    dialog.setContentText("Type in a Weapon Group");
                    while (true) {
                        Optional<String> s = dialog.showAndWait();
                        if (s.isPresent()) {
                            try {
                                if (EquipmentManager.getWeaponGroups().get(s.get().toLowerCase()) != null)
                                    return s.get();
                            } catch (Exception ignored) {}
                        }
                    }
            }
            return "";
        });
        BiConsumer<String, String> applySkillProf = (attr, prof)-> attributes.apply(new AttributeMod(Attribute.valueOf(attr), Proficiency.valueOf(prof)));
        engine.put("applySkillProf", applySkillProf);
        BiConsumer<String, String> removeSkillProf = (attr, prof)-> attributes.remove(new AttributeMod(Attribute.valueOf(attr), Proficiency.valueOf(prof)));
        engine.put("removeSkillProf", removeSkillProf);
        BiConsumer<String, String> applyGroupProf = (group, prof)-> attributes.apply(new WeaponGroupMod(EquipmentManager.getWeaponGroups().get(group.toLowerCase()), Proficiency.valueOf(prof)));
        engine.put("applyGroupProf", applyGroupProf);
        BiConsumer<String, String> removeGroupProf = (group, prof)-> attributes.remove(new WeaponGroupMod(EquipmentManager.getWeaponGroups().get(group.toLowerCase()), Proficiency.valueOf(prof)));
        engine.put("removeGroupProf", removeGroupProf);
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
        currentlyChanging = jsString;
        jsStrings.add(jsString);
        apply(jsString);
        currentlyChanging = "";
    }

    private void apply(String jsString){
        try {
            engine.eval("bonus = add;");
            engine.eval("skillProficiency = applySkillProf;");
            engine.eval("weaponGroupProficiency = applyGroupProf;");
            engine.eval(jsString.trim());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    void jsRemove(String jsString) {
        remove(jsString);
        jsStrings.remove(jsString);
        for (String s : variables.getOrDefault(jsString, Collections.emptyList())) {
            bindings.remove(s);
        }
        variables.remove(jsString);
    }
    private void remove(String jsString) {
        try {
            engine.eval("bonus = subtract;");
            engine.eval("proficiency = removeProf;");
            engine.eval("weaponGroupProficiency = removeGroupProf;");
            engine.eval(jsString.trim());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public int get(String variable) {
        return mods.getOrDefault(variable, 0);
    }
}
