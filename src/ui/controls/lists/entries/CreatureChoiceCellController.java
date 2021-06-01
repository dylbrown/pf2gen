package ui.controls.lists.entries;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import model.creatures.CustomCreatureValue;
import model.creatures.scaling.Scale;
import model.creatures.scaling.ScaleMap;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class CreatureChoiceCellController<T> {
    private final ObjectProperty<Integer> spinnerValueProperty;
    private final IntegerProperty level;
    private final ChangeListener<Number> onItemChange;
    private ScaleMap scaleMap = null;
    @FXML
    private AnchorPane cell;
    @FXML
    private Label label;
    @FXML
    private Slider slider;
    @FXML
    private Spinner<Integer> spinner;
    private CustomCreatureValue<T> item;
    Semaphore changingValue = new Semaphore(1);

    public CreatureChoiceCellController(IntegerProperty level)
    {
        this.level = level;
        this.onItemChange = this::onItemChange;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/gm/attributeChoiceCell.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setSnapToTicks(true);
        slider.setMinorTickCount(0);
        slider.setMajorTickUnit(1);

        SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
        spinner.setValueFactory(factory);
        spinnerValueProperty = factory.valueProperty();

        factory.valueProperty().addListener((o, oldVal, newVal) -> {
            if(changingValue.tryAcquire()) {
                slider.setValue(newVal);
                if(item != null)
                    item.modifier.set(newVal);
                changingValue.release();
            }
        });
        slider.valueProperty().addListener((o, oldVal, newVal) -> {
            if(changingValue.tryAcquire()) {
                factory.setValue(newVal.intValue());
                if(item != null)
                    item.modifier.set(newVal.intValue());
                changingValue.release();
            }
        });
        level.addListener((o, oldVal, newVal) -> {
            if(changingValue.tryAcquire()) {
                updateSlider(scaleMap, newVal.intValue());
                changingValue.release();
            }
        });
        slider.setLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Double aDouble) {
                int val = (int) Math.round(aDouble);
                for (Scale scale : scaleMap.getMap().keySet()) {
                    if(scaleMap.get(scale, level.get()) == val)
                        return scale.toString().substring(0, 2);
                }
                return Integer.toString(val);
            }
            @Override
            public Double fromString(String s) {
                return null;
            }
        });
    }

    private void onItemChange(ObservableValue<? extends Number> o, Number oldVal, Number newVal) {
        if(changingValue.tryAcquire()) {
            slider.setValue(newVal.intValue());
            spinnerValueProperty.setValue(newVal.intValue());
            changingValue.release();
        }
    }

    private void updateSlider(ScaleMap scaleMap, int level) {
        if(scaleMap == null)
            return;
        slider.setMin(scaleMap.getMin(level));
        slider.setMax(scaleMap.get(Scale.Extreme, level));
        slider.setMajorTickUnit(.5);
        slider.setMajorTickUnit(1);
        slider.setValue(spinnerValueProperty.getValue());
    }

    public Node getCell() {
        return cell;
    }

    public void setItem(CustomCreatureValue<T> item) {
        if(item != this.item) {
            if(!changingValue.tryAcquire())
                throw new RuntimeException("Failed to acquire change when setting item");
            if(item != null)
                scaleMap = ScaleMap.get(item.getTargetString());
            updateSlider(scaleMap, level.get());
            if(this.item != null)
                this.item.modifier.removeListener(onItemChange);
            this.item = item;
            if(item != null) {
                spinnerValueProperty.setValue(item.getModifier());
                label.setText(item.getTargetString());
                this.item.modifier.addListener(onItemChange);
            }
            changingValue.release();
        }
    }
}
