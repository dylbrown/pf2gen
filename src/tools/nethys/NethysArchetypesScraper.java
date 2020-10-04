package tools.nethys;

import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class NethysArchetypesScraper extends NethysListScraper {
    private final Map<String, StringBuilder> archetypes = new ConcurrentHashMap<>();
    private final Map<String, String> sourceMap = new ConcurrentHashMap<>();
    public static void main(String[] args) {
        new NethysArchetypesScraper(
                "https://2e.aonprd.com/Archetypes.aspx",
                "generated/archetypes.pfdyl",
                source->source.equals("Advanced Player's Guide"));
    }

    public NethysArchetypesScraper(String inputURL, String outputPath, Predicate<String> sourceValidator) {
        super(true);
        parseList(inputURL, "ctl00_MainContent_DetailedOutput",
                href->href.contains("Archetypes") && href.contains("ID"), e -> true);
        printOutput(outputPath, sourceValidator);
    }

    @Override
    protected void setupItem(String href) {
        System.out.println(href);
        if (href.contains("Archetypes")) {
            parseList("http://2e.aonprd.com/" + href, "ctl00_MainContent_DetailedOutput",
                    s -> s.contains("Feats") && s.contains("ID"), e -> e.parent().hasClass("title"));
        } else {
            NethysClassFeatScraper.FeatEntry entry = NethysClassFeatScraper.addFeat(href);
            if (!entry.contents.equals("")) {
                sourceMap.putIfAbsent(entry.archetype, entry.source);
                archetypes.computeIfAbsent(entry.archetype, key -> new StringBuilder())
                        .append(entry.contents);
            }
        }
    }

    @Override
    protected void afterThreadsCompleted() {
        TreeSet<String> keys = new TreeSet<>(archetypes.keySet());
        for (String archetype : keys) {
            StringBuilder entry = archetypes.get(archetype);
            String source = sourceMap.get(archetype);
            sources.computeIfAbsent(source, s->new HashMap<>())
                    .put(archetype, "<!--" + archetype + "-->\n" + entry);
        }

    }

    @Override
    Entry addItem(Document doc) {
        throw new UnsupportedOperationException();
    }
}
