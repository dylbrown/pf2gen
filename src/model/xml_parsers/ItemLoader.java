package model.xml_parsers;

import model.enums.Trait;
import model.equipment.Equipment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static model.util.StringUtils.camelCaseWord;

public class ItemLoader extends FileLoader<Equipment> {
    private List<Equipment> items = null;

    public ItemLoader(String s) {
        path = new File("data/equipment/"+s);
    }
    @Override
    public List<Equipment> parse() {
        if(items == null) {
            items = new ArrayList<>();
            Document doc = getDoc(path);
            NodeList nodes = doc.getElementsByTagName("Item");
            for(int i=0; i<nodes.getLength(); i++) {
                if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element curr = (Element) nodes.item(i);
                items.add(makeItem(curr));
            }
        }
        return items;
    }

    private Equipment makeItem(Element item) {
        Equipment.Builder builder = new Equipment.Builder();
        NodeList nodeList = item.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            if(nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element curr = (Element) nodeList.item(i);
            String trim = curr.getTextContent().trim();
            parseTag(trim, curr, builder);
        }
        return builder.build();
    }

    static void parseTag(String trim, Element curr, Equipment.Builder builder) {
        switch (curr.getTagName()) {
            case "Name":
                builder.setName(trim);
                break;
            case "Description":
                builder.setDescription(trim);
                break;
            case "Price":
                builder.setValue(getPrice(trim));
                break;
            case "Bulk":
                if (trim.toUpperCase().equals("L"))
                    builder.setWeight(.1);
                else
                    builder.setWeight(Double.parseDouble(trim));
                break;
            case "Hands":
                builder.setHands(Integer.parseInt(trim));
                break;
            case "Traits":
                Arrays.stream(trim.split(",")).map((item)->
                        Trait.valueOf(camelCaseWord(item.trim())))
                    .forEachOrdered(builder::addTrait);
                break;
        }
    }


    private static double getPrice(String priceString) {
        String[] split = priceString.split(" ");
        double value = Double.parseDouble(split[0]);
        switch(split[1].toLowerCase()) {
            case "cp":
                value *= .1;
                break;
            case "gp":
                value *= 10;
                break;
            case "pp":
                value *= 100;
                break;
        }
        return value;
    }
}
