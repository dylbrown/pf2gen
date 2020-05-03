package ui.ftl.wrap;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;
import model.equipment.ItemCount;
import model.equipment.weapons.Weapon;
import model.player.PC;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

public class PF2GenObjectWrapper extends DefaultObjectWrapper {
    public PF2GenObjectWrapper(Version version) {
        super(version);
        setMethodAppearanceFineTuner((input, output) -> {
            String name = input.getMethod().getName();
            if(input.getMethod().getParameterCount() == 0 && name.substring(0, 3).equals("get") && name.length() > 3){
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
        if(obj instanceof PC) return new CharacterWrapper((PC) obj, this);
        if(obj instanceof Weapon) return new WeaponWrapper((Weapon) obj, this);
        if(obj instanceof ItemCount) return new ItemCountWrapper((ItemCount) obj, this);
        return super.handleUnknownType(obj);
    }
}
