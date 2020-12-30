package sample;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

import static org.opencv.imgproc.Imgproc.COLOR_GRAY2RGB;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;

public class Picture_cut {

    public Mat matrix;

    Picture_cut(Mat imread_w)
    {
        imread_w=resize_image(imread_w);    //normalizuje wielkość żeby wysikość==szerokość

        Mat imread =new Mat();
        Imgproc.cvtColor(imread_w,imread,COLOR_RGB2GRAY);
        Imgproc.cvtColor(imread,imread,COLOR_GRAY2RGB);
        double[] background = imread.get(0,0);
        int[] rows = new int[imread.rows()];
        int[] cols = new int[imread.cols()];
        int centerX = 0;
        int width = 0;
        int centerY = 0;
        int height = 0;

        for(int i = 0; i<imread.rows();i++) {
            for (int j =0;j<imread.cols();j++)
            {

                if(!IsBackground(imread.get(j,i), background))
                        rows[i]++;
            }
            if(rows[i] > 0)
                height++;
            //if(rows[i] >= rows[centerY])
                //centerY = i;
        }
//-----------------------------------------------------------
        for(int i = 0; i<imread.rows();i++){
            if(rows[i] > 0){
                centerY = i;
                break;
            }
        }
        centerY=centerY+height/2;
//-----------------------------------------------------------



        for(int i = 0; i<imread.cols();i++) {
            for (int j =0;j<imread.rows();j++)
            {
                 if(!IsBackground(imread.get(i,j), background))
                     cols[i]++;
                 else
                     imread.put(i,j, new double[]{255,255,255});
            }
            if(cols[i] > 0)
                width++;
            //if(cols[i] >= cols[centerX])
                //centerX = i;
        }
//-----------------------------------------------------------
        for(int i = 0; i<imread.cols();i++){
            if(cols[i] > 0){
                centerX = i;
                break;
            }
        }
        centerX=centerX+width/2;
//------------------------------------------------------------
        int margin = 0;
        System.out.println("w="+width+" h="+height);
        System.out.println("cx="+centerX+" cy="+centerY);

        int rowStart, rowEnd, colStart, colEnd;
        /*
        int movey =0 ;
        for(int i = 0; i<rows.length;i++)
            if(rows[i] == rows[centerY])
                movey++;

        centerY -= movey/2;

        int movex =0 ;
        for(int i = 0; i<cols.length;i++)
            if(cols[i] == cols[centerX])
                movex++;

        centerX -= movex/2;
        */
        rowStart = centerX-width/2-margin;
        if(rowStart < 0)
            rowStart = 0;
        rowEnd = centerX+width/2+margin;
        if(rowEnd > rows.length)
            rowEnd = rows.length;
        colStart = centerY-height/2-margin;
        if(colStart < 0)
            colStart = 0;
        colEnd = centerY+height/2+margin;
        if(colEnd > cols.length)
            colEnd = cols.length;
        matrix = imread_w.submat(rowStart, rowEnd, colStart, colEnd);
       // matrix = imread.submat(colStart, colEnd, rowStart, rowEnd);

    }

    //zmienia wilekość zdjecia tak zeby wysokosc==szerokosc
    private Mat resize_image(Mat mat){
        if(mat.cols()!=mat.rows())
            Imgproc.resize(mat,mat,new Size((int)((mat.cols()+mat.rows())/2) , (int)((mat.cols()+mat.rows())/2)));
        return mat;
    }


    Boolean IsBackground(double[] color, double[] background)
    {
        int m =10;
        if(color[0] <= background[0]+m && color[0] >= background[0]-m)
            if(color[1] <= background[1]+m && color[1] >= background[1]-m)
                if(color[2] <= background[2]+m && color[2] >= background[2]-m)
                    return true;
        return false;
    }
}
