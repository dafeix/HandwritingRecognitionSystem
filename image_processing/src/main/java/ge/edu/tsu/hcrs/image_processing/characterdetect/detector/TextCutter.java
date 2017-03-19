package ge.edu.tsu.hcrs.image_processing.characterdetect.detector;

import ge.edu.tsu.hcrs.image_processing.characterdetect.model.Contour;
import ge.edu.tsu.hcrs.image_processing.characterdetect.model.TextAdapter;
import ge.edu.tsu.hcrs.image_processing.characterdetect.model.TextRow;
import ge.edu.tsu.hcrs.image_processing.characterdetect.util.ContourUtil;
import ge.edu.tsu.hcrs.image_processing.characterdetect.util.TextAdapterUtil;
import ge.edu.tsu.hcrs.image_processing.exception.TextAdapterSizeException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TextCutter {

    public static void saveCutCharacters(String srcImagePath, String srcTextPath, String resultImagesPath, TextCutterParams params) throws TextAdapterSizeException {
        try {
            BufferedImage image = ImageIO.read(new File(srcImagePath));
            BufferedReader br = new BufferedReader(new FileReader(srcTextPath));
            String text = "";
            String line;
            while ((line = br.readLine()) != null) {
                text += line + System.lineSeparator();
            }
            TextAdapter textAdapter = ContoursDetector.detectContours(image, params);
            int resultCount = TextAdapterUtil.countCharactersFromTextAdapter(textAdapter);
            int expectedCount = TextAdapterUtil.countCharactersFromText(text, params.isDoubleQuoteAsTwoChar());
            File file = new File(resultImagesPath);
            file.mkdirs();
            if (resultCount != expectedCount) {
                if (params.isSaveAnyway()) {
                    int i = 1;
                    for (TextRow textRow : textAdapter.getRows()) {
                        for (Contour contour : textRow.getContours()) {
                            BufferedImage resultImage = ContourUtil.getBufferedImageFromContour(contour);
                            ImageIO.write(resultImage, "png", new File(resultImagesPath + "/" + i + ".png"));
                            i++;
                        }
                    }
                }
                throw new TextAdapterSizeException(expectedCount, resultCount);
            } else {
                int i = 0;
                for (TextRow textRow : textAdapter.getRows()) {
                    for (Contour contour : textRow.getContours()) {
                        while (i < text.length() && TextAdapterUtil.isUnnecessaryCharacter(text.charAt(i))) {
                            i++;
                        }
                        BufferedImage resultImage = ContourUtil.getBufferedImageFromContour(contour);
                        ImageIO.write(resultImage, "png", new File(resultImagesPath + "/" + (i + 1) + "_" + getForbiddenCharsValue(text.charAt(i)) + ".png"));
                        i++;
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static String getForbiddenCharsValue(char c) {
        String s;
        switch (c) {
            case '?' :
                s = "questionMark";
                break;
            case '<' :
                s = "lessThan";
                break;
            case '>' :
                s = "greaterThan";
                break;
            case ':' :
                s = "colon";
                break;
            case '"' :
                s = "doubleQuote";
                break;
            case '/' :
                s = "forwardSlash";
                break;
            case '\\' :
                s = "backslash";
                break;
            case '|' :
                s = "verticalBar";
                break;
            case '*' :
                s = "asterisk";
                break;
            default:
                s = "" + c;
        }
        return s;
    }
}
