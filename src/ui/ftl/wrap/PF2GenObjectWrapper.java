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
import model.player.AttributeManager;
import model.player.PC;
import model.player.QualityManager;
import model.spells.Spell;
import model.spells.SpellList;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

public class PF2GenObjectWrapper extends DefaultObjectWrapper {
    private final PC character;

    public PF2GenObjectWrapper(Version version, PC character) {
        super(version);
        this.character = character;
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

    public PF2GenObjectWrapper(Version version) {
        this(version, null);
    }


    @Override
    protected TemplateModel handleUnknownType(Object obj) throws TemplateModelException {
        if(obj instanceof PC) return new CharacterWrapper((PC) obj, this);
        if(obj instanceof Creature) return new CreatureWrapper((Creature) obj, this);
        if(obj instanceof Item)
            return new ItemWrapper(character, (Item) obj, this);
        if(obj instanceof ItemCount) return new ItemCountWrapper((ItemCount) obj, this);
        if(obj instanceof StringProperty) return new StringPropertyWrapper((StringProperty) obj);
        if(obj instanceof AttributeManager) return new AttributesWrapper((AttributeManager) obj, this, character);
        if(obj instanceof QualityManager) return new QualitiesWrapper((QualityManager) obj, this);
        if(obj instanceof Spell) return new SpellWrapper((Spell) obj, this);
        if(obj instanceof SpellList) return new SpellListWrapper((SpellList) obj, this);
        if(obj instanceof ReadOnlyObjectProperty) //noinspection rawtypes
            return new ReadOnlyObjectPropertyWrapper((ReadOnlyObjectProperty) obj);
        return super.handleUnknownType(obj);
    }
}
