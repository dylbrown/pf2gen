package tools.nethys;

import com.gargoylesoftware.htmlunit.WebClient;
import model.util.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

class NethysFeatListScraper extends NethysListScraper {

	public static void main(String[] args) {
		new NethysFeatListScraper(
				"C:\\Users\\dylan\\Downloads\\RadGridExport.csv",
				"feats\\skill.pfdyl",
				source->true, true);
	}

	NethysFeatListScraper(String inputURL, String outputPath, Predicate<String> sourceValidator, boolean multithreaded) {
		super(inputURL, outputPath, "ctl00_RadDrawer1_Content_MainContent_Rad_AllFeats",
				href -> href.contains("Feats.aspx?ID"), sourceValidator, multithreaded);
	}

	NethysFeatListScraper(String inputURL, Consumer<String> output, Predicate<String> sourceValidator) {
		super(inputURL, output, "ctl00_RadDrawer1_Content_MainContent_Rad_AllFeats",
				href -> href.contains("Feats.aspx?ID"), sourceValidator);
	}

	NethysFeatListScraper(String inputURL, Consumer<String> output, Predicate<String> sourceValidator, boolean multithreaded) {
		super(inputURL, output, "ctl00_RadDrawer1_Content_MainContent_Rad_AllFeats",
				href -> href.contains("Feats.aspx?ID"), sourceValidator, multithreaded);
	}

	@Override
	protected void setupItem(String href, Element row, WebClient webClient) throws IOException {
		System.out.println(href);
		FeatEntry entry = addItemStatic(makeDocument("https://2e.aonprd.com/"+href, webClient), sourceValidator);
		if (!entry.entry.isBlank()) {
			String clean = StringUtils.clean(entry.source);
			sources.computeIfAbsent(clean, key ->
							new ConcurrentHashMap<>())
					.computeIfAbsent("", s -> Collections.synchronizedList(new ArrayList<>()))
					.add(entry);
			sourceNames.computeIfAbsent(clean, s -> entry.source);
		}
	}

	@Override
	FeatEntry addItem(Document doc) {
		return addItemStatic(doc, sourceValidator);
	}

