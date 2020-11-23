package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import java.io.File;


public class Controller {

    Picture_transform picture_transform;

    @FXML
    private Label answer;


    public void detect_date_click(ActionEvent actionEvent) {
        if(picture_transform!=null) {
            int[] a=picture_transform.read_date();
            answer.setText("godzina "+a[0]+":"+a[1]);
        }
        else
            System.out.println("No fille loaded");

        System.out.println("detect");
    }


    public void load_picture_click(ActionEvent actionEvent) {
        System.out.println("load");
        String patch=select_file();
        if(patch==null){
            answer.setText("Nie wczytano pliku");
        }else {
            picture_transform=new Picture_transform(patch);
        }

    }



    private String select_file(){
        FileChooser fileChooser =new FileChooser();
        fileChooser.setTitle("Wybierz zdjęcie");
        String string=null;
        try {
            File file = fileChooser.showOpenDialog(null);
            string= file.getPath();
            //string="C:/Users/lasek/Desktop/zegary/0003.png";
        }catch (Exception ignored){}
        return string;
    }



}
