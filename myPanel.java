import javax.swing.*;
import java.awt.*;

public class myPanel extends JPanel {
    myPanel() {

        this.setPreferredSize(new Dimension(1100, 750));
        this.setLayout(new GridLayout(0, 2));

    }

    public void paint(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        int tempRED;
        int tempGREEN;
        int tempBLUE;
        int[] redNum = new int[256];
        int[] greenNum = new int[256];
        int[] blueNum = new int[256];

//        g.drawLine(10,10,10,210);
//        g.drawLine(290,210,10,210);
//        g.drawLine(10,220,10,420);
//        g.drawLine(10,420,290,420);
//        g.drawLine(10,430,10,680);
//        g.drawLine(10,680,290,680);

        for (int y = 0; y < editPNG.rgbHeight; y++) {
            for (int x = 0; x < editPNG.rgbWidth; x++) {
                tempRED=((editPNG.imageRGB[y][x] >> 16)& 0xFF);
                tempGREEN=((editPNG.imageRGB[y][x] >> 8)& 0xFF);
                tempBLUE=((editPNG.imageRGB[y][x])& 0xFF);

//                tempRED = editPNG.Rcolor[y][x];
//                tempGREEN = editPNG.Gcolor[y][x];
//                tempBLUE = editPNG.Bcolor[y][x];

                redNum[tempRED]++;
                greenNum[tempGREEN]++;
                blueNum[tempBLUE]++;

                g.setColor(new Color(tempRED, tempGREEN, tempBLUE));
                g.drawRect(x + 300, y, 1, 1);


            }
        }
    }
}

