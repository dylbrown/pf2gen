package model.xml_parsers;

import model.attributes.Attribute;
import model.data_managers.AllDomains;
import model.data_managers.AllSpells;
import model.enums.Alignment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import setting.Deity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeityLoader extends FileLoader<Deity> {
    private List<Deity> deities = null;

    public DeityLoader(String location) {
        path = new File(location);
    }

    @Override
    public List<Deity> parse() {
        if (deities == null) {
            deities = new ArrayList<>();
            Document doc = getDoc(path);
            NodeList spellNodes = doc.getElementsByTagName("Deity");
            for (int i = 0; i < spellNodes.getLength(); i++) {
                if (spellNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) spellNodes.item(i);
                deities.add(getDeity(curr));
            }
        }
        return Collections.unmodifiableList(deities);
    }

    private Deity getDeity(Element domain) {
        NodeList nodeList = domain.getChildNodes();
        Deity.Builder builder = new Deity.Builder();
        builder.setPage(Integer.parseInt(domain.getAttribute("page")));
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            switch (curr.getTagName().toLowerCase()) {
                case "name":
                    builder.setName(trim);
                    break;
                case "title":
                    builder.setTitle(trim);
                    break;
                case "description":
                    builder.setDescription(trim);
                    break;
                case "edicts":
                    builder.setEdicts(trim);
                    break;
                case "anathema":
                    builder.setAnathema(trim);
                    break;
                case "areasofconcern":
                    builder.setAreasOfConcern(trim);
                    break;
                case "deityalignment":
                    builder.setDeityAlignment(Alignment.valueOf(trim.toUpperCase()));
                    break;
                case "followeralignments":
                    for (String s : trim.split(", ?")) {
                        builder.addFollowerAlignments(Alignment.valueOf(s.toUpperCase().trim()));
                    }
                    break;
                case "divinefont":
                    if (trim.toLowerCase().contains("harm"))
                        builder.setHarmFont(true);
                    if (trim.toLowerCase().contains("heal"))
                        builder.setHealFont(true);
                    break;
                case "divineskill":
                    builder.setDivineSkills(Stream.of(trim.split(" or ")).map(Attribute::robustValueOf).collect(Collectors.toList()));
                    break;
                case "domains":
                    for (String s : trim.split(", ?")) {
                        builder.addDomains(AllDomains.find(s));
                    }
                    break;
                case "spells":
                    NodeList spellsList = curr.getChildNodes();
                    for (int j = 0; j < spellsList.getLength(); j++) {
                        if (spellsList.item(j).getNodeType() != Node.ELEMENT_NODE)
                            continue;
                        Element currSpell = (Element) spellsList.item(j);
                        builder.addSpell(Integer.parseInt(currSpell.getAttribute("level")),
                                AllSpells.find(currSpell.getAttribute("name")));
                    }
                    break;
            }
        }
        return builder.build();
    }
}
