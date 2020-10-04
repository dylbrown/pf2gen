package tools.nethys;

import model.util.StringUtils;
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
import java.util.TreeMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public abstract class NethysListScraper extends NethysScraper {
    final Map<String, Map<String, String>> sources = new ConcurrentHashMap<>();
    final Set<Integer> ids = new HashSet<>();
    final ExecutorService executorService = Executors.newCachedThreadPool();
    final CompletionService<Boolean> completionService= new ExecutorCompletionService<>(executorService);
    final Semaphore semaphore = new Semaphore(50);
    final AtomicInteger counter = new AtomicInteger(0);
    private final boolean multithreaded;

    protected NethysListScraper() {
        this(false);
    }

    public NethysListScraper(boolean multithreaded) {
        this.multithreaded = multithreaded;
    }

    public NethysListScraper(String inputURL, String outputPath, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator) {
        this(inputURL, outputPath, container, hrefValidator, sourceValidator, false);
    }

    public NethysListScraper(String inputURL, String outputPath, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator, boolean multithreaded) {
        this.multithreaded = multithreaded;
        parseList(inputURL, container, hrefValidator, e -> true);
        printOutput(outputPath, sourceValidator);
    }

    protected final void printOutput(String outputPath, Predicate<String> sourceValidator) {
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(new File(outputPath)), 32768);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Checking Completion now");
        try {
            for(int i = 0; i < counter.get(); i++) {
                completionService.take();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        afterThreadsCompleted();
        System.out.println("Writing to disk");
        for (Map.Entry<String, Map<String, String>> entry : sources.entrySet()) {
            if(!sourceValidator.test(entry.getKey()))
                continue;
            try {
                for (String value : new TreeMap<>(entry.getValue()).values()) {
                    out.append(value);
                }
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

    protected void parseList(String inputURL, String container, Predicate<String> hrefValidator,
                             Predicate<Element> elementValidator) {
        Document rootDocument;
        try  {
            rootDocument = Jsoup.connect(inputURL).get();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(inputURL);
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
                    if(multithreaded)
                        setupItemMultithreaded(href);
                    else
                        setupItem(href);
                }
            } catch (Exception e) {
                System.out.println(href);
                e.printStackTrace();
            }
        });
    }

    protected void setupItem(String href) throws IOException {
        System.out.println(href);
        Entry entry = addItem(Jsoup.connect("http://2e.aonprd.com/"+href).get());
        if (!entry.entry.isBlank())
            sources.computeIfAbsent(StringUtils.clean(entry.source), key -> new ConcurrentHashMap<>())
                    .put(entry.entryName, entry.entry);
    }

    protected void setupItemMultithreaded(String href) {
        counter.incrementAndGet();
        completionService.submit(() -> {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            try {
                setupItem(href);
            } catch (Exception e) {
                e.printStackTrace();
            }
            semaphore.release();
        }, true);
    }

    public static class Entry {
        public final String entryName;
        public final String entry;
        public final String source;

        public Entry(String entryName, String entry, String source) {
            this.entryName = entryName;
            this.entry = entry;
            this.source = source;
        }
    }

    abstract Entry addItem(Document doc);

    public boolean isMultithreaded() {
        return multithreaded;
    }
}
