package model.xml_parsers;

import model.abilities.Ability;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.enums.Type;

import java.io.File;
import java.util.List;

public class ChoicesLoader extends FeatsLoader {

    static{
        sources.put(ChoicesLoader.class, e-> Type.Choice);
    }

    public ChoicesLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    public void addChoices(String contents, List<Ability> makeAbilities) {
        for (Ability ability : makeAbilities) {
            addItem(contents, ability);
        }
    }
}
