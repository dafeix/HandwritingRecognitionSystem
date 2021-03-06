package ge.edu.tsu.hrs.control_panel.console.fx.ui.cutsymbols;

import ge.edu.tsu.hrs.control_panel.console.fx.ui.component.TCHButton;
import ge.edu.tsu.hrs.control_panel.console.fx.ui.component.TCHComboBox;
import ge.edu.tsu.hrs.control_panel.console.fx.ui.component.TCHComponentSize;
import ge.edu.tsu.hrs.control_panel.console.fx.ui.component.TCHFieldLabel;
import ge.edu.tsu.hrs.control_panel.console.fx.ui.component.TCHLabel;
import ge.edu.tsu.hrs.control_panel.console.fx.ui.component.TCHNumberTextField;
import ge.edu.tsu.hrs.control_panel.console.fx.ui.component.TCHTextArea;
import ge.edu.tsu.hrs.control_panel.console.fx.ui.component.TCHTextField;
import ge.edu.tsu.hrs.control_panel.console.fx.ui.main.ControlPanel;
import ge.edu.tsu.hrs.control_panel.console.fx.util.ImageFactory;
import ge.edu.tsu.hrs.control_panel.console.fx.util.Messages;
import ge.edu.tsu.hrs.control_panel.model.common.HRSPath;
import ge.edu.tsu.hrs.control_panel.model.imageprocessing.TextCutterParameters;
import ge.edu.tsu.hrs.control_panel.model.sysparam.Parameter;
import ge.edu.tsu.hrs.control_panel.service.common.HRSPathService;
import ge.edu.tsu.hrs.control_panel.service.common.HRSPathServiceImpl;
import ge.edu.tsu.hrs.control_panel.service.imageprocessing.ImageProcessingService;
import ge.edu.tsu.hrs.control_panel.service.imageprocessing.ImageProcessingServiceImpl;
import ge.edu.tsu.hrs.control_panel.service.systemparameter.SystemParameterService;
import ge.edu.tsu.hrs.control_panel.service.systemparameter.SystemParameterServiceImpl;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class CutSymbolsPane extends VBox {

    private final HRSPathService hrsPathService = new HRSPathServiceImpl();

    private final ImageProcessingService imageProcessingService = new ImageProcessingServiceImpl();

    private final SystemParameterService systemParameterService = new SystemParameterServiceImpl();

    private final Parameter doubleQuoteAsTwoChar = new Parameter("doubleQuoteAsTwoChar", "true");

    private static final double TOP_PANE_PART = 0.75;

    private static final double IMAGE_WIDTH_PART = 0.30;

    private ImageView srcImageView;

    private TCHButton cutButton;

    private TCHTextArea textArea;

    private SymbolsPane symbolsPane;

    private CheckBox cutWithoutParamsCheckBox;

    private TCHTextField extraColorsPartField;

    public CutSymbolsPane() {
        this.setSpacing(8);
        this.getChildren().addAll(getTopPane(), getBottomPane());
    }

    public CutSymbolsPane(BufferedImage image) {
        this();
        srcImageView.setImage(SwingFXUtils.toFXImage(image, null));
        cutButton.setDisable(false);
        cutWithoutParamsCheckBox.setSelected(true);
        extraColorsPartField.setDisable(false);
    }

    private HBox getTopPane() {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(4, 4, 0, 4));
        hBox.setSpacing(10);
        hBox.prefHeightProperty().bind(ControlPanel.getCenterHeightBinding().multiply(TOP_PANE_PART));
        VBox leftVBox = new VBox();
        leftVBox.setSpacing(10);
        srcImageView = new ImageView();
        srcImageView.setImage(ImageFactory.getImage("no_photo.png"));
        srcImageView.fitHeightProperty().bind(ControlPanel.getCenterHeightBinding().multiply(TOP_PANE_PART).divide(2).subtract(5));
        srcImageView.fitWidthProperty().bind(ControlPanel.getCenterWidthBinding().multiply(IMAGE_WIDTH_PART).subtract(5));
        srcImageView.setOnMouseEntered(event -> this.setCursor(Cursor.HAND));
        srcImageView.setOnMouseExited(event -> this.setCursor(Cursor.DEFAULT));
        srcImageView.setOnMouseClicked(event -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File(hrsPathService.getPath(HRSPath.CLEANED_IMAGES_PATH)));
                fileChooser.setTitle(Messages.get("openSrcImage"));
                File file = fileChooser.showOpenDialog(ControlPanel.getStage());
                srcImageView.setImage(new Image(file.toURI().toURL().toExternalForm()));
                cutButton.setDisable(false);
            } catch (Exception ex) {
                srcImageView.setImage(ImageFactory.getImage("no_photo.png"));
                cutButton.setDisable(true);
                System.out.println("Can't load image!");
            }
        });
        textArea = new TCHTextArea();
        textArea.setPromptText(Messages.get("noText"));
        textArea.prefHeightProperty().bind(ControlPanel.getCenterHeightBinding().multiply(TOP_PANE_PART).divide(2).subtract(5));
        textArea.prefWidthProperty().bind(ControlPanel.getCenterWidthBinding().multiply(IMAGE_WIDTH_PART).subtract(5));
        leftVBox.getChildren().addAll(srcImageView, textArea);
        symbolsPane = new SymbolsPane();
        symbolsPane.prefHeightProperty().bind(ControlPanel.getCenterHeightBinding().multiply(TOP_PANE_PART).subtract(7));
        symbolsPane.prefWidthProperty().bind(ControlPanel.getCenterWidthBinding().multiply(1 - IMAGE_WIDTH_PART).subtract(15));
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setContent(symbolsPane);
        hBox.getChildren().addAll(leftVBox, scrollPane);
        return hBox;
    }

    private ScrollPane getBottomPane() {
        ScrollPane scrollPane = new ScrollPane();
        VBox vBox = new VBox();
        vBox.setSpacing(5);
        vBox.setStyle("-fx-border-color: green; -fx-border-radius: 10px; -fx-border-size: 1px;");
        vBox.prefHeightProperty().bind(ControlPanel.getCenterHeightBinding().multiply(1 - TOP_PANE_PART));
        TCHLabel titleLabel = new TCHLabel(Messages.get("cutSymbolsParams"));
        titleLabel.setStyle("-fx-font-family: sylfaen; -fx-font-size: 16px;");
        titleLabel.setTextFill(Color.GREEN);
        FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL);
        flowPane.setAlignment(Pos.TOP_CENTER);
        flowPane.setHgap(15);
        flowPane.setVgap(5);
        flowPane.prefWidthProperty().bind(ControlPanel.getCenterWidthBinding());
        cutWithoutParamsCheckBox = new CheckBox();
        cutWithoutParamsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            extraColorsPartField.setDisable(!newValue);
        });
        extraColorsPartField = new TCHTextField("0.5", TCHComponentSize.SMALL);
        TCHFieldLabel extraColorsPartFieldLabel = new TCHFieldLabel(Messages.get("extraColorsPart"), extraColorsPartField);
        extraColorsPartField.setDisable(true);
        TCHFieldLabel cutWithoutParamsFieldLabel = new TCHFieldLabel(Messages.get("cutWithoutParams"), cutWithoutParamsCheckBox);
        TCHNumberTextField checkedRGBMaxValueField = new TCHNumberTextField(new BigDecimal("-5777216"), TCHComponentSize.SMALL);
        TCHFieldLabel checkedRGBMaxValueFieldLabel = new TCHFieldLabel(Messages.get("checkedRGBMaxValue"), checkedRGBMaxValueField);
        TCHNumberTextField checkNeighborRGBMaxValueField = new TCHNumberTextField(new BigDecimal("-2"), TCHComponentSize.SMALL);
        TCHFieldLabel checkNeighborRGBMaxValueFieldLabel = new TCHFieldLabel(Messages.get("checkNeighborRGBMaxValue"), checkNeighborRGBMaxValueField);
        TCHComboBox doubleQuoteAsTwoCharComboBox = new TCHComboBox(Arrays.asList(Boolean.TRUE, Boolean.FALSE));
        TCHFieldLabel doubleQuoteAsTwoCharFieldLabel = new TCHFieldLabel(Messages.get("doubleQuoteAsTwoChar"), doubleQuoteAsTwoCharComboBox);
        TCHComboBox useJoiningFunctionalComboBox = new TCHComboBox(Arrays.asList(Boolean.TRUE, Boolean.FALSE));
        TCHFieldLabel useJoiningFunctionalFieldLabel = new TCHFieldLabel(Messages.get("useJoiningFunctional"), useJoiningFunctionalComboBox);
        TCHNumberTextField percentageOfSameForJoiningField = new TCHNumberTextField(new BigDecimal(75), TCHComponentSize.SMALL);
        TCHFieldLabel percentageOfSameForJoiningFieldLabel = new TCHFieldLabel(Messages.get("percentageOfSameForJoining"), percentageOfSameForJoiningField);
        TCHNumberTextField percentageOfSamesForOneRowField = new TCHNumberTextField(new BigDecimal(25), TCHComponentSize.SMALL);
        TCHFieldLabel percentageOfSamesForOneRowFieldLabel = new TCHFieldLabel(Messages.get("percentageOfSamesForOneRow"), percentageOfSamesForOneRowField);
        TCHNumberTextField noiseAreaField = new TCHNumberTextField(new BigDecimal(15), TCHComponentSize.SMALL);
        TCHFieldLabel noiseAreaFieldLabel = new TCHFieldLabel(Messages.get("noiseArea"), noiseAreaField);
        cutButton = new TCHButton(Messages.get("cut"));
        cutButton.setDisable(true);
        cutButton.setOnAction(event -> {
            TextCutterParameters parameters = null;
            if (!cutWithoutParamsCheckBox.isSelected()) {
                parameters = new TextCutterParameters();
                parameters.setUseJoiningFunctional(Boolean.valueOf(useJoiningFunctionalComboBox.getValue().toString()));
                parameters.setDoubleQuoteAsTwoChar(Boolean.valueOf(doubleQuoteAsTwoCharComboBox.getValue().toString()));
                parameters.setPercentageOfSameForJoining(percentageOfSameForJoiningField.getNumber().intValue());
                parameters.setCheckedRGBMaxValue(checkedRGBMaxValueField.getNumber().intValue());
                parameters.setCheckNeighborRGBMaxValue(checkNeighborRGBMaxValueField.getNumber().intValue());
                parameters.setPercentageOfSamesForOneRow(percentageOfSamesForOneRowField.getNumber().intValue());
                parameters.setNoiseArea(noiseAreaField.getNumber().intValue());
            }
            List<BufferedImage> images = imageProcessingService.getCutSymbols(SwingFXUtils.fromFXImage(srcImageView.getImage(), null), parameters, false,
                    extraColorsPartField.getText().isEmpty() ? null : Float.valueOf(extraColorsPartField.getText()));
            symbolsPane.initSymbols(images, imageProcessingService.processTextForImage(textArea.getText(), parameters == null ? systemParameterService.getBooleanParameterValue(doubleQuoteAsTwoChar) :
                    parameters.isDoubleQuoteAsTwoChar()), parameters);
        });
        flowPane.getChildren().addAll(cutWithoutParamsFieldLabel, extraColorsPartFieldLabel, checkedRGBMaxValueFieldLabel, checkNeighborRGBMaxValueFieldLabel, doubleQuoteAsTwoCharFieldLabel, useJoiningFunctionalFieldLabel,
                percentageOfSameForJoiningFieldLabel, percentageOfSamesForOneRowFieldLabel, noiseAreaFieldLabel, cutButton);
        vBox.getChildren().addAll(titleLabel, flowPane);
        scrollPane.setContent(vBox);
        return scrollPane;
    }
}
