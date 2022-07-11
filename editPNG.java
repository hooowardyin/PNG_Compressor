import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Vector;
import huffman.InternalNode;
import static huffman.HuffmanCompress.doHuffmanCompress;
import static huffman.huffDe.doHuffmanDe;


public class editPNG implements ActionListener {

    private JFrame frame;
    private JPanel panel;
    private JPanel panel1;
    private JButton button;
    private JButton button1;
    private JButton buttonExit;
    private JLabel label;

    myPanel imagePanel;
    ditheringPanel dithered;
    public static int[][] imageRGB;
    public static int[][] Rcolor;
    public static int[][] Gcolor;
    public static int[][] Bcolor;

    public static double[][] Y;
    public static double[][] U;
    public static double[][] V;

    public static double[][] DCTMatrix;
    public static double[][] transposeMatrix;
    public static int[][] afterDCT;

    public static int[][] UafterDCT;
    public static int[][] VafterDCT;

    public static double[][]quantizationTable={
            {1,1,2,4,8,16,32,64},
            {1,1,2,4,8,16,32,64},
            {2,2,2,4,8,16,32,64},
            {4,4,4,4,8,16,32,64},
            {8,8,8,8,8,16,32,64},
            {16,16,16,16,16,16,32,64},
            {32,32,32,32,32,32,32,64},
            {64,64,64,64,64,64,64,64},

//            {1,1,1,1,1,1,1,1},
//            {1,1,1,1,1,1,1,1},
//            {1,1,1,1,1,1,1,1},
//            {1,1,1,1,1,1,1,1},
//            {1,1,1,1,1,1,1,1},
//            {1,1,1,1,1,1,1,1},
//            {1,1,1,1,1,1,1,1},
//            {1,1,1,1,1,1,1,1},
    };

    public static int[][] afterQntz;

    public static int[][] UafterQntz;
    public static int[][] VafterQntz;


    // for DC values
    public static int[] DPCM;

    public static int[] uDPCM;
    public static int[] vDPCM;




    // for AC values
    public static Vector<Integer> RLC;

    public static Vector<Integer> uRLC;
    public static Vector<Integer> vRLC;






    public static int rgbWidth;
    public static int rgbHeight;

    public editPNG() {



        frame = new JFrame();

        panel = new JPanel();
        panel1= new JPanel();

//        imagePanel= new myPanel();
        dithered = new ditheringPanel();

        button = new JButton("open file");
        button.setSize(50,50);
        button.addActionListener( this);

        button1 = new JButton("show the decompressed image");
        button1.setSize(50,50);
        button1.addActionListener( this);

        buttonExit = new JButton("Exit");
        buttonExit.setSize(50,50);
        buttonExit.addActionListener( this);



        label = new JLabel("choose a .PNG file");


        panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        panel.setLayout(new GridLayout(0,1));
        panel.add(button);
        panel.add(label);

        panel1.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        panel1.setLayout(new GridLayout(0,1));
        panel1.add(button1);
        panel1.add(buttonExit);




        frame.setLocationRelativeTo(null);
        frame.add(panel,BorderLayout.NORTH);
        frame.add(panel1,BorderLayout.SOUTH);

        frame.setPreferredSize(new Dimension(1850,900));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("image compress and depress");
        frame.pack();
        frame.setVisible(true);


    }




    public static void main(String args[]) throws IOException {

        new editPNG();

    }

    public static void buildDCT(){
        DCTMatrix= new double[8][8];
        transposeMatrix = new double[8][8];
        double a1= Math.sqrt(0.125);
        double a2= Math.sqrt(0.25);


        for(double i=0 ; i<8; i++){
            for(double j=0; j<8; j++){



                if(i==0){
                    DCTMatrix[(int) i][(int) j] = a1 * Math.cos( (2 * j + 1) * i * 3.14159 / (2 * 8) );
                }

                else{
                    DCTMatrix[(int) i][(int) j] = a2 * Math.cos((2 * j + 1) * i * 3.14159 / (2 * 8));
                }

                transposeMatrix[(int) j][(int) i] = DCTMatrix[(int) i][(int) j];

            }
        }
    }

