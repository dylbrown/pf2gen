package ui.controllers.gm;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;
import model.creatures.Creature;
import model.data_managers.sources.SourcesLoader;
import ui.controls.lists.ObservableCategoryEntryList;
import ui.controls.lists.entries.CreatureEntry;
import ui.controls.lists.factories.TreeCellFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EncounterTabController {
    @FXML
    private Spinner<Integer> partyLevel;
    @FXML
    private WebView preview;
    @FXML
    private AnchorPane leftPane, rightPane, budget;

    @FXML
    private void initialize() {
        partyLevel.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        NumberAxis yAxis = new NumberAxis(20, 180, 20);
        yAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number number) {
                switch (number.intValue()) {
                    case 40:
                        return "Triv.";
                    case 60:
                        return "Low";
                    case 80:
                        return "Mod.";
                    case 120:
                        return "Sev.";
                    case 160:
                        return "Ext.";
                    default:
                        return "";
                }
            }

            @Override
            public Number fromString(String s) {
                switch (s) {
                    case "Triv.":
                        return 40;
                    case "Low":
                        return 60;
                    case "Mod.":
                        return 80;
                    case "Sev.":
                        return 120;
                    case "Ext.":
                        return 160;
                    default:
                        return 0;
                }
            }
        });
        ObservableList<String> strings = FXCollections.observableArrayList("Creatures");
        CategoryAxis xAxis = new CategoryAxis(strings);
        xAxis.setTickLabelRotation(270);
        xAxis.setTickLabelFont(Font.font(10));
        yAxis.setTickLabelFont(Font.font(10));

        StackedBarChart<Number, String> chart = new StackedBarChart<>(yAxis, xAxis);
        chart.setLegendVisible(false);
        XYChart.Series<Number, String> series1 = new XYChart.Series<>();
        series1.setName("Gnoll");
        series1.getData().add(new XYChart.Data<>(60, "Creatures"));
        XYChart.Series<Number, String> series2 = new XYChart.Series<>();
        series2.setName("Goblin");
        series2.getData().add(new XYChart.Data<>(40, "Creatures"));
        chart.getData().add(series1);
        chart.getData().add(series2);
        budget.getChildren().add(chart);
        chart.setCategoryGap(0);
        chart.maxWidthProperty().bind(budget.widthProperty());
        chart.maxHeightProperty().bind(budget.heightProperty());

        ObservableCategoryEntryList<Creature, CreatureEntry> allCreatures = new ObservableCategoryEntryList<>(
                FXCollections.observableList(new ArrayList<>(
                        SourcesLoader.instance().creatures().getAll().values())
                ),
                (creature, count) -> {
                },
                creature -> String.valueOf(creature.getLevel()),
                creature -> "",
                CreatureEntry::new,
                CreatureEntry::new,
                this::makeColumns);
        AnchorPane.setTopAnchor(allCreatures, 0.0);
        AnchorPane.setBottomAnchor(allCreatures, 0.0);
        AnchorPane.setLeftAnchor(allCreatures, 0.0);
        AnchorPane.setRightAnchor(allCreatures, 0.0);
        rightPane.getChildren().add(allCreatures);
    }

    private List<TreeTableColumn<CreatureEntry, String>> makeColumns(ReadOnlyDoubleProperty width) {
        TreeTableColumn<CreatureEntry, String> name = new TreeTableColumn<>("Name");
        TreeTableColumn<CreatureEntry, String> level = new TreeTableColumn<>("Level");
        name.setCellValueFactory(new TreeCellFactory<>("name"));
        name.minWidthProperty().bind(width.multiply(.6));
        level.setCellValueFactory(new TreeCellFactory<>("level"));
        level.setStyle( "-fx-alignment: CENTER;");
        level.setComparator((s1,s2)->{
            double d1 = (!s1.equals(""))? Double.parseDouble(s1) : -1;
            double d2 = (!s2.equals(""))? Double.parseDouble(s2) : -1;
            return Double.compare(d1, d2);
        });
        return Arrays.asList(name, level);
    }
}
