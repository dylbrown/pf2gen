package tools.nethys;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import model.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

abstract class NethysScraper {

	public static class Entry {

		public static final Entry EMPTY = new Entry("", "", "");

		public final String entryName;
		public final String entry;
		public final String source;

		public Entry(String entryName, String entry, String source) {
			this.entryName = entryName;
			this.entry = entry;
			this.source = source;
		}

		public String getEntryName() {
			return entryName;
		}
	}

	static WebClient webClient = null;
	static synchronized Document makeDocumentStatic(String url) {
		if(webClient == null) {
			webClient = ProxyPool.makeClient();
		}
		return makeDocument(url, webClient);
	}

	static Document makeDocument(String url, WebClient webClient) {
		if(webClient == null) {
			return null;
		}
		try {
			WebResponse response = webClient.getPage(new URL(url)).getWebResponse();
			return Jsoup.parse(response.getContentAsStream(), response.getContentCharset().name(), url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	static String getAfter(Element output, String bContents) {
		Elements elems = output.getElementsMatchingOwnText("\\A" + bContents + "\\z");
		if(elems.size() > 0) {
			Elements matchingText = output.getElementsMatchingText("\\A" + bContents + "\\z");
			Iterator<Element> iterator = matchingText.iterator();
			Element result = matchingText.first();
			do {
				if(!iterator.hasNext()) break;
				result = iterator.next();
			} while (!result.tagName().equals("b"));
			Node node = result.nextSibling();
			while((node instanceof Element && ((Element) node).wholeText().trim().equals("")) ||
					(node instanceof TextNode && ((TextNode) node).getWholeText().trim().equals("")))
				node = node.nextSibling();
			StringBuilder builder = new StringBuilder();
			while(true) {
				if(node == null) break;
				if (node instanceof TextNode)
					builder.append(((TextNode) node).getWholeText().replaceAll(";$", ""));
				if (node instanceof Element) {
					String s = ((Element) node).wholeText().replaceAll(";$", "");
					if(((Element) node).tagName().equals("b") ||
							((Element) node).tagName().equals("hr") ||
							((Element) node).tagName().equals("br")) break;
					builder.append(s);
				}
				node = node.nextSibling();
			}
			return builder.toString().trim().replaceAll(";\\z", "");
		}
		return "";
	}

	String getHeaderContents(Element output, String headerTitle) {
		Element header = output.getElementsMatchingOwnText(headerTitle).select(":not(#main)").first();
		StringBuilder contents = new StringBuilder();
		if(header != null) {
			Node curr = header.nextSibling();
			while(curr != null && !(curr instanceof Element && ((Element) curr).tagName().equals("h2"))) {
				String text = "";
				if(curr instanceof TextNode) {
					text = ((TextNode) curr).text();
				}
				if(curr instanceof Element) {
					text = ((Element) curr).text();
					if(((Element) curr).tagName().equals("br"))
						contents.append("\n");
				}
				contents.append(text);
				curr = curr.nextSibling();
			}
		}
		return contents.toString();
	}

	List<String> getHeaderList(Element output, String headerTitle) {
		Element header = output.getElementsMatchingOwnText(headerTitle).first();
		if(header != null) {
			List<String> results = new ArrayList<>();
			Node curr = header.nextSibling();
			while(curr != null && !(curr instanceof Element && ((Element) curr).tagName().equals("h2"))) {
				String text = "";
				if(curr instanceof TextNode) {
					text = ((TextNode) curr).text();
				}
				if(curr instanceof Element) {
					text = ((Element) curr).text();
				}
				if(!text.isBlank())
					results.add(text);
				curr = curr.nextSibling();
			}
			return results;
		}
		return Collections.emptyList();
	}

	String getRestOfLine(Element output, String bContents) {
		Elements elems = output.getElementsMatchingOwnText("\\A" + bContents + "\\z");
		if(elems.size() > 0) {
			return getRestOfLineFromNode(elems.first());
		}
		return "";
	}

	static Pair<String, String> getSourcePage(Element output) {
		String sourcePage = getAfter(output, "Source");
		int end = sourcePage.indexOf("pg. ");
		int endPage = sourcePage.indexOf(' ', end + 4);
		if(endPage == -1)
			endPage = sourcePage.length();
		String source = sourcePage.substring(0, end-1);
		String page = sourcePage.substring(end+4, endPage);
		return new Pair<>(source, page);
	}

	protected String getRestOfLineNoTags(Element output, String bContents) {
		Elements elems = output.getElementsMatchingOwnText("\\A" + bContents + "\\z");
		if(elems.size() > 0) {
			return getRestOfLineFromNodeNoTags(elems.first());
		}
		return "";
	}

	String getRestOfLineFromNode(Node node) {
		StringBuilder string = new StringBuilder();
		while(true) {
			if(node == null) break;
			if(node instanceof TextNode) string.append(((TextNode) node).getWholeText());
			if(node instanceof Element) {
				if(((Element) node).tagName().equals("br") ||
						((Element) node).tagName().matches("h\\d") ||
						((Element) node).tagName().equals("hr"))
					break;
				string.append(node.outerHtml());
			}
			node = node.nextSibling();
		}
		return string.toString();
	}

	String getRestOfLineFromNodeNoTags(Node node) {
		StringBuilder string = new StringBuilder();
		while(true) {
			if(node == null) break;
			if(node instanceof TextNode) string.append(((TextNode) node).getWholeText());
			if(node instanceof Element) {
				if(((Element) node).tagName().equals("br") ||
						((Element) node).tagName().matches("h\\d") ||
						((Element) node).tagName().equals("hr"))
					break;
				string.append(((Element) node).text());
			}
			node = node.nextSibling();
		}
		return string.toString();
	}

	String getEntry(String name) {
		return "\t<"+name+">%s</"+name+">\n";
	}

	static String parseDesc(Node curr) {
		if(curr instanceof TextNode) {
			return ((TextNode) curr).getWholeText().replaceAll("(\r\n|\n)", " ");
		}
		if(curr instanceof Element) {
			Element elem = (Element) curr;
			if(elem.hasClass("title")) {
				return "&lt;" + elem.tagName() + "&gt;" + elem.wholeText().trim() + "&lt;/" + elem.tagName() + "&gt;";
			} else if(elem.childNodeSize() > 0) {
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

	protected String getUntil(Node start, Predicate<Node> isEnd) {
		Node curr = start;
		StringBuilder builder = new StringBuilder();
		while(!isEnd.test(curr)) {
			builder.append(parseDesc(curr));
			curr = curr.nextSibling();
		}
		return builder.toString();
	}
}
