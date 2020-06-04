package tools.nethys;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

abstract class NethysScraper {
	String getAfter(Element output, String bContents) {
		Elements elems = output.getElementsMatchingOwnText("\\A" + bContents + "\\z");
		if(elems.size() > 0) {
			Node node = output.getElementsMatchingText("\\A"+bContents+"\\z").first().nextSibling();
			while(node instanceof Element || (node instanceof TextNode && ((TextNode) node).getWholeText().trim().equals(""))) node = node.nextSibling();
			if (node instanceof TextNode)
				return ((TextNode) node).getWholeText().trim().replaceAll(";$", "").trim();
		}
		return "";
	}

	String getRestOfLine(Element output, String bContents) {
		Elements elems = output.getElementsMatchingOwnText("\\A" + bContents + "\\z");
		if(elems.size() > 0) {
			StringBuilder string = new StringBuilder();
			Node node = elems.first().nextSibling();
			while(true) {
				if(node == null) break;
				if(node instanceof TextNode) string.append(((TextNode) node).getWholeText());
				if(node instanceof Element) {
					if(((Element) node).tag().getName().equals("br") || ((Element) node).tag().getName().matches("h\\d"))
						break;
					string.append(((Element) node).wholeText());
				}
				node = node.nextSibling();
			}
			return string.toString();
		}
		return "";
	}

	String getEntry(String name) {
		return "\t<"+name+">%s</"+name+">\n";
	}
}
