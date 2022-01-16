package share.data;

import javax.swing.*;
import java.awt.*;

public class ImageBlock extends JPanel {
    private String imageName;
    private Image image;
    private final int ImageBlockIdx;

    public ImageBlock(int Index) {
        this.ImageBlockIdx = Index;
    }

    public int getImageBlockIdx() {
        return ImageBlockIdx;
    }

    public synchronized void setImageMsg(String ImageName, Image img) {
        this.imageName = ImageName;
        this.image = img;

        this.repaint();
    }

    public synchronized Image getImage() {
        return image;
    }

    public synchronized String getImageName() {
        return imageName;
    }

    public synchronized void clearImageMsg() {
        this.image = null;
    }

    public synchronized boolean isEmpty() {
        return this.image == null;
    }

    public void paintComponent(Graphics g) {
        if (this.image == null) {
            /* Paint the Background color if no image */
            super.paintComponent(g);
        } else {
            g.drawImage(this.image, 0, 0, null);
        }
    }
}