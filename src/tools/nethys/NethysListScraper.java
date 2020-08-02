package tools.nethys;

import model.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public abstract class NethysListScraper extends NethysScraper {
    final Map<String, StringBuilder> sources = new ConcurrentHashMap<>();
    final Set<Integer> ids = new HashSet<>();
    final ExecutorService executorService = Executors.newCachedThreadPool();
    final CompletionService<Boolean> completionService= new ExecutorCompletionService<>(executorService);
    final AtomicInteger counter = new AtomicInteger(0);

    protected NethysListScraper() {

    }

    public NethysListScraper(String inputURL, String outputPath, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator) {
        parseList(inputURL, container, hrefValidator, e -> true);
        printOutput(outputPath, sourceValidator);
    }

    protected final void printOutput(String outputPath, Predicate<String> sourceValidator) {
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(new File(outputPath)), 65536);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            for(int i = 0; i < counter.get(); i++) {
                completionService.take();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        afterThreadsCompleted();
        for (Map.Entry<String, StringBuilder> entry : sources.entrySet()) {
            if(!sourceValidator.test(entry.getKey()))
                continue;
            try {
                out.write(entry.getValue().toString());
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

    protected void afterThreadsCompleted() {}

    protected final void parseList(String inputURL, String container, Predicate<String> hrefValidator,
                             Predicate<Element> elementValidator) {
        Document rootDocument;
        try  {
            rootDocument = Jsoup.connect(inputURL).get();
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
                if(elementValidator.test(element) && hrefValidator.test(href) && !ids.contains(id)) {
                    ids.add(id);
                    try  {
                        setupItem(href);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                System.out.println(href);
                e.printStackTrace();
            }
        });
    }

    protected void setupItem(String href) throws IOException {
        System.out.println(href);
        Pair<String, String> pair = addItem(Jsoup.connect("http://2e.aonprd.com/"+href).get());
        if (!pair.first.equals(""))
            sources.computeIfAbsent(pair.second.toLowerCase(), key -> new StringBuilder())
                    .append(pair.first);
    }

    abstract Pair<String, String> addItem(Document doc);
}
