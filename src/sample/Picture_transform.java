package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.StrictMath.round;
import static org.opencv.imgproc.Imgproc.*;

public class Picture_transform {

    private String file_patch;
    private Mat matrix;
    private Mat mat;
    private int thresh=90;

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
            matrix = new Picture_cut(Imgcodecs.imread(file_patch)).matrix;
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
            //odwroc_tlo();
            Imgcodecs.imwrite(System.getProperty("user.home") + "\\Desktop\\zegary\\odp1.jpg", matrix);
            polar_transform();
            Imgcodecs.imwrite(System.getProperty("user.home") + "\\Desktop\\zegary\\odp2.jpg", mat);
            dilate_transform();
            Imgcodecs.imwrite(System.getProperty("user.home") + "\\Desktop\\zegary\\odp3.jpg", mat);
            inverse_binary_threshold();
            Imgcodecs.imwrite(System.getProperty("user.home") + "\\Desktop\\zegary\\odp4.jpg", mat);
            sobel_transform();
            Imgcodecs.imwrite(System.getProperty("user.home") + "\\Desktop\\zegary\\odp5.jpg", mat);
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
            Imgproc.threshold(mat, mat, thresh, 255, THRESH_BINARY_INV);
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
        double poczatek_przeszukiwania =0.35;    //w procetach

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

    public void odwroc_tlo(){
        int max=0;
        int min =255;
        Mat m=matrix.clone();
        Imgproc.cvtColor(m,m, COLOR_RGB2GRAY);
        int y_center= m.rows()/2;
        int x_center= m.cols()/2;
        double zakres = 0.1; //w procetach od sierodka odchylenie
        double zakres_na_punkt_środpowy=0.5*zakres;
        Point p1=new Point((int)(x_center*((0.5-zakres)/0.5)),(int)(y_center*((0.5-zakres)/0.5)));  //wierzcholki kwadratu do sprawdzania kolorów (lewy górny)
        Point p2=new Point((int)(x_center*((0.5+zakres)/0.5)),(int)(y_center*((0.5+zakres)/0.5)));  //wierzcholki kwadratu do sprawdzania kolorów (prawy dolny)

        Point kropka_p1=new Point((int)(x_center*((0.5-zakres_na_punkt_środpowy)/0.5)),(int)(y_center*((0.5-zakres_na_punkt_środpowy)/0.5)));  //wierzcholki kwadratu do sprawdzania kolorów (lewy górny)
        Point kropka_p2=new Point((int)(x_center*((0.5+zakres_na_punkt_środpowy)/0.5)),(int)(y_center*((0.5+zakres_na_punkt_środpowy)/0.5)));  //wierzcholki kwadratu do sprawdzania kolorów (prawy dolny)

        for(int i = (int) p1.x; i<(int) p2.x; i++){
            if(i==(int)kropka_p1.x)
                i=(int)kropka_p2.x;
            for(int j = (int) p1.y; j<(int) p2.y; j++){
                if(i==(int)kropka_p1.y)
                    i=(int)kropka_p2.y;

                if(max<(int)m.get(i,j)[0])
                    max=(int)m.get(i,j)[0];
                if(min>(int)m.get(i,j)[0])
                    min=(int)m.get(i,j)[0];


            }
        }
        Imgcodecs.imwrite(System.getProperty("user.home") + "\\Desktop\\odp0.jpg", m);
        // szuka wartości tła na histogramie
        Mat bHist = new Mat();
        List<Mat> a=new ArrayList<>();
        float[] range = {0, 256}; //the upper boundary is exclusive
        MatOfFloat histRange = new MatOfFloat(range);
        Core.split(m, a);
        Imgproc.calcHist(a,new MatOfInt(0), new Mat(), bHist, new MatOfInt(255), histRange, false);
        int wartosc_tla=0;
        for(int i=0, max_pikseli=0;i<bHist.rows();i++)
            if(max_pikseli<(int)bHist.get(i,0)[0]){
                max_pikseli=(int)bHist.get(i,0)[0];
                wartosc_tla=i;
            }

        System.out.println("max="+max+" min="+min+" wartosc_tla="+wartosc_tla);

            if(wartosc_tla>(max-min)){

                Core.bitwise_not(matrix,matrix);
                min=255-min;
                max=255-max;
            }

            thresh= Math.abs(max-min);
            System.out.println(""+thresh);
    }


    private void save_image(){
        String string = System.getProperty("user.home") + "\\Desktop\\zegary\\odp.jpg";
        System.out.println(string);

        try {
            Imgcodecs.imwrite(string, mat);
        }catch (Exception e){
            System.out.println("Error save image");
        }
    }


}
