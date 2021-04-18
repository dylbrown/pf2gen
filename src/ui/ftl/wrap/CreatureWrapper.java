package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import model.attributes.BaseAttribute;
import model.creatures.Creature;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CreatureWrapper extends MapGenericWrapper<Creature> {

    public CreatureWrapper(Creature creature, ObjectWrapper objectWrapper) {
        super(creature, objectWrapper);
        map.put("modifiers", c->new CreatureAttributeWrapper(wrapper, c.getModifiers()));
        map.put("skilllist", c->creature.getModifiers().entrySet().stream()
                .filter(e-> Arrays.asList(BaseAttribute.getSkills()).contains(e.getKey().getBase()))
                .map(e->new SkillEntry(e.getKey().toString(), e.getValue().getModifier()))
                .sorted((e1,e2)->e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList()));
    }

    public class SkillEntry implements TemplateHashModel {
        private final String name;
        private final Integer value;

        public SkillEntry(String name, Integer value) {
            this.name = name;
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }

        @Override
        public TemplateModel get(String s) throws TemplateModelException {
            switch (s.toLowerCase()) {
                case "name":
                    return wrapper.wrap(name);
                case "value":
                    return wrapper.wrap(value);
            }
            return null;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
