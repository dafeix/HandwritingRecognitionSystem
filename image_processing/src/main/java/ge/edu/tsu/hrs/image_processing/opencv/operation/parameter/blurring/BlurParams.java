package ge.edu.tsu.hrs.image_processing.opencv.operation.parameter.blurring;

public class BlurParams implements BlurringParams {

    // ksize - Gaussian kernel size. ksize.width and ksize.height can differ but they
    // both must be positive and odd. Or, they can be zeros and then they are computed from sigma.
    private int kSizeWidth = 5;

    private int kSizeHeight = 5;

    public int getkSizeWidth() {
        return kSizeWidth;
    }

    public void setkSizeWidth(int kSizeWidth) {
        this.kSizeWidth = kSizeWidth;
    }

    public int getkSizeHeight() {
        return kSizeHeight;
    }

    public void setkSizeHeight(int kSizeHeight) {
        this.kSizeHeight = kSizeHeight;
    }
}
