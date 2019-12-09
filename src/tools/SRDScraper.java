package tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

abstract class SRDScraper {
	private final Map<String, StringBuilder> sources = new HashMap<>();
	SRDScraper(String inputURL, String outputPath, int sourceColumn) {
		Document doc;
		BufferedWriter out;
		try  {
			doc = Jsoup.connect(inputURL).get();
			out = new BufferedWriter(new FileWriter(new File(outputPath)));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		doc.getElementById("archive-data-table").getElementsByTag("tbody").first().getElementsByTag("tr").forEach(element -> {
			String source, href = "", output;
			try {
				source = element.child(sourceColumn).text();
				href = element.child(0).child(0).attr("href");
				output = addItem(href, source);
				if(!output.equals(""))
					sources.computeIfAbsent(source.toLowerCase(), key->new StringBuilder())
							.append(output);
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
	abstract String addItem(String href, String source);
}
