package src;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import src.general.config.Global;

import java.util.Objects;

/**
 * This is the main entry into the application
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("src/login/login_view.fxml")));
        primaryStage.setTitle("Connect to device");
        primaryStage.setScene(new Scene(root, 600, 500));
        primaryStage.show();

        //Remove focus from the first textbox in the form
        //so that the user can see the hint/prompt of the textbox
        root.requestFocus();

        //also init loading form and then hide it. Show/hide as needed.
        Parent loadingForm = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("src/general/loading/loading_view.fxml")));
        Global.loadingForm = new Stage();
        Global.loadingForm.setScene(new Scene(loadingForm));
    }


    public static void main(String[] args) {
        launch(args);
    }
}
