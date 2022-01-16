package share.data;

import javax.swing.*;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class ImagePanelTest {
    public static void main(String[] args) {
        JFrame ui = new JFrame();
        ui.add(new ImagePanel(50, 20, 20));
        ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ui.setSize(630, 680);
        ui.setLayout(new FlowLayout());
        ui.setResizable(true);

        ui.pack();
        ui.setVisible(true);
        System.out.println("stop");
    }
}