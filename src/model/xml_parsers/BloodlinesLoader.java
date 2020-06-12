package model.xml_parsers;

import model.abilities.Ability;
import model.abilities.AbilitySet;
import model.abilities.SpellAbility;
import model.abilities.abilitySlots.FilledSlot;
import model.attributes.Attribute;
import model.attributes.AttributeMod;
import model.data_managers.sources.SourceConstructor;
import model.data_managers.sources.SourcesLoader;
import model.enums.Proficiency;
import model.enums.Type;
import model.spells.CasterType;
import model.spells.SpellType;
import model.spells.Tradition;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class BloodlinesLoader extends AbilityLoader<Ability> {

	static {
		source = (element -> Type.Class);
	}

	public BloodlinesLoader(SourceConstructor sourceConstructor, File root) {
		super(sourceConstructor, root);
	}

	public static Ability makeBloodline(Element item) {
		SpellAbility.Builder bloodline = new SpellAbility.Builder();
		bloodline.setName(item.getAttribute("name"));
		bloodline.setDescription(item.getElementsByTagName("Description").item(0).getTextContent());
		String tradition = item.getElementsByTagName("Tradition").item(0).getTextContent();
		bloodline.setTradition(Tradition.valueOf(tradition));
		bloodline.setCasterType(CasterType.Spontaneous);
		bloodline.setGivesPrerequisites(Collections.singletonList(tradition+" Bloodline"));
		String[] skills = item.getElementsByTagName("Skills").item(0).getTextContent().split(", ?");
		bloodline.setAttrMods(Arrays.asList(
				new AttributeMod(Attribute.robustValueOf(skills[0]), Proficiency.Trained),
				new AttributeMod(Attribute.robustValueOf(skills[1]), Proficiency.Trained)));

		//Granted Spells
		List<Ability> grantedAbilities = new ArrayList<>();
		for (String level : item.getElementsByTagName("GrantedSpells").item(0).getTextContent().split(", ?")) {
			String[] split = level.split("(: |cantrip )");
			SpellAbility.Builder builder = new SpellAbility.Builder(); builder.setName(split[0]+"-level granted spells");
			if(!split[0].equals("") && !split[0].equals("1st"))
				builder.setPrerequisites(Collections.singletonList(split[0]+"-level spells"));
			builder.addBonusSpell(SpellType.Spell, SourcesLoader.instance().spells().find(split[1]));
			grantedAbilities.add(builder.build());
		}
		AbilitySet.Builder grantedSet = new AbilitySet.Builder();
		grantedSet.setAbilities(grantedAbilities);
		bloodline.addAbilitySlot(new FilledSlot("Granted Spells", 1, grantedSet.build()));

		//Bloodline Spells
		String[] bSpells = item.getElementsByTagName("BloodlineSpells").item(0).getTextContent().split(", ?");
		bloodline.addBonusSpell(SpellType.Focus, SourcesLoader.instance().spells().find(bSpells[0].split(": ")[1]));
		SpellAbility.Builder advanced = new SpellAbility.Builder(); advanced.setName("Advanced Bloodline Spell");
		SpellAbility.Builder greater = new SpellAbility.Builder();  greater.setName("Greater Bloodline Spell");
		advanced.addBonusSpell(SpellType.Focus, SourcesLoader.instance().spells().find(bSpells[1].split(": ")[1]));
		greater.addBonusSpell(SpellType.Focus, SourcesLoader.instance().spells().find(bSpells[2].split(": ")[1]));
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
