package share.data;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ImageSlicerTest {
    BufferedImage getResImage() throws IOException {
        BufferedImage img = ImageIO.read(ClassLoader.getSystemResourceAsStream("testImage.png"));

        return img;
    }

//    @Test
//    void resize() throws IOException, InterruptedException {
//        display(ImageSlicer.resize(getResImage()), "full");
//
//        Thread.sleep(5000000);
//    }
//
//    @Test
//    void slice() throws IOException, InterruptedException {
//        BufferedImage[] slice = ImageSlicer.slice(ImageSlicer.resize(getResImage()));
//        System.out.println(slice.length);
//        for (int i = 0; i < 4; i++) {
//            display(slice[i], String.valueOf(i));
//        }
//        Thread.sleep(5000000);
//    }
//
//    public void display(BufferedImage image, String name) {
//        JFrame frame = new JFrame();
//        JLabel label = new JLabel();
//
//
//        frame.setTitle(name);
//        frame.setSize(image.getWidth(), image.getHeight());
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        label.setIcon(new ImageIcon(image));
//        frame.getContentPane().add(label, BorderLayout.CENTER);
//        frame.setLocationRelativeTo(null);
//        frame.pack();
//        frame.setVisible(true);
//        label.setIcon(new ImageIcon(image));
//    }
}