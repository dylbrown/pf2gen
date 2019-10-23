package ui.ftl;

import freemarker.core.*;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

import java.util.Locale;

class SignedTemplateNumberFormatFactory extends TemplateNumberFormatFactory {
    static final SignedTemplateNumberFormatFactory INSTANCE = new SignedTemplateNumberFormatFactory();
    private SignedTemplateNumberFormatFactory(){}
    @Override
    public TemplateNumberFormat get(String s, Locale locale, Environment environment) {
        return SignedTemplateNumberFormat.INSTANCE;
    }
    private static class SignedTemplateNumberFormat extends TemplateNumberFormat {
        private static final SignedTemplateNumberFormat INSTANCE = new SignedTemplateNumberFormat();
        @Override
        public String formatToPlainText(TemplateNumberModel numberModel) throws TemplateValueFormatException, TemplateModelException {
            Number number = TemplateFormatUtil.getNonNullNumber(numberModel);
            if(number.intValue() < 0)
                return number.toString();
            else
                return "+"+number.toString();
        }

        @Override
        public boolean isLocaleBound() {
            return false;
        }

        @Override
        public String getDescription() {
            return "Adds sign of integer to front.";
        }
    }
}
