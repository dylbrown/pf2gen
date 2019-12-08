package ui.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

public class AbilityEntry extends HBox {
	private static DoubleProperty maxWidth = new SimpleDoubleProperty(0);
	public AbilityEntry(Node... children) {
		super(children);
		this.setPadding(new Insets(10));
		this.setSpacing(5);
		this.setAlignment(Pos.CENTER);
		if(this.getWidth() > maxWidth.get())
			maxWidth.set(this.getWidth());
		this.minWidthProperty().bind(maxWidth);
		this.widthProperty().addListener(change->{
			if(this.getWidth() > maxWidth.get())
				maxWidth.set(this.getWidth());
		});
	}
}
