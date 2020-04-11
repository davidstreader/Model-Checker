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

public class LabelEdgeController implements Initializable {

    private String newEdgeName = "";

    @FXML
    private Button AcceptName;

    @FXML
    private TextField EdgeNameTextField;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void doAcceptName(ActionEvent event) {
        Stage stage = (Stage) AcceptName.getScene().getWindow();
        stage.close();
    }

    public void handleKeyPressed(KeyEvent keyEvent) {
        newEdgeName += keyEvent.getText();

    }

    public String getLabelNameValue() {
        String toReturn = newEdgeName;
        newEdgeName = "";
        return toReturn;
    }
}
