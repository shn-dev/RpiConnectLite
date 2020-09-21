package src.general.popup;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Popup {

    public static void issuePopup(String title, String text){
        Parent root = new GridPane();
        Label label = new Label(text);
        Scene scene = new Scene(label, 300, 200);
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();

        root.requestFocus();
    }
}
