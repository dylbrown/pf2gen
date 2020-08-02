package tools.nethys;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.Iterator;
import java.util.function.Predicate;

abstract class NethysScraper {
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
					if(s.trim().length() == 0 || ((Element) node).tagName().equals("b")) break;
					builder.append(s);
				}
				node = node.nextSibling();
			}
			return builder.toString().trim().replaceAll(";\\z", "");
		}
		return "";
	}

	String getRestOfLine(Element output, String bContents) {
		Elements elems = output.getElementsMatchingOwnText("\\A" + bContents + "\\z");
		if(elems.size() > 0) {
			return getRestOfLineFromNode(elems.first());
		}
		return "";
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
				if(((Element) node).tag().getName().equals("br") || ((Element) node).tag().getName().matches("h\\d"))
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
				if(((Element) node).tag().getName().equals("br") || ((Element) node).tag().getName().matches("h\\d"))
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
			if(elem.childNodeSize() > 0) {
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
