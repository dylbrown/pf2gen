package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import model.abilities.Ability;
import model.abilities.Activity;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.enums.Slot;
import model.enums.Type;
import model.equipment.ItemCount;
import model.player.InventoryManager;
import model.player.PC;
import ui.ftl.EquipmentList;
import ui.ftl.FIHash;
import ui.ftl.entries.AttributeEntry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static model.util.StringUtils.camelCaseWord;
import static ui.Main.character;

public class CharacterWrapper implements TemplateHashModel {
    private final Map<String, Object> map = new HashMap<>();
    private final ObjectWrapper wrapper;
    public CharacterWrapper(PC character, ObjectWrapper wrapper) {
        this.wrapper = wrapper;

        //map.put("totalweight", (NumberSupplier) ()->character.inventory().getTotalWeight());
        for (Ability ability : character.abilities().getAbilities()) {
            if(ability.getType() == null) continue;
            if(ability.getType().equals(Type.Heritage)){
                map.put("heritage", ability);
                break;
            }
        }
        if(!map.containsKey("heritage"))
            map.put("heritage", "No Heritage");

        map.put("attributes", getAttributeMap());
        map.put("attacks", character.combat().getAttacks().stream()
                .map((o)->new ItemCountWrapper(new ItemCount(o, 1), wrapper)).collect(Collectors.toList()));

        map.put("getSlot", new FIHash((s)->{
            ItemCount count = character.inventory().getEquipped(Slot.valueOf(s));
            return (count != null) ? new ItemCountWrapper(count, wrapper) : null;
        },
                ()->character.inventory().getEquipped().size()>0, wrapper));

        map.put("abilityMod", new FIHash((s)->
                character.scores().getMod(
                    AbilityScore.valueOf(camelCaseWord(s))),
                ()->false, wrapper));

        map.put("abilityScore", new FIHash((s)->
                character.scores().getScore(AbilityScore.valueOf(camelCaseWord(s))),
                ()->false, wrapper));


        map.put("items", new EquipmentList(character.inventory().getUnequipped(), character.inventory().getEquipped(), wrapper));


        map.put("inventory", character.inventory().getItems().values());
        map.put("qualities", character.qualities());
        map.put("combat", character.combat());

        map.put("skills", getSkills());

        map.put("spells", character.spells());
    }

    private Map<String, AttributeEntry> getAttributeMap() {
        Map<String, AttributeEntry> map = new HashMap<>();
        for (Attribute value : Attribute.values()) {
            map.put(value.toString().toLowerCase(), new AttributeEntry(value, "",
                    character.attributes().getProficiency(value, ""),
                    character.levelProperty(),
                    wrapper));
        }


        return map;
    }

    private List<AttributeEntry> getSkills() {
        List<AttributeEntry> entries = new ArrayList<>();
        for (Attribute value : Attribute.getSkills()) {
            if(value.equals(Attribute.Lore)) {
                for (String lore : character.attributes().lores()) {
                    entries.add(new AttributeEntry(value, lore,
                            character.attributes().getProficiency(value, lore),
                            character.levelProperty(),
                            wrapper));
                }
            }
            entries.add(new AttributeEntry(value, "",
                    character.attributes().getProficiency(value, ""),
                    character.levelProperty(),
                    wrapper));
        }
        return entries;
    }

    public void refresh() {
        updateAbilities();
        //TODO: Replace this with something listener-based
        map.put("attributes", getAttributeMap());
        map.put("inventory", character.inventory().getItems().values());
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
            items.add(ability);
        }
        return items;
    }

    @Override
    public TemplateModel get(String s) throws TemplateModelException {
        if(map.get(s) == null) {
            for (Method method : PC.class.getMethods()) {
                if (method.getName().toLowerCase().equals("get" + s.toLowerCase())
                        && method.getParameterCount() == 0) {
                    try {
                        return wrapper.wrap(method.invoke(character));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (Method method : InventoryManager.class.getMethods()) {
                if (method.getName().toLowerCase().equals("get" + s.toLowerCase())
                        && method.getParameterCount() == 0) {
                    try {
                        return wrapper.wrap(method.invoke(character.inventory()));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return wrapper.wrap(map.get(s));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
