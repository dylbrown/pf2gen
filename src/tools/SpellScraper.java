package tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class SpellScraper extends SRDScraper {

	public static void main(String[] args) {
		new SpellScraper();
	}
	private SpellScraper() {
		super("http://pf2.d20pfsrd.com/spell", "generated/spells.pfdyl", 4);
	}

	@Override
	String addItem(String href, String source) {
		Document doc;
		try {
			doc = Jsoup.connect(href).get();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		Element article = doc.getElementsByTag("article").first();

		String spellName = article.getElementsByTag("h1").first().text();
		String spellLevel = article.getElementsByClass("spell-level").first().text();
		if(article.getElementsByClass("spell-type").first().text().equals("Cantrip"))
			spellLevel = "0";
		List<String> traits = new ArrayList<>();
		for (Element trait : article.getElementsByClass("trait")) {
			traits.add(trait.text());
		}
		String traditions = article.getElementsByClass("traditions").first().text();
		String cast = doBSearch(article, "Cast").replaceAll("\\[[^]]*] ?", "");
		String requirements = doBSearch(article, "Requirements");
		String range = "";
		Elements elems = article.getElementsByClass("spell-range");
		if(elems.size() > 0) range = elems.first().text();

		String area = "";
		elems = article.getElementsByClass("spell-area");
		if(elems.size() > 0) area = elems.first().text();

		String targets = "";
		elems = article.getElementsByClass("spell-targets");
		if(elems.size() > 0) targets = elems.first().text();

		String duration = doBSearch(article, "Duration");

		String save = doBSearch(article, "Saving Throw");

		List<StringBuilder> descriptions = new ArrayList<>();
		List<String> heightenedNames = new ArrayList<>();
		heightenedNames.add("");
		descriptions.add(new StringBuilder());

		elems = article.getElementsByTag("hr").first().siblingElements();
		int i = article.getElementsByTag("hr").first().elementSiblingIndex();
		while (i < elems.size()-1) {
			for (Element b : elems.get(i).getElementsByTag("b")) {
				if(b.text().contains("Heightened")){
					descriptions.add(new StringBuilder());
					heightenedNames.add(b.text());
					break;
				}
			}
			descriptions.get(descriptions.size()-1).append(elems.get(i).text()).append("\n\n");
			i++;
		}
		StringBuilder results = new StringBuilder();
		results.append("<Spell>")
				.append("\n    <Name>").append(spellName).append("</Name>")
				.append("\n    <Level>").append(spellLevel).append("</Level>")
				.append("\n    <Traits>").append(String.join(", ", traits)).append("</Traits>")
				.append("\n    <Traditions>").append(String.join(", ",traditions)).append("</Traditions>")
				.append("\n    <Cast>").append(cast).append("</Cast>");
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
		results.append("\n    <Description>").append(descriptions.get(0).toString().trim()).append("</Description>");
		for(int j=1; j<descriptions.size(); j++) {
			String number = heightenedNames.get(j).replaceAll("[^\\d]*", "");
			if(number.equals("")) number = "1";
			if(heightenedNames.get(j).contains("+")) {
				results.append("\n    <Heightened every=\"").append(number).append("\">")
						.append(descriptions.get(j).toString().trim()).append("</Heightened>");
			} else {
				results.append("\n    <Heightened level=\"").append(number).append("\">")
						.append(descriptions.get(j).toString().trim()).append("</Heightened>");
			}
		}
		results.append("\n</Spell>\n");
		return results.toString();
	}

	private String doBSearch(Element article, String searchFor) {
		for (Element b : article.getElementsByTag("b")) {
			if(b.text().equals(searchFor)) {
				for (String section : b.parent().text().split("; ?")) {
					if(section.contains(searchFor)) {
						return section.replaceAll(searchFor+" ?", "");
					}
				}
				break;
			}
		}
		return "";
	}
}
