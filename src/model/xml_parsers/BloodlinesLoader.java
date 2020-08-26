package model.xml_parsers;

import model.abilities.Ability;
import model.abilities.AbilitySetExtension;
import model.abilities.SpellExtension;
import model.ability_scores.AbilityScore;
import model.ability_slots.FilledSlot;
import model.attributes.Attribute;
import model.attributes.AttributeMod;
import model.data_managers.sources.SourcesLoader;
import model.enums.Proficiency;
import model.enums.Type;
import model.spells.CasterType;
import model.spells.SpellType;
import model.spells.Tradition;
import model.util.ObjectNotFoundException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BloodlinesLoader extends AbilityLoader<Ability> {
	private static BloodlinesLoader bloodlinesLoader;

	static {
		sources.put(BloodlinesLoader.class, (element -> Type.Choice));
	}

	private BloodlinesLoader() {
		super(null, null, null);
	}

	public static Ability makeBloodlineStatic(Element item) {
		if(bloodlinesLoader == null) bloodlinesLoader = new BloodlinesLoader();
		return bloodlinesLoader.makeBloodline(item);
	}

	@Override
	protected Ability parseItem(File file, Element item) {
		return makeBloodline(item);
	}

	private Ability makeBloodline(Element item) {
		Ability.Builder bloodline = new Ability.Builder();
		bloodline.setType(Type.Choice);
		SpellExtension.Builder spellExt = bloodline.getExtension(SpellExtension.Builder.class);
		bloodline.setName(item.getAttribute("name"));
		bloodline.setDescription(item.getElementsByTagName("Description").item(0).getTextContent());
		String tradition = item.getElementsByTagName("Tradition").item(0).getTextContent();
		spellExt.setTradition(Tradition.valueOf(tradition));
		spellExt.setCasterType(CasterType.Spontaneous);
		spellExt.setCastingAbility(AbilityScore.Cha);
		spellExt.setSpellListName("Sorcerer");
		bloodline.setGivesPrerequisites(Collections.singletonList(tradition+" Bloodline"));
		String[] skills = item.getElementsByTagName("Skills").item(0).getTextContent().split(", ?");
		bloodline.setAttrMods(Arrays.asList(
				new AttributeMod(Attribute.robustValueOf(skills[0]), Proficiency.Trained),
				new AttributeMod(Attribute.robustValueOf(skills[1]), Proficiency.Trained)));

		//Granted Spells
		List<Ability> grantedAbilities = new ArrayList<>();
		for (String level : item.getElementsByTagName("GrantedSpells").item(0).getTextContent().split(", ?")) {
			String[] split = level.split("(: |cantrip )");
			if(split[0].equals("")) split[0] = "0th";
			Ability.Builder builder = new Ability.Builder(); builder.setName(split[0]+"-level granted spells");
			if(!split[0].trim().equals("") && !split[0].equals("1st"))
				builder.setPrerequisites(Collections.singletonList(split[0]+"-level spells"));
			try {
				builder.getExtension(SpellExtension.Builder.class)
						.addBonusSpell(SpellType.Spell, SourcesLoader.ALL_SOURCES.spells().find(split[1]));
			} catch (ObjectNotFoundException e) {
				e.printStackTrace();
			}
			builder.getExtension(SpellExtension.Builder.class).setSpellListName("Sorcerer");
			grantedAbilities.add(builder.build());
		}
		Ability.Builder grantedSet = new Ability.Builder();
		grantedSet.setName(bloodline.getName() + " - Granted Spells");
		grantedSet.getExtension(AbilitySetExtension.Builder.class).setAbilities(grantedAbilities);
		bloodline.addAbilitySlot(new FilledSlot("Granted Spells", 1, grantedSet.build()));

		//Bloodline Spells
		String[] bSpells = item.getElementsByTagName("BloodlineSpells").item(0).getTextContent().split(", ?");
		try {
			spellExt.addBonusSpell(SpellType.Focus, SourcesLoader.ALL_SOURCES.spells().find(bSpells[0].split(": ")[1]));
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}
		Ability.Builder advanced = new Ability.Builder(); advanced.setName("Advanced Bloodline Spell");
		Ability.Builder greater = new Ability.Builder();  greater.setName("Greater Bloodline Spell");
		try {
			advanced.getExtension(SpellExtension.Builder.class)
					.addBonusSpell(SpellType.Focus, SourcesLoader.ALL_SOURCES.spells().find(bSpells[1].split(": ")[1]));
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}
		advanced.getExtension(SpellExtension.Builder.class).setSpellListName("Sorcerer");
		try {
			greater.getExtension(SpellExtension.Builder.class)
					.addBonusSpell(SpellType.Focus, SourcesLoader.ALL_SOURCES.spells().find(bSpells[2].split(": ")[1]));
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}
		greater.getExtension(SpellExtension.Builder.class).setSpellListName("Sorcerer");
		advanced.setPrerequisites(Collections.singletonList("Advanced Bloodline"));
		advanced.setPrerequisites(Collections.singletonList("Greater Bloodline"));
		bloodline.addAbilitySlot(new FilledSlot("Advanced Bloodline Spell", 1, advanced.build()));
		bloodline.addAbilitySlot(new FilledSlot("Greater Bloodline Spell", 1, greater.build()));

		//Blood Magic
		Ability.Builder bloodMagic = new Ability.Builder(); bloodMagic.setName("Blood Magic");
		bloodMagic.setDescription(item.getElementsByTagName("BloodMagic").item(0).getTextContent());
		bloodline.addAbilitySlot(new FilledSlot("Blood Magic", 1, bloodMagic.build()));

		//Extra Ability Slots
		NodeList slots = item.getElementsByTagName("AbilitySlot");
		for(int j = 0; j < slots.getLength(); j++) {
			Element slot = (Element) slots.item(j);
			if(slot.getParentNode().equals(item)) {
				bloodline.addAbilitySlot(makeAbilitySlot(slot, 1));
			}
		}
		return bloodline.build();
	}
}
