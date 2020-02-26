package tools.nethys.builders;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

	private String name, sourcePage, level, price, description, hands, bulk, usage;
	private List<String> traits = new ArrayList<>();

	public ItemBuilder() {}

	private ItemBuilder(ItemBuilder itemBuilder) {
		this.name = itemBuilder.name;
		this.sourcePage = itemBuilder.sourcePage;
		this.level = itemBuilder.level;
		this.price = itemBuilder.price;
		this.description = itemBuilder.description;
		this.hands = itemBuilder.hands;
		this.bulk = itemBuilder.bulk;
		this.usage = itemBuilder.usage;
		this.traits = itemBuilder.traits;
	}

	public ItemBuilder makeSubItem() {
		return new ItemBuilder(this);
	}

	public void setSourcePage(String sourcePage) {
		this.sourcePage = sourcePage;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public void setHands(String hands) {
		this.hands = hands;
	}

	public void setBulk(String bulk) {
		this.bulk = bulk;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void appendToDescription(String desc) {
		if(this.description == null) this.description = desc;
		else this.description += "\n\n" + desc;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public void addTrait(String trait) {
		this.traits.add(trait);
	}

	/*
	*   <Name>Light mace</Name>
		<Price>4 sp</Price>
		<Damage>1d4 B</Damage>
		<Bulk>L</Bulk>
		<Hands>1</Hands>
		<Group>Club</Group>
		<Traits>Agile, finesse, shove
		</Traits>
	* */
	public String build() {
		StringBuilder item = new StringBuilder();
		item.append("<Item level=\"").append(level);
		item.append("\" page=\"").append(sourcePage).append("\">\n");
		item.append("\t<Name>").append(name).append("</Name>\n");
		if(price != null)
			item.append("\t<Price>").append(price).append("</Price>\n");
		if(bulk != null)
			item.append("\t<Bulk>").append(bulk).append("</Bulk>\n");
		if(hands != null)
			item.append("\t<Hands>").append(hands).append("</Hands>\n");
		if(traits.size() > 0)
			item.append("\t<Traits>").append(String.join(", ", traits)).append("</Traits>\n");
		if(usage != null)
			item.append("\t<Usage>").append(usage).append("</Usage>\n");
		if(description != null)
			item.append("\t<Description>")
					.append(description)
					.append("</Description>\n");

		item.append("</Item>\n");
		return item.toString();
	}
}
