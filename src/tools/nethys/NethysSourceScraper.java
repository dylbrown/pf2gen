package tools.nethys;

import model.util.StringUtils;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public abstract class NethysSourceScraper extends NethysScraper {
    protected final String source;
    Map<String, List<Entry>> objects = new ConcurrentHashMap<>();

    public NethysSourceScraper(String inputURL, String source, Predicate<String> hrefValidator) {
        this.source = source;

        Document rootDocument = makeDocumentStatic(inputURL);
        rootDocument.getElementById("ctl00_RadDrawer1_Content_MainContent_DetailedOutput")
                .getElementsByTag("a").parallelStream()
                .map(element -> element.attr("href"))
                .filter(hrefValidator).forEach(href->{
                    Document document = makeDocumentStatic("https://2e.aonprd.com/" + href);
                    Entry entry = addItem(document);
                    if(entry != null)
                        objects.computeIfAbsent(entry.getEntryName(),
                                        s -> Collections.synchronizedList(new ArrayList<>()))
                            .add(entry);
                });
        objects.keySet().forEach(name->{
            File file = new File(nameToFileName(name));
            if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                System.out.println("Failed to create dir "+ file);
            }
        });
        objects.entrySet().parallelStream().forEach(entry->{
            BufferedWriter out;
            try {
                out = new BufferedWriter(new FileWriter(nameToFileName(entry.getKey())), 32768);
                for (Entry singleObject : entry.getValue()) {
                    out.write(singleObject.entry);
                }
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    protected String nameToFileName(String name) {
        return "generated/" + source + "/" + StringUtils.clean(name) + ".pfdyl";
    }

    abstract Entry addItem(Document doc);
}
