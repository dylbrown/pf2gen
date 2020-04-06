package model.xml_parsers;

import model.abilities.Ability;
import model.enums.Type;
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
