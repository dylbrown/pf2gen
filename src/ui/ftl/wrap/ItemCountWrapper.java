package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import model.enums.Slot;
import model.equipment.Equipment;
import model.equipment.ItemCount;
import model.util.Pair;
import ui.ftl.TemplateFiller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ItemCountWrapper implements TemplateHashModel {
    private final ItemCount itemCount;
    private final ObjectWrapper wrapper;
    private Slot slot = null;
    public ItemCountWrapper(ItemCount itemCount, ObjectWrapper wrapper) {
        this.wrapper = wrapper;
        this.itemCount = itemCount;
    }

    public ItemCountWrapper(Pair<Slot, ItemCount> pair, ObjectWrapper wrapper) {
        this.wrapper = wrapper;
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
        TemplateModel model = wrapper.wrap(itemCount.stats());
        if(model instanceof TemplateHashModel)
            return ((TemplateHashModel) model).get(s);
        throw new TemplateModelException("Could not find member "+s+" of ItemCount");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
