package model.xml_parsers;

import model.abilities.Ability;
import model.abilities.GranterExtension;
import model.abilities.SpellExtension;
import model.ability_slots.FilledSlot;
import model.ability_slots.SingleChoiceSlot;
import model.attributes.Attribute;
import model.attributes.AttributeMod;
import model.data_managers.sources.Source;
import model.enums.Proficiency;
import model.enums.Type;
import model.setting.Domain;
import model.spells.SpellType;
import model.util.ObjectNotFoundException;
import model.util.StringUtils;
import model.xml_parsers.setting.DomainsLoader;
import org.w3c.dom.Element;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MysteriesLoader extends AbilityLoader<Ability> {

    static {
        sources.put(MysteriesLoader.class, (element -> Type.Choice));
    }

    public MysteriesLoader(Source.Builder sourceBuilder) {
        super(null, null, sourceBuilder);
    }

    @Override
    protected Ability parseItem(File file, Element item) {
        return makeMystery(item);
    }

    public Ability makeMystery(Element item) {
        Ability.Builder mystery = new Ability.Builder(getSource());
        mystery.setType(Type.Choice);
        SpellExtension.Builder spellExt = mystery.getExtension(SpellExtension.Builder.class);
        spellExt.setSpellListName("Oracle");
        mystery.setName(item.getAttribute("name"));
        mystery.setDescription(item.getElementsByTagName("Description").item(0).getTextContent());

        // Trained Skill
        mystery.getExtension(GranterExtension.Builder.class).setAttrMods(Collections.singletonList(
                new AttributeMod(Attribute.valueOf(item.getElementsByTagName("Skill").item(0).getTextContent()), Proficiency.Trained)));

        //Granted Spells
        try {
            mystery.getExtension(SpellExtension.Builder.class).addBonusSpell(SpellType.Cantrip, findFromDependencies("Spell", SpellsLoader.class, item.getElementsByTagName("Cantrip").item(0).getTextContent()));
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }

        //Mystery Spells
        String[] bSpells = item.getElementsByTagName("RevelationSpells").item(0).getTextContent().split(", ?");
        try {
            spellExt.addBonusSpell(SpellType.Focus, findFromDependencies("Spell",
                    SpellsLoader.class,
                    bSpells[0].split(": ")[1]));
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }
        Ability.Builder advanced = new Ability.Builder(getSource()); advanced.setName("Advanced Revelation Spell");
        Ability.Builder greater = new Ability.Builder(getSource());  greater.setName("Greater Revelation Spell");
        try {
            advanced.getExtension(SpellExtension.Builder.class)
                    .addBonusSpell(SpellType.Focus, findFromDependencies("Spell",
                            SpellsLoader.class,
                            bSpells[1].split(": ")[1]));
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }
        advanced.getExtension(SpellExtension.Builder.class).setSpellListName("Oracle");
        try {
            greater.getExtension(SpellExtension.Builder.class)
                    .addBonusSpell(SpellType.Focus, findFromDependencies("Spells",
                            SpellsLoader.class,
                            bSpells[2].split(": ")[1]));
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }
        greater.getExtension(SpellExtension.Builder.class).setSpellListName("Oracle");
        advanced.addPrerequisite("Advanced Revelation");
        greater.addPrerequisite("Greater Revelation");
        mystery.addAbilitySlot(new FilledSlot("Advanced Revelation Spell", 1, advanced.build()));
        mystery.addAbilitySlot(new FilledSlot("Greater Revelation Spell", 1, greater.build()));

        //Domains
        String[] domains = item.getElementsByTagName("Domains").item(0).getTextContent().split(", ?");
        List<Ability> domainSpells = Arrays.stream(domains).map(domainName -> {
            try {
                Domain domain = findFromDependencies("Domain", DomainsLoader.class, domainName);
                Ability.Builder builder = new Ability.Builder(getSource());
                builder.setName(StringUtils.capitalize(domainName));
                builder.setPage(domain.getPage());
                SpellExtension.Builder spells = builder.getExtension(SpellExtension.Builder.class);
                spells.setSpellListName("Oracle");
                spells.addBonusSpell(
                        SpellType.Focus,
                        domain.getDomainSpell());
                return builder.build();
            } catch (ObjectNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        mystery.addAbilitySlot(new SingleChoiceSlot("Oracle Domain", 1, domainSpells));

        return mystery.build();
    }
}
