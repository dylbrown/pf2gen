package tools.nethys;

import model.data_managers.sources.SourceConstructor;
import model.enums.Proficiency;
import model.util.StringUtils;
import model.xml_parsers.FeatsLoader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import tools.ClassTableParser;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

class NethysClassTableScraper extends ClassTableParser {
	public static void main(String[] args) {
		new NethysClassTableScraper("http://2e.aonprd.com/Classes.aspx?ID=13", "generated/classTable.txt");
	}

	private Document doc;
	private Element detailedOutput;
	private final Set<String> templateAbilities;

	private NethysClassTableScraper() {
		super();
		FeatsLoader featsLoader = new FeatsLoader(new SourceConstructor("Core Rulebook/feats/base_class.pfdyl", true), new File("data/"), null);
		templateAbilities = featsLoader.getAll().keySet();
	}
	NethysClassTableScraper(String inputURL, Writer output, int indent) {
		this();
		scrape(inputURL, s->{
			if(!s.isBlank()) {
				String tabs = "\t".repeat(indent);
				try {
					output.write(tabs);
					output.write(s.replaceAll("\n(?!$)", "\n" + tabs));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private NethysClassTableScraper(String inputURL, String outputPath) {
		this();
		BufferedWriter out;
		try  {
			out = new BufferedWriter(new FileWriter(outputPath));

			scrape(inputURL, str -> {
				try {
					out.write(str);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void scrape(String inputURL, Consumer<String> write) {
		try  {
			doc = Jsoup.connect(inputURL).get();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		detailedOutput = doc.getElementById("ctl00_MainContent_DetailedOutput");

		className = doc.selectFirst("#ctl00_MainContent_DetailedOutput .title").ownText().toLowerCase();

		int level = 0;
		for (Element features : doc.selectFirst("#ctl00_MainContent_DetailedOutput table").select("tr td:nth-child(2)")) {
			write.accept(parseTableLine(level, features.text()));
			level++;
		}
	}

	@Override
	protected String getDescription(String feature) {
		Element header = selectMatchingOwnText(detailedOutput, "h2.title", "(?i)" + feature.replace("’", "'"));
		if(header == null)
			return "";
		Node currNode = header.nextSibling();
		StringBuilder description = new StringBuilder();
		while(currNode != null) {
			if(currNode instanceof Element && ((Element) currNode).tagName().equals("h2"))
				break;
			description.append(NethysScraper.parseDesc(currNode));
			currNode = currNode.nextSibling();
		}
		return description.toString().replaceAll("(&lt;br&gt;)+\\z", "");
	}

	@Override
	protected void applyProficiencies(StringBuilder listBuilder) {
		Map<String, List<String>> proficiencies = new HashMap<>();

		Element curr = detailedOutput.getElementsContainingOwnText("Initial Proficiencies").first();
		while (!(curr.tagName().equals("h1") && curr.wholeText().equals("Class Features"))) {
			Node currNode = curr.nextSibling();
			Element next = curr.nextElementSibling();
			while (!next.tagName().equals("h2") && !next.tagName().equals("h1")) {
				next = next.nextElementSibling();
			}
			while(currNode != next) {
				if(currNode instanceof TextNode && !((TextNode) currNode).text().isBlank() &&
						!(((TextNode) currNode).text().startsWith("At 1st level, you gain"))) {
					String[] split = ((TextNode) currNode).text().split(" in ", 2);
					if(split.length == 1)
						System.out.println("Test");
					String value = split[1].trim();
					if(!(value.startsWith("a number of additional skills equal to ") ||
							value.equals("all armor"))) {
						if(value.equals("unarmed attacks"))
							value = "unarmed";
						if(value.equals("unarmored defense"))
							value = "unarmored";
						value = value.replace("spell attack rolls", "spell attacks");

						proficiencies.computeIfAbsent(Proficiency.robustValueOf(split[0]).name(),
								a->new ArrayList<>())
								.add(StringUtils.camelCase(value));
					}
				}
				currNode = currNode.nextSibling();
			}

			curr = next;
		}
		listBuilder.append("\t<AbilitySlot state=\"filled\" name=\"Initial Proficiencies\">\n" +
				"\t\t<Ability>\n");
		for (Map.Entry<String, List<String>> entry : proficiencies.entrySet()) {
			listBuilder.append("\t\t\t<AttributeMods Proficiency=\"")
					.append(entry.getKey()).append("\">")
					.append(String.join(", ", entry.getValue()))
					.append("</AttributeMods>\n");
		}
		listBuilder.append("\t\t</Ability>\n" +
				"\t</AbilitySlot>\n");
	}

	@Override
	protected void handleDefaultCase(StringBuilder listBuilder, String feature) {
		if(templateAbilities.contains(StringUtils.clean(feature))) {
			listBuilder.append("\t<AbilitySlot state=\"filled\" name=\"")
					.append(StringUtils.camelCase(feature))
					.append("\" type=\"ClassFeature\" contents=\"")
					.append(StringUtils.camelCase(feature))
					.append("\" />\n");
		}else{
			super.handleDefaultCase(listBuilder, feature);
		}
	}
}
