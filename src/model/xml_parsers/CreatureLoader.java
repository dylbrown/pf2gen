package model.xml_parsers;

import model.Creature;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import org.w3c.dom.Element;

import java.io.File;

public class CreatureLoader extends AbilityLoader<Creature> {
    public CreatureLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    @Override
    protected Creature parseItem(File file, Element item) {
        Creature.Builder builder = new Creature.Builder();
        return builder.build();
    }
}
