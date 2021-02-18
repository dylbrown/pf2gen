package tools.nethys;

import model.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class NethysTraitsScraper extends NethysListScraper {

    public static void main(String[] args) {
        new NethysTraitsScraper("https://2e.aonprd.com/Traits.aspx", "generated/traits.pfdyl");
    }

    public NethysTraitsScraper(String inputURL, String outputPath) {
        super(true);
        parseList(inputURL, "ctl00_MainContent_DetailedOutput",
                href->href.contains("Traits.aspx?ID="), e -> true);
        printOutput(outputPath, source->source.equals("advanced_player's_guide"));
    }

    @Override
    protected void parseList(String inputURL, String container, Predicate<String> hrefValidator, Predicate<Element> elementValidator) {
        Document rootDocument;
        try  {
            rootDocument = Jsoup.connect(inputURL).get();
        } catch (IOException e) {
            e.printStackTrace();
            return;
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
                            setupItem(href, currentSection);
                    }
                } catch (Exception e) {
                    System.out.println(href);
                    e.printStackTrace();
                }
            }
        }
    }

    private void setupItem(String href, String currentSection) throws IOException {
        System.out.println(href);
        Entry entry = addItem(Jsoup.connect("http://2e.aonprd.com/"+href).get());
        if (!entry.entry.isBlank())
            sources.computeIfAbsent(StringUtils.clean(entry.source), key ->
                    new ConcurrentHashMap<>())
                    .computeIfAbsent(currentSection, s-> Collections.synchronizedList(new ArrayList<>()))
                    .add(entry);
    }

    private void setupItemMultithreaded(String href, String currentSection) {
        counter.incrementAndGet();
        completionService.submit(() -> {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            try {
                setupItem(href, currentSection);
            } catch (Exception e) {
                e.printStackTrace();
            }
            semaphore.release();
        }, true);
    }

    @Override
    protected void printList(Map<String, List<Entry>> map, Writer out) {
        map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry->
                {
                    try {
                        out.append("<Category name=\"").append(entry.getKey()).append("\">\n");
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    entry.getValue().stream().sorted(Comparator.comparing(e->e.entryName)).forEach(e->{
                        try {
                            out.append(e.entry);
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    });
                    try {
                        out.append("</Category>\n");
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                }
        );
    }

    @Override
    Entry addItem(Document doc) {
        StringBuilder result = new StringBuilder();
        Element output = doc.getElementById("ctl00_MainContent_DetailedOutput");
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
