package model.xml_parsers;

import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Trait;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

public class TraitsLoader extends FileLoader<Trait> {
    public TraitsLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    @Override
    protected void loadMultiple(String category, String location) {
        if(!loadTracker.isNotLoaded(location) || location == null)
            return;
        clearAccumulators();
        loadTracker.setLoaded(location);
        File subFile = getSubFile(location);
        Document doc = getDoc(subFile);
        NodeList categoryNodes = doc.getDocumentElement().getChildNodes();
        for(int i = 0; i < categoryNodes.getLength(); i++) {
            Node categoryNode = categoryNodes.item(i);
            if(categoryNode.getNodeType() != Node.ELEMENT_NODE)
                continue;
            category = ((Element) categoryNode).getAttribute("name");
            NodeList traitNodes = categoryNode.getChildNodes();
            for(int j = 0; j < traitNodes.getLength(); j++) {
                Node trait = traitNodes.item(j);
                if(trait.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Trait t = parseItem(subFile, (Element) trait, category);
                addItem(category, t);
            }
        }
    }

    @Override
    protected Trait parseItem(File file, Element item, String category) {
        NodeList nodeList = item.getChildNodes();
        Trait.Builder builder = new Trait.Builder();
        setSource(builder, item);
        builder.setCategory(category);
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            switch (curr.getTagName().toLowerCase()) {
                case "name":
                    builder.setName(trim);
                    break;
                case "description":
                    builder.setDescription(trim);
                    break;
            }
        }
        return builder.build();
    }

    @Override
    protected Trait parseItem(File file, Element item) {
        return parseItem(file, item, "Core");
    }
}
