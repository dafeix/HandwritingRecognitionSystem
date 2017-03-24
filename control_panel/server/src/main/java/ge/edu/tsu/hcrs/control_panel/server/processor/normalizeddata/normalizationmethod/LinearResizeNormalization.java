package ge.edu.tsu.hcrs.control_panel.server.processor.normalizeddata.normalizationmethod;

import ge.edu.tsu.hcrs.control_panel.model.network.TrainingDataInfo;
import ge.edu.tsu.hcrs.control_panel.model.network.normalizeddata.NormalizedData;
import ge.edu.tsu.hcrs.control_panel.server.processor.imageprocessing.ImageProcessingProcessor;

import java.awt.image.BufferedImage;

public class LinearResizeNormalization extends NormalizationMethod {

    private final ImageProcessingProcessor imageProcessingProcessor = new ImageProcessingProcessor();

    @Override
    public NormalizedData getNormalizedDataFromImage(BufferedImage image, TrainingDataInfo trainingDataInfo, Character letter) {
        BufferedImage resizedImage = imageProcessingProcessor.resizeImage(image, false, trainingDataInfo.getWidth(), trainingDataInfo.getHeight());
        NormalizedData normalizedData = new NormalizedData();
        normalizedData.setLetter(letter);
        Float[] data = new Float[trainingDataInfo.getHeight() * trainingDataInfo.getWidth()];
        for (int i = 0; i < trainingDataInfo.getHeight(); i++) {
            for (int j = 0; j < trainingDataInfo.getWidth(); j++) {
                data[i * trainingDataInfo.getWidth() + j] = (float)resizedImage.getRGB(j, i) / blackPixel;
            }
        }
        normalizedData.setData(data);
        return normalizedData;
    }
}
