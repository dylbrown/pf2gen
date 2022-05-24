package model.xml_parsers;

import model.data_managers.sources.Source;
import model.data_managers.sources.SourceConstructor;
import model.data_managers.sources.SourceLoadTracker;
import model.data_managers.sources.SourcesLoader;
import model.util.ObjectNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static model.util.StringUtils.clean;

public abstract class FileLoader<T> {
    protected final SourceConstructor sourceConstructor;
    protected final SourceLoadTracker loadTracker;
    private final File root;
    private static final DocumentBuilderFactory factory;
    private final NavigableMap<String, T> allItems = new TreeMap<>();
    private final NavigableMap<String, NavigableMap<String, T>> categorizedItems = new TreeMap<>();

    static{
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
    }

    private boolean loadedFromRepository = false;
    private Source source = null;

    protected String getName(T t) {
        return t.toString();
    }

    public FileLoader(SourceConstructor sourceConstructor, File root, Source.Builder sourceBuilder) {
        this.sourceConstructor = sourceConstructor;
        this.loadTracker = new SourceLoadTracker(sourceConstructor, sourceBuilder);
        this.root = root;
        if(sourceBuilder != null) {
            source = sourceBuilder.onBuild((source)->this.source = source);
        }
    }

    protected FileLoader(SourceConstructor sourceConstructor, File root) {
        this.sourceConstructor = sourceConstructor;
        this.loadTracker = new SourceLoadTracker(sourceConstructor, null);
        this.root = root;
    }

    public Source getSource() {
        return source;
    }

    public File getRoot() {
        return root;
    }

    public boolean isNotAllLoaded() {
        return loadTracker.isNotAllLoaded();
    }

    public NavigableMap<String, T> getAll() {
        if(loadTracker.isNotAllLoaded()) {
            if(sourceConstructor.getType() == SourceConstructor.Type.SingleFileMultiItem) {
                load("");
            } else {
                for (Map.Entry<String, List<String>> entry : sourceConstructor.map().entrySet()) {
                    for (String path : entry.getValue()) {
                        if(sourceConstructor.isMultiplePerFile())
                            loadMultiple(entry.getKey(), path);
                        else
                            loadSingle(path);
                    }
                }
            }
        }
        return Collections.unmodifiableNavigableMap(allItems);
    }

    public T find(String name) {
        if(loadTracker.isNotLoaded(name))
            load(name);
        return allItems.get(clean(name));

    }


    public T find(String category, String name) {
        return getCategory(category).get(clean(name));
    }

    public <A, B extends FileLoader<A>> A findFromDependencies(String nameOfA, Class<B> loaderClass, String name) throws ObjectNotFoundException {
        A a;
        B loader = source.getLoader(loaderClass);
        a = (loader != null) ? loader.find(name) : null;
        if(a != null) return a;
        for (String dependency : source.getDependencies()) {
            Source childSource = SourcesLoader.instance().find(dependency);
            if(childSource != null) {
                loader = childSource.getLoader(loaderClass);
                a = (loader != null) ? loader.find(name) : null;
                if(a != null) return a;
            }
        }
        throw new ObjectNotFoundException(name, nameOfA);
    }

    public <A, B extends FileLoader<A>> A findFromDependencies(String nameOfA, Class<B> loaderClass, String name, String category) throws ObjectNotFoundException {
        A a;
        B loader = source.getLoader(loaderClass);
        a = (loader != null) ? loader.find(category, name) : null;
        if(a != null) return a;
        for (String dependency : source.getDependencies()) {
            Source childSource = SourcesLoader.instance().find(dependency);
            if(childSource != null) {
                loader = childSource.getLoader(loaderClass);
                a = (loader != null) ? loader.find(category, name) : null;
                if(a != null) return a;
            }
        }
        throw new ObjectNotFoundException(name, nameOfA);
    }

    public <A, B extends FileLoader<A>> NavigableMap<String, A> findCategoryFromDependencies(String nameOfA, Class<B> loaderClass, String category) {
        TreeMap<String, A> map = new TreeMap<>();
        B loader = source.getLoader(loaderClass);
        if (loader != null) {
            loader.getCategory(category).entrySet().parallelStream()
                    .forEach(e->map.put(e.getKey(), e.getValue()));
        }
        for (String dependency : source.getDependencies()) {
            Source childSource = SourcesLoader.instance().find(dependency);
            if(childSource != null) {
                loader = childSource.getLoader(loaderClass);
                if (loader != null) {
                    loader.getCategory(category).entrySet().parallelStream()
                            .forEach(e->map.put(e.getKey(), e.getValue()));
                }
            }
        }
        return map;
    }

