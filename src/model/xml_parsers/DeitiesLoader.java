package model.xml_parsers;

import model.attributes.Attribute;
import model.data_managers.sources.SourceConstructor;
import model.data_managers.sources.SourcesLoader;
import model.enums.Alignment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import setting.Deity;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeitiesLoader extends FileLoader<Deity> {

    public DeitiesLoader(SourceConstructor sourceConstructor, File root) {
        super(sourceConstructor, root);
    }

    @Override
    protected Deity parseItem(File file, Element deity) {
        NodeList nodeList = deity.getChildNodes();
        Deity.Builder builder = new Deity.Builder();
        builder.setPage(Integer.parseInt(deity.getAttribute("page")));
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
                case "favoredweapon":
                        builder.setFavoredWeapon(SourcesLoader.instance().weapons().find(trim));
                    break;
                case "domains":
                    for (String s : trim.split(", ?")) {
                        builder.addDomains(SourcesLoader.instance().domains().find(s));
                    }
                    break;
                case "spells":
                    NodeList spellsList = curr.getChildNodes();
                    for (int j = 0; j < spellsList.getLength(); j++) {
                        if (spellsList.item(j).getNodeType() != Node.ELEMENT_NODE)
                            continue;
                        Element currSpell = (Element) spellsList.item(j);
                        builder.addSpell(Integer.parseInt(currSpell.getAttribute("level")),
                                SourcesLoader.instance().spells().find(currSpell.getAttribute("name")));
                    }
                    break;
            }
        }
        return builder.build();
    }
}
