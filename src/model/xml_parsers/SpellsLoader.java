package model.xml_parsers;

import model.data_managers.sources.SourceConstructor;
import model.spells.Spell;
import model.spells.Tradition;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class SpellsLoader extends FileLoader<Spell> {
	private final Map<Tradition, SortedMap<Integer, List<Spell>>> spellsByLevel = new HashMap<>();

	public SpellsLoader(SourceConstructor sourceConstructor, File root) {
		super(sourceConstructor, root);
	}

	public List<Spell> getSpells(Tradition tradition, int level) {
		if(tradition == null) return Collections.emptyList();
		if(spellsByLevel.isEmpty()) {
			for (Spell spell : getAll().values()) {
				for (Tradition curr : spell.getTraditions()) {
					spellsByLevel.computeIfAbsent(curr, s->new TreeMap<>())
							.computeIfAbsent(spell.getLevelOrCantrip(), (key)->new ArrayList<>())
							.add(spell);
				}
			}
		}
		return Collections.unmodifiableList(spellsByLevel.get(tradition).get(level));
	}

	@Override
	protected Spell parseItem(File file, Element spell) {
		NodeList nodeList = spell.getChildNodes();
		Spell.Builder builder = new Spell.Builder();
		builder.setPage(Integer.parseInt(spell.getAttribute("page")));
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
