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
import model.items.BaseItem;
import model.items.Item;
import model.items.ItemInstance;
import model.items.ItemInstanceChoices;
import ui.controllers.DecisionsController;
import ui.controls.Popup;
import ui.html.ItemHTMLGenerator;

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
        super(getChoices(item));
        this.item = new ItemInstance(item);
        this.tryToBuy = tryToBuy;
    }

    private static ObservableList<Choice<?>> getChoices(BaseItem item) {
        ObservableList<Choice<?>> choices = FXCollections.observableArrayList(
                c->new Observable[]{c.numSelectionsProperty()}
        );
        choices.addAll(item.getExtension(ItemInstanceChoices.class).getChoices().values());
        return choices;
    }

    @FXML
    protected void initialize() {
        super.initialize();
        instancePreview.getEngine().loadContent(ItemHTMLGenerator.parse(item));
        decisions.addListener((ListChangeListener<? super Choice<?>>) c->
                instancePreview.getEngine().loadContent(ItemHTMLGenerator.parse(item)));
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
