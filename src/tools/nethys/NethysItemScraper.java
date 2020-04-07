package tools.nethys;

import model.util.Pair;
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
import java.util.*;

class NethysItemScraper extends NethysScraper {
	private Map<String, StringBuilder> sources = new HashMap<>();
	private final Set<Integer> visited = new HashSet<>();
	private static final List<Integer> extensions = new ArrayList<>(Arrays.asList(
			1,15,6,45,21,22,23,2,31,32,33,34,41
	));

	public static void main(String[] args) {
		for (int extension : extensions) {
			new NethysItemScraper("https://2e.aonprd.com/Equipment.aspx?Category="+extension);
		}
	}

	private NethysItemScraper(String inputURL) {
		Document doc;
		BufferedWriter out;
		try  {
			doc = Jsoup.connect(inputURL).get();
			String title = doc.getElementById("main").getElementsByClass("title").first().text();
			File outputFile = new File("generated/"
					+ title.toLowerCase().replaceAll(" ", "_")
					+ ".pfdyl");
			if(outputFile.exists()) return;
			out = new BufferedWriter(new FileWriter(outputFile));
			out.write("<pf2:Items category=\""+title+"\" xmlns:pf2=\"https://dylbrown.github.io\"\n" +
					"\t\t   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
					"\t\t   xsi:schemaLocation=\"https://dylbrown.github.io ../../schemata/item.xsd\">\n");
			parseTable(doc, out);
			out.write("</pf2:Items>");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseTable(Document doc, BufferedWriter out) {
		if (doc.getElementById("ctl00_MainContent_TreasureElement") == null){
			for (Element subCat : doc.getElementById("ctl00_MainContent_SubNavigation").getElementsByTag("a")) {
				try {
					Document subDoc = Jsoup.connect("https://2e.aonprd.com/"+subCat.attr("href")).get();
					String subCatName = subDoc.getElementById("main")
							.getElementsByClass("title").first().text();
					out.write("<SubCategory name=\""+subCatName+"\">\n");
					parseTable(subDoc, out);
					out.write("</SubCategory>");
					sources = new HashMap<>();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return;
		}
		doc.getElementById("ctl00_MainContent_TreasureElement").getElementsByTag("a").forEach(element -> {
			String href = "";
			try {
				href = element.attr("href");
				if (href.contains("ID")) {
					int i = Integer.parseInt(href.replaceAll(".*\\?ID=", ""));
					if (!visited.contains(i)) {
						visited.add(i);
						Pair<String, String> pair = addItem(href);
						if (!pair.first.equals(""))
							sources.computeIfAbsent(pair.second.toLowerCase(), key -> new StringBuilder())
									.append(pair.first);
					}
				}
			} catch (Exception e) {
				System.out.println(href);
				e.printStackTrace();
			}
		});
		for (Map.Entry<String, StringBuilder> entry : sources.entrySet()) {
			try {
				if(entry.getKey().equals("core rulebook"))
					out.write(entry.getValue().toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private Pair<String, String> addItem(String href) {
		StringBuilder items = new StringBuilder();
		Document doc;
		try  {
			doc = Jsoup.connect("http://2e.aonprd.com/"+href).get();
		} catch (IOException e) {
			e.printStackTrace();
			return new Pair<>("", "");
		}
//		if(href.equals("Equipment.aspx?ID=495"))
//			System.out.println("Jellyfish");
		Element output = doc.getElementById("main");
		String sourceAndPage = output.getElementsMatchingText("\\ASource\\z").first().nextElementSibling().text();
		String source = sourceAndPage.replaceAll(" pg.*", "");

		Elements titles = output.getElementsByClass("title");
		ItemBuilder baseItem = new ItemBuilder();
		baseItem.setLevel(titles.first().getElementsByTag("span").text()
								.replaceAll("[^\\d]", ""));
		makeSpecificItem(titles.first(), baseItem);
		if(titles.size() == 1) {
			return new Pair<>(baseItem.build(), source);
		} else {
			for(int i=1; i < titles.size(); i++) {
				items.append(makeSpecificItem(titles.get(i), baseItem.makeSubItem()).build());
			}
		}
		return new Pair<>(items.toString(), source);
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
				}
			}
			curr = curr.nextSibling();
		}
		if(desc.length() > 0) item.appendToDescription(desc.toString());

		return item;
	}

	private String parseDesc(Node curr) {
		if(curr instanceof TextNode) {
			return ((TextNode) curr).getWholeText().replaceAll("(\r\n|\n)", " ");
		}
		if(curr instanceof Element) {
			Element elem = (Element) curr;
			if(elem.childNodeSize() > 0) {
				StringBuilder text = new StringBuilder();
				if(elem.tagName().equals("b")) text.append("&lt;b&gt;");
				if(elem.tagName().equals("i")) text.append("&lt;i&gt;");
				for (Node childNode : elem.childNodes()) {
					text.append(parseDesc(childNode));
				}
				if(elem.tagName().equals("b")) text.append("&lt;/b&gt;");
				if(elem.tagName().equals("i")) text.append("&lt;/i&gt;");
				return text.toString();
			}
			else if(elem.tagName().equals("img")) {
				if(elem.hasClass("actionDark")) return "";
				return "[[" + elem.attr("alt") + "]]";
			}
			else if(elem.tagName().equals("br")) {
				return "&lt;br&gt;";
			}
		}
		return "";
	}
}
