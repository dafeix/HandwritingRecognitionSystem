package ge.edu.tsu.hrs.control_panel.server.processor.imageprocessing;

import ge.edu.tsu.hrs.control_panel.model.imageprocessing.ImageCondition;
import ge.edu.tsu.hrs.control_panel.model.imageprocessing.TextCutterParameters;
import ge.edu.tsu.hrs.control_panel.model.imageprocessing.blurrin.BlurringParameters;
import ge.edu.tsu.hrs.control_panel.model.imageprocessing.morphological.MorphologicalParameters;
import ge.edu.tsu.hrs.control_panel.model.imageprocessing.threshold.ThresholdParameters;
import ge.edu.tsu.hrs.control_panel.model.sysparam.Parameter;
import ge.edu.tsu.hrs.control_panel.server.processor.systemparameter.SystemParameterProcessor;
import ge.edu.tsu.hrs.control_panel.server.util.CharacterUtil;
import ge.edu.tsu.hrs.image_processing.characterdetect.detector.ContoursDetector;
import ge.edu.tsu.hrs.image_processing.characterdetect.detector.TextCutterParams;
import ge.edu.tsu.hrs.image_processing.characterdetect.model.Contour;
import ge.edu.tsu.hrs.image_processing.characterdetect.model.TextAdapter;
import ge.edu.tsu.hrs.image_processing.characterdetect.model.TextRow;
import ge.edu.tsu.hrs.image_processing.characterdetect.util.ContourUtil;
import ge.edu.tsu.hrs.image_processing.opencv.operation.BinaryConverter;
import ge.edu.tsu.hrs.image_processing.opencv.operation.ImageResizer;
import ge.edu.tsu.hrs.image_processing.opencv.operation.MorphologicalOperations;
import ge.edu.tsu.hrs.image_processing.opencv.operation.NoiseRemover;
import ge.edu.tsu.hrs.image_processing.opencv.operation.parameter.ImageResizerParams;
import ge.edu.tsu.hrs.image_processing.opencv.operation.parameter.blurring.BilateralFilterParams;
import ge.edu.tsu.hrs.image_processing.opencv.operation.parameter.blurring.BlurParams;
import ge.edu.tsu.hrs.image_processing.opencv.operation.parameter.blurring.GaussianBlurParams;
import ge.edu.tsu.hrs.image_processing.opencv.operation.parameter.blurring.MedianBlurParams;
import ge.edu.tsu.hrs.image_processing.opencv.operation.parameter.morphological.DilationParams;
import ge.edu.tsu.hrs.image_processing.opencv.operation.parameter.morphological.ErosionParams;
import ge.edu.tsu.hrs.image_processing.opencv.operation.parameter.threshold.AdaptiveThresholdParams;
import ge.edu.tsu.hrs.image_processing.opencv.operation.parameter.threshold.OtsuBinarizationParams;
import ge.edu.tsu.hrs.image_processing.opencv.operation.parameter.threshold.SimpleThresholdParams;
import ge.edu.tsu.hrs.image_processing.util.OpenCVUtil;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ImageProcessingProcessor {

    private final SystemParameterProcessor systemParameterProcessor = new SystemParameterProcessor();

    private final Parameter maxNumberOfColors = new Parameter("maxNumberOfColors", "200");

    private final Parameter useJoiningFunctional = new Parameter("useJoiningFunctional", "true");

    private final Parameter backgroundMinPart = new Parameter("backgroundMinPart", "0.03");

    private final Parameter extraColorsPart = new Parameter("extraColorsPart", "0.5");

    public List<BufferedImage> getCutSymbols(BufferedImage srcImage, TextCutterParameters parameters, boolean forceNotJoining, Float extraPart) {
        TextCutterParams textCutterParams = new TextCutterParams();
        if (parameters != null) {
            textCutterParams.setCheckedRGBMaxValue(parameters.getCheckedRGBMaxValue());
            textCutterParams.setCheckNeighborRGBMaxValue(parameters.getCheckNeighborRGBMaxValue());
            textCutterParams.setPercentageOfSameForJoining(parameters.getPercentageOfSameForJoining());
            textCutterParams.setPercentageOfSamesForOneRow(parameters.getPercentageOfSamesForOneRow());
            textCutterParams.setUseJoiningFunctional(parameters.isUseJoiningFunctional());
            textCutterParams.setNoiseArea(parameters.getNoiseArea());
        } else {
            fillTextCutterParams(textCutterParams, srcImage, forceNotJoining, extraPart);
        }
        TextAdapter textAdapter = ContoursDetector.detectContours(srcImage, textCutterParams);
        List<BufferedImage> images = new ArrayList<>();
        for (TextRow textRow : textAdapter.getRows()) {
            for (Contour contour : textRow.getContours()) {
                BufferedImage image = ContourUtil.getBufferedImageFromContour(contour);
                if (parameters == null && !forceNotJoining) {
                    image = simpleClean(image);
                }
                images.add(image);
            }
        }
        return images;
    }

    public List<String> processTextForImage(String text, boolean doubleQuoteAsTwoChar) {
        List<String> symbols = new ArrayList<>();
        for (char c : text.toCharArray()) {
            if (!isUnnecessaryCharacter(c)) {
                if (doubleQuoteAsTwoChar && (c == '"' || c == '“' || c == '”')) {
                    symbols.add("'");
                    symbols.add("'");
                } else {
                    symbols.add("" + c);
                }
            }
        }
        return symbols;
    }

    public void saveCutSymbols(List<BufferedImage> images, List<String> text, String directoryPath) {
        try {
            File directory = new File(directoryPath);
            int nextId = 1;
            for (File f : directory.listFiles()) {
                if (f.isFile()) {
                    try {
                        String fileNameWithoutExtension = f.getName().replaceFirst("[.][^.]+$", "");
                        String id = fileNameWithoutExtension.split("_")[0];
                        if (Integer.parseInt(id) >= nextId) {
                            nextId = Integer.parseInt(id) + 1;
                        }
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            for (int i = 0; i < images.size(); i++) {
                char c = text.get(i).length() > 0 ? text.get(i).charAt(0) : ' ';
                ImageIO.write(images.get(i), "png", new File(directoryPath + "/" + (nextId) + "_" + CharacterUtil.getCharValueForFileName(c) + ".png"));
                nextId++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static BufferedImage resizeImage(BufferedImage srcImage, boolean scaleResizing, double x, double y) {
        opencv_core.Mat srcMat = OpenCVUtil.bufferedImageToMat(srcImage);
        ImageResizerParams params = new ImageResizerParams();
        if (scaleResizing) {
            params.setFx(x);
            params.setFy(y);
            params.setWidth(0);
            params.setHeight(0);
        } else {
            params.setFx(0);
            params.setFy(0);
            params.setWidth((int) x);
            params.setHeight((int) y);
        }
        opencv_core.Mat resultMat = ImageResizer.resize(srcMat, params);
        return OpenCVUtil.matToBufferedImage(resultMat);
    }

    public BufferedImage cleanImage(BufferedImage srcImage, BlurringParameters blurringParameters, ThresholdParameters thresholdParameters, MorphologicalParameters morphologicalParameters) {
        opencv_core.Mat mat = OpenCVUtil.bufferedImageToMat(srcImage);
        if (mat.type() != opencv_core.CV_8UC1) {
            opencv_imgproc.cvtColor(mat, mat, opencv_imgproc.CV_RGB2GRAY);
        }
        if (blurringParameters == null || thresholdParameters == null || morphologicalParameters == null) {
            return cleanImageWithoutParameters(mat);
        }
        switch (blurringParameters.getType()) {
            case BILATERAL_FILTER:
                BilateralFilterParams bilateralFilterParams = new BilateralFilterParams();
                if (blurringParameters.getDiameter() != null) {
                    bilateralFilterParams.setDiameter(blurringParameters.getDiameter());
                }
                if (blurringParameters.getSigmaColor() != null) {
                    bilateralFilterParams.setSigmaColor(blurringParameters.getSigmaColor());
                }
                if (blurringParameters.getSigmaSpace() != null) {
                    bilateralFilterParams.setSigmaSpace(blurringParameters.getSigmaSpace());
                }
                mat = NoiseRemover.applyNoiseRemoval(mat, bilateralFilterParams, blurringParameters.getAmount());
                break;
            case BLUR:
                BlurParams blurParams = new BlurParams();
                if (blurringParameters.getkSizeHeight() != null) {
                    blurParams.setkSizeHeight(blurringParameters.getkSizeHeight());
                }
                if (blurringParameters.getkSizeWidth() != null) {
                    blurParams.setkSizeWidth(blurringParameters.getkSizeWidth());
                }
                mat = NoiseRemover.applyNoiseRemoval(mat, blurParams, blurringParameters.getAmount());
                break;
            case GAUSSIAN_BLUR:
                GaussianBlurParams gaussianBlurParams = new GaussianBlurParams();
                if (blurringParameters.getkSizeWidth() != null) {
                    gaussianBlurParams.setkSizeWidth(blurringParameters.getkSizeWidth());
                }
                if (blurringParameters.getkSizeHeight() != null) {
                    gaussianBlurParams.setkSizeHeight(blurringParameters.getkSizeHeight());
                }
                if (blurringParameters.getBorderType() != null) {
                    gaussianBlurParams.setBorderType(blurringParameters.getBorderType());
                }
                if (blurringParameters.getSigmaX() != null) {
                    gaussianBlurParams.setSigmaX(blurringParameters.getSigmaX());
                }
                if (blurringParameters.getSigmaY() != null) {
                    gaussianBlurParams.setSigmaY(blurringParameters.getSigmaY());
                }
                mat = NoiseRemover.applyNoiseRemoval(mat, gaussianBlurParams, blurringParameters.getAmount());
                break;
            case MEDIAN_BLUR:
                MedianBlurParams medianBlurParams = new MedianBlurParams();
                if (blurringParameters.getkSize() != null) {
                    medianBlurParams.setkSize(blurringParameters.getkSize());
                }
                mat = NoiseRemover.applyNoiseRemoval(mat, medianBlurParams, blurringParameters.getAmount());
                break;
            case NO_BLURRING:
                default:
                    break;
        }
        switch (thresholdParameters.getThresholdMethodType()) {
            case ADAPTIVE_THRESHOLD:
                AdaptiveThresholdParams adaptiveThresholdParams = new AdaptiveThresholdParams();
                if (thresholdParameters.getAdaptiveMethod() != null) {
                    adaptiveThresholdParams.setAdaptiveMethod(thresholdParameters.getAdaptiveMethod());
                }
                if (thresholdParameters.getThresholdType() != null) {
                    adaptiveThresholdParams.setThresholdType(thresholdParameters.getThresholdType());
                }
                if (thresholdParameters.getBlockSize() != null) {
                    adaptiveThresholdParams.setBlockSize(thresholdParameters.getBlockSize());
                }
                if (thresholdParameters.getMaxValue() != null) {
                    adaptiveThresholdParams.setMaxValue(thresholdParameters.getMaxValue());
                }
                if (thresholdParameters.getC() != null) {
                    adaptiveThresholdParams.setC(thresholdParameters.getC());
                }
                mat = BinaryConverter.applyThreshold(mat, adaptiveThresholdParams);
                break;
            case OTSU_BINARIZATION:
                OtsuBinarizationParams otsuBinarizationParams = new OtsuBinarizationParams();
                if (thresholdParameters.getMaxValue() != null) {
                    otsuBinarizationParams.setMaxValue(thresholdParameters.getMaxValue());
                }
                if (thresholdParameters.getThresh() != null) {
                    otsuBinarizationParams.setThresh(thresholdParameters.getThresh());
                }
                if (thresholdParameters.getType() != null) {
                    otsuBinarizationParams.setType(thresholdParameters.getType());
                }
                mat = BinaryConverter.applyThreshold(mat, otsuBinarizationParams);
                break;
            case SIMPLE_THRESHOLD:
                SimpleThresholdParams simpleThresholdParams = new SimpleThresholdParams();
                if (thresholdParameters.getMaxValue() != null) {
                    simpleThresholdParams.setMaxValue(thresholdParameters.getMaxValue());
                }
                if (thresholdParameters.getThresh() != null) {
                    simpleThresholdParams.setThresh(thresholdParameters.getThresh());
                }
                if (thresholdParameters.getType() != null) {
                    simpleThresholdParams.setType(thresholdParameters.getType());
                }
                mat = BinaryConverter.applyThreshold(mat, simpleThresholdParams);
                break;
            case NO_THRESHOLD:
                default:
                    break;
        }
        DilationParams dilationParams = new DilationParams();
        if (morphologicalParameters.getDilationElem() != null) {
            dilationParams.setDilation_elem(morphologicalParameters.getDilationElem());
        }
        if (morphologicalParameters.getDilationSize() != null) {
            dilationParams.setDilation_size(morphologicalParameters.getDilationSize());
        }
        ErosionParams erosionParams = new ErosionParams();
        if (morphologicalParameters.getErosionElem() != null) {
            erosionParams.setErosion_elem(morphologicalParameters.getErosionElem());
        }
        if (morphologicalParameters.getErosionSize() != null) {
            erosionParams.setErosion_size(morphologicalParameters.getErosionSize());
        }
        switch (morphologicalParameters.getMorphologicalType()) {
            case DILATION_EROSION:
                mat = MorphologicalOperations.applyDilation(mat, dilationParams, false, morphologicalParameters.getDilationAmount());
                mat = MorphologicalOperations.applyErosion(mat, erosionParams, false, morphologicalParameters.getErosionAmount());
                break;
            case EROSION_DILATION:
                mat = MorphologicalOperations.applyErosion(mat, erosionParams, false, morphologicalParameters.getErosionAmount());
                mat = MorphologicalOperations.applyDilation(mat, dilationParams, false, morphologicalParameters.getDilationAmount());
                break;
            case NO_OPERATION:
                default:
                    break;
        }
        return OpenCVUtil.matToBufferedImage(mat);
    }

    private BufferedImage cleanImageWithoutParameters(opencv_core.Mat mat) {
        BufferedImage testImage = OpenCVUtil.matToBufferedImage(mat);
        Set<Integer> set = new TreeSet<>();
        for (int i = 0; i < testImage.getHeight(); i++) {
            for (int j = 0; j < testImage.getWidth(); j++) {
                set.add(testImage.getRGB(j, i));
            }
        }
        if (set.size() < systemParameterProcessor.getIntegerParameterValue(maxNumberOfColors)) {
            return OpenCVUtil.matToBufferedImage(mat);
        }
        ImageCondition imageCondition = detectImageCondition();
        BilateralFilterParams bilateralFilterParams = new BilateralFilterParams();
        AdaptiveThresholdParams adaptiveThresholdParams = new AdaptiveThresholdParams();
        OtsuBinarizationParams otsuBinarizationParams = new OtsuBinarizationParams();
        switch (imageCondition) {
            case DARK_ON_BRIGHT:
                mat = NoiseRemover.applyNoiseRemoval(mat, bilateralFilterParams, 1);
                mat = BinaryConverter.applyThreshold(mat, adaptiveThresholdParams);
                break;
            case BRIGHT_ON_DARK:
                mat = NoiseRemover.applyNoiseRemoval(mat, bilateralFilterParams, 1);
                otsuBinarizationParams.setType(9);
                mat = BinaryConverter.applyThreshold(mat, otsuBinarizationParams);
                break;
            case DARK_ON_BRIGHT_WITH_LINES:
                mat = NoiseRemover.applyNoiseRemoval(mat, bilateralFilterParams, 4);
                mat = BinaryConverter.applyThreshold(mat, adaptiveThresholdParams);
                break;
            case UNKNOWN:
                break;
        }
        return OpenCVUtil.matToBufferedImage(mat);
    }

    public void fillTextCutterParams(TextCutterParams params, BufferedImage image, boolean forceNotJoining, Float extraPart) {
        if (forceNotJoining) {
            params.setUseJoiningFunctional(false);
        } else {
            params.setUseJoiningFunctional(systemParameterProcessor.getBooleanParameterValue(useJoiningFunctional));
        }
        Map<Integer, Integer> rgbMap = new TreeMap<>();
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                int x = image.getRGB(j, i);
                if (rgbMap.containsKey(x)) {
                    rgbMap.put(x, rgbMap.get(x) + 1);
                } else {
                    rgbMap.put(x, 1);
                }
            }
        }
        int border = 0;
        int area = image.getHeight() * image.getWidth();
        for (int x : rgbMap.keySet()) {
            if (border > 1 && rgbMap.get(x) >= area * systemParameterProcessor.getFloatParameterValue(backgroundMinPart)) {
                break;
            }
            border++;
        }
        if (extraPart == null) {
            border = (int) ((1 - systemParameterProcessor.getFloatParameterValue(extraColorsPart)) * border);
        } else {
            border = (int) ((1 - extraPart) * border);
        }
        for (Integer x : rgbMap.keySet()) {
            if (border == 0) {
                params.setCheckedRGBMaxValue(x);
                params.setCheckNeighborRGBMaxValue(x);
            }
            border--;
        }
    }

    public BufferedImage simpleClean(BufferedImage image) {
        opencv_core.Mat mat = OpenCVUtil.bufferedImageToMat(image);
        if (mat.type() != opencv_core.CV_8UC1) {
            opencv_imgproc.cvtColor(mat, mat, opencv_imgproc.CV_RGB2GRAY);
        }
        mat = BinaryConverter.applyThreshold(mat, new OtsuBinarizationParams());
        return OpenCVUtil.matToBufferedImage(mat);
    }

    private ImageCondition detectImageCondition() {
        return ImageCondition.DARK_ON_BRIGHT_WITH_LINES;
    }

    private static boolean isUnnecessaryCharacter(char c) {
        return c == ' ' || c == '\n' || c == '\r';
    }
}
