package sample;

import org.opencv.core.Mat;

import java.util.Arrays;

public class Picture_cut {

    public Mat matrix;

    Picture_cut(Mat imread)
    {
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

                if(!IsBackground(imread.get(i,j), background))
                        rows[i]++;
            }
            if(rows[i] > 0)
                height++;
            if(rows[i] >= rows[centerY])
                centerY = i;
        }
        for(int i = 0; i<imread.cols();i++) {
            for (int j =0;j<imread.rows();j++)
            {
                 if(!IsBackground(imread.get(i,j), background))
                     cols[i]++;
                 else
                     imread.put(i,j, new double[]{255,255,255});
            }
            System.out.println();
            if(cols[i] > 0)
                width++;
            if(cols[i] >= cols[centerX])
                centerX = i;
        }
        int margin = 5;

        int rowStart, rowEnd, colStart, colEnd;
        int movey =0 ;

        for(int i = 0; i<rows.length;i++)
            if(rows[i] == rows[centerY])
                movey++;
        centerY -= movey;
        int movex =0 ;
        for(int i = 0; i<cols.length;i++)
            if(cols[i] == cols[centerX])
                movex++;
        centerX -= movex;

        rowStart = centerY-height/2-margin;
        if(rowStart < 0)
            rowStart = 0;
        rowEnd = centerY+height/2+margin;
        if(rowEnd > rows.length)
            rowEnd = rows.length;
        colStart = centerX-width/2-margin;
        if(colStart < 0)
            colStart = 0;
        colEnd = centerX+width/2+margin;
        if(colEnd > cols.length)
            colEnd = cols.length;
        matrix = imread.submat(rowStart, rowEnd, colStart, colEnd);
    }
    Boolean IsBackground(double[] color, double[] background)
    {
        if(color[0] == background[0])
            if(color[1] == background[1])
                if(color[2] == background[2])
                    return true;
        return false;
    }
}
