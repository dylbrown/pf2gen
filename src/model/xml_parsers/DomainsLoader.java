package model.xml_parsers;

import model.data_managers.AllSpells;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import setting.Domain;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DomainsLoader extends FileLoader<Domain> {
    private List<Domain> domains = null;

    public DomainsLoader(String location) {
        path = new File(location);
    }

    @Override
    public List<Domain> parse() {
        if(domains == null) {
            domains = new ArrayList<>();
            Document doc = getDoc(path);
            NodeList spellNodes = doc.getElementsByTagName("Domain");
            for (int i = 0; i < spellNodes.getLength(); i++) {
                if (spellNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) spellNodes.item(i);
                domains.add(getDomain(curr));
            }
        }
        return Collections.unmodifiableList(domains);
    }

    private Domain getDomain(Element domain) {
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
                    builder.setDomainSpell(AllSpells.find(trim));
                    break;
                case "advanceddomainspell":
                    builder.setAdvancedDomainSpell(AllSpells.find(trim));
                    break;
            }
        }
        return builder.build();
    }
}
