package tools.nethys;

import model.util.StringUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import tools.nethys.builders.ItemBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class NethysItemScraper extends NethysScraper {
	// source, category, subcategory
	private Map<String, Map<String, Map<String, StringBuilder>>> strings = new HashMap<>();
	private Set<Integer> visited = new HashSet<>();

	public static void main(String[] args) {
		new NethysItemScraper("C:\\Users\\dylan\\Downloads\\RadGridExport (4).csv");
	}

	private NethysItemScraper(String inputURL) {
		try {
			boolean isFirst = true;
			for (CSVRecord record : CSVParser.parse(new File(inputURL), Charset.defaultCharset(), CSVFormat.DEFAULT)) {
				if(isFirst) {
					isFirst = false;
					continue;
				}
				String href = record.get(0).replaceAll("(.*href=\"|\">(.|\n|\r)*)", "");
				String source = record.get(2).replaceAll("((.|\n|\r)*<a [^>]+>|</a>(.|\n|\r)*)", "");
				String category = StringUtils.clean(record.get(5).replaceAll("((.|\n|\r)*<a [^>]+>|</a>(.|\n|\r)*)", ""));
				String subCategory = record.get(6).replaceAll("((.|\n|\r)*<a [^>]+>|</a>(.|\n|\r)*)", "");
				if(subCategory.equals("—")) subCategory = null;
				StringBuilder stringBuilder = strings.computeIfAbsent(source, s -> new HashMap<>())
						.computeIfAbsent(category, s -> new HashMap<>())
						.computeIfAbsent(subCategory, s -> new StringBuilder());
				addItem(href, stringBuilder);
			}

			for (Map.Entry<String, Map<String, Map<String, StringBuilder>>> sourceEntry : strings.entrySet()) {
				String source = StringUtils.sanitizePath(sourceEntry.getKey());
				String itemsFolder = "generated/" + source + "/items/";
				if(!new File(itemsFolder).mkdirs())
					System.out.println(itemsFolder);
				for (Map.Entry<String, Map<String, StringBuilder>> categoryEntry : sourceEntry.getValue().entrySet()) {
					String category = categoryEntry.getKey();
					BufferedWriter out = new BufferedWriter(
							new FileWriter(itemsFolder + StringUtils.clean(category) + ".pfdyl"));
					out.write("<pf2:Items category=\""+category+"\" xmlns:pf2=\"https://dylbrown.github.io\"\n" +
							"\t\t   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
							"\t\t   xsi:schemaLocation=\"https://dylbrown.github.io ../../../schemata/item.xsd\">\n");
					for (Map.Entry<String, StringBuilder> subcategoryEntry : categoryEntry.getValue().entrySet()) {
						String subcategory = subcategoryEntry.getKey();
						if(subcategory != null)
							out.write("<SubCategory name=\""+subcategory+"\">\n");
						out.write(subcategoryEntry.getValue().toString());
						if(subcategory != null)
							out.write("</SubCategory>\n");
					}
					out.write("</pf2:Items>");
					out.close();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addItem(String href, StringBuilder items) {
		Document doc;
		try  {
			doc = Jsoup.connect(href).get();
		} catch (IOException e) {
			System.out.println(href);
			e.printStackTrace();
			return;
		}
		Element output = doc.getElementById("main");
		Elements titles = output.getElementsByClass("title");
		ItemBuilder baseItem = new ItemBuilder();
		baseItem.setLevel(titles.first().getElementsByTag("span").text()
								.replaceAll("[^\\d]", ""));
		makeSpecificItem(titles.first(), baseItem);
		if(titles.size() == 1) {
			items.append(baseItem.build());
		} else {
			for(int i=1; i < titles.size(); i++) {
				items.append(makeSpecificItem(titles.get(i), baseItem.makeSubItem()).build());
			}
		}
	}

	private ItemBuilder makeSpecificItem(Element element, ItemBuilder item) {
		StringBuilder desc = new StringBuilder();
		item.setName(element.ownText());
		Node curr = element.nextSibling();
		String previousLabel = null;
		boolean inDescription = false;
		while (!(curr instanceof Element && ((Element) curr).tagName().equals("h2")) && curr != null) {
			if(inDescription) {
				desc.append(parseDesc(curr));
			}else if(previousLabel == null) {
				if(curr instanceof Element && ((Element) curr).hasClass("trait")) {
					item.addTrait(((Element) curr).text());
				}else if(curr instanceof Element && ((Element) curr).tagName().equals("b")) {
					previousLabel = ((Element) curr).text();
				} else if(curr instanceof TextNode && ((TextNode) curr).getWholeText().trim().length() > 0) {
					desc.append(parseDesc(curr));
					inDescription = true;
				} else if(curr instanceof Element && ((Element) curr).tagName().equals("hr")) {
					inDescription = true;
				}
			} else {
				if(curr instanceof Element && (((Element) curr).tagName().equals("br")
											|| ((Element) curr).tagName().equals("b")
											|| ((Element) curr).tagName().equals("hr"))) {
					previousLabel = null;
					continue;
				}
				String trim = null;
				if(curr instanceof Element) {
					trim = ((Element) curr).text().trim().replaceAll(";\\z", "");
				} else if(curr instanceof TextNode && ((TextNode) curr).getWholeText().trim().length() > 0) {
					trim = ((TextNode) curr).getWholeText().trim().replaceAll(";\\z", "");
				}
				if(trim != null && trim.length() > 0){
					switch (previousLabel.trim().toLowerCase()) {
						case "source":
							item.setSourcePage(trim.replaceAll(".*pg[^\\d]*", ""));
							break;
						case "level":
							item.setLevel(trim.replaceAll("[^\\d]+", ""));
							break;
						case "price":
							item.setPrice(trim);
							break;
						case "hands":
							item.setHands(trim.replaceAll("[^\\d]+", ""));
							break;
						case "bulk":
							item.setBulk(trim);
							break;
						case "usage":
							item.setUsage(trim);
							break;
					}
					previousLabel = null;
				}
			}
			curr = curr.nextSibling();
		}
		if(desc.length() > 0) item.appendToDescription(desc.toString());

		return item;
	}
}