    // DCT
    public static void doDCT(double[][] inputM , int[][] outputM ,int startX, int startY){
        double[][] temp = new double[8][8];
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                for(int k=0; k<8; k++){
                    temp[i][j] = temp[i][j] + DCTMatrix[i][k] * inputM[k + startX][j + startY];
                }

            }
        }

        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                for(int k=0; k<8; k++){
                    outputM[i + startX][j + startY] = (int) (outputM[i + startX][j + startY] + temp[i][k] * transposeMatrix[k][j]);
                }

            }
        }

    }

    // inverse DCT
    public static void invDCT(int[][] inputM ,double[][] outputM ,int startX, int startY){
        double[][] temp = new double[8][8];
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                for(int k=0; k<8; k++){
                    temp[i][j] = temp[i][j] + transposeMatrix[i][k] * (double)inputM[k + startX][j + startY];
                }

            }
        }

        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                for(int k=0; k<8; k++){
                    outputM[i + startX][j + startY] = outputM[i + startX][j + startY] + temp[i][k] * DCTMatrix[k][j];
                }

            }
        }

    }

    // quantization

    public static void quantize(int[][] inputM, int[][] outputM ,int starti, int startj){
        for(int i = 0; i< 8; i++){
            for (int j=0; j<8; j++){
                outputM[i + starti][j + startj] = (int) (inputM[i + starti][j + startj] / quantizationTable[i][j]);
            }
        }
    }

    // inverse quantization
    public static void RevQuantize(int[][] inputM, int[][] outputM,  int starti, int startj){
        for(int i = 0; i< 8; i++){
            for (int j=0; j<8; j++){
                outputM[i + starti][j + startj] = (int) (inputM[i + starti][j + startj] * quantizationTable[i][j]);
            }
        }
    }

    // for RLC use
    public static int zeroCount = 0;

    public static void record(int[][] matrix, Vector<Integer> savedRLC, int starty, int startx, boolean end){

        int value = (int) matrix[starty][startx];

        if(end){

            savedRLC.add(zeroCount);
            savedRLC.add(value);
            zeroCount = 0;

        }

        else{
            if(value != 0){
                savedRLC.add(zeroCount);
                savedRLC.add(value);
                zeroCount = 0;
            }
            else{
                zeroCount++;
            }

        }
    }


    // RLC path

    public static void runLength(int[][] matrix, Vector<Integer> savedRLC, int starty, int startx){

        int y=0;
        int x=0;
        int dy , dx;

        x++;
        record(matrix, savedRLC,starty + y,startx + x, false);
        dy = 1;
        dx = -1;

        while(true){

            y+= dy;
            x+= dx;
            record(matrix, savedRLC,starty + y,startx + x, false);

            if(y == 7){
                break;
            }

            if(x == 0){
                y++;
                record(matrix, savedRLC,starty + y,startx + x, false);
                dy = -1;
                dx =  1;

            }
            else if(y == 0){
                x++;
                record(matrix, savedRLC,starty + y,startx + x, false);
                dy = 1;
                dx = -1;

            }
        }

        x++;
        record(matrix, savedRLC,starty + y,startx + x, false);
        dy = -1;
        dx = 1;

        while(true){



            y+= dy;
            x+= dx;
            record(matrix, savedRLC,starty + y,startx + x, false);

            if(x == 7 && y != 7){
                y++;
                record(matrix, savedRLC,starty + y,startx + x, false);
                dy = 1;
                dx = -1;
            }
            else if(y == 7 && x != 7){

                x++;
                if( x == 7){
                    record(matrix, savedRLC,starty + y,startx + x, true);
                    return;
                }
                record(matrix, savedRLC,starty + y,startx + x, false);
                dy = -1;
                dx = 1;
            }
        }


    }

    //decode RLC

    public static int iterator = 0;
    public static void decodeRLC(int[][] matrix, Vector<Integer> inputRLC, int starty, int startx){

        int y=0;
        int x=0;
        int dy , dx;

        dy = 1;
        dx = -1;

        x++;
        if(inputRLC.get(iterator) == 0){
            iterator++;
            matrix[starty + y][startx + x] = inputRLC.get(iterator);
            iterator++;

        }
        else {
            matrix[starty + y][startx + x] = 0;
            inputRLC.set(iterator,inputRLC.get(iterator)-1);

        }
        y+= dy;
        x+= dx;


        while(true){

            if(y == 7){
                if(inputRLC.get(iterator) == 0){
                    iterator++;
                    matrix[starty + y][startx + x] = inputRLC.get(iterator);
                    iterator++;

                    x++;
                }
                else if (inputRLC.get(iterator) != 0){
                    matrix[starty + y][startx + x] = 0;
                    inputRLC.set(iterator,inputRLC.get(iterator)-1);
                    x++;
                }

                dy = -1;
                dx = 1;
                break;
            }

            if(x == 0){
                if(inputRLC.get(iterator) == 0){
                    iterator++;
                    matrix[starty + y][startx + x] = inputRLC.get(iterator);
                    iterator++;

                    y++;
                }
                else if (inputRLC.get(iterator) != 0){
                    matrix[starty + y][startx + x] = 0;
                    inputRLC.set(iterator,inputRLC.get(iterator)-1);
                    y++;
                }

                dy = -1;
                dx =  1;

            }
            else if(y == 0){
                if(inputRLC.get(iterator) == 0){
                    iterator++;
                    matrix[starty + y][startx + x] = inputRLC.get(iterator);
                    iterator++;

                    x++;
                }
                else if (inputRLC.get(iterator) != 0){
                    matrix[starty + y][startx + x] = 0;
                    inputRLC.set(iterator,inputRLC.get(iterator)-1);

                    x++;
                }


                dy = 1;
                dx = -1;

            }
            else {
                if(inputRLC.get(iterator) == 0){
                    iterator++;
                    matrix[starty + y][startx + x] = inputRLC.get(iterator);
                    iterator++;

                    y+= dy;
                    x+= dx;
                }
                else if (inputRLC.get(iterator) != 0){
                    matrix[starty + y][startx + x] = 0;
                    inputRLC.set(iterator,inputRLC.get(iterator)-1);
                    y+= dy;
                    x+= dx;
                }
            }
        }



        // second half matrix
        while(true){

            if(x == 7 && y != 7){
                if(inputRLC.get(iterator) == 0){
                    iterator++;
                    matrix[starty + y][startx + x] = inputRLC.get(iterator);
                    iterator++;

                }
                else if (inputRLC.get(iterator) != 0){
                    matrix[starty + y][startx + x] = 0;
                    inputRLC.set(iterator,inputRLC.get(iterator)-1);

                }
                y++;
                dy = 1;
                dx = -1;
            }
            else if(y == 7 && x != 7){
                if(inputRLC.get(iterator) == 0){
                    iterator++;
                    matrix[starty + y][startx + x] = inputRLC.get(iterator);
                    iterator++;

                }
                else if (inputRLC.get(iterator) != 0){
                    matrix[starty + y][startx + x] = 0;
                    inputRLC.set(iterator,inputRLC.get(iterator)-1);

                }

                x++;

                dy = -1;
                dx = 1;
            }
            else if(y == 7 && x == 7){

                iterator++;
                matrix[starty + y][startx + x] = inputRLC.get(iterator);
                iterator++;

                return;
            }
            else {
                if(inputRLC.get(iterator) == 0){
                    iterator++;
                    matrix[starty + y][startx + x] = inputRLC.get(iterator);
                    iterator++;

                    y+= dy;
                    x+= dx;
                }
                else if (inputRLC.get(iterator) != 0){
                    matrix[starty + y][startx + x] = 0;
                    inputRLC.set(iterator,inputRLC.get(iterator)-1);
                    y+= dy;
                    x+= dx;
                }
            }

        }


    }








    public static void readPNG(String filePath) throws IOException {


        BufferedImage png = ImageIO.read(new File(filePath));


        int width = png.getWidth();
        int height = png.getHeight();

        rgbWidth = width;
        rgbHeight = height;

        System.out.println("width is: "+ width +" height is: " + height);

        int[][] pixels = new int[height][width];
        Rcolor = new int[height][width];
        Gcolor = new int[height][width];
        Bcolor = new int[height][width];

        Y = new double[height][width];
        U = new double[height][width];
        V = new double[height][width];


        // read the RGB
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y][x]=png.getRGB(x,y);

            }

        }




        imageRGB=pixels;

        // separate R G B
        // get Y U V
        for(int y = 0; y<rgbHeight; y++){
            for(int x = 0; x<rgbWidth; x++){
                Rcolor[y][x]=((imageRGB[y][x] >> 16)& 0xFF);
                Gcolor[y][x]=((imageRGB[y][x] >> 8)& 0xFF);
                Bcolor[y][x]=((imageRGB[y][x])& 0xFF);

                Y[y][x]= (0.3*Rcolor[y][x] + 0.587*Gcolor[y][x] + 0.114*Bcolor[y][x]);

                U[y][x] = (-0.3 * Rcolor[y][x] - 0.587 * Gcolor[y][x] + 0.886 * Bcolor[y][x]) -128;
                V[y][x] = (0.701 * Rcolor[y][x] - 0.587 * Gcolor[y][x] - 0.144 * Bcolor[y][x]) -128;

            }
        }

        // cast Y to integer
        int[][] intY = new int[height][width];

        for(int y = 0; y< height; y++){
            for(int x = 0 ; x< width; x++)
            intY[y][x] = (int)(Y[y][x]);
        }

        int[] yToSave = new int[height*width];
        int track=0;
        for(int y = 0; y< height; y++){
            for(int x = 0 ; x< width; x++){
                yToSave[track] = intY[y][x];
                track++;
            }
        }







        // test