	static FeatEntry addItemStatic(Document doc, Predicate<String> sourceValidator) {
		if(doc == null) {
			return FeatEntry.EMPTY;
		}
		Element output = doc.getElementById("ctl00_RadDrawer1_Content_MainContent_DetailedOutput");

		String sourceAndPage = output.getElementsMatchingText("\\ASource\\z").first().nextElementSibling().text();
		String source = sourceAndPage.replaceAll(" pg.*", "");
		String pageNo = sourceAndPage.replaceAll(".*pg\\. ", "");
		if(sourceValidator != null && !sourceValidator.test(StringUtils.clean(source))) {
			return null;
		}

		String featName = output.getElementsByTag("h1").first().text().replaceAll(" Feat.*", "").trim();
		String level = output.getElementsByClass("title").first().getElementsByTag("span").text().replaceAll("[^\\d]*", "");
		StringBuilder descBuilder = new StringBuilder();
		Node afterHr = output.getElementsByTag("hr").first().nextSibling();
		while(afterHr != null) {			if(afterHr instanceof TextNode)
				descBuilder.append(((TextNode) afterHr).getWholeText());
			else if(afterHr instanceof Element) {
				String tagName = ((Element) afterHr).tagName();
				if(tagName.equals("h2")) {
					String text = ((Element) afterHr).text();
					if (text.equals("Traits") || text.equals("Archetype Use") || text.endsWith("Leads To..."))
						break;
				}
				if(tagName.equals("b") || tagName.equals("i") || tagName.equals("br"))
					descBuilder.append("&lt;").append(tagName).append("&gt;");
				descBuilder.append(((Element) afterHr).text());
				if(tagName.equals("b") || tagName.equals("i"))
					descBuilder.append("&lt;/").append(tagName).append("&gt;");
			}
			afterHr = afterHr.nextSibling();
		}
		String description = descBuilder.toString().replaceAll("(&lt;br&gt;)+\\z", "");

		List<String> traits = new ArrayList<>();
		for (Element trait : output.getElementsByClass("trait")) {
			traits.add(trait.getElementsByTag("a").text());
		}

		Elements actionIcons = output.getElementsByClass("action");
		String cost = "";
		if(actionIcons.size() > 0) {
			switch(actionIcons.first().attr("title")) {
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
		String prereqs = StringUtils.capitalize(getAfter(output, "Prerequisites"));
		prereqs = prereqs.replace(";", ",");

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
			feat.append("\t<Template>Basic Spellcasting Benefits</Template>\n");
		}else if(description.startsWith("You gain the expert spellcasting benefits.")){
			feat.append("\t<Template>Expert Spellcasting Benefits</Template>\n");
		}else if(description.startsWith("You gain the master spellcasting benefits.")){
			feat.append("\t<Template>Master Spellcasting Benefits</Template>\n");
		}else if(description.startsWith("You gain one "+archetype.toLowerCase()+" feat. For the purpose of ")) {
			feat.append("\t<Template>Advanced Multiclass Feat</Template>\n");
		}else if(archetype.length() > 0 && featName.endsWith("Breadth")) {
			feat.append("\t<Template>Spellcasting Breadth</Template>\n");
		}

		feat.append("\t<Traits>").append(String.join(", ", traits)).append("</Traits>\n");
		if(!archetype.equals(""))
			feat.append("\t<Archetype>").append(archetype).append("</Archetype>\n");
		if(!prereqs.equals(""))
			feat.append("\t<Prerequisites>").append(prereqs).append("</Prerequisites>\n");
		if(!trigger.equals(""))
			feat.append("\t<Trigger>").append(trigger).append("</Trigger>\n");
		if(!frequency.equals(""))
			feat.append("\t<Frequency>").append(frequency).append("</Frequency>\n");
		if(!requirements.equals(""))
			feat.append("\t<Requirements>").append(requirements).append("</Requirements>\n");
		feat.append("\t<Description>").append(description).append("</Description>\n");
		if(description.equals("You gain a 1st- or 2nd-level "+archetype.toLowerCase()+" feat.")) {
			feat.append("\t<AbilitySlot state=\"feat\" name=\"").append(archetype)
					.append(" Feat\" type=\"Class(").append(archetype)
					.append(") Feat\" level=\"2\"/>\n");
		}
		feat.append("</Ability>\n");
		return new FeatEntry(featName, feat.toString(), source, archetype, level);
	}

	@Override
	protected void printList(Map<String, List<Entry>> map, Consumer<String> out) {
		map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry->
				{
					Comparator<Entry> comparing = Comparator.comparing(e -> {
						if (e instanceof NethysFeatListScraper.FeatEntry) {
							if(!((NethysFeatListScraper.FeatEntry) e).level.isBlank()) {
								return Integer.parseInt(((NethysFeatListScraper.FeatEntry) e).level);
							}
						}
						return 0;
					}); 
					entry.getValue().stream().sorted(comparing.thenComparing(Entry::getEntryName)).forEach(e-> out.accept(e.entry));
				}
		);
	}


	public static class FeatEntry extends Entry {

		public static final FeatEntry EMPTY = new FeatEntry("", "", "", "", "");

		public final String archetype;
		public final String level;

		public FeatEntry(String entryName, String entry, String source, String archetype, String level) {
			super(entryName, entry, source);
			this.archetype = archetype;
			this.level = level;
		}
	}

	@Override
	protected void writePrefix(BufferedWriter out) throws IOException {
		out.write("<?xml version = \"1.0\"?>\n" +
				"<pf2:feats xmlns:pf2=\"https://dylbrown.github.io\"\n" +
				"           xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"           xsi:schemaLocation=\"https://dylbrown.github.io ../../../../schemata/abc/class.xsd\">\n");
	}

	@Override
	protected void writeSuffix(BufferedWriter out) throws IOException {
		out.write("</pf2:feats>");
	}
}
