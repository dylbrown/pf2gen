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
import model.enums.Slot;
import model.equipment.ItemCount;
import model.player.InventoryManager;
import model.player.PC;
import ui.ftl.entries.AttributeEntry;
import ui.ftl.entries.ItemCountWrapper;
import ui.ftl.entries.SkillEntry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static model.util.StringUtils.camelCaseWord;
import static ui.Main.character;

class CharacterWrapper implements TemplateHashModel {
    private final Map<String, Object> map = new HashMap<>();
    private final Configuration cfg;
    CharacterWrapper(PC character, Configuration cfg) {
        this.cfg = cfg;

        //map.put("totalweight", (NumberSupplier) ()->character.inventory().getTotalWeight());

        map.put("attributes", getAttributeMap());
        map.put("attacks", character.getAttacks().stream()
                .map((o)->new ItemCountWrapper(new ItemCount(o, 1))).collect(Collectors.toList()));

        map.put("getSlot", new FIHash((s)->{
            ItemCount count = character.inventory().getEquipped(Slot.valueOf(s));
            return (count != null) ? new ItemCountWrapper(count) : null;
        },
                ()->character.inventory().getEquipped().size()>0, cfg.getObjectWrapper()));

        map.put("abilityMod", new FIHash((s)->
                character.scores().getMod(
                    AbilityScore.valueOf(camelCaseWord(s))),
                ()->false, cfg.getObjectWrapper()));

        map.put("abilityScore", new FIHash((s)->
                character.scores().getScore(AbilityScore.valueOf(camelCaseWord(s))),
                ()->false, cfg.getObjectWrapper()));


        map.put("items", new EquipmentList(character.inventory().getUnequipped(), character.inventory().getEquipped()));


        map.put("inventory", character.inventory().getItems().values().stream()
                .map(ItemCountWrapper::new).collect(Collectors.toList()));

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
        map.put("inventory", character.inventory().getItems().values().stream().map(ItemCountWrapper::new).collect(Collectors.toList()));
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
            if(ability.getDesc().equals("")) continue;
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
    public boolean isEmpty() {
        return false;
    }
}
