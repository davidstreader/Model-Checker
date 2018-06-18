package mc.client.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.stage.Window;
import lombok.Setter;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by smithjord3 on 30/01/18.
 */
public class SettingsController implements Initializable{
    private Integer maxNodes = 40;
    private Integer linkageLength = 120;
    private Integer repulse = 10;
    private Integer speed = 10;
    private Integer spring = 10;

    @Setter
    private Window window;

    @FXML
    private Slider maxNodesSlider;

    @FXML
    private Slider linkageLengthSlider;

    @FXML
    private Slider repulseSlider;
    @FXML
    private Slider springSlider;

    @FXML
    private Slider speedSlider;

    @FXML
    private void handleSettingsConfirmation(ActionEvent e) {
        linkageLength = (int)linkageLengthSlider.getValue();
        maxNodes = (int)maxNodesSlider.getValue();

        window.hide();
    }

    @FXML
    private void handleResetSettings(ActionEvent e) {
        maxNodesSlider.setValue(40);
        linkageLengthSlider.setValue(120);
        repulseSlider.setValue(10);
        springSlider.setValue(10);
        speedSlider.setValue(10);
        maxNodes = 40;
        linkageLength = 120;
    }

    public Integer getMaxNodes() {
        return maxNodes;
    }

    public Integer getLinkageLength() {
        return linkageLength;
    }
    public Integer getRepulse() {
        return repulse;
    }
    public Integer getSpeed() {
        return speed;
    }
    public Integer getSpring() {
        return spring;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        maxNodesSlider.setValue(maxNodes);
        linkageLengthSlider.setValue(linkageLength);

        maxNodesSlider.valueProperty().addListener((arg0, arg1, newVal) -> {
            maxNodes = newVal.intValue();
        });


        linkageLengthSlider.valueProperty().addListener((arg0, arg1, newVal) -> {
            linkageLength = newVal.intValue();
        });
        repulseSlider.valueProperty().addListener((arg0, arg1, newVal) -> {
            repulse = newVal.intValue();
            //System.out.printf("Rep set to %1.2f \n",repulse);
        });
        springSlider.valueProperty().addListener((arg0, arg1, newVal) -> {
            spring = newVal.intValue();

        });
        speedSlider.valueProperty().addListener((arg0, arg1, newVal) -> {
            speed = newVal.intValue();

        });

    }

    public SettingsController(Integer numNodes, Integer linkageLength_,Integer repulse_) {
        maxNodes = numNodes;
        linkageLength = linkageLength_;
        repulse = repulse_;
    }

    public SettingsController() {

    }

}
