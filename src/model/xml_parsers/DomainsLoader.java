package model.xml_parsers;

import model.data_managers.sources.SourceConstructor;
import model.data_managers.sources.SourcesLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import setting.Domain;

import java.io.File;

public class DomainsLoader extends FileLoader<Domain> {

    public DomainsLoader(SourceConstructor sourceConstructor, File root) {
        super(sourceConstructor, root);
    }

    @Override
    protected Domain parseItem(File file, Element domain) {
        NodeList nodeList = domain.getChildNodes();
        Domain.Builder builder = new Domain.Builder();
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
                case "domainspell":
                    builder.setDomainSpell(SourcesLoader.instance().spells().find(trim));
                    break;
                case "advanceddomainspell":
                    builder.setAdvancedDomainSpell(SourcesLoader.instance().spells().find(trim));
                    break;
            }
        }
        return builder.build();
    }
}