//        for(int y = 0; y<8; y++){
//            for(int x=0; x<8; x++){
//                System.out.println(Y[y][x] + " ");
//            }
//            System.out.println();
//        }


        // build DCT matrix
        buildDCT();

        afterDCT = new int[height][width];
        UafterDCT = new int[height][width];
        VafterDCT = new int[height][width];

        // do DCT transform

        // for Y
//        for(int y = 0; y<height-7; y+=8){
//            for(int x = 0; x<width-7; x+=8){
//                doDCT(Y,afterDCT,y,x);
//            }
//        }

        // for U
        for(int y = 0; y<height-7; y+=8){
            for(int x = 0; x<width-7; x+=8){
                doDCT(U,UafterDCT,y,x);
            }
        }
        // for V
        for(int y = 0; y<height-7; y+=8){
            for(int x = 0; x<width-7; x+=8){
                doDCT(V,VafterDCT,y,x);
            }
        }





        //build quantization
        // for Y
//        afterQntz = new int[height][width];
//
//        for(int y=0 ; y<height-7; y+= 8){
//            for(int x = 0; x<width-7; x+= 8){
//                quantize(afterDCT,afterQntz, y, x);
//            }
//        }

        // for U
        UafterQntz = new int[height][width];
        for(int y=0 ; y<height-7; y+= 8){
            for(int x = 0; x<width-7; x+= 8){
                quantize(UafterDCT,UafterQntz, y, x);
            }
        }

        // for U
        VafterQntz = new int[height][width];
        for(int y=0 ; y<height-7; y+= 8){
            for(int x = 0; x<width-7; x+= 8){
                quantize(VafterDCT, VafterQntz, y, x);
            }
        }

        //test
