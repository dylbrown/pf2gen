package ui.ftl.wrap;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;
import model.items.Item;
import model.player.AttributeManager;
import model.player.PC;
import model.player.QualityManager;

public class ObjectWrapperCharacter extends PF2GenObjectWrapper {

    private final PC character;

    public ObjectWrapperCharacter(Version version, PC character) {
        super(version);
        this.character = character;
    }

    @Override
    protected TemplateModel handleUnknownType(Object obj) throws TemplateModelException {
        if(obj instanceof Item)
            return new ItemWrapper(character, (Item) obj, this);
        if(obj instanceof PC) return new CharacterWrapper((PC) obj, this);
        if(obj instanceof AttributeManager) return new AttributesWrapper((AttributeManager) obj, this, character);
        if(obj instanceof QualityManager) return new QualitiesWrapper((QualityManager) obj, this);
        return super.handleUnknownType(obj);
    }
}
