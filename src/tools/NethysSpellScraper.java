package tools;

import model.util.Pair;
import model.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NethysSpellScraper {
	private final Map<String, StringBuilder> sources = new HashMap<>();

	public static void main(String[] args) {
		new NethysSpellScraper("http://2e.aonprd.com/Spells.aspx?Focus=true&Tradition=0", "generated/focusSpells.txt");
	}

	private NethysSpellScraper(String inputURL, String outputPath) {
		Document doc;
		BufferedWriter out;
		try  {
			doc = Jsoup.connect(inputURL).get();
			out = new BufferedWriter(new FileWriter(new File(outputPath)));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		doc.getElementById("ctl00_MainContent_DetailedOutput").getElementsByTag("a").forEach(element -> {
			String href = "";
			try {
				href = element.attr("href");
				if(href.contains("ID")) {
					Pair<String, String> pair = addSpell(href);
					if (!pair.first.equals(""))
						sources.computeIfAbsent(pair.second.toLowerCase(), key -> new StringBuilder())
								.append(pair.first);
				}
			} catch (Exception e) {
				System.out.println(href);
				e.printStackTrace();
			}
		});
		for (StringBuilder value : sources.values()) {
			try {
				out.write(value.toString());
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
	private Pair<String, String> addSpell(String href) {
		Document doc;
		try  {
			doc = Jsoup.connect("http://2e.aonprd.com/"+href).get();
		} catch (IOException e) {
			e.printStackTrace();
			return new Pair<>("", "");
		}
		Element output = doc.getElementById("ctl00_MainContent_DetailedOutput");

		String spellName = output.getElementsByClass("title").first().ownText();
		String spellLevel = output.getElementsByClass("title").first().getElementsByTag("span").text().replaceAll("[^\\d]*", "");
		String spellType = output.getElementsByClass("title").first().getElementsByTag("span").text().replaceAll("[ \\d]*", "");
		StringBuilder description = new StringBuilder();
		Node afterHr = output.getElementsByTag("hr").first().nextSibling();
		while(afterHr != null) {
			if(afterHr instanceof TextNode)
				description.append(((TextNode) afterHr).getWholeText());
			else if(afterHr instanceof Element) {
				if(((Element) afterHr).tagName().equals("hr")) break;
				description.append(((Element) afterHr).text());
			}
			afterHr = afterHr.nextSibling();
		}
		List<String> traits = new ArrayList<>();
		for (Element trait : output.getElementsByClass("trait")) {
			traits.add(trait.getElementsByTag("a").text());
		}
		String sourceAndPage = output.getElementsMatchingText("\\ASource\\z").first().nextElementSibling().text();
		String source = sourceAndPage.replaceAll(" pg.*", "");
		String pageNo = sourceAndPage.replaceAll(".*pg\\. ", "");

		String area = getAfter(output, "Area");
		String cast = StringUtils.camelCase(getAfter(output, "Cast"));
		if(output.getElementsByAttributeValue("alt", "Free Action").size() > 0)
			cast = "Free (" + cast + ")";
		if(output.getElementsByAttributeValue("alt", "Reaction").size() > 0)
			cast = "Reaction (" + cast + ")";
		String duration = getAfter(output, "Duration");
		String range = getAfter(output, "Range");
		String requirements = getAfter(output, "Requirements");
		String save = getAfter(output, "Save");
		String targets = getAfter(output, "Targets");
		List<String> traditions = new ArrayList<>();
		if(output.getElementsMatchingText("\\ATraditions\\z").size() > 0){
			Element curr = output.getElementsMatchingText("\\ATraditions\\z").first().nextElementSibling();
			while (!curr.tagName().equals("br")) {
				traditions.add(curr.text());
				curr = curr.nextElementSibling();
			}
		}

		StringBuilder results = new StringBuilder();
		results.append("<Spell page=\"").append(pageNo).append("\"");
		if(!spellType.equals("Spell"))
			results.append(" type=\"").append(spellType).append("\"");
		results.append(">\n    <Name>").append(spellName).append("</Name>")
				.append("\n    <Level>").append(spellLevel).append("</Level>")
				.append("\n    <Traits>").append(String.join(", ", traits)).append("</Traits>");
		if(traditions.size() > 0)
			results.append("\n    <Traditions>").append(String.join(", ",traditions)).append("</Traditions>");
		results.append("\n    <Cast>").append(cast.trim().replaceAll(";\\z", "")).append("</Cast>");
		if(!requirements.equals(""))
			results.append("\n    <Requirements>").append(requirements).append("</Requirements>");
		if(!range.equals(""))
			results.append("\n    <Range>").append(range).append("</Range>");
		if(!area.equals(""))
			results.append("\n    <Area>").append(area).append("</Area>");
		if(!targets.equals(""))
			results.append("\n    <Targets>").append(targets).append("</Targets>");
		if(!duration.equals(""))
			results.append("\n    <Duration>").append(duration).append("</Duration>");
		if(!save.equals(""))
			results.append("\n    <Save>").append(save).append("</Save>");
		results.append("\n    <Description>").append(description).append("</Description>");
		if(output.getElementsByTag("hr").size() == 2) {
			Element curr = output.getElementsByTag("hr").last().nextElementSibling();
			while(curr != null) {
				if(!(curr.nextSibling() instanceof TextNode)) break;
				String number = curr.text().replaceAll("[^\\d]*", "");
				if(number.equals("")) number = "1";
				if(curr.text().contains("+")) {
					results.append("\n    <Heightened every=\"").append(number).append("\">")
							.append(((TextNode) curr.nextSibling()).getWholeText()).append("</Heightened>");
				} else {
					results.append("\n    <Heightened level=\"").append(number).append("\">")
							.append(((TextNode) curr.nextSibling()).getWholeText()).append("</Heightened>");
				}
				curr = curr.nextElementSibling();
			}
		}
		results.append("\n</Spell>\n");

		return new Pair<>(results.toString(), source);
	}

	private String getAfter(Element output, String bContents) {
		if(output.getElementsMatchingOwnText("\\A"+bContents+"\\z").size() > 0) {
			Node node = output.getElementsMatchingText("\\A"+bContents+"\\z").first().nextSibling();
			while(node instanceof Element || (node instanceof TextNode && ((TextNode) node).getWholeText().trim().equals(""))) node = node.nextSibling();
			if (node instanceof TextNode)
				return ((TextNode) node).getWholeText().trim().replaceAll(";$", "");
		}
		return "";
	}
}
