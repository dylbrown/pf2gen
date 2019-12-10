package model.xml_parsers;

import model.enums.Type;
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

	private static final SpellsLoader instance;
	static{instance = new SpellsLoader();}

	public static SpellsLoader instance() {
		return instance;
	}

	public SpellsLoader() {
		path = new File("data/spells");
	}

	@Override
	protected Type getSource() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Spell> parse() {
		if(spells == null) {
			spells = new ArrayList<>();
			for (Document doc : getDocs(path)) {
				NodeList spellNodes = doc.getElementsByTagName("Spell");
				for (int i = 0; i < spellNodes.getLength(); i++) {
					if (spellNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
						continue;
					Element curr = (Element) spellNodes.item(i);
					spells.add(getSpell(curr));
				}
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
		return builder.build();
	}
}
