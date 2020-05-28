package ui.ftl.wrap;

import freemarker.template.TemplateScalarModel;
import javafx.beans.property.StringProperty;

public class StringPropertyWrapper implements TemplateScalarModel {

    private final StringProperty property;

    public StringPropertyWrapper(StringProperty property) {
        this.property = property;
    }

    @Override
    public String getAsString() {
        return property.get();
    }
}
