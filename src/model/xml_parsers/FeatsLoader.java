package model.xml_parsers;

import model.abilities.Ability;
import model.enums.Type;
import model.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;

public class FeatsLoader extends AbilityLoader<Ability> {
    private Map<String, List<Ability>> featsMap = new HashMap<>();
    private List<Ability> feats;
    public FeatsLoader(String location) {
        path = new File(location);
    }

    @Override
    public List<Ability> parse() {
        if(feats == null) {
            feats = new ArrayList<>();
            for (Pair<Document, String> docEntry : getDocs(path)) {
                Document doc = docEntry.first;
                String file = docEntry.second;
                if(file.equals("bloodline.pfdyl")) {
                    for (Ability ability : new BloodlinesLoader(doc).parse()) {
                        this.feats.add(ability);
                        this.featsMap.computeIfAbsent(file.replaceAll(".pfdyl", "").toLowerCase(),
                                s->new ArrayList<>()).add(ability);
                    }
                    continue;
                }
                NodeList abilities = doc.getElementsByTagName("pf2:feats").item(0).getChildNodes();
                for(int i=0; i<abilities.getLength(); i++) {
                    if(abilities.item(i).getNodeType() == Node.ELEMENT_NODE){
                        Ability ability = makeAbility((Element) abilities.item(i),
                                ((Element) abilities.item(i)).getAttribute("name"));
                        this.feats.add(ability);
                        this.featsMap.computeIfAbsent(file.replaceAll(".pfdyl", "").toLowerCase().replaceAll(" ", ""),
                                s->new ArrayList<>()).add(ability);
                    }
                }
            }
        }
        return Collections.unmodifiableList(feats);
    }

    @Override
    protected Type getSource(Element element) {
        return Type.General;
    }

    public List<Ability> getFeats(String type) {
        parse();
        return Collections.unmodifiableList(featsMap.getOrDefault(type.toLowerCase().replaceAll(" ", ""), Collections.emptyList()));
    }
}
