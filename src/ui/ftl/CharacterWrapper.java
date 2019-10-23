package ui.ftl;

import freemarker.template.Configuration;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import model.abilities.Ability;
import model.abilities.AbilitySet;
import model.abilities.Activity;
import model.ability_scores.AbilityScore;
import model.enums.Attribute;
import model.player.InventoryManager;
import model.player.PC;
import ui.ftl.entries.AttributeEntry;
import ui.ftl.entries.SkillEntry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static model.util.StringUtils.camelCaseWord;
import static ui.Main.character;

public class CharacterWrapper implements TemplateHashModel {
    private Map<String, Object> map = new HashMap<>();
    private Configuration cfg;
    CharacterWrapper(PC character, Configuration cfg) {
        this.cfg = cfg;

        //map.put("totalweight", (NumberSupplier) ()->character.inventory().getTotalWeight());

        map.put("attributes", getAttributeMap());

        map.put("abilityMod", new FIHash((s)->
                character.scores().getMod(
                    AbilityScore.valueOf(camelCaseWord(s))),
                ()->false, cfg.getObjectWrapper()));

        map.put("abilityScore", new FIHash((s)->
                character.scores().getScore(AbilityScore.valueOf(camelCaseWord(s))),
                ()->false, cfg.getObjectWrapper()));


        map.put("items", new EquipmentList(character.inventory().getUnequipped(), character.inventory().getEquipped())
        );

        //root.computeIfAbsent("inventory", (key)-> character.inventory());

        map.put("skills", getSkills());
        refresh();
    }

    private Map<String, AttributeEntry> getAttributeMap() {
        Map<String, AttributeEntry> map = new HashMap<>();
        for (Attribute value : Attribute.values()) {
            map.put(value.toString().toLowerCase(), new AttributeEntry(value,
                    character.attributes().getProficiency(value),
                    character.getLevelProperty(),
                    cfg.getObjectWrapper()));
        }


        return map;
    }

    void refresh() {
        updateAbilities();
    }

    private void updateAbilities() {
        List<Ability> abilities = flattenAbilities(character.abilities().getAbilities());
        abilities.sort(((o1, o2) -> {
            if (o1 instanceof Activity && !(o2 instanceof Activity)) return -1;
            if (o2 instanceof Activity && !(o1 instanceof Activity)) return 1;
            return o1.toString().compareTo(o2.toString());
        }));
        map.put("abilities", abilities);
    }

    private List<Ability> flattenAbilities(List<Ability> abilities) {
        List<Ability> items = new ArrayList<>();
        for (Ability ability: abilities) {
            if(!(ability instanceof AbilitySet))
                items.add(ability);
            else{
                items.add(ability);
                items.addAll(flattenAbilities(((AbilitySet) ability).getAbilities()));
            }

        }
        return items;
    }

    private List<SkillEntry> getSkills() {
        List<SkillEntry> entries = new ArrayList<>();
        for (Attribute attribute : Attribute.getSkills()) {
            entries.add(new SkillEntry(attribute, ()->character.getTotalMod(attribute)));
        }
        return entries;
    }

    @Override
    public TemplateModel get(String s) throws TemplateModelException {
        if(map.get(s) == null) {
            for (Method method : PC.class.getMethods()) {
                if (method.getName().toLowerCase().equals("get" + s.toLowerCase())
                        && method.getParameterCount() == 0) {
                    try {
                        return cfg.getObjectWrapper().wrap(method.invoke(character));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (Method method : InventoryManager.class.getMethods()) {
                if (method.getName().toLowerCase().equals("get" + s.toLowerCase())
                        && method.getParameterCount() == 0) {
                    try {
                        return cfg.getObjectWrapper().wrap(method.invoke(character.inventory()));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return cfg.getObjectWrapper().wrap(map.get(s));
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return false;
    }
}
