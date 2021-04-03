package tools.nethys;

import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class NethysWeaponsArmorScraper extends NethysListScraper {

    public NethysWeaponsArmorScraper(boolean multithreaded) {
        super(multithreaded);
    }

    public NethysWeaponsArmorScraper(String inputURL, String outputPath, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator) {
        super(inputURL, outputPath, container, hrefValidator, sourceValidator);
    }

    public NethysWeaponsArmorScraper(String inputURL, Writer out, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator) {
        super(inputURL, out, container, hrefValidator, sourceValidator);
    }

    public NethysWeaponsArmorScraper(String inputURL, String outputPath, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator, boolean multithreaded) {
        super(inputURL, outputPath, container, hrefValidator, sourceValidator, multithreaded);
    }

    public NethysWeaponsArmorScraper(String inputURL, Writer out, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator, boolean multithreaded) {
        super(inputURL, out, container, hrefValidator, sourceValidator, multithreaded);
    }

    @Override
    protected void printList(Map<String, List<Entry>> map, Writer out) {
        for (String proficiency : getProficiencies()) {
            List<Entry> entries = map.get(proficiency);
            if(entries == null)
                continue;
            try {
                out.append("<").append(proficiency).append(">\n");
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            entries.stream().sorted(Comparator.comparing(e -> e.entryName)).forEach(e -> {
                try {
                    out.append(e.entry);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            });
            try {
                out.append("</").append(proficiency).append(">\n");
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

    }

    protected abstract List<String> getProficiencies();

    public static class CategoryEntry extends Entry {
        public final String category;

        public CategoryEntry(String entryName, String entry, String source, String category) {
            super(entryName, entry, source);
            this.category = category;
        }

        @Override
        public String getEntryName() {
            return category;
        }
    }
}
