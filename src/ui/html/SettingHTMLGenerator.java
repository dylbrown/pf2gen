package ui.html;

import model.attributes.Attribute;
import model.enums.Alignment;
import model.spells.Spell;
import model.setting.Deity;
import model.setting.Domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SettingHTMLGenerator {
    public static String parse(Deity deity) {
        String font = "none";
        if(deity.isHarmFont() && deity.isHealFont()) {
            font = "harm or heal";
        } else if(deity.isHealFont()) {
            font = "heal";
        } else if(deity.isHarmFont()) {
            font = "harm";
        }
        List<String> spells = new ArrayList<>();
        for (Map.Entry<Integer, Spell> entry : deity.getSpells().entrySet()) {
            spells.add(entry.getKey() + ": " + entry.getValue());
        }

        return String.format("<h3>%s (%s) [%s]</h3>" +
                        "<p>%s<p>" +
                        "<b>Edicts</b> %s<br>" +
                        "<b>Anathema</b> %s<br>" +
                        "<b>Areas of Concern</b> %s<br>" +
                        "<b>Follower Alignments</b> %s<br>" +
                        "<b>Divine Font</b> %s<br>" +
                        "<b>Divine Skill</b> %s<br>" +
                        "<b>Favored Weapon</b> %s<br>" +
                        "<b>Domains</b> %s<br>" +
                        "<b>Spells</b> %s<br>",
                deity.getName(),
                deity.getTitle(),
                deity.getDeityAlignment().name(),
                deity.getDescription(),
                deity.getEdicts(),
                deity.getAnathema(),
                deity.getAreasOfConcern(),
                deity.getFollowerAlignments().stream().map(Alignment::name).collect(Collectors.joining(", ")),
                font,
                deity.getDivineSkillChoices().stream().map(Attribute::toString).collect(Collectors.joining(" or ")),
                (deity.getFavoredWeapon() != null) ? deity.getFavoredWeapon().getItem().getName() : "None",
                deity.getDomains().stream().map(Domain::getName).collect(Collectors.joining(", ")),
                String.join(", ", spells));
    }

    public static String parse(Domain domain) {
        return String.format("<h3>%s</h3>" +
                "<p>%s<p>" +
                "<b>Domain Spell</b> %s<br>" +
                "<b>Advanced Domain Spell</b> %s<br>",
                domain.getName(),
                domain.getDescription(),
                domain.getDomainSpell().getName(),
                domain.getAdvancedDomainSpell().getName());
    }
}