//        for(int y = 0; y<8; y++){
//            for(int x=0; x<8; x++){
//                System.out.print(UafterDCT[y][x] + " ");
//            }
//            System.out.println();
//        }


        // store CD DPCM values for channel Y

//        DPCM = new int[height*width/64];
        int count=0;
//        for (int y = 0; y < height-7; y+= 8 ){
//            for(int x = 0; x < width-7 ; x+= 8){
//                DPCM[count] = (int) afterQntz[y][x];
//                count++;
//            }
//        }

        // DPCM for U

        uDPCM = new int[height*width/64];
        count=0;
        for (int y = 0; y < height-7; y+= 8 ){
            for(int x = 0; x < width-7 ; x+= 8){
                uDPCM[count] = (int) UafterQntz[y][x];
                count++;
            }
        }

        // DPCM for V

        vDPCM = new int[height*width/64];
        count=0;
        for (int y = 0; y < height-7; y+= 8 ){
            for(int x = 0; x < width-7 ; x+= 8){
                vDPCM[count] = (int) VafterQntz[y][x];
                count++;
            }
        }





        // zigzag scan

        RLC = new Vector<>();

        uRLC= new Vector<>();
        vRLC= new Vector<>();

        // for Y
//        for(int y = 0; y<height-7 ; y+= 8){
//            for(int x = 0; x<width-7 ; x+= 8){
//                runLength(afterQntz,RLC,y,x);
//            }
//        }

        // for U
        for(int y = 0; y<height-7 ; y+= 8){
            for(int x = 0; x<width-7 ; x+= 8){
                runLength(UafterQntz,uRLC,y,x);
            }
        }

        // for V
        for(int y = 0; y<height-7 ; y+= 8){
            for(int x = 0; x<width-7 ; x+= 8){
                runLength(VafterQntz,vRLC,y,x);
            }
        }

        // write to file




        FileOutputStream myPen = new FileOutputStream("interMedia.txt");

        // for Y

        for(int i = 0; i < yToSave.length; i++){
            myPen.write(ByteBuffer.allocate(4).putInt(yToSave[i]).array());
        }

