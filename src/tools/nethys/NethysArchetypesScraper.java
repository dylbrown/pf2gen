package tools.nethys;

import com.gargoylesoftware.htmlunit.WebClient;
import model.util.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class NethysArchetypesScraper extends NethysListScraper {
    public static void main(String[] args) {
        new NethysArchetypesScraper(
                "https://2e.aonprd.com/Archetypes.aspx",
                "feats/archetypes.pfdyl",
                source->true);
    }

    public NethysArchetypesScraper(String inputURL, String outputPath, Predicate<String> sourceValidator) {
        super(false, sourceValidator);
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
            Document doc = makeDocument("https://2e.aonprd.com/" + href, webClient);
            NethysFeatListScraper.FeatEntry entry = NethysFeatListScraper.addItemStatic(doc, sourceValidator);
            if (entry != null && !entry.entry.isBlank()) {
                String clean = StringUtils.clean(entry.source);
                sources.computeIfAbsent(clean, key ->
                        new ConcurrentHashMap<>())
                        .computeIfAbsent(entry.archetype, s->Collections.synchronizedList(new ArrayList<>()))
                        .add(entry);
                sourceNames.computeIfAbsent(clean, s->entry.source);
            }
        }
    }

    protected void parseList(String inputURL, String container, Predicate<String> hrefValidator,
                             Predicate<Element> elementValidator) {
        WebClient webClient = null;
        try {
            webClient = semaphore.getItem();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Document rootDocument = makeDocument(inputURL, webClient);
        if(multithreaded) {
            semaphore.putItem(webClient);
        }

        String sourceAndPage = rootDocument.getElementsMatchingText("\\ASource\\z").first().nextElementSibling().text();
        if(sourceValidator != null && inputURL.contains("ID") &&
                !sourceValidator.test(StringUtils.clean(sourceAndPage.replaceAll(" pg.*", "")))) {
            return;
        }

        AtomicBoolean firstRow = new AtomicBoolean(true);
        WebClient finalWebClient = webClient;
        rootDocument.getElementById(container).getElementsByTag("a").forEach(element -> {
                    if(!firstRow.get()) {
                        String href = "";
                        try {
                            href = element.attr("href");
                            int id = -1;
                            try{
                                id = Integer.parseInt(href.replaceAll(".*ID=", ""));
                            }catch (NumberFormatException ignored) {}
                            if(elementValidator.test(element) && hrefValidator.test(href) && !ids.contains(id)) {
                                ids.add(id);
                                if(multithreaded)
                                    setupItemMultithreaded(href, element);
                                else
                                    setupItem(href, element, finalWebClient);
                            }
                        } catch (Exception e) {
                            System.out.println(href);
                            e.printStackTrace();
                        }
                    } else firstRow.set(false);
                });
        if(!multithreaded) {
            semaphore.putItem(webClient);
        }
    }

    @Override
    protected void printList(Map<String, List<Entry>> map, Consumer<String> out) {
        map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry->
                {
                    out.accept("\t<!--");
                    out.accept(entry.getKey());
                    out.accept("-->\n");
                    Comparator<Entry> comparing = Comparator.comparingInt(e -> {
                        if (e instanceof NethysFeatListScraper.FeatEntry)
                            return Integer.parseInt(((NethysFeatListScraper.FeatEntry) e).level);
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
