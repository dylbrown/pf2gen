package ui.ftl.entries;

import freemarker.template.TemplateScalarModel;

@FunctionalInterface
public interface StringSupplier extends TemplateScalarModel {
    @Override
    String getAsString();
}
