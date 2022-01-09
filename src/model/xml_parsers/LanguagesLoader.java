package model.xml_parsers;

import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Language;
import model.enums.Rarity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

public class LanguagesLoader extends FileLoader<Language> {

    public LanguagesLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
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
        NodeList childNodes = doc.getDocumentElement().getElementsByTagName("Language");
        for(int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if(item.getNodeType() != Node.ELEMENT_NODE)
                continue;
            Language language = parseItem(subFile, (Element) item, category);
            if(category.isBlank()) category = language.getRarity().toString();
            addItem(category, language);
        }
    }

    @Override
    protected Language parseItem(File file, Element item) {
        Language.Builder builder = new Language.Builder(getSource());

        builder.setName(item.getTextContent());
        builder.rarity = Rarity.valueOf(((Element) item.getParentNode()).getTagName());

        return builder.build();
    }
}
