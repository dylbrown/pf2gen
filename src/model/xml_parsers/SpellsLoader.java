package model.xml_parsers;

import model.spells.Spell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpellsLoader extends FileLoader<Spell> {
	private List<Spell> spells = null;

	public SpellsLoader(String s) {
		path = new File("data/spells/"+s);
	}

	@Override
	public List<Spell> parse() {
		if(spells == null) {
			spells = new ArrayList<>();
			Document doc = getDoc(path);
			NodeList spellNodes = doc.getElementsByTagName("Spell");
			for (int i = 0; i < spellNodes.getLength(); i++) {
				if (spellNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
					continue;
				Element curr = (Element) spellNodes.item(i);
				spells.add(getSpell(curr));
			}
		}
		return Collections.unmodifiableList(spells);
	}

	private Spell getSpell(Element spell) {
		NodeList nodeList = spell.getChildNodes();
		Spell.Builder builder = new Spell.Builder();
		for(int i=0; i<nodeList.getLength(); i++) {
			if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			Element curr = (Element) nodeList.item(i);
			String trim = curr.getTextContent().trim();
			try {
				Method setter = builder.getClass().getMethod("set" + curr.getTagName(), String.class);
				setter.invoke(builder, trim);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		if(spell.getAttribute("type").equals("Cantrip")) builder.setCantrip(true);
		return builder.build();
	}
}
