package model.xml_parsers.setting;

import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.util.ObjectNotFoundException;
import model.xml_parsers.FileLoader;
import model.xml_parsers.SpellsLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import model.setting.Domain;

import java.io.File;

public class DomainsLoader extends FileLoader<Domain> {

    public DomainsLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    @Override
    protected Domain parseItem(File file, Element domain) {
        NodeList nodeList = domain.getChildNodes();
        Domain.Builder builder = new Domain.Builder(getSource());
        builder.setPage(Integer.parseInt(domain.getAttribute("page")));
        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            switch(curr.getTagName().toLowerCase()) {
                case "name":
                    builder.setName(trim);
                    break;
                case "description":
                    builder.setDescription(trim);
                    break;
                case "domainspell":
                    try {
                        builder.setDomainSpell(findFromDependencies("Spell",
                                SpellsLoader.class,
                                trim));
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case "advanceddomainspell":
                    try {
                        builder.setAdvancedDomainSpell(findFromDependencies("Spell",
                                SpellsLoader.class,
                                trim));
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        return builder.build();
    }
}
