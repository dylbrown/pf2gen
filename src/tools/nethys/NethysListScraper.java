package tools.nethys;

import model.util.StringUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class NethysListScraper extends NethysScraper {
    public enum Type {
        CSV,
        Webpage
    }

    final Map<String, Map<String, List<Entry>>> sources = new ConcurrentHashMap<>();
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
        this(inputURL, outputPath, container, hrefValidator, sourceValidator, Type.Webpage);
    }

    public NethysListScraper(String inputURL, String outputPath, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator, Type type) {
        this(inputURL, outputPath, container, hrefValidator, sourceValidator, type, false);
    }

    public NethysListScraper(String inputURL, Consumer<String> out, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator) {
        this(inputURL, out, container, hrefValidator, sourceValidator, false);
    }

    public NethysListScraper(String inputURL, String outputPath, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator, boolean multithreaded) {
        this(inputURL, outputPath ,container, hrefValidator, sourceValidator, Type.Webpage, multithreaded);
    }

    public NethysListScraper(String inputURL, String outputPath, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator, Type type, boolean multithreaded) {
        this.multithreaded = multithreaded;
        if(type == Type.Webpage)
            parseList(inputURL, container, hrefValidator, e -> true);
        if(type == Type.CSV)
            parseCSV(inputURL, hrefValidator);
        printOutput(outputPath, sourceValidator);
    }

    public NethysListScraper(String inputURL, Consumer<String> out, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator, boolean multithreaded) {
        this.multithreaded = multithreaded;
        parseList(inputURL, container, hrefValidator, e -> true);
        printOutput(out, sourceValidator);
    }

    protected final void printOutput(Function<String, String> sourceToFile, String prefix, String suffix) {
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
        sources.entrySet().parallelStream().forEach(entry->{
            try {
                File file = new File(sourceToFile.apply(entry.getKey()));
                if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    System.out.println("Failed to create dir "+ file);
                    return;
                }
                BufferedWriter out = new BufferedWriter(new FileWriter(file), 32768);
                out.write(prefix);
                printList(entry.getValue(), s-> {
                    try {
                        out.write(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                out.write(suffix);
                out.close();
                System.out.println("Wrote "+file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    protected final void printOutput(String outputPath, Predicate<String> sourceValidator) {
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(outputPath), 32768);
            printOutput(str -> {
                try {
                    out.write(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, sourceValidator);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected final void printOutput(Consumer<String> out, Predicate<String> sourceValidator) {
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
        for (Map.Entry<String, Map<String, List<Entry>>> entry : sources.entrySet()) {
            if(!sourceValidator.test(entry.getKey()))
                continue;
            printList(entry.getValue(), out);
        }
    }

    protected void printList(Map<String, List<Entry>> map, Consumer<String> out) {
        map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry->
                entry.getValue().stream().sorted(Comparator.comparing(e->e.entryName)).forEach(e-> out.accept(e.entry))
        );
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
        AtomicBoolean firstRow = new AtomicBoolean(true);
        rootDocument.getElementById(container).getElementsByTag("tbody").first()
                .getElementsByTag("tr").forEach(element -> {
            if(!firstRow.get()) {
                String href = "";
                try {
                    href = element.select("a[href*=\"?ID=\"]").first().attr("href");
                    int id = -1;
                    try{
                        id = Integer.parseInt(href.replaceAll(".*ID=", ""));
                    }catch (NumberFormatException ignored) {}
                    if(elementValidator.test(element) && hrefValidator.test(href) && !ids.contains(id)) {
                        ids.add(id);
                        if(multithreaded)
                            setupItemMultithreaded(href, element);
                        else
                            setupItem(href, element);
                    }
                } catch (Exception e) {
                    System.out.println(href);
                    e.printStackTrace();
                }
            } else firstRow.set(false);
        });
    }

    protected void parseCSV(String inputURL, Predicate<String> hrefValidator) {
        try {
            CSVParser.parse(new File(inputURL), Charset.defaultCharset(), CSVFormat.DEFAULT).forEach(record -> {
                        String href = "";
                        try {
                            href = record.get(0).replaceAll("(.*href=\"|\">.*)", "");
                            int id = -1;
                            try{
                                id = Integer.parseInt(href.replaceAll(".*ID=", ""));
                            }catch (NumberFormatException ignored) {}
                            if(hrefValidator.test(href) && !ids.contains(id)) {
                                ids.add(id);
                                if(multithreaded)
                                    setupItemMultithreaded(href, null);
                                else
                                    setupItem(href, null);
                            }
                        } catch (Exception e) {
                            System.out.println(href);
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void setupItem(String href, Element row) throws IOException {
        System.out.println(href);
        Entry entry = addItem(Jsoup.connect("http://2e.aonprd.com/"+href).get());
        if (!entry.entry.isBlank())
            sources.computeIfAbsent(StringUtils.clean(entry.source), key ->
                    new ConcurrentHashMap<>())
                    .computeIfAbsent(entry.getEntryName(), s->Collections.synchronizedList(new ArrayList<>()))
                    .add(entry);
    }

    protected void setupItemMultithreaded(String href, Element row) {
        counter.incrementAndGet();
        completionService.submit(() -> {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            try {
                setupItem(href, row);
            } catch (Exception e) {
                e.printStackTrace();
            }
            semaphore.release();
        }, true);
    }

    abstract Entry addItem(Document doc);

    public boolean isMultithreaded() {
        return multithreaded;
    }
}
