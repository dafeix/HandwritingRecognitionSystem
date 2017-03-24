package ge.edu.tsu.hcrs.control_panel.server.processor.normalizeddata;

import ge.edu.tsu.hcrs.control_panel.model.network.TrainingDataInfo;
import ge.edu.tsu.hcrs.control_panel.model.network.normalizeddata.GroupedNormalizedData;
import ge.edu.tsu.hcrs.control_panel.model.network.normalizeddata.NormalizedData;
import ge.edu.tsu.hcrs.control_panel.server.dao.normalizeddata.GroupedNormalizedDataDAO;
import ge.edu.tsu.hcrs.control_panel.server.dao.normalizeddata.GroupedNormalizedDataDAOImpl;
import ge.edu.tsu.hcrs.control_panel.server.dao.normalizeddata.NormalizedDataDAO;
import ge.edu.tsu.hcrs.control_panel.server.dao.normalizeddata.NormalizedDataDAOImpl;
import ge.edu.tsu.hcrs.control_panel.server.processor.normalizeddata.normalizationmethod.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NormalizedDataProcessor {

    private final NormalizedDataDAO normalizedDataDAO = new NormalizedDataDAOImpl();

    private final GroupedNormalizedDataDAO groupedNormalizedDataDAO = new GroupedNormalizedDataDAOImpl();

    public void addNormalizedDatum(GroupedNormalizedData groupedNormalizedData, List<String> directories) {
        TrainingDataInfo trainingDataInfo = new TrainingDataInfo();
        trainingDataInfo.setWidth(groupedNormalizedData.getWidth());
        trainingDataInfo.setHeight(groupedNormalizedData.getHeight());
        trainingDataInfo.setMinValue(groupedNormalizedData.getMinValue());
        trainingDataInfo.setMaxValue(groupedNormalizedData.getMaxValue());
        trainingDataInfo.setNormalizationType(groupedNormalizedData.getNormalizationType());
        List<NormalizedData> normalizedDatum = new ArrayList<>();
        Date date = new Date();
        for (String directory : directories) {
            File file = new File(directory);
            addNormalizedData(trainingDataInfo, file, normalizedDatum);
        }
        int groupedNormalizedDataId = groupedNormalizedDataDAO.addOrGetGroupedNormalizedDataId(groupedNormalizedData);
        groupedNormalizedData.setId(groupedNormalizedDataId);
        groupedNormalizedData.setDuration((new Date().getTime() - date.getTime()));
        normalizedDataDAO.addNormalizedDatum(normalizedDatum, groupedNormalizedData);
    }

    private void addNormalizedData(TrainingDataInfo trainingDataInfo, File file, List<NormalizedData> normalizedDatum) {
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                addNormalizedData(trainingDataInfo, f, normalizedDatum);
            } else {
                try {
                    BufferedImage image = ImageIO.read(f);
                    NormalizationMethod normalizationMethod = null;
                    switch (trainingDataInfo.getNormalizationType()) {
                        case DISCRETE_BY_AREA:
                            normalizationMethod = new DiscreteByAreaNormalization();
                            break;
                        case DISCRETE_RESIZE:
                            normalizationMethod = new DiscreteResizeNormalization();
                            break;
                        case LINEAR_BY_AREA:
                            normalizationMethod = new LinearByAreaNormalization();
                            break;
                        case LINEAR_RESIZE:
                            normalizationMethod = new LinearResizeNormalization();
                            break;
                    }
                    NormalizedData normalizedData = normalizationMethod.getNormalizedDataFromImage(image, trainingDataInfo, getLetterFromFile(f));
                    normalizedDatum.add(normalizedData);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }

    private Character getLetterFromFile(File file) {
        String fileName = file.getName();
        String fileNameWithoutExtension = fileName.replaceFirst("[.][^.]+$", "");
        String text = fileNameWithoutExtension.split("_")[1];
        switch (text) {
            case "questionMark" :
                return '?';
            case "lessThan" :
                return '<';
            case "greaterThan" :
                return '>';
            case "colon" :
                return ':';
            case "doubleQuote" :
                return '"';
            case "forwardSlash" :
                return '/';
            case "backslash" :
                return '\\';
            case "verticalBar" :
                return  '|';
            case "asterisk" :
                return '*';
            default:
                return text.charAt(0);
        }
    }
}
