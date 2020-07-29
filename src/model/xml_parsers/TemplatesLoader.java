package model.xml_parsers;

import model.abilities.Ability;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import org.w3c.dom.Element;

import java.io.File;
import java.util.function.Supplier;

public class TemplatesLoader extends AbilityLoader<TemplatesLoader.BuilderSupplier> {
    public TemplatesLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        super(sourceConstructor, root, sourceBuilder);
    }

    @Override
    protected TemplatesLoader.BuilderSupplier parseItem(File file, Element item) {
        Ability.Builder builder = makeAbility(item);
        return new BuilderSupplier(() -> new Ability.Builder(builder), builder);
    }

    public static class BuilderSupplier implements Supplier<Ability.Builder> {
        private final Supplier<Ability.Builder> supplier;
        private final Ability.Builder original;

        public BuilderSupplier(Supplier<Ability.Builder> supplier, Ability.Builder original) {
            this.supplier = supplier;
            this.original = original;
        }

        @Override
        public Ability.Builder get() {
            return supplier.get();
        }

        @Override
        public String toString() {
            return original.getName();
        }
    }
}
