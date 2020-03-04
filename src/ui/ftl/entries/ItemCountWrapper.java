package ui.ftl.entries;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import model.enums.Slot;
import model.equipment.Equipment;
import model.equipment.ItemCount;
import model.equipment.weapons.Weapon;
import model.util.Pair;
import ui.Main;
import ui.ftl.TemplateFiller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ItemCountWrapper implements TemplateHashModel {
    private final ItemCount itemCount;
    private Slot slot = null;
    public ItemCountWrapper(ItemCount itemCount) {
        this.itemCount = itemCount;
    }

    public ItemCountWrapper(Pair<Slot, ItemCount> pair) {
        this.slot = pair.first;
        this.itemCount = pair.second;
    }

    public String location() {
        if(slot != null) {
            return Slot.getPrettyName(slot);
        }else{
            return "Carried";
        }
    }

    public Equipment stats(){return itemCount.stats();}

    public int count() {
        return itemCount.getCount();
    }

    public int attack() {
        return Main.character.getAttackMod((Weapon) itemCount.stats());
    }

    public int damageMod() {
        return Main.character.getDamageMod((Weapon) itemCount.stats());
    }

    @Override
    public TemplateModel get(String s) throws TemplateModelException {
        for (Method method : ItemCountWrapper.class.getMethods()) {
            if(method.getParameterCount() == 0
                    && method.getName().toLowerCase().equals(s.toLowerCase())){
                try {
                    return TemplateFiller.getWrapper().wrap(method.invoke(this));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        if(s.equals("name")){
            System.out.println("Test");
        }
        for (Method method : itemCount.stats().getClass().getMethods()) {
            if(method.getParameterCount() == 0
                    && (method.getName().toLowerCase().equals("get" + s.toLowerCase())
                        || method.getName().toLowerCase().equals(s.toLowerCase()))){
                try {
                    return TemplateFiller.getWrapper().wrap(method.invoke(itemCount.stats()));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        throw new TemplateModelException("Not Found");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
