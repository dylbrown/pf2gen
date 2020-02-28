package model.xml_parsers;

import model.data_managers.EquipmentManager;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

abstract class FileLoader<T> {
    File path;
    private static final DocumentBuilderFactory factory;

    static{
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        EquipmentManager.getEquipment();//TODO: Separate out loading weapon groups
    }

    public abstract List<T> parse();

    Document getDoc(File path) {
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

    void iterateElements(Document doc, String tagName, Consumer<Element> consumer) {
        NodeList groupNodes = doc.getElementsByTagName(tagName);
        for(int i=0; i<groupNodes.getLength(); i++) {
            if(groupNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            consumer.accept((Element) groupNodes.item(i));
        }
    }

    List<Pair<Document, String>> getDocs(File path) {
        List<Pair<Document, String>> results = new ArrayList<>();
        if(path.exists()) {
            for (File file : Objects.requireNonNull(path.listFiles())) {
                if(file.getName().substring(file.getName().length()-5).equals("pfdyl"))
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
}
