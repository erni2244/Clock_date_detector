package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

import static java.lang.Math.abs;
import static java.lang.StrictMath.round;
import static org.opencv.imgproc.Imgproc.*;

public class Picture_transform {

    private String file_patch;
    private Imgcodecs imageCodecs=new Imgcodecs();
    private Mat matrix;
    private Mat mat;

    public WritableImage writableImage;

    public Picture_transform(String file_patch) {
        this.file_patch=file_patch;
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        load_file_to_matrix();

    }

    private void ShowImage(Mat image)
    {

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg",  image, matOfByte);
        //Storing the encoded Mat in a byte array
        byte[] byteArray = matOfByte.toArray();
        //Displaying the image
        InputStream in = new ByteArrayInputStream(byteArray);
        BufferedImage bufImage = null;
        try {
            bufImage = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        writableImage = SwingFXUtils.toFXImage(bufImage, null);
    }


    private void load_file_to_matrix(){
        try{
            matrix = new Picture_cut(imageCodecs.imread(file_patch)).matrix;
            ShowImage(matrix);

            System.out.println("New image width: "+matrix.cols()+" height: "+matrix.rows());
            mat = new Mat();//imageCodecs.imread(file_patch);
            System.out.println("Image Loaded");
        }catch (Exception e){
            System.out.println("Error" + e.getMessage());
        }
    }

    public boolean check_matrix(){
        return matrix!=null;
    }


    public int[] read_date(){
        if(check_matrix()){
            polar_transform();
            dilate_transform();
            inverse_binary_threshold();
            sobel_transform();
            System.out.println("transform done");
            return czytaj_godz();
        }
        return new int[]{0,0};
    }

    private void polar_transform(){
        Point point =new Point(round(matrix.width()/2),round(matrix.height()/2));
        Size size=new Size(matrix.width(),matrix.height());
        double maxRadius = 0.8*Math.min(point.x,point.y);
        try {
            warpPolar(matrix, mat, size, point,maxRadius,WARP_POLAR_LINEAR);
            save_image();
        }catch (Exception e){
            System.out.println("Error polar transform");
        }
    }

    private void dilate_transform(){
        Mat kernel=Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(100, 1));
        try {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
            Imgproc.dilate(mat, mat, kernel);
            save_image();
        }catch (Exception e){
            System.out.println("Error dilate transform");
        }
    }

    private void inverse_binary_threshold(){
        try {
            Imgproc.threshold(mat, mat, 90, 255, THRESH_BINARY_INV);
            //Imgproc.adaptiveThreshold(mat,mat,255,ADAPTIVE_THRESH_GAUSSIAN_C,THRESH_BINARY_INV,3,3);
            save_image();
        }catch (Exception e){
            System.out.println("Error inverse binary threshold");
        }
    }

    private void sobel_transform(){
        try {
            Imgproc.Sobel(mat,mat,-1,0,1);
            save_image();
        }catch (Exception e){
            System.out.println("Error Sobel transform");
        }
    }

    private int[] read_lines(){
        int x_size=mat.cols();
        int y_size=mat.rows();
        int flaga=0;
        int[] wskazowki = new int[10];  //dla bezpieczenstwa jest wiecej bo powino byc 3
        int nr_wskazowki=0;
        double poczatek_przeszukiwania =0.4;    //w procetach

        //znajduje gdzie sa wskazowki
            for(int j=0;j<y_size;j++){
                if(mat.get(j, (int)(x_size*poczatek_przeszukiwania))[0]!=0){
                    if(flaga==0){
                        wskazowki[nr_wskazowki]=j;
                        nr_wskazowki++;
                        flaga=1;
                    }
                }else{flaga=0;}
            }


        int []dlug={0,0,0,0,0,0,0,0,0,0};
        int szerokosc_wskazowki=5;
        int nr=0;
        for(int i=0;i<nr_wskazowki;i++) {
            for (int j = (int)(x_size*poczatek_przeszukiwania); j < x_size-szerokosc_wskazowki; j++) {
                    for (int z = -szerokosc_wskazowki; z < szerokosc_wskazowki; z++) {
                        if (mat.get(abs(wskazowki[nr]+z), j)[0] != 0) {
                            dlug[nr]++;
                            break;
                        }
                }
            }
            nr++;
        }

        //------------------------------------------
        int[][] tab_to_sort=new int[10][2];
        for(int i=0;i<10;i++){
            tab_to_sort[i][0]=wskazowki[i];
            tab_to_sort[i][1]=dlug[i];
        }
        Arrays.sort(tab_to_sort, new Comparator<int[]>() {
            @Override
            public int compare(int[] t1, int[] t2) {
                Integer itemIdOne = t1[1];
                Integer itemIdTwo = t2[1];
                return itemIdOne.compareTo(itemIdTwo);
            }
        });
        //-----------------------------------------------
        int[] odp=new int[2];
        if(nr_wskazowki==1) {
            odp[0] = tab_to_sort[9][0];     //takie same bo się nakladaja
            odp[1] = tab_to_sort[9][0];
        }else if(nr_wskazowki>2){
            odp[0]=tab_to_sort[9][0];       //najdlozsza
            odp[1]=tab_to_sort[7][0];       //trzecia od najdluzszej
        }else {
            odp[0]=tab_to_sort[9][0];       //najdlozsza
            odp[1]=tab_to_sort[8][0];       //druga po najdluzszej
        }
        return odp;
    }

    private int[] czytaj_godz(){
        try {
            int[] io=read_lines();
            double h=(double) io[1]/mat.rows();
            h=12*h+3;
            if(h>12)
                h-=12;

            double m=(double)io[0]/mat.rows();
            m=60*m+15;
            if(m>60)
                m-=60;

            //System.out.println("dodz= "+(int)h+":"+(int)m);

            return new int[]{(int) h,(int) m};
        }catch (Exception e){
            System.out.println("Error read date");
        }
        return new int[]{0,0};

    }




    private void save_image(){
        String string = System.getProperty("user.home") + "\\Desktop\\zegary\\odp.jpg";
        System.out.println(string);

        try {
            imageCodecs.imwrite(string, mat);
        }catch (Exception e){
            System.out.println("Error save image");
        }
    }


}
