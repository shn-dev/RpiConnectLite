package src.login;

import src.connection.CredentialHandler;
import src.general.config.Global;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class LoginController {

    //required public no args constructor
    public LoginController() {
    }

    public void setCredentialHandler() {
        Global.credentialHandler = new CredentialHandler() {
            @Override
            public String setSFTPHost() {
                SFTPHost = ip.getText();
                return SFTPHost;
            }

            @Override
            public String setSFTPPort() {
                SFTPPort = port.getText();
                return SFTPPort;
            }

            @Override
            public String setSFTPUser() {
                SFTPUser = username.getText();
                return SFTPUser;
            }

            @Override
            public String setSFTPPass() {
                SFTPPass = pass.getText();
                return SFTPPass;
            }

            @Override
            public String setSFTPWorkingDir() {
                SFTPWorkingDir = workingDir.getText();
                return SFTPWorkingDir;
            }

            @Override
            public String setSFTPPrivateKey() {
                return null;
            }
        };
    }

    @FXML
    public void initialize() {
        if (!Global.Constants.isProduction) {
            username.setText(Global.Constants.DEV_DEFAULT_USER);
            pass.setText(Global.Constants.DEV_DEFAULT_PASS);
            ip.setText(Global.Constants.DEV_DEFAULT_HOST);
            port.setText(String.valueOf(Global.Constants.DEV_DEFAULT_PORT));
            workingDir.setText(Global.Constants.DEV_DEFAULT_WORKING_DIR);
        }
    }

    //TODO: Add private key field

    @FXML
    private TextField username;
    @FXML
    private PasswordField pass;
    @FXML
    private TextField ip;
    @FXML
    private TextField port;
    @FXML
    private TextField workingDir;

    @FXML
    public void connectBtnClick(ActionEvent actionEvent) throws IOException {

        Global.loadingForm.show();

        //Set the credentials so that they are globally available in the app.
        setCredentialHandler();

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("src/filebrowser/filebrowser_view.fxml")));
        Stage browserStage = new Stage();
        browserStage.setTitle("Viewing device");
        browserStage.setScene(new Scene(root));

        //Remove focus from the first textbox in the form
        //so that the user can see the hint/prompt of the textbox
        root.requestFocus();
    }
}
