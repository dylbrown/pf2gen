package model.xml_parsers;

import model.data_managers.sources.SourceConstructor;
import model.data_managers.sources.SourceLoadTracker;
import model.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.function.Consumer;

import static model.util.StringUtils.clean;

public abstract class FileLoader<T> {
    private final SourceConstructor sourceConstructor;
    protected final SourceLoadTracker loadTracker;
    private final File root;
    private static final DocumentBuilderFactory factory;
    private final NavigableMap<String, T> allItems = new TreeMap<>();
    private final NavigableMap<String, NavigableMap<String, T>> categorizedItems = new TreeMap<>();

    static{
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
    }

    public FileLoader(SourceConstructor sourceConstructor, File root) {
        this.sourceConstructor = sourceConstructor;
        this.loadTracker = new SourceLoadTracker(sourceConstructor);
        this.root = root;
    }

    public File getRoot() {
        return root;
    }

    public NavigableMap<String, T> getAll() {
        if(loadTracker.isNotAllLoaded()) {
            if(sourceConstructor.getType() == SourceConstructor.Type.SingleFileMultiItem) {
                load("");
            } else {
                for (Map.Entry<String, String> entry : sourceConstructor.map().entrySet()) {
                    if(sourceConstructor.isMultiplePerFile())
                        loadMultiple(entry.getKey(), entry.getValue());
                    else
                        loadSingle(entry.getValue());
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

    public NavigableMap<String, T> getCategory(String category) {
        if(sourceConstructor.getType() != SourceConstructor.Type.MultiItemMultiFile)
            return getAll();
        if(loadTracker.isNotLoaded(category))
            load(category, "");
        return Collections.unmodifiableNavigableMap(getCategoryInternal(category));
    }

    private NavigableMap<String, T> getCategoryInternal(String category) {
        return categorizedItems.computeIfAbsent(clean(category), s->new TreeMap<>());
    }

    private void load(String category, String name) {
        if(sourceConstructor.getType() == SourceConstructor.Type.MultiItemMultiFile) {
            loadMultiple(category, sourceConstructor.getLocation(category));
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
                loadSingle(sourceConstructor.getLocation(name));
                break;
            case MultiItemMultiFile:
                for (Map.Entry<String, String> entry : sourceConstructor.map().entrySet()) {
                    loadMultiple(entry.getKey(), entry.getValue());
                }
                break;
        }
    }

    protected File getSubFile(String location) {
        return new File(root.getPath() + "/" + location);
    }

    private void loadSingle(String location) {
        loadTracker.setLoaded(location);
        File subFile = getSubFile(location);
        Document doc = getDoc(subFile);
        T t = parseItem(subFile.getName(), doc.getDocumentElement());
        addItem("", t);
    }

    protected void loadMultiple(String category, String location) {
        loadTracker.setLoaded(location);
        File subFile = getSubFile(location);
        Document doc = getDoc(subFile);
        NodeList childNodes = doc.getDocumentElement().getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if(item.getNodeType() != Node.ELEMENT_NODE)
                continue;
            T t = parseItem(subFile.getName(), (Element) item);
            addItem(category, t);
        }
    }

    protected void addItem(String category, T t) {
        allItems.put(clean(t.toString()), t);
        if(category != null && category.length() > 0)
            getCategoryInternal(category).put(clean(t.toString()), t);
    }

    protected abstract T parseItem(String filename, Element item);

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
                URL url = new URL("https://dylbrown.github.io/pf2gen_data/"+path.toString().replaceAll("\\\\", "/"));
                System.out.println("Could not find "+path.getName()+" on disk, loading from repository.");
                doc= factory.newDocumentBuilder().parse(url.openStream());
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

    protected List<Pair<Document, String>> getDocs(File path) {
        List<Pair<Document, String>> results = new ArrayList<>();
        if(path.exists()) {
            for (File file : Objects.requireNonNull(path.listFiles())) {
                if(file.getName().endsWith("pfdyl"))
                    results.add(new Pair<>(getDoc(file), file.getName()));
            }

        }else{
            try {
                URL index = new URL("https://dylbrown.github.io/pf2gen_data/"+path.toString().replaceAll("\\\\", "/")+"/index.txt"+ "?_=" + System.currentTimeMillis() );
                URLConnection urlConnection = index.openConnection();
                urlConnection.setDefaultUseCaches(false);
                urlConnection.setUseCaches(false);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String temp;
                while((temp = bufferedReader.readLine()) != null)
                    results.add(new Pair<>(getDoc(new File(path.toString()+"\\"+temp+".pfdyl")), temp+".pfdyl"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public Set<String> getCategories() {
        return Collections.unmodifiableSet(categorizedItems.keySet());
    }
}
