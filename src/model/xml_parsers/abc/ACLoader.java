package model.xml_parsers.abc;

import model.abc.AC;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

abstract class ACLoader<T extends AC, U extends AC.Builder> extends ABCLoader<T, U> {
    void parseElement(Element curr, String trim, U builder) {
        switch (curr.getTagName()) {
            case "HP":
                builder.setHP(Integer.parseInt(trim));
                break;
            case "Feats":
                NodeList childNodes = curr.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    if(childNodes.item(j) instanceof Element)
                        builder.addFeat(makeAbility((Element) childNodes.item(j), ((Element) childNodes.item(j)).getAttribute("name")));
                }
                break;
            default:
                super.parseElement(curr, trim ,builder);
        }
    }
}