package model.xml_parsers;

import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Trait;
import model.spells.BaseSpell;
import model.spells.MagicalSchool;
import model.spells.Spell;
import model.spells.Tradition;
import model.util.ObjectNotFoundException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class SpellsLoader extends FileLoader<Spell> {
	private final Map<Tradition, SortedMap<Integer, List<Spell>>> spellsByLevel = new HashMap<>();

	public SpellsLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
		super(sourceConstructor, root, sourceBuilder);
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
		if(spellsByLevel.get(tradition) == null || spellsByLevel.get(tradition).get(level) == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(spellsByLevel.get(tradition).get(level));
	}

	@Override
	protected Spell parseItem(File file, Element spell) {
		NodeList nodeList = spell.getChildNodes();
		BaseSpell.Builder builder = new BaseSpell.Builder();
		builder.setPage(Integer.parseInt(spell.getAttribute("page")));
		for(int i=0; i<nodeList.getLength(); i++) {
			if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			Element curr = (Element) nodeList.item(i);
			String trim = curr.getTextContent().trim();
			if(curr.getTagName().equals("Heightened")) {
				String every = curr.getAttribute("every");
				String level = curr.getAttribute("level");
				if(every.length() > 0) {
					builder.setHeightenedEvery(Integer.parseInt(every), trim);
				}else if(level.length() > 0){
					builder.addHeightenedLevel(Integer.parseInt(level), trim);
				}
			}else if(curr.getTagName().equals("Traits")){
				for (String traitName : trim.split(", ?")) {
					try {
						builder.addTrait(findFromDependencies("Trait",
								TraitsLoader.class,
								traitName));
					} catch (ObjectNotFoundException e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					Method setter = builder.getClass().getMethod("set" + curr.getTagName(), String.class);
					setter.invoke(builder, trim);
				} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		if(spell.getAttribute("type").equals("Cantrip")) builder.setCantrip(true);
		for (Trait trait : builder.getTraits()) {
			MagicalSchool school = MagicalSchool.tryGet(trait.getName());
			if(school != null) {
				builder.setSchool(school);
				break;
			}
		}

		return builder.build();
	}
}
