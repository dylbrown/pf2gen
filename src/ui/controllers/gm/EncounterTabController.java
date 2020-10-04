package ui.controllers.gm;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import model.creatures.Creature;
import model.data_managers.sources.SourcesLoader;
import ui.controls.lists.ObservableCategoryEntryList;
import ui.controls.lists.entries.CreatureCount;
import ui.controls.lists.entries.CreatureCountEntry;
import ui.controls.lists.entries.CreatureEntry;
import ui.controls.lists.factories.TreeCellFactory;
import ui.ftl.CreaturesTemplateFiller;
import ui.html.CreatureHTMLGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class EncounterTabController {
    @FXML
    private Spinner<Integer> partyLevel;
    @FXML
    private WebView preview;
    @FXML
    private Button exportAll;
    @FXML
    private AnchorPane leftPane, rightPane, budget;
    private final ObservableList<CreatureCount> selected = FXCollections.observableArrayList();
    private final Map<Creature, CreatureCount> selectedMap = FXCollections.observableHashMap();
    private final Map<Creature, XYChart.Series<Number, String>> seriesMap = FXCollections.observableHashMap();
    private StackedBarChart<Number, String> chart;
    private NumberAxis xAxis;

    @FXML
    private void initialize() {
        exportAll.setOnAction(e -> export("creature_index_card.html.ftl", selected));
        preview.getEngine().setUserStyleSheetLocation(getClass().getResource("/webview_style.css").toString());
        partyLevel.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, 20, 1));
        xAxis = new NumberAxis(0, 180, 20);
        xAxis.setTickLabelFormatter(new StringConverter<>() {
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
        CategoryAxis yAxis = new CategoryAxis(strings);
        yAxis.setTickLabelRotation(270);
        yAxis.setTickLabelFont(Font.font(10));
        xAxis.setTickLabelFont(Font.font(10));
        yAxis.setAutoRanging(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setTickLabelsVisible(false);
        xAxis.setAutoRanging(false);

        chart = new StackedBarChart<>(xAxis, yAxis);
        chart.setData(FXCollections.observableArrayList());
        chart.setLegendVisible(false);
        budget.getChildren().add(chart);
        chart.setCategoryGap(0);
        chart.minWidth(0);
        chart.minHeight(0);
        chart.maxWidthProperty().bind(budget.widthProperty());
        chart.maxHeightProperty().bind(budget.heightProperty());

        ObservableCategoryEntryList<Creature, CreatureEntry> allCreatures = new ObservableCategoryEntryList<>(
                FXCollections.observableList(new ArrayList<>(
                        SourcesLoader.ALL_SOURCES.creatures().getAll().values())
                ),
                (creature, count) -> {
                    if (count % 2 == 0) {
                        addToEncounter(creature);
                    }
                },
                creature -> String.valueOf(creature.getLevel()),
                creature -> "",
                CreatureEntry::new,
                CreatureEntry::new,
                this::makeColumns
        );
        allCreatures.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal)->{
            if(newVal != null && newVal.getValue() != null && newVal.getValue().getContents() != null)
                preview.getEngine().loadContent(CreatureHTMLGenerator.parse(newVal.getValue().getContents()));
        });
        AnchorPane.setTopAnchor(allCreatures, 0.0);
        AnchorPane.setBottomAnchor(allCreatures, 0.0);
        AnchorPane.setLeftAnchor(allCreatures, 0.0);
        AnchorPane.setRightAnchor(allCreatures, 0.0);
        rightPane.getChildren().add(allCreatures);

        ObservableCategoryEntryList<CreatureCount, CreatureCountEntry> selectedCreatures = new ObservableCategoryEntryList<>(selected,
                (creature, count) -> {
                    preview.getEngine().loadContent(CreatureHTMLGenerator.parse(creature.getCreature()));
                    if(count % 2 == 0) {
                        removeFromEncounter(creature.getCreature());
                    }
                },
                creature -> String.valueOf(creature.getCreature().getLevel()),
                creature -> "",
                CreatureCountEntry::new,
                CreatureCountEntry::new,
                this::makeCountColumns
        );
        AnchorPane.setTopAnchor(selectedCreatures, 0.0);
        AnchorPane.setBottomAnchor(selectedCreatures, 0.0);
        AnchorPane.setLeftAnchor(selectedCreatures, 0.0);
        AnchorPane.setRightAnchor(selectedCreatures, 0.0);
        leftPane.getChildren().add(selectedCreatures);
        selectedCreatures.setPlaceholder(new Label("No Creatures Added."));

        partyLevel.valueProperty().addListener(c->{
            for (Map.Entry<Creature, XYChart.Series<Number, String>> entry : seriesMap.entrySet()) {
                for (XYChart.Data<Number, String> datum : entry.getValue().getData()) {
                    datum.setXValue(getXP(entry.getKey()) * selectedMap.get(entry.getKey()).getCount());
                }
            }
        });
    }

    private File exportLocation = null;
    private void export(String template, ObservableList<CreatureCount> selected) {
        FileChooser fileChooser = new FileChooser();
        if(exportLocation != null) {
            fileChooser.setInitialDirectory(exportLocation.getParentFile());
            fileChooser.setInitialFileName(exportLocation.getName());
        } else {
            fileChooser.setInitialDirectory(new File("./"));
        }
        //Set extension filter for text files
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("HTML files", "*.html")
        );

        //Show save file dialog
        File file = fileChooser.showSaveDialog(chart.getScene().getWindow());

        if (file != null) {
            try {
                List<Creature> creatures = selected.stream().map(CreatureCount::getCreature).collect(Collectors.toList());
                PrintWriter out = new PrintWriter(file);
                out.println(new CreaturesTemplateFiller(creatures).getSheet(template));
                out.close();
                exportLocation = file;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void addToEncounter(Creature creature) {
        XYChart.Data<Number, String> datum;
        CreatureCount creatureCount = selectedMap.get(creature);
        if(creatureCount != null) {
            creatureCount.add(1);
            datum = seriesMap.get(creature).getData().get(0);
        } else {
            creatureCount = new CreatureCount(creature, 1);
            selected.add(creatureCount);
            selectedMap.put(creature, creatureCount);

            XYChart.Series<Number, String> newSeries = new XYChart.Series<>();
            newSeries.setName(creature.getName());
            seriesMap.put(creature, newSeries);
            chart.getData().add(newSeries);
            datum = new XYChart.Data<>(getXP(creature), "Creatures");
            newSeries.getData().add(datum);
        }
        datum.setXValue(getXP(creature) * creatureCount.getCount());
        final Text label = new Text(creature.getName());
        int labelNumber = creatureCount.getCount();
        CreatureCount finalCreatureCount = creatureCount;
        setupLabel(label, datum.getNode(), finalCreatureCount, labelNumber);
        datum.nodeProperty().addListener((o, oldNode, newNode)-> setupLabel(label, newNode, finalCreatureCount, labelNumber));
    }

    private void setupLabel(Text label, Node node, CreatureCount creatureCount, int labelNumber) {
        if(node == null)
            return;
        label.setTextOrigin(VPos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFill(Color.WHITE);
        label.setFont(Font.font(label.getFont().getFamily(),
                FontWeight.BOLD, FontPosture.REGULAR, label.getFont().getSize()));
        ((Group) node.getParent()).getChildren().add(label);
        node.parentProperty().addListener((ov, oldParent, parent) -> {
            Group parentGroup = (Group) parent;
            if(parentGroup != null)
                parentGroup.getChildren().add(label);
            else
                Platform.runLater(()->((Group) oldParent).getChildren().remove(label));
        });
        node.boundsInParentProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> ov, Bounds oldVal, Bounds newVal) {
                if (labelNumber > creatureCount.getCount()) {
                    ((Group) label.getParent()).getChildren().remove(label);
                    node.boundsInParentProperty().removeListener(this);
                    return;
                }
                double labelWidth = newVal.getWidth() / creatureCount.getCount();
                double xPos = newVal.getMinX() + labelWidth * (labelNumber - .5);
                label.setLayoutX(xPos - label.prefWidth(-1) / 2);
                label.setLayoutY(newVal.getCenterY());
                if (newVal.getHeight() / labelWidth > 1.2) {
                    label.setFont(Font.font(Math.min(15.0, labelWidth)));
                    label.setRotate(270);
                    label.setWrappingWidth(newVal.getHeight() - 4);
                    double overflowRatio = label.getBoundsInLocal().getHeight() / labelWidth;
                    while (overflowRatio > 1) {
                        label.setFont(Font.font(label.getFont().getSize() * .8));
                        overflowRatio = label.getBoundsInLocal().getHeight() / labelWidth;
                    }
                } else {
                    label.setFont(Font.font(Math.min(20, labelWidth / 6)));
                    label.setRotate(0);
                    label.setWrappingWidth(labelWidth - 10);
                }
            }
        });
        EventHandler<MouseEvent> eventHandler = e -> {
            preview.getEngine().loadContent(CreatureHTMLGenerator.parse(creatureCount.getCreature()));
            if (e.getClickCount() % 2 == 0) {
                removeFromEncounter(creatureCount.getCreature());
            }
        };
        node.setOnMouseClicked(eventHandler);
        label.setOnMouseClicked(eventHandler);
    }

    private int getXP(Creature creature) {
        return getXP(creature.getLevel() - partyLevel.getValue());
    }

    // Worth noting: This is beautiful. Outstanding design work from Paizo on their XP numbers
    private int getXP(int levelDifference) {
        if(levelDifference % 2 == 0) {
            return Math.toIntExact(Math.round(40 * Math.pow(2, (levelDifference / 2.0))));
        } else {
            return (getXP(levelDifference + 1) + getXP(levelDifference - 1)) / 2;
        }
    }

    private void removeFromEncounter(Creature creature) {
        CreatureCount existing = selectedMap.get(creature);
        existing.remove(1);
        XYChart.Data<Number, String> datum = seriesMap.get(creature).getData().get(0);
        datum.setXValue(getXP(creature) * existing.getCount());
        if(existing.getCount() <= 0) {
            selectedMap.remove(creature);
            selected.remove(existing);
            chart.getData().remove(seriesMap.get(creature));
            seriesMap.remove(creature);
        }
        chart.requestLayout();
        xAxis.requestAxisLayout();
        chart.layout();
        xAxis.layout();
    }

    private List<TreeTableColumn<CreatureEntry, ?>> makeColumns(ReadOnlyDoubleProperty width) {
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

    private List<TreeTableColumn<CreatureCountEntry, ?>> makeCountColumns(ReadOnlyDoubleProperty width) {
        TreeTableColumn<CreatureCountEntry, String> name = new TreeTableColumn<>("Name");
        TreeTableColumn<CreatureCountEntry, String> level = new TreeTableColumn<>("Level");
        TreeTableColumn<CreatureCountEntry, String> count = new TreeTableColumn<>("Count");
        name.setCellValueFactory(new TreeCellFactory<>("name"));
        name.minWidthProperty().bind(width.multiply(.5));
        level.setCellValueFactory(new TreeCellFactory<>("level"));
        level.setStyle( "-fx-alignment: CENTER;");
        level.setComparator((s1,s2)->{
            double d1 = (!s1.equals(""))? Double.parseDouble(s1) : -1;
            double d2 = (!s2.equals(""))? Double.parseDouble(s2) : -1;
            return Double.compare(d1, d2);
        });
        count.setStyle( "-fx-alignment: CENTER;");
        count.setCellValueFactory(new TreeCellFactory<>("count"));
        return Arrays.asList(name, level, count);
    }
}
