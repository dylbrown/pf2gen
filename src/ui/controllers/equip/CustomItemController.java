package ui.controllers.equip;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import model.ability_slots.Choice;
import model.equipment.BaseItem;
import model.equipment.Item;
import model.equipment.ItemInstance;
import model.equipment.ItemInstanceChoices;
import ui.controllers.DecisionsController;
import ui.controls.Popup;
import ui.html.EquipmentHTMLGenerator;

import java.util.function.Function;

public class CustomItemController extends DecisionsController implements Popup.Controller {
    private Stage stage = null;

    @FXML
    private WebView instancePreview;

    @FXML
    private Button buy;

    private final ItemInstance item;
    private final Function<Item, Boolean> tryToBuy;

    public CustomItemController(BaseItem item, Function<Item, Boolean> tryToBuy) {
        this.item = new ItemInstance(item);
        this.tryToBuy = tryToBuy;
    }

    @SuppressWarnings("rawtypes")
    @FXML
    private void initialize() {
        ObservableList<Choice> choices = FXCollections.observableArrayList(
                c->new Observable[]{c.numSelectionsProperty()}
        );
        choices.addAll(item.getExtension(ItemInstanceChoices.class).getChoices().values());
        initialize(
                choices
        );
        instancePreview.getEngine().loadContent(EquipmentHTMLGenerator.parse(item));
        choices.addListener((ListChangeListener<? super Choice>) c->
                instancePreview.getEngine().loadContent(EquipmentHTMLGenerator.parse(item)));
        buy.setOnAction(e->{
            if(tryToBuy.apply(this.item))
                stage.close();
        });
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
