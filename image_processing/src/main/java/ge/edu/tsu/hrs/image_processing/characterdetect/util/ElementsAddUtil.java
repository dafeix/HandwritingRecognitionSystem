package ge.edu.tsu.hrs.image_processing.characterdetect.util;

import ge.edu.tsu.hrs.image_processing.characterdetect.model.Contour;
import ge.edu.tsu.hrs.image_processing.characterdetect.model.Point;
import ge.edu.tsu.hrs.image_processing.characterdetect.model.TextAdapter;
import ge.edu.tsu.hrs.image_processing.characterdetect.model.TextRow;

import java.util.Map;

public class ElementsAddUtil {

    public static void addPointAndUpdate(Contour contour, Point point) {
        contour.getContourCoordinates().add(point);
        contour.setTopPoint((short) Math.min(contour.getTopPoint(), point.getX()));
        contour.setRightPoint((short) Math.max(contour.getRightPoint(), point.getY()));
        contour.setBottomPoint((short) Math.max(contour.getBottomPoint(), point.getX()));
        contour.setLeftPoint((short) Math.min(contour.getLeftPoint(), point.getY()));
    }

    public static void addContourAndUpdate(TextRow textRow, Contour contour) {
        textRow.getContours().add(contour);
        textRow.setTopPoint((short) Math.min(textRow.getTopPoint(), contour.getTopPoint()));
        textRow.setRightPoint((short) Math.max(textRow.getRightPoint(), contour.getRightPoint()));
        textRow.setBottomPoint((short) Math.max(textRow.getBottomPoint(), contour.getBottomPoint()));
        textRow.setLeftPoint((short) Math.min(textRow.getLeftPoint(), contour.getLeftPoint()));
    }

    public static void addTextRowAndUpdate(TextAdapter textAdapter, TextRow textRow) {
        textAdapter.getRows().add(textRow);
        Contour lastContour = null;
        Map<Integer, Integer> distanceBetweenContours = textAdapter.getDistanceBetweenContours();
        for (Contour contour : textRow.getContours()) {
            if (lastContour != null) {
                int distance = contour.getLeftPoint() - lastContour.getRightPoint();
                if (distanceBetweenContours.get(distance) == null) {
                    distanceBetweenContours.put(distance, 1);
                } else {
                    distanceBetweenContours.put(distance, distanceBetweenContours.get(distance) + 1);
                }
            }
            lastContour = contour;
        }
    }
}
