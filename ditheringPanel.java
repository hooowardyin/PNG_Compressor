import javax.swing.*;
import java.awt.*;

public class ditheringPanel extends JPanel {


    ditheringPanel(){
        this.setPreferredSize(new Dimension(750,750));
        this.setLayout(new GridLayout(0,2));



    }
    public void paint (Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;


        for(int y =0; y<editPNG.rgbHeight;y++){
            for (int x =0; x<editPNG.rgbWidth;x++){

                g.setColor(new Color(editPNG.Rcolor[y][x], editPNG.Gcolor[y][x], editPNG.Bcolor[y][x]));
                g.drawRect(x,y,1,1);


            }
        }

    }
}
