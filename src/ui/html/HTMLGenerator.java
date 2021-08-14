package ui.html;

import model.abc.Ancestry;
import model.abc.Background;
import model.abc.PClass;
import model.abilities.Ability;
import model.abilities.GranterExtension;
import model.abilities.SpellExtension;
import model.creatures.Creature;
import model.items.Item;
import model.setting.Deity;
import model.setting.Domain;
import model.spells.Spell;
import ui.html.ability_extensions.GranterExtensionHTMLGenerator;
import ui.html.ability_extensions.SpellExtensionHTMLGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class HTMLGenerator {

    private static class GeneratorPair<T> {
        public final Class<T> tClass;
        public final Function<T, String> generator;
        public GeneratorPair(Class<T> tClass, Function<T, String> generator) {
            this.tClass = tClass;
            this.generator = generator;
        }

        public <U> String function(U t) {
            return generator.apply(tClass.cast(t));
        }
    }
    private final Map<Class<?>, GeneratorPair<?>> generators;
    private static final HTMLGenerator INSTANCE;
    static {
        INSTANCE = new HTMLGenerator();
    }
    private HTMLGenerator() {
        generators = new HashMap<>();
        add(Ancestry.class, ABCHTMLGenerator::parse);
        add(Background.class, ABCHTMLGenerator::parse);
        add(PClass.class, ABCHTMLGenerator::parse);
        add(Deity.class, SettingHTMLGenerator::parse);
        add(Domain.class, SettingHTMLGenerator::parse);
        add(Item.class, ItemHTMLGenerator::parse);
        add(Ability.class, AbilityHTMLGenerator::parse);
        // Ability Extensions
        add(SpellExtension.class, SpellExtensionHTMLGenerator::parse);
        add(GranterExtension.class, GranterExtensionHTMLGenerator::parse);

        add(Spell.class, s -> SpellHTMLGenerator.parse(s, s.getLevelOrCantrip()));
        add(Creature.class, CreatureHTMLGenerator::parse);
    }
    private <T> void add(Class<T> tClass, Function<T, String> generator) {
        generators.put(tClass, new GeneratorPair<>(tClass, generator));
    }
    public static <T> Function<T, String> getGenerator(Class<T> tClass) {
        GeneratorPair<?> stringFunction = INSTANCE.generators.get(tClass);
        Class<? super T> superclass = tClass;
        while(stringFunction == null && superclass != null) {
            superclass = superclass.getSuperclass();
            stringFunction = INSTANCE.generators.get(superclass);
        }
        if(stringFunction == null) return t->"";
        return stringFunction::function;
    }
    public static <T> String generate(T t) {
        return generateInternal(t.getClass(), t);
    }

    private static <T> String generateInternal(Class<T> tClass, Object o) {
        return getGenerator(tClass).apply(tClass.cast(o));
    }
}
