package ui.ftl.wrap;

import freemarker.template.*;
import model.abilities.Ability;
import model.abilities.CustomTextExtension;
import model.ability_slots.Choice;
import model.player.PC;
import model.util.DataTemplateMap;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ui.ftl.wrap.PF2GenObjectWrapper.CUSTOM_FORMATS;

public class AbilityWrapper extends MapGenericWrapper<Ability> {
    private static final Configuration cfg;

    static {
        cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setObjectWrapper(new PF2GenObjectWrapper(Configuration.VERSION_2_3_29));
        cfg.setCustomNumberFormats(CUSTOM_FORMATS);
    }

    private final PC character;

    public AbilityWrapper(PC character, Ability ability, ObjectWrapper wrapper) {
        super(ability, wrapper);
        this.character = character;
        map.put("name", this::getName);
        map.put("hasExtension".toLowerCase(),
                a->((TemplateMethodModelEx) o->a.hasExtension(o.get(0).toString())));
        map.put("getExtensionByName".toLowerCase(),
                a->((TemplateMethodModelEx) o->a.getExtensionByName(o.get(0).toString())));
    }

    private String getName(Ability ability) {
        CustomTextExtension textExt = ability.getExtension(CustomTextExtension.class);
        if (textExt == null) return ability.getName();
        try {
            Template nameTemplate = new Template("Name", textExt.getCustomName(), cfg);
            StringWriter results = new StringWriter();
            nameTemplate.process(new DataTemplateMap(Collections.singletonMap("getChoice", (TemplateMethodModelEx) this::getChoice), cfg.getObjectWrapper()), results);
            return results.toString();
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
            return ability.getName();
        }
    }

    private TemplateModel getChoice(List<Object> list) throws TemplateModelException {
        if (list.size() != 1)
            throw new TemplateModelException("Wrong number of arguments");
        List<String> args = new ArrayList<>();
        for (Object o : list) {
            if (!(o instanceof TemplateScalarModel))
                throw new TemplateModelException("Argument cannot be converted to a string!");
            args.add(((TemplateScalarModel) o).getAsString());
        }
        Optional<Choice<?>> matchingChoice = character.decisions().getDecisions().stream().filter(c -> c.getName().equalsIgnoreCase(args.get(0))).findAny();
        if (matchingChoice.isEmpty()) return (TemplateScalarModel) () -> "";
        return (TemplateScalarModel) () -> matchingChoice.get().getSelections().stream()
                .map(Object::toString).collect(Collectors.joining(", "));
    }
}
