package tools;

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

public class NethysClassTableScraper extends ClassTableParser {
	public static void main(String[] args) {
		new NethysClassTableScraper("http://2e.aonprd.com/Classes.aspx?ID=12", "generated/classTable.txt");
	}

	private Document doc;
	private NethysClassTableScraper(String inputURL, String outputPath) {
		BufferedWriter out;
		try  {
			doc = Jsoup.connect(inputURL).get();
			out = new BufferedWriter(new FileWriter(new File(outputPath)));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		className = doc.selectFirst("#ctl00_MainContent_DetailedOutput .title").ownText().toLowerCase();
		int spellCount = (className.equals("sorcerer")) ? 3 : 2;

		int level = 0;
		for (Element features : doc.selectFirst("#ctl00_MainContent_DetailedOutput table").select("tr td:nth-child(2)")) {
			try {
				out.write(parseTableLine(level, features.text()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			level++;
		}
		try {
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	String getDescription(String feature) {
		StringBuilder desc = new StringBuilder();
		Elements text = doc.getElementsMatchingOwnText(StringUtils.camelCase(feature));
		if(text.size() == 0) return "";
		Node curr = text.first().nextSibling();
		while(curr != null && !(curr instanceof Element && ((Element) curr).tagName().equals("h2"))) {
			if(curr instanceof TextNode)
				desc.append(((TextNode) curr).getWholeText());
			else if(curr instanceof Element)
				desc.append(((Element) curr).text());
			curr = curr.nextSibling();
		}
		return desc.toString();
	}
}
