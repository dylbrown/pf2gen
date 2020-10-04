package tools.nethys;

import model.util.Pair;
import model.util.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

class NethysSpellScraper extends NethysListScraper {
	private final Map<String, StringBuilder> sources = new HashMap<>();

	public static void main(String[] args) {
		new NethysSpellScraper("http://2e.aonprd.com/Spells.aspx?Focus=true&Tradition=0", "generated/focusSpells.txt", source->true);
	}

	private NethysSpellScraper(String inputURL, String outputPath, Predicate<String> sourceValidator) {
		super(inputURL, outputPath, "ctl00_MainContent_DetailedOutput", href -> href.contains("ID"), sourceValidator);
	}

	@Override
	Entry addItem(Document doc) {
		Element output = doc.getElementById("ctl00_MainContent_DetailedOutput");

		String spellName = output.getElementsByClass("title").first().ownText();
		String spellLevel = output.getElementsByClass("title").first().getElementsByTag("span").text().replaceAll("[^\\d]*", "");
		String spellType = output.getElementsByClass("title").first().getElementsByTag("span").text().replaceAll("[ \\d]*", "");
		StringBuilder description = new StringBuilder();
		Node afterHr = output.getElementsByTag("hr").first().nextSibling();
		while(afterHr != null) {
			description.append(parseDesc(afterHr));
			if(afterHr instanceof Element && ((Element) afterHr).tagName().equals("hr"))
				break;
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
		Elements text = output.getElementsMatchingOwnText("\\ACast\\z");
		if(text.size() == 1) {
			Element currElem = text.first().nextElementSibling();
			while (currElem != null &&
					!currElem.tagName().equals("img") &&
					!currElem.tagName().equals("b"))
				currElem = currElem.nextElementSibling();
			if(currElem != null && currElem.tagName().equals("img")) {
				cast = currElem.attr("alt") + " (" + cast + ")";
			}
		}
		String duration = getAfter(output, "Duration");
		String range = getAfter(output, "Range");
		String requirements = getAfter(output, "Requirements");
		String save = getAfter(output, "Saving Throw");
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

		return new Entry(spellName, results.toString(), source);
	}
}
