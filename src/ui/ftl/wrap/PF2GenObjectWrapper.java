package ui.ftl.wrap;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;
import model.creatures.Creature;
import model.equipment.Item;
import model.equipment.ItemCount;
import model.spells.Spell;
import model.spells.SpellList;
import ui.ftl.SignedTemplateNumberFormatFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.Map;

public class PF2GenObjectWrapper extends DefaultObjectWrapper {
    public static final Map<String, SignedTemplateNumberFormatFactory> CUSTOM_FORMATS =
            Collections.singletonMap("s", SignedTemplateNumberFormatFactory.INSTANCE);

    public PF2GenObjectWrapper(Version version) {
        super(version);
        setMethodAppearanceFineTuner((input, output) -> {
            String name = input.getMethod().getName();
            if(input.getMethod().getParameterCount() == 0 && name.startsWith("get") && name.length() > 3){
                try {
                    output.setExposeAsProperty(new PropertyDescriptor(name.substring(3).toLowerCase(),input.getMethod(), null));
                } catch (IntrospectionException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected TemplateModel handleUnknownType(Object obj) throws TemplateModelException {
        if(obj instanceof Creature) return new CreatureWrapper((Creature) obj, this);
        if(obj instanceof Item)
            return new ItemWrapper((Item) obj, this);
        if(obj instanceof ItemCount) return new ItemCountWrapper((ItemCount) obj, this);
        if(obj instanceof StringProperty) return new StringPropertyWrapper((StringProperty) obj);
        if(obj instanceof Spell) return new SpellWrapper((Spell) obj, this);
        if(obj instanceof SpellList) return new SpellListWrapper((SpellList) obj, this);
        if(obj instanceof ReadOnlyObjectProperty) //noinspection rawtypes
            return new ReadOnlyObjectPropertyWrapper((ReadOnlyObjectProperty) obj);
        return super.handleUnknownType(obj);
    }
}
