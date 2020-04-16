package model.xml_parsers.abc;

import model.abc.ABC;
import model.xml_parsers.AbilityLoader;
import org.w3c.dom.Element;

abstract class ABCLoader<T extends ABC, U extends ABC.Builder> extends AbilityLoader<T> {
    void parseElement(Element curr, String trim, U builder) {
        switch (curr.getTagName()) {
            case "Name":
                builder.setName(trim);
                break;
            case "Description":
                builder.setDescription(trim);
                break;
        }
    }
}
