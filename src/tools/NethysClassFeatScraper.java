package tools;

import model.util.Pair;
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

public class NethysClassFeatScraper {
	private final Map<String, StringBuilder> sources = new HashMap<>();

	public static void main(String[] args) {
		new NethysClassFeatScraper("http://2e.aonprd.com/Feats.aspx?Traits=26", "generated/classFeats.txt");
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
					Pair<String, String> pair = addFeat(href);
					if(!pair.first.equals(""))
						sources.computeIfAbsent(pair.second.toLowerCase(), key->new StringBuilder())
								.append(pair.first);
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
	private Pair<String, String> addFeat(String href) {
		Document doc;
		try  {
			doc = Jsoup.connect("http://2e.aonprd.com/"+href).get();
		} catch (IOException e) {
			e.printStackTrace();
			return new Pair<>("", "");
		}
		Element output = doc.getElementById("ctl00_MainContent_DetailedOutput");

		String featName = output.getElementsByAttributeValue("href", href).first().text();
		String level = output.getElementsByClass("title").first().getElementsByTag("span").text().replaceAll("[^\\d]*", "");
		StringBuilder description = new StringBuilder();
		Node afterHr = output.getElementsByTag("hr").first().nextSibling();
		while(afterHr != null) {
			if(afterHr instanceof TextNode)
				description.append(((TextNode) afterHr).getWholeText());
			else if(afterHr instanceof Element)
				description.append(((Element) afterHr).text());
			afterHr = afterHr.nextSibling();
		}
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
		String requirements = getAfter(output, "Requirements");
		String trigger = getAfter(output, "Trigger");

		StringBuilder feat = new StringBuilder();
		feat.append("<Ability name=\"")
				.append(featName).append("\" level=\"").append(level)
				.append("\" page=\"").append(pageNo);
		if(!cost.equals(""))
			feat.append("\" cost=\"").append(cost);
		feat.append("\">\n\t\t<Traits>").append(String.join(", ", traits)).append("</Traits>\n");
		if(!prereqs.equals(""))
			feat.append("\t\t<Prerequisites>").append(prereqs).append("</Prerequisites>\n");
		if(!trigger.equals(""))
			feat.append("\t\t<Trigger>").append(trigger).append("</Trigger>\n");
		if(!frequency.equals(""))
			feat.append("\t\t<Frequency>").append(frequency).append("</Frequency>\n");
		if(!requirements.equals(""))
			feat.append("\t\t<Requirements>").append(requirements).append("</Requirements>\n");
		feat.append("\t\t<Description>").append(description).append("</Description>\n")
			.append("</Ability>");



		return new Pair<>(feat.toString(), source);
	}

	private String getAfter(Element output, String bContents) {
		if(output.getElementsMatchingOwnText(bContents).size() > 0) {
			Node node = output.getElementsMatchingText("\\A"+bContents+"\\z").first().nextSibling();
			if (node instanceof TextNode)
				return ((TextNode) node).getWholeText().trim();
		}
		return "";
	}
}
