package ui.ftl.entries;

import freemarker.template.TemplateNumberModel;

@FunctionalInterface
public interface NumberSupplier extends TemplateNumberModel {
    Number getAsNumber();
}