//        for(int i = 0 ; i < DPCM.length; i++){
//            myPen.write(ByteBuffer.allocate(4).putInt(DPCM[i]).array());
//        }
//        for(int i = 0; i< RLC.size(); i++){
//            myPen.write(ByteBuffer.allocate(4).putInt(RLC.get(i)).array());
//        }



        // for U
        for(int i = 0 ; i < uDPCM.length; i++){
            myPen.write(ByteBuffer.allocate(4).putInt(uDPCM[i]).array());
        }
        for(int i = 0; i< uRLC.size(); i++){
            myPen.write(ByteBuffer.allocate(4).putInt(uRLC.get(i)).array());
        }

        // for V
        for(int i = 0 ; i < vDPCM.length; i++){
            myPen.write(ByteBuffer.allocate(4).putInt(vDPCM[i]).array());
        }
        for(int i = 0; i< vRLC.size(); i++){
            myPen.write(ByteBuffer.allocate(4).putInt(vRLC.get(i)).array());
        }

        myPen.close();

        // entropy coding

        doHuffmanCompress("interMedia.txt", "afterCompressed.txt");
        doHuffmanDe("afterCompressed.txt","deCompressed.txt");



        // read from file

        FileInputStream reader = new FileInputStream("deCompressed.txt");
        byte[] buffer4 = new byte[4];
        int[] readformY = new int[yToSave.length];

//        // read DPCM for Y
//        for (int i = 0; i< DPCM.length; i++){
//            reader.read(buffer4);
//            readDPCM[i] = ByteBuffer.wrap(buffer4).getInt();
//        }
//
//        // read AC RLC for Y
//
//        Vector<Integer> readRLC = new Vector<>();
//        for (int i = 0; i< RLC.size(); i++){
//            reader.read(buffer4);
//            readRLC.add(ByteBuffer.wrap(buffer4).getInt());
//        }

        //read Y channel
        for (int i = 0; i< yToSave.length; i++){
            reader.read(buffer4);
            readformY[i] = ByteBuffer.wrap(buffer4).getInt();
        }

        int[][] newY = new int[height][width];
        track=0;
        for(int y = 0; y< height; y++){
            for(int x = 0 ; x< width; x++)
                newY[y][x] = readformY[track];
            track++;
        }






        // read DPCM for U
        int[] UreadDPCM = new int[uDPCM.length];

        for (int i = 0; i< uDPCM.length; i++){
            reader.read(buffer4);
            UreadDPCM[i] = ByteBuffer.wrap(buffer4).getInt();
        }

        // read AC RLC for U

        Vector<Integer> UreadRLC = new Vector<>();
        for (int i = 0; i< uRLC.size(); i++){
            reader.read(buffer4);
            UreadRLC.add(ByteBuffer.wrap(buffer4).getInt());
        }

        // read DPCM for V
        int[] VreadDPCM = new int[vDPCM.length];

        for (int i = 0; i< vDPCM.length; i++){
            reader.read(buffer4);
            VreadDPCM[i] = ByteBuffer.wrap(buffer4).getInt();
        }

        // read AC RLC for V

        Vector<Integer> VreadRLC = new Vector<>();
        for (int i = 0; i< vRLC.size(); i++){
            reader.read(buffer4);
            VreadRLC.add(ByteBuffer.wrap(buffer4).getInt());
        }


        // decode DPCM

//        int[][] yDCT = new int[height][width];
        int[][] uDCT = new int[height][width];
        int[][] vDCT = new int[height][width];



        // decode AC RLC

        // for Y U V
        iterator = 0;
