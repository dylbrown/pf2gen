package ui.html;

import model.enums.Trait;

public class TraitHTMLGenerator {
    public static String parse(Trait trait) {
        return "<b>" + trait.getName() + "</b><br>" +
        trait.getSource() + "<br>" + trait.getDescription();
    }
}
