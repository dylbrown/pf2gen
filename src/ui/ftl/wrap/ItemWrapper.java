package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateMethodModelEx;
import model.creatures.Attack;
import model.creatures.Creature;
import model.items.Item;
import model.items.ItemExtension;
import model.items.weapons.Weapon;
import model.player.PC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class ItemWrapper extends GenericWrapper<Item> {

    private final Function<Item, Integer> getAttackMod;
    private final Function<Item, Object> getDamage;

    public ItemWrapper(PC character, Item item, ObjectWrapper wrapper) {
        super(item, wrapper);
        getAttackMod = character.combat()::getAttackMod;
        getDamage = character.combat()::getDamage;
    }

    public ItemWrapper(Item item, ObjectWrapper wrapper) {
        super(item, wrapper);
        getAttackMod = null;
        getDamage = null;
    }

    public ItemWrapper(Creature creature, Item item, ObjectWrapper wrapper) {
        super(item, wrapper);
        getAttackMod = i -> {
            for (Attack attack : creature.getAttacks()) {
                if(attack.getName().equalsIgnoreCase(i.getName()))
                    return attack.getModifier();
            }
            throw new RuntimeException("Could not find attack");
        };
        getDamage = i -> {
            for (Attack attack : creature.getAttacks()) {
                if(attack.getName().equalsIgnoreCase(i.getName()))
                    return attack.getDamage();
            }
            throw new RuntimeException("Could not find attack");
        };
    }

    @Override
    Object getSpecialCase(String s, Item item) {
        if(s.equalsIgnoreCase("hasextension")) {
            return (TemplateMethodModelEx) list -> item.hasExtension(list.get(0).toString());
        }
        if(item.hasExtension(Weapon.class)) {
            switch (s) {
                case "attack":
                    if(getAttackMod != null)
                        return getAttackMod.apply(item);
                    break;
                case "damage":
                    if(getDamage != null)
                        return getDamage.apply(item);
                    break;
            }
        }
        for (ItemExtension extension : item.getExtensions()) {
            for (Method method : extension.getClass().getMethods()) {
                if (method.getName().toLowerCase().equals("get" + s.toLowerCase())
                        && method.getParameterCount() == 0) {
                    try {
                        return method.invoke(extension);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        assert(false);
                    }
                }
            }
        }
        return null;
    }
}
