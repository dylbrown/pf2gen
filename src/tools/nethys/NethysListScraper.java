package tools.nethys;

import model.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public abstract class NethysListScraper extends NethysScraper {
    final Map<String, StringBuilder> sources = new HashMap<>();
    final Set<Integer> ids = new HashSet<>();

    public NethysListScraper(String inputURL, String outputPath, String container, Predicate<String> hrefValidator) {
        Document rootDocument;
        BufferedWriter out;
        try  {
            rootDocument = Jsoup.connect(inputURL).get();
            out = new BufferedWriter(new FileWriter(new File(outputPath)));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        rootDocument.getElementById(container).getElementsByTag("a").forEach(element -> {
            String href = "";
            try {
                href = element.attr("href");
                int id = -1;
                try{
                    id = Integer.parseInt(href.replaceAll(".*ID=", ""));
                }catch (NumberFormatException ignored) {}
                if(hrefValidator.test(href) && !ids.contains(id)) {
                    ids.add(id);
                    try  {
                        System.out.println(href);
                        Pair<String, String> pair = addItem(Jsoup.connect("http://2e.aonprd.com/"+href).get());
                        if (!pair.first.equals(""))
                            sources.computeIfAbsent(pair.second.toLowerCase(), key -> new StringBuilder())
                                    .append(pair.first);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                System.out.println(href);
                e.printStackTrace();
            }
        });
        for (StringBuilder value : sources.values()) {
            try {
                out.write(value.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    abstract Pair<String, String> addItem(Document doc);
}
