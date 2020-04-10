package mc.client.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class NewProcessController implements Initializable {

    private String newProcessName = "";

    @FXML
    private Button AcceptName;

    @FXML
    private TextField NewProcessTextField;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void doAcceptName(ActionEvent event) {
        Stage stage = (Stage) AcceptName.getScene().getWindow();
        stage.close();
    }

    public void handleKeyPressed(KeyEvent keyEvent) {
        newProcessName += keyEvent.getText();

    }

    public String getNewProcessNameValue() {
        String toReturn = newProcessName;
        newProcessName = "";
        return toReturn;
    }
}
