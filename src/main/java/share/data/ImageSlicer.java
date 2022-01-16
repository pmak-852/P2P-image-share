package share.data;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageSlicer {
    /* Break image into blocks */
    public final int pixel;
    public final int targetWidth, targetHeight;

    public final int numberOfCol, numberOfRow;

    public ImageSlicer(int pixelOfBlock, int newWidthPixel, int newHeightPixel) {
        this.pixel = pixelOfBlock;
        this.targetHeight = newHeightPixel;
        this.targetWidth = newWidthPixel;

        this.numberOfCol = newWidthPixel / pixelOfBlock;
        this.numberOfRow = newHeightPixel / pixelOfBlock;
    }

    public BufferedImage resize(BufferedImage img) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(img, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resized;

    }

    public BufferedImage[] slice(BufferedImage img) {
        BufferedImage[] sliced = new BufferedImage[numberOfCol * numberOfRow];

        for (int row = 0; row < numberOfRow; row++) {
            for (int col = 0; col < numberOfCol; col++) {
                BufferedImage temp = img.getSubimage(col * pixel, row * pixel, pixel, pixel);
                sliced[row * numberOfRow + col] = temp;
            }
        }

        return sliced;
    }

    public int getN() {
        return numberOfCol * numberOfRow;
    }
}
