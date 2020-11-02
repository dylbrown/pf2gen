package ui.ftl.wrap;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;
import model.creatures.Creature;
import model.items.Item;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ObjectWrapperCreatures extends PF2GenObjectWrapper {
    private final List<Creature> creatures;
    private int currentIndex = 0;

    public ObjectWrapperCreatures(Version version, List<Creature> creatures, Map<String, Object> model) {
        super(version);
        model.put("creatureIndex", (Consumer<Integer>) this::setIndex);
        this.creatures = creatures;
    }

    private void setIndex(int i) {
        currentIndex = i;
    }

    @Override
    protected TemplateModel handleUnknownType(Object obj) throws TemplateModelException {
        if(obj instanceof Item)
            return new ItemWrapper(creatures.get(currentIndex), (Item) obj, this);
        return super.handleUnknownType(obj);
    }
}
