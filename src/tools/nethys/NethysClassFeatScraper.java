package tools.nethys;

import model.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

class NethysClassFeatScraper extends NethysScraper {
	private final Map<String, StringBuilder> sources = new HashMap<>();

	public static void main(String[] args) {
		new NethysClassFeatScraper("http://2e.aonprd.com/Feats.aspx?Traits=166", "generated/classFeats.txt");
	}

	private NethysClassFeatScraper(String inputURL, String outputPath) {
		Document doc;
		BufferedWriter out;
		try  {
			doc = Jsoup.connect(inputURL).get();
			out = new BufferedWriter(new FileWriter(new File(outputPath)));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		AtomicBoolean firstRow = new AtomicBoolean(true);
		doc.getElementById("ctl00_MainContent_TableElement").getElementsByTag("tbody").first().getElementsByTag("tr").forEach(element -> {
			if(!firstRow.get()) {
				String href = "";
				try {
					href = element.child(0).child(0).attr("href");
					FeatEntry entry = addFeat(href);
					if(!entry.contents.equals(""))
						sources.computeIfAbsent(entry.source.toLowerCase(), key->new StringBuilder())
								.append(entry.contents);
				} catch (Exception e) {
					System.out.println(href);
					e.printStackTrace();
				}
			} else firstRow.set(false);
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
	public static class FeatEntry {
		public final String archetype, source, contents;

		public FeatEntry(String archetype, String source, String contents) {
			this.archetype = archetype;
			this.source = source;
			this.contents = contents;
		}
	}
	public static FeatEntry addFeat(String href) {
		Document doc;
		try  {
			doc = Jsoup.connect("http://2e.aonprd.com/"+href).get();
		} catch (IOException e) {
			e.printStackTrace();
			return new FeatEntry("", "", "");
		}
		Element output = doc.getElementById("ctl00_MainContent_DetailedOutput");

		String featName = output.getElementsByAttributeValue("href", href).first().text();
		String level = output.getElementsByClass("title").first().getElementsByTag("span").text().replaceAll("[^\\d]*", "");
		StringBuilder descBuilder = new StringBuilder();
		Node afterHr = output.getElementsByTag("hr").first().nextSibling();
		while(afterHr != null) {
			if(afterHr instanceof TextNode)
				descBuilder.append(((TextNode) afterHr).getWholeText());
			else if(afterHr instanceof Element) {
				String tagName = ((Element) afterHr).tagName();
				if(tagName.equals("b") || tagName.equals("i") || tagName.equals("br"))
					descBuilder.append("&lt;").append(tagName).append("&gt;");
				descBuilder.append(((Element) afterHr).text());
				if(tagName.equals("b") || tagName.equals("i"))
					descBuilder.append("&lt;/").append(tagName).append("&gt;");
			}
			afterHr = afterHr.nextSibling();
		}
		String description = descBuilder.toString();

		List<String> traits = new ArrayList<>();
		for (Element trait : output.getElementsByClass("trait")) {
			traits.add(trait.getElementsByTag("a").text());
		}
		String sourceAndPage = output.getElementsMatchingText("\\ASource\\z").first().nextElementSibling().text();
		String source = sourceAndPage.replaceAll(" pg.*", "");
		String pageNo = sourceAndPage.replaceAll(".*pg\\. ", "");

		Elements actionIcons = output.getElementsByAttributeValueContaining("src", "Images\\Actions");
		String cost = "";
		if(actionIcons.size() > 0) {
			switch(actionIcons.first().attr("alt")) {
				case "Single Action":
					cost = "1";
					break;
				case "Two Actions":
					cost = "2";
					break;
				case "Three Actions":
					cost = "3";
					break;
				case "Free Action":
					cost = "Free";
					break;
				case "Reaction":
					cost = "Reaction";
					break;
			}
		}
		String prereqs = StringUtils.camelCase(getAfter(output, "Prerequisites"));
		String frequency = getAfter(output, "Frequency");
		String archetype = getAfter(output, "Archetype");
		String requirements = getAfter(output, "Requirements");
		String trigger = getAfter(output, "Trigger");

		StringBuilder feat = new StringBuilder();
		feat.append("<Ability name=\"")
				.append(featName).append("\" level=\"").append(level)
				.append("\" page=\"").append(pageNo);
		if(!cost.equals(""))
			feat.append("\" cost=\"").append(cost);
		feat.append("\">\n");

		// Template Detection
		if(description.startsWith("You gain the basic spellcasting benefits.")) {
			feat.append("\t\t<Template>Basic Spellcasting Benefit</Template>\n");
		}else if(description.startsWith("You gain the expert spellcasting benefits.")){
			feat.append("\t\t<Template>Expert Spellcasting Benefits</Template>\n");
		}else if(description.startsWith("You gain the master spellcasting benefits.")){
			feat.append("\t\t<Template>Master Spellcasting Benefits</Template>\n");
		}else if(description.startsWith("You gain one "+archetype.toLowerCase()+" feat. For the purpose of ")) {
			feat.append("\t\t<Template>Advanced Multiclass Feat</Template>\n");
		}else if(archetype.length() > 0 && featName.endsWith("Breadth")) {
			feat.append("\t\t<Template>Spellcasting Breadth</Template>\n");
		}

		feat.append("\t\t<Traits>").append(String.join(", ", traits)).append("</Traits>\n");
		if(!archetype.equals(""))
			feat.append("\t\t<Archetype>").append(archetype).append("</Archetype>\n");
		if(!prereqs.equals(""))
			feat.append("\t\t<Prerequisites>").append(prereqs).append("</Prerequisites>\n");
		if(!trigger.equals(""))
			feat.append("\t\t<Trigger>").append(trigger).append("</Trigger>\n");
		if(!frequency.equals(""))
			feat.append("\t\t<Frequency>").append(frequency).append("</Frequency>\n");
		if(!requirements.equals(""))
			feat.append("\t\t<Requirements>").append(requirements).append("</Requirements>\n");
		feat.append("\t\t<Description>").append(description).append("</Description>\n");
		if(description.equals("You gain a 1st- or 2nd-level "+archetype.toLowerCase()+" feat.")) {
			feat.append("\t\t<AbilitySlot state=\"feat\" name=\"").append(archetype)
					.append(" Feat\" type=\"Class(").append(archetype)
					.append(") Feat\" level=\"2\"/>\n");
		}
		feat.append("</Ability>\n");



		return new FeatEntry(archetype, source, feat.toString());
	}
}
