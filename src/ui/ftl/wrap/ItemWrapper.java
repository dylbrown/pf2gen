package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateMethodModelEx;
import model.equipment.Item;
import model.equipment.ItemExtension;
import model.equipment.weapons.Weapon;
import model.player.PC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ItemWrapper extends GenericWrapper<Item> {

    private final PC character;

    public ItemWrapper(PC character, Item item, ObjectWrapper wrapper) {
        super(item, wrapper);
        this.character = character;
    }

    @Override
    Object getSpecialCase(String s, Item item) {
        if(s.equalsIgnoreCase("hasextension")) {
            return (TemplateMethodModelEx) list -> item.hasExtension(list.get(0).toString());
        }
        if(item.hasExtension(Weapon.class)) {
            switch (s) {
                case "attack":
                    return character.combat().getAttackMod(item);
                case "damage":
                    return character.combat().getDamage(item);
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
                    }
                }
            }
        }
        return null;
    }
}
