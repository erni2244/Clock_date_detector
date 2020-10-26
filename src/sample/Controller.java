package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import java.io.File;


public class Controller {

    Picture_transform picture_transform;

    @FXML
    private Label answer;

    @FXML
    private AnchorPane anchorPane;

    public void detect_date_click(ActionEvent actionEvent) {
        System.out.println("detect");
        if(picture_transform.check_matrix()){
            picture_transform.polar_transform();
            System.out.println("transform");
        }
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
            string=(String)file.getPath();
        }catch (Exception e){}
        return string;
    }



}
