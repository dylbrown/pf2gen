package model.xml_parsers;

import model.abilities.Ability;
import model.enums.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatsLoader extends FileLoader<Ability> {
    private List<Ability> feats;
    public FeatsLoader(String location) {
        path = new File(location);
    }

    @Override
    public List<Ability> parse() {
        if(feats == null) {
            feats = new ArrayList<>();
            Document doc = getDoc(path);
            NodeList abilities = doc.getElementsByTagName("feats").item(0).getChildNodes();
            for(int i=0; i<abilities.getLength(); i++) {
                if(abilities.item(i).getNodeType() == Node.ELEMENT_NODE)
                    this.feats.add(makeAbility((Element) abilities.item(i),
                            ((Element) abilities.item(i)).getAttribute("name")));
            }
        }
        return Collections.unmodifiableList(feats);
    }

    @Override
    protected Type getSource() {
        return Type.General;
    }
}
