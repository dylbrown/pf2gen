package tools.nethys;

import model.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class NethysArchetypesScraper extends NethysListScraper {
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
    protected void setupItem(String href, Element row) {
        System.out.println(href);
        if (href.contains("Archetypes")) {
            parseList("http://2e.aonprd.com/" + href, "ctl00_MainContent_DetailedOutput",
                    s -> s.contains("Feats") && s.contains("ID"), e -> e.parent().hasClass("title"));
        } else {
            Document doc;
            try  {
                doc = Jsoup.connect(href).get();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(href);
                return;
            }
            NethysFeatListScraper.FeatEntry entry = NethysFeatListScraper.addItemStatic(doc);
            if (!entry.entry.isBlank()) {
                sources.computeIfAbsent(StringUtils.clean(entry.source), key ->
                        new ConcurrentHashMap<>())
                        .computeIfAbsent(entry.archetype, s->Collections.synchronizedList(new ArrayList<>()))
                        .add(entry);
            }
        }
    }

    @Override
    protected void printList(Map<String, List<Entry>> map, Writer out) {
        map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry->
                {
                    try {
                        out.append("\t<!--").append(entry.getKey()).append("-->\n");
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    Comparator<Entry> comparing = Comparator.comparing(e -> {
                        if (e instanceof NethysFeatListScraper.FeatEntry)
                            return Integer.getInteger(((NethysFeatListScraper.FeatEntry) e).level);
                        return 0;
                    });
                    entry.getValue().stream().sorted(comparing.thenComparing(e->e.entryName)).forEach(e->{
                        try {
                            out.append(e.entry);
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    });
                }
        );
    }

    @Override
    Entry addItem(Document doc) {
        throw new UnsupportedOperationException();
    }
}
