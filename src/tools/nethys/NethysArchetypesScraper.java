package tools.nethys;

import com.gargoylesoftware.htmlunit.WebClient;
import model.util.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class NethysArchetypesScraper extends NethysListScraper {
    public static void main(String[] args) {
        new NethysArchetypesScraper(
                "https://2e.aonprd.com/Archetypes.aspx",
                "generated/archetypes.pfdyl",
                source->source.equals("Advanced Player's Guide"));
    }

    public NethysArchetypesScraper(String inputURL, String outputPath, Predicate<String> sourceValidator) {
        super(true, sourceValidator);
        parseList(inputURL, "main",
                href->href.contains("Archetypes") && href.contains("ID"), e -> true);
        printOutput(outputPath);
    }

    @Override
    protected void setupItem(String href, Element row, WebClient webClient) {
        System.out.println(href);
        if (href.contains("Archetypes")) {
            parseList("https://2e.aonprd.com/" + href, "main",
                    s -> s.contains("Feats") && s.contains("ID"), e -> e.parent().hasClass("title"));
        } else {
            Document doc = makeDocument(href, webClient);
            NethysFeatListScraper.FeatEntry entry = NethysFeatListScraper.addItemStatic(doc);
            if (!entry.entry.isBlank()) {
                String clean = StringUtils.clean(entry.source);
                sources.computeIfAbsent(clean, key ->
                        new ConcurrentHashMap<>())
                        .computeIfAbsent(entry.archetype, s->Collections.synchronizedList(new ArrayList<>()))
                        .add(entry);
                sourceNames.computeIfAbsent(clean, s->entry.source);
            }
        }
    }

    @Override
    protected void printList(Map<String, List<Entry>> map, Consumer<String> out) {
        map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry->
                {
                    out.accept("\t<!--");
                    out.accept(entry.getKey());
                    out.accept("-->\n");
                    Comparator<Entry> comparing = Comparator.comparing(e -> {
                        if (e instanceof NethysFeatListScraper.FeatEntry)
                            return Integer.getInteger(((NethysFeatListScraper.FeatEntry) e).level);
                        return 0;
                    });
                    entry.getValue().stream().sorted(comparing.thenComparing(e->e.entryName))
                            .forEach(e-> out.accept(e.entry));
                }
        );
    }

    @Override
    Entry addItem(Document doc) {
        throw new UnsupportedOperationException();
    }
}
