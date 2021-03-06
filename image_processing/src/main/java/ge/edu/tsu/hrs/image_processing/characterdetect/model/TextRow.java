package ge.edu.tsu.hrs.image_processing.characterdetect.model;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class TextRow {

    private short topPoint = Short.MAX_VALUE;

    private short rightPoint = -1;

    private short bottomPoint = -1;

    private short leftPoint = Short.MAX_VALUE;

    private Queue<Contour> contours = new PriorityQueue<>(new Comparator<Contour>() {
        @Override
        public int compare(Contour o1, Contour o2) {
            return Short.compare(o1.getLeftPoint(), o2.getLeftPoint());
        }
    });

    public short getTopPoint() {
        return topPoint;
    }

    public void setTopPoint(short topPoint) {
        this.topPoint = topPoint;
    }

    public short getRightPoint() {
        return rightPoint;
    }

    public void setRightPoint(short rightPoint) {
        this.rightPoint = rightPoint;
    }

    public short getBottomPoint() {
        return bottomPoint;
    }

    public void setBottomPoint(short bottomPoint) {
        this.bottomPoint = bottomPoint;
    }

    public short getLeftPoint() {
        return leftPoint;
    }

    public void setLeftPoint(short leftPoint) {
        this.leftPoint = leftPoint;
    }

    public Queue<Contour> getContours() {
        return contours;
    }

    public void setContours(Queue<Contour> contours) {
        this.contours = contours;
    }
}
