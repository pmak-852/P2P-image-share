package share.data;

import share.message.P2PMessage;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ImagePanel extends JPanel {
    String ImageName;
    final List<ImageBlock> blocks = Collections.synchronizedList(new ArrayList<>());
    final Set<Integer> lockedIndex = Collections.synchronizedSet(new LinkedHashSet<>());
    final int pixel_size, numberOfRows, numberOfColumns;

    public ImagePanel(int pixelOfBlk, int numberOfRows, int numberOfColumns) {
        /* pixel here used for displaying only */
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        this.pixel_size = pixelOfBlk;

        this.setPreferredSize(new Dimension(numberOfRows * pixel_size, numberOfColumns * pixel_size));
        this.setLayout(new GridLayout(this.numberOfColumns, this.numberOfRows, 0, 0));
        for (int t = 0; t < numberOfRows * numberOfColumns; t++) {
            ImageBlock blk = new ImageBlock(t);
            blk.setSize(new Dimension(pixel_size, pixel_size));
            blk.setPreferredSize(new Dimension(pixel_size, pixel_size));

            int rc = ThreadLocalRandom.current().nextInt(0, 255);
            int gc = ThreadLocalRandom.current().nextInt(0, 255);
            int bc = ThreadLocalRandom.current().nextInt(0, 255);

            blk.setBackground(new Color(rc, gc, bc));
            blk.setVisible(true);

            this.blocks.add(blk);
            this.add(blk);
        }
    }

    public synchronized String getImageName() {
        return ImageName;
    }

    public synchronized void setImageName(String ImageName) {
        this.ImageName = ImageName;
    }

    public synchronized void setImageBlock(P2PMessage imageP2PMessage) {
        this.setImageBlock(imageP2PMessage.getImgName(), imageP2PMessage.getImageBlockN(), imageP2PMessage.getSerializedImg());
    }

    public synchronized void setImageBlock(String imgName, int idx, Image img) {
        if (this.getImageName().equalsIgnoreCase(imgName)) {
            this.blocks.get(idx).setImageMsg(imgName, img);
        }
    }

    public synchronized ImageBlock blockAvailable(int pos, String imageName) {
        ImageBlock block = this.blocks.get(pos);
        if (block.getImageName().equalsIgnoreCase(imageName) && block.getImage() != null) {
            return block;
        }
        return null;
    }

    public synchronized ImageBlock getNextEmptyBlockN() {
        for (ImageBlock cell : this.blocks) {
            if (cell.isEmpty() || cell.getImageName() == null || !cell.getImageName().equalsIgnoreCase(this.getImageName())) {
                if (!this.lockedIndex.contains(cell.getImageBlockIdx())) {
                    return cell;
                }
            }
        }
        return null;
    }

    public void lockImageBlock(int idx) {
        this.lockedIndex.add(idx);
    }

    public void releaseImageBlock(int idx) {
        this.lockedIndex.remove(idx);
    }
}
