package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import model.abilities.Ability;
import model.abilities.ActivityExtension;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.attributes.BaseAttribute;
import model.enums.Slot;
import model.enums.Type;
import model.items.ItemCount;
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

public class CharacterWrapper implements TemplateHashModel {
    private final Map<String, Object> map = new HashMap<>();
    private final ObjectWrapper wrapper;
    private final PC character;

    public CharacterWrapper(PC character, ObjectWrapper wrapper) {
        this.character = character;
        this.wrapper = wrapper;

        //map.put("totalweight", (NumberSupplier) ()->character.inventory().getTotalWeight());

        map.put("attributes", character.attributes());
        map.put("attacks", character.combat().getAttacks().stream()
                .map((o)->new ItemCountWrapper(new ItemCount(o.getItem(), 1), wrapper)).collect(Collectors.toList()));

        map.put("getSlot", new FIHash((s)->{
            ItemCount count = character.inventory().getEquipped(Slot.valueOf(s));
            return (count != null) ? new ItemCountWrapper(count, wrapper) : null;
        },
                ()->character.inventory().getEquipped().size()>0, wrapper));

        map.put("abilityMod", new FIHash((s)->
                character.scores().getMod(
                    AbilityScore.robustValueOf(camelCaseWord(s))),
                ()->false, wrapper));

        map.put("abilityScore", new FIHash((s)->
                character.scores().getScore(AbilityScore.robustValueOf(camelCaseWord(s))),
                ()->false, wrapper));


        map.put("items", new EquipmentList(character.inventory().getUnequipped(), character.inventory().getEquipped(), wrapper));


        map.put("qualities", character.qualities());
        map.put("combat", character.combat());

        map.put("spells", character.spells());

        refresh();
    }

    private List<AttributeEntry> getSkills() {
        List<AttributeEntry> entries = new ArrayList<>();
        for (Attribute value : BaseAttribute.getSkills()) {
            if(value.equals(BaseAttribute.Lore)) {
                for (Attribute custom : character.attributes().getAll(BaseAttribute.Lore)) {
                    entries.add(new AttributeEntry(character, custom,
                            character.attributes().getProficiency(custom),
                            character.levelProperty(),
                            wrapper));
                }
            }else entries.add(new AttributeEntry(character, value,
                    character.attributes().getProficiency(value),
                    character.levelProperty(),
                    wrapper));
        }
        return entries;
    }

    public void refresh() {
        updateAbilities();
        //TODO: Replace this with something listener-based
        map.put("inventory", character.inventory().getItems().values());
        map.put("heritage", "No Heritage");
        map.put("skills", getSkills());
        for (Ability ability : character.abilities().getAbilities()) {
            if(Type.Heritage.equals(ability.getType())){
                map.put("heritage", ability);
                break;
            }
        }
    }

    private void updateAbilities() {
        List<Ability> abilities = flattenAbilities(character.abilities().getAbilities());
        abilities.sort(((o1, o2) -> {
            boolean o1Activity = o1.getExtension(ActivityExtension.class) != null;
            boolean o2Activity = o2.getExtension(ActivityExtension.class) != null;
            if (o1Activity && !o2Activity) return -1;
            if (o2Activity && !o1Activity) return 1;
            return o1.toString().compareTo(o2.toString());
        }));
        map.put("abilities", abilities.stream().distinct().collect(Collectors.toList()));
    }

    private List<Ability> flattenAbilities(List<Ability> abilities) {
        List<Ability> items = new ArrayList<>();
        for (Ability ability: abilities) {
            if(ability.getDescription().equals("")) continue;
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
                        assert(false);
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
                        assert(false);
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
