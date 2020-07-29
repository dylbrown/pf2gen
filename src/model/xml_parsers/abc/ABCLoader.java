package model.xml_parsers.abc;

import model.abc.ABC;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.xml_parsers.AbilityLoader;
import org.w3c.dom.Element;

import java.io.File;

abstract class ABCLoader<T extends ABC, U extends ABC.Builder> extends AbilityLoader<T> {
    public ABCLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    void parseElement(Element curr, String trim, U builder) {
        switch (curr.getTagName()) {
            case "Name":
                builder.setName(trim);
                break;
            case "Description":
                builder.setDescription(trim);
                break;
        }
    }
}
