package model.xml_parsers.setting;

import model.attributes.Attribute;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Alignment;
import model.items.weapons.Weapon;
import model.util.ObjectNotFoundException;
import model.xml_parsers.FileLoader;
import model.xml_parsers.SpellsLoader;
import model.xml_parsers.equipment.WeaponsLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import model.setting.Deity;
import model.setting.Domain;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeitiesLoader extends FileLoader<Deity> {

    public DeitiesLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    @Override
    protected Deity parseItem(File file, Element deity) {
        NodeList nodeList = deity.getChildNodes();
        Deity.Builder builder = new Deity.Builder();
        setSource(builder, deity);
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
                    builder.setDivineSkills(Stream.of(trim.split(" or ")).map(Attribute::valueOf).collect(Collectors.toList()));
                    break;
                case "favoredweapon":
                    try {
                        builder.setFavoredWeapon(findFromDependencies("Weapon",
                                WeaponsLoader.class,
                                trim).getExtension(Weapon.class));
                    } catch (ObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case "domains":
                    for (String s : trim.split(", ?")) {
                        Domain domain = null;
                        try {
                            domain = findFromDependencies("Domain",
                                    DomainsLoader.class,
                                    s);
                        } catch (ObjectNotFoundException e) {
                            e.printStackTrace();
                        }
                        if(domain != null)
                            builder.addDomains(domain);
                        else
                            System.out.println("Warning: Could not find Domain "+s);
                    }
                    break;
                case "spells":
                    NodeList spellsList = curr.getChildNodes();
                    for (int j = 0; j < spellsList.getLength(); j++) {
                        if (spellsList.item(j).getNodeType() != Node.ELEMENT_NODE)
                            continue;
                        Element currSpell = (Element) spellsList.item(j);
                        try {
                            builder.addSpell(Integer.parseInt(currSpell.getAttribute("level")),
                                    findFromDependencies("Spell",
                                            SpellsLoader.class,
                                            currSpell.getAttribute("name")));
                        } catch (ObjectNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
        return builder.build();
    }
}
