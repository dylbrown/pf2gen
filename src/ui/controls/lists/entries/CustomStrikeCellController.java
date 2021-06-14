package ui.controls.lists.entries;

import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import model.creatures.CustomStrike;

import java.io.IOException;
import java.util.function.Consumer;

public class CustomStrikeCellController {

    @FXML
    private AnchorPane cell;
    @FXML
    private VBox attackDamageContainer;
    @FXML
    private TextField strikeName;
    @FXML
    private Label damage;
    @FXML
    private Button delete, editDamage;

    private final CreatureChoiceCellController<String> attackBonus;
    private CustomStrike item;

    public CustomStrikeCellController(IntegerProperty level, Consumer<CustomStrike> delete)
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/gm/customStrikeCell.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        attackBonus = new CreatureChoiceCellController<>(level);
        attackDamageContainer.getChildren().add(0, attackBonus.getCell());

        this.delete.setOnAction(e->{
            if(item != null) {
                delete.accept(item);
            }
        });
    }

    public void setItem(CustomStrike item) {
        if(item != this.item) {
            attackBonus.setItem((item != null) ? item.modifier : null);
            if(this.item != null)
                this.item.name.unbind();
            this.item = item;
            if(item != null) {
                strikeName.textProperty().bindBidirectional(item.name);
            }
        }
    }

    public Node getCell() {
        return cell;
    }
}
