package model.xml_parsers.equipment;

import model.abilities.Ability;
import model.enums.Type;
import model.xml_parsers.AbilityLoader;
import org.w3c.dom.Element;

import java.util.List;

public class ItemAbilityLoader extends AbilityLoader<Ability> {
    @Override
    public List<Ability> parse() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Type getSource(Element element) {
        return Type.Item;
    }
}
