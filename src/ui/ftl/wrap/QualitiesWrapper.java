package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import model.player.QualityManager;

public class QualitiesWrapper implements TemplateHashModel {

    private final QualityManager manager;
    private final ObjectWrapper wrapper;

    public QualitiesWrapper(QualityManager manager, ObjectWrapper wrapper) {
        this.manager = manager;
        this.wrapper = wrapper;
    }
    @Override
    public TemplateModel get(String s) throws TemplateModelException {
        s = s.toLowerCase();
        if(s.equals("languages")) return wrapper.wrap(manager.getLanguages());
        return wrapper.wrap(manager.get(s));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
