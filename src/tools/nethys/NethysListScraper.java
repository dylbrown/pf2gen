package tools.nethys;

import com.gargoylesoftware.htmlunit.WebClient;
import model.util.StringUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    final Map<String, String> sourceNames = new ConcurrentHashMap<>();
    final Set<Integer> ids = new HashSet<>();
    final ExecutorService executorService = Executors.newCachedThreadPool();
    final CompletionService<Boolean> completionService= new ExecutorCompletionService<>(executorService);
    final AtomicInteger counter = new AtomicInteger(0);
    final Predicate<String> sourceValidator;
    private final boolean multithreaded;
    protected final ProxyPool semaphore = new ProxyPool(20);

    protected NethysListScraper() {
        this(false, source->true);
    }

    public NethysListScraper(boolean multithreaded, Predicate<String> sourceValidator) {
        this.multithreaded = multithreaded;
        this.sourceValidator = sourceValidator;
    }

    public NethysListScraper(String inputURL, String outputPath, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator) {
        this(inputURL, outputPath, container, hrefValidator, sourceValidator, Type.Webpage);
    }

    public NethysListScraper(String inputURL, String outputPath, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator, Type type) {
        this(inputURL, outputPath, container, hrefValidator, sourceValidator, type, true);
    }

    public NethysListScraper(String inputURL, Consumer<String> out, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator) {
        this(inputURL, out, container, hrefValidator, sourceValidator, false);
    }

    public NethysListScraper(String inputURL, String outputPath, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator, boolean multithreaded) {
        this(inputURL, outputPath ,container, hrefValidator, sourceValidator, Type.CSV, multithreaded);
    }

    public NethysListScraper(String inputURL, String outputPath, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator, Type type, boolean multithreaded) {
        this.multithreaded = multithreaded;
        this.sourceValidator = sourceValidator;
        if(type == Type.Webpage)
            parseList(inputURL, container, hrefValidator, e -> true);
        if(type == Type.CSV)
            parseCSV(inputURL, hrefValidator);
        printOutput(outputPath);
    }

    public NethysListScraper(String inputURL, Consumer<String> out, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator, boolean multithreaded) {
        this.multithreaded = multithreaded;
        this.sourceValidator = sourceValidator;
        parseList(inputURL, container, hrefValidator, e -> true);
        printOutput(source->out);
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

    protected final void printOutput(String outputPath) {
        List<BufferedWriter> writers = new ArrayList<>();
        final Function<String, Consumer<String>> getOutput = source -> {
            File file = new File("generated/" + source + "/" + outputPath);
            if(file.getParentFile().mkdirs())
                System.out.println("Created "+ file);
            BufferedWriter out;
            try {
                out = new BufferedWriter(new FileWriter(file), 32768);
                writers.add(out);
                return str -> {
                    try {
                        out.write(str);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };
        printOutput(getOutput);
        for (BufferedWriter writer : writers) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected final void printOutput(Function<String, Consumer<String>> getOutput) {
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
            printList(entry.getValue(), getOutput.apply(sourceNames.get(entry.getKey())));
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
        AtomicBoolean firstRow = new AtomicBoolean(true);
        WebClient finalWebClient = webClient;
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

    protected void parseCSV(String inputURL, Predicate<String> hrefValidator) {
        WebClient webClient = null;
        try {
            webClient = multithreaded ? null : semaphore.getItem();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            WebClient finalWebClient = webClient;
            CSVParser.parse(new File(inputURL), Charset.defaultCharset(), CSVFormat.DEFAULT).forEach(record -> {
                // Check source
                String source = record.get(2).replaceAll(".*title=\"", "")
                        .replaceAll("\">.*", "");
                if(!sourceValidator.test(StringUtils.clean(source))) {
                    return;
                }
                // Load content
                String href = "";
                try {
                    href = record.get(0).replaceAll("(.*href=\"|\">.*)", "");
                    int id = -1;
                    try {
                        id = Integer.parseInt(href.replaceAll(".*ID=", ""));
                    } catch (NumberFormatException ignored) {
                    }
                    if (hrefValidator.test(href) && !ids.contains(id)) {
                        ids.add(id);
                        if (multithreaded)
                            setupItemMultithreaded(href, null);
                        else
                            setupItem(href, null, finalWebClient);
                    }
                } catch (Exception e) {
                    System.out.println(href);
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        semaphore.putItem(webClient);
    }

    protected void setupItem(String href, Element row, WebClient webClient) throws IOException {
        System.out.println(href);
        Entry entry = addItem(makeDocument("https://2e.aonprd.com/" + href, webClient));
        if (!entry.entry.isBlank()) {
            String clean = StringUtils.clean(entry.source);
            sources.computeIfAbsent(StringUtils.clean(entry.source), key ->
                            new ConcurrentHashMap<>())
                    .computeIfAbsent(entry.getEntryName(), s->Collections.synchronizedList(new ArrayList<>()))
                    .add(entry);
            sourceNames.computeIfAbsent(clean, s->entry.source);
        }
    }

    protected void setupItemMultithreaded(String href, Element row) {
        counter.incrementAndGet();
        completionService.submit(() -> {
            WebClient webClient = null;
            try {
                webClient = semaphore.getItem();
                try {
                    setupItem(href, row, webClient);
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

    abstract Entry addItem(Document doc);

    public boolean isMultithreaded() {
        return multithreaded;
    }
}