    public NavigableMap<String, T> getCategory(String category) {
        if(loadTracker.isNotLoaded(category))
            load(category, "");
        return Collections.unmodifiableNavigableMap(getCategoryInternal(category));
    }

    private NavigableMap<String, T> getCategoryInternal(String category) {
        return categorizedItems.computeIfAbsent(clean(category), s->new TreeMap<>());
    }

    private void load(String category, String name) {
        if(sourceConstructor.getType() == SourceConstructor.Type.MultiItemMultiFile) {
            for (String path : sourceConstructor.getLocation(category)) {
                loadMultiple(category, path);
            }
        } else load(name);
    }

    private void load(String name) {
        switch (sourceConstructor.getType()) {
            case SingleFileSingleItem:
                loadSingle(sourceConstructor.getLocation());
                break;
            case SingleFileMultiItem:
                loadMultiple("", sourceConstructor.getLocation());
                break;
            case MultiFileSingleItem:
                for (String path : sourceConstructor.getLocation(name)) {
                    loadSingle(path);
                }
                break;
            case MultiItemMultiFile:
                for (Map.Entry<String, List<String>> entry : sourceConstructor.map().entrySet()) {
                    for (String path : entry.getValue()) {
                        loadMultiple(entry.getKey(), path);
                    }
                }
                break;
        }
    }

    protected File getSubFile(String location) {
        return new File(root.getPath() + "/" + location);
    }

    private void loadSingle(String location) {
        if(!loadTracker.isNotLoaded(location))
            return;
        clearAccumulators();
        loadTracker.setLoaded(location);
        File subFile = getSubFile(location);
        Document doc = getDoc(subFile);
        T t = parseItem(subFile, doc.getDocumentElement());
        addItem("", t);
    }

    protected void loadMultiple(String category, String location) {
        if(!loadTracker.isNotLoaded(location) || location == null)
            return;
        clearAccumulators();
        loadTracker.setLoaded(location);
        File subFile = getSubFile(location);
        Document doc = getDoc(subFile);
        NodeList childNodes = doc.getDocumentElement().getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if(item.getNodeType() != Node.ELEMENT_NODE)
                continue;
            T t = parseItem(subFile, (Element) item, category);
            addItem(category, t);
        }
    }

    protected void addItem(String category, T t) {
        synchronized (this) {
            String clean = clean(getName(t));
            assert(!clean.isBlank());
            allItems.put(clean, t);
            if(category != null && category.length() > 0)
                getCategoryInternal(category).put(clean, t);
        }
    }

    protected abstract T parseItem(File file, Element item);

    protected T parseItem(File file, Element item, String category) {
        return parseItem(file, item);
    }

    protected Document getDoc(File path) {
        Document doc = null;
        if(path.exists()) {
            try {
                doc = factory.newDocumentBuilder().parse(path);
            } catch (IOException | SAXException | ParserConfigurationException e) {
                e.printStackTrace();
            }
        }else{
            try {
                URL url = new URL("https://dylbrown.github.io/pf2gen_data/" + path.toString()
                                .replaceAll("\\\\", "/")
                                .replaceAll(" ", "%20"));
                System.out.println(" - remote: "+path.getName()+"");
                doc= factory.newDocumentBuilder().parse(url.openStream());
                this.loadedFromRepository = true;
            } catch ( SAXException|IOException|ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        assert doc != null;
        return doc;
    }

    protected void iterateElements(Document doc, String tagName, Consumer<Element> consumer) {
        NodeList groupNodes = doc.getElementsByTagName(tagName);
        for(int i=0; i<groupNodes.getLength(); i++) {
            if(groupNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            consumer.accept((Element) groupNodes.item(i));
        }
    }

    public Set<String> getCategories() {
        if(sourceConstructor.getType() == SourceConstructor.Type.MultiItemMultiFile) {
            return Collections.unmodifiableSet(sourceConstructor.map().keySet());
        }
        return Collections.unmodifiableSet(categorizedItems.keySet());
    }

    public boolean isLoadedFromRepository() {
        return loadedFromRepository;
    }

    protected void clearAccumulators() {}
}
