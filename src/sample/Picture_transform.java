package sample;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static java.lang.StrictMath.round;
import static org.opencv.imgproc.Imgproc.*;

public class Picture_transform {

    private String file_patch;
    private Imgcodecs imageCodecs=new Imgcodecs();
    private Mat matrix;
    private Mat mat;


    public Picture_transform(String file_patch) {
        this.file_patch=file_patch;
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        load_file_to_matrix();

    }




    private void load_file_to_matrix(){
        try{
            matrix = imageCodecs.imread(file_patch);
            mat = new Mat();//imageCodecs.imread(file_patch);
            System.out.println("Image Loaded");
        }catch (Exception e){
            System.out.println("Error");
        }
    }

    public boolean check_matrix(){
        return matrix!=null;
    }

    public void polar_transform(){
        Point point =new Point(round(matrix.width()/2),round(matrix.height()/2));
        Size size=new Size(matrix.width(),matrix.height());
        double maxRadius = 0.8*Math.min(point.x,point.y);
        try {
            warpPolar(matrix, mat, size, point,maxRadius,WARP_POLAR_LINEAR);
        }catch (Exception e){
            System.out.println("Error");
        }

        save_imagine();
    }


    private void save_imagine(){
        String string ="C:/Users/lasek/Desktop/odp.jpg";
        try {
            imageCodecs.imwrite(string, mat);
        }catch (Exception e){}

    }


}
