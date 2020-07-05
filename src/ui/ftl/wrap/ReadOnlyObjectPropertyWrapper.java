package ui.ftl.wrap;

import freemarker.template.TemplateScalarModel;
import javafx.beans.property.ReadOnlyObjectProperty;

@SuppressWarnings("rawtypes")
public class ReadOnlyObjectPropertyWrapper implements TemplateScalarModel {

    private final ReadOnlyObjectProperty property;

    public ReadOnlyObjectPropertyWrapper(ReadOnlyObjectProperty property) {
        this.property = property;
    }
    @Override
    public String getAsString() {
        if(property.get() == null)
            return "";
        return property.get().toString();
    }
}
