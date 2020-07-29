package model.xml_parsers.abc;

import model.abc.AC;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;

abstract class ACLoader<T extends AC, U extends AC.Builder> extends ABCLoader<T, U> {
    public ACLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    void parseElement(Element curr, String trim, U builder) {
        switch (curr.getTagName()) {
            case "HP":
                builder.setHP(Integer.parseInt(trim));
                break;
            case "Feats":
                NodeList childNodes = curr.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    if(childNodes.item(j) instanceof Element)
                        builder.addFeat(makeAbility((Element) childNodes.item(j), ((Element) childNodes.item(j)).getAttribute("name")).build());
                }
                break;
            default:
                super.parseElement(curr, trim ,builder);
        }
    }
}
