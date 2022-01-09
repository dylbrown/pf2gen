package model.xml_parsers;

import model.creatures.CreatureFamily;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

public class CreatureFamilyLoader extends FileLoader<CreatureFamily> {
    public CreatureFamilyLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    @Override
    protected CreatureFamily parseItem(File file, Element item) {
        CreatureFamily.Builder builder = new CreatureFamily.Builder(getSource());
        NodeList childNodes = item.getChildNodes();
        builder.setPage(Integer.parseInt(item.getAttribute("page")));
        for(int i = 0; i < childNodes.getLength(); i++) {
            if(childNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) childNodes.item(i);
            String contents = curr.getTextContent();
            switch (curr.getTagName()) {
                case "Name":
                    builder.setName(contents);
                    break;
                case "Description":
                    builder.setDescription(contents);
                    break;
            }
        }
        return builder.build();
    }
}