//        for(int y = 0; y<height-7; y+= 8){
//            for(int x = 0; x<width-7; x+= 8){
//                decodeRLC(yDCT,readRLC,y,x);
//            }
//        }
        iterator = 0;
        for(int y = 0; y<height-7; y+= 8){
            for(int x = 0; x<width-7; x+= 8){
                decodeRLC(uDCT,UreadRLC,y,x);
            }
        }
        iterator = 0;
        for(int y = 0; y<height-7; y+= 8){
            for(int x = 0; x<width-7; x+= 8){
                decodeRLC(vDCT,VreadRLC,y,x);
            }
        }

        // for Y U V
        int decodeCount = 0;
        for(int y = 0; y<height-7; y+= 8){
            for(int x = 0; x<width-7; x+= 8){
//                yDCT[y][x] = readDPCM[decodeCount];
                uDCT[y][x] = UreadDPCM[decodeCount];
                vDCT[y][x] = VreadDPCM[decodeCount];
                decodeCount++;
            }
        }


        // inverse quantization

        int[][] yDeQntz = new int[height][width];
        int[][] uDeQntz = new int[height][width];
        int[][] vDeQntz = new int[height][width];

        for(int y=0 ; y<height-7; y+= 8){
            for(int x = 0; x<width-7; x+= 8){
//                RevQuantize(yDCT,yDCT, y, x);
                RevQuantize(uDCT,uDCT, y, x);
                RevQuantize(vDCT,vDCT, y, x);
            }
        }

        // inverse DCT

        double[][] finalY = new double[height][width];
        double[][] finalU = new double[height][width];
        double[][] finalV = new double[height][width];

        for(int y = 0; y<height-7; y+= 8){
            for(int x = 0; x < width-7; x+= 8){
//                invDCT(yDCT,finalY,y,x);
                invDCT(uDCT,finalU,y,x);
                invDCT(vDCT,finalV,y,x);
            }
        }

        for(int y = 0; y<height; y++) {
            for (int x = 0; x < width; x++) {
//                finalY[y][x] = finalY[y][x] + 128;
                finalU[y][x] = finalU[y][x] + 128;
                finalV[y][x] = finalV[y][x] + 128;
            }
        }

        // convert YUV back to RGB

        int[][] finalR = new int[height][width];
        int[][] finalG = new int[height][width];
        int[][] finalB = new int[height][width];

        for(int y = 0; y<height; y++) {
            for (int x = 0; x < width; x++) {
                // R
                finalR[y][x] = (int) (newY[y][x]+finalV[y][x]);
                if(finalR[y][x]>255){
                    finalR[y][x]=255;
                }
                if(finalR[y][x]<0){
                    finalR[y][x]=0;
                }
                //G
                finalG[y][x] = (int) (newY[y][x] - 0.1942*finalU[y][x] - 0.509*finalV[y][x]);
                if(finalG[y][x]>255){
                    finalG[y][x]=255;
                }
                if(finalG[y][x]<0){
                    finalG[y][x]=0;
                }
                //B
                finalB[y][x] = (int) (newY[y][x] +finalU[y][x]);
                if(finalB[y][x]>255){
                    finalB[y][x]=255;
                }
                if(finalB[y][x]<0){
                    finalB[y][x]=0;
                }

            }
        }

        Rcolor=finalR;
        Gcolor=finalG;
        Bcolor=finalB;












//        System.out.println("got all RGBs");


        //test//
//        for (int y = 0; y < 2; y++) {
//            for (int x = 0; x < 2; x++) {
//                System.out.println("RED is: " + ((pixels[y][x] >> 16)& 0xFF));
//                System.out.println("GREEN is: " + (pixels[y][x] >> 8 & 0xFF));
//                System.out.println("BLUE is: " + (pixels[y][x] & 0xFF));
//                System.out.println();
//            }
//
//        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == button){
            try {
                JFileChooser fileChooser = new JFileChooser();
                int success = fileChooser.showOpenDialog(null);
                File file = null;
                if (success == JFileChooser.APPROVE_OPTION) {
                    file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                    System.out.println(file);
                    readPNG(String.valueOf(file));

                    imagePanel= new myPanel();
                    imagePanel.removeAll();
                    imagePanel.revalidate();
                    imagePanel.repaint();

                    frame.remove(dithered);

                    dithered.removeAll();

                    frame.add(imagePanel,BorderLayout.WEST);
                    frame.invalidate();
                    frame.validate();
                    frame.repaint();
                    frame.pack();
                    frame.setVisible(true);

                }else{
                    System.out.println("cannot open the file");
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        else if(e.getSource() == button1){
            dithered.removeAll();
            dithered.revalidate();
            dithered.repaint();

//            frame.add(imagePanel,BorderLayout.WEST);
            frame.invalidate();
            frame.validate();
            frame.repaint();

            frame.add(dithered,BorderLayout.EAST);
            frame.pack();

        }

        else if(e.getSource() == buttonExit){
            System.exit(0);
        }
    }
}
