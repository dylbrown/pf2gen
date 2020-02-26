package tools.nethys;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

abstract class NethysScraper {
	String getAfter(Element output, String bContents) {
		if(output.getElementsMatchingOwnText("\\A"+bContents+"\\z").size() > 0) {
			Node node = output.getElementsMatchingText("\\A"+bContents+"\\z").first().nextSibling();
			while(node instanceof Element || (node instanceof TextNode && ((TextNode) node).getWholeText().trim().equals(""))) node = node.nextSibling();
			if (node instanceof TextNode)
				return ((TextNode) node).getWholeText().trim().replaceAll(";$", "");
		}
		return "";
	}
}
