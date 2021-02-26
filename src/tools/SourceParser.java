package tools;


import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

abstract class SourceParser {
    protected Element selectMatchingOwnText(Element parent, String query, String regex) {
        Elements selected = parent.select(query);
        for (Element element : selected) {
            if(element.ownText().matches(regex))
                return element;
        }
        return null;
    }
}
