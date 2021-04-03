package model.xml_parsers;

import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Sense;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

public class SensesLoader extends FileLoader<Sense> {

    public SensesLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    @Override
    protected Sense parseItem(File file, Element item) {
        Sense.Builder builder = new Sense.Builder();

        NodeList classProperties = item.getChildNodes();

        for(int i=0; i<classProperties.getLength(); i++) {
            if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) classProperties.item(i);
            String trim = curr.getTextContent().trim();
            switch (curr.getTagName()) {
                case "Name":
                    builder.setName(trim);
                    break;
                case "Description":
                    builder.setDescription(trim);
                    break;
            }
        }
        return builder.build();
    }
}
