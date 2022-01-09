package tools.nethys;

import com.gargoylesoftware.htmlunit.WebClient;
import model.util.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class NethysTraitsScraper extends NethysListScraper {

    public static void main(String[] args) {
        new NethysTraitsScraper();
    }

    public NethysTraitsScraper() {
        super(true, source->true);
        parseList("https://2e.aonprd.com/Traits.aspx", "ctl00_RadDrawer1_Content_MainContent_DetailedOutput",
                href->href.contains("Traits.aspx?ID="), e -> true);
        String prefix = "<?xml version = \"1.0\"?>\n" +
                "<pf2:traits xmlns:pf2=\"https://dylbrown.github.io\"\n" +
                "            xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "            xsi:schemaLocation=\"https://dylbrown.github.io ../../schemata/trait.xsd\">";
        String suffix = "\n" + "</pf2:traits>";
        printOutput(source->"generated/"+source+"/traits.pfdyl", prefix, suffix);
    }

    @Override
    protected void parseList(String inputURL, String container, Predicate<String> hrefValidator, Predicate<Element> elementValidator) {
        WebClient webClient = null;
        try {
            webClient = semaphore.getItem();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Document rootDocument = makeDocument(inputURL, webClient);
        if(isMultithreaded()) {
            semaphore.putItem(webClient);
        }
        String currentSection = "Core";
        for (Element child : rootDocument.getElementById(container).children()) {
            if(child.tagName().startsWith("h")) {
                currentSection = child.text().replace(" Traits", "");
            }
            else if(child.children().first() != null && child.children().first().tagName().equals("a")) {
                child = child.children().first();
                String href = "";
                try {
                    href = child.attr("href");
                    int id = -1;
                    try{
                        id = Integer.parseInt(href.replaceAll(".*ID=", ""));
                    }catch (NumberFormatException ignored) {}
                    if(elementValidator.test(child) && hrefValidator.test(href) && !ids.contains(id)) {
                        ids.add(id);
                        if(isMultithreaded())
                            setupItemMultithreaded(href, currentSection);
                        else
                            setupItem(href, currentSection, webClient);
                    }
                } catch (Exception e) {
                    System.out.println(href);
                    e.printStackTrace();
                }
            }
        }
        semaphore.putItem(webClient);
    }

    private void setupItem(String href, String currentSection, WebClient webClient) throws IOException {
        System.out.println(href);
        Entry entry = addItem(makeDocument("https://2e.aonprd.com/"+href, webClient));
        if (!entry.entry.isBlank()) {
            String clean = StringUtils.sanitizePath(entry.source);
            sources.computeIfAbsent(clean, key ->
                            new ConcurrentHashMap<>())
                    .computeIfAbsent(currentSection, s-> Collections.synchronizedList(new ArrayList<>()))
                    .add(entry);
            sourceNames.computeIfAbsent(clean, s->entry.source);
        }
    }

    private void setupItemMultithreaded(String href, String currentSection) {
        counter.incrementAndGet();
        completionService.submit(() -> {
            WebClient webClient = null;
            try {
                webClient = semaphore.getItem();
                try {
                    setupItem(href, currentSection, webClient);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.putItem(webClient);
            }
        }, true);
    }

    @Override
    protected void printList(Map<String, List<Entry>> map, Consumer<String> out) {
        map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry->
                {
                    out.accept("<Category name=\"");
                    out.accept(entry.getKey());
                    out.accept("\">\n");
                    entry.getValue().stream().sorted(Comparator.comparing(e->e.entryName))
                            .forEach(e-> out.accept(e.entry));
                    out.accept("</Category>\n");
                }
        );
    }

    @Override
    Entry addItem(Document doc) {
        if(doc == null) {
            return Entry.EMPTY;
        }
        StringBuilder result = new StringBuilder();
        Element output = doc.getElementById("main");
        String sourcePage = getAfter(output, "Source");
        int end = sourcePage.indexOf("pg. ");
        int endPage = sourcePage.indexOf(' ', end + 4);
        if(endPage == -1)
            endPage = sourcePage.length();
        String name = output.getElementsByTag("h1").first().text();
        String source = sourcePage.substring(0, end-1);
        String page = sourcePage.substring(end+4, endPage);
        result.append("\t<Trait page=\"").append(page).append("\">\n\t\t<Name>")
                .append(name)
                .append("</Name>\n\t\t<Description>");
        Node curr = output.getElementsByTag("br").first().nextSibling();
        while (curr != null && !(curr instanceof Element && ((Element) curr).tagName().equals("h2"))) {
            result.append(parseDesc(curr));
            curr = curr.nextSibling();
        }
        result.append("</Description>\n\t</Trait>\n");
        return new Entry(name, result.toString(), source);
    }
}
