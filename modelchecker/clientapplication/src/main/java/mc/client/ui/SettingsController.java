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
    private Integer delay = 10;
    private Integer repulse = 10;
    private Integer spring = 25;
    private Integer step = 10;

    @Setter
    private Window window;

    @FXML
    private Slider maxNodesSlider;

    @FXML
    private Slider delaySlider;

    @FXML
    private Slider repulseSlider;
    @FXML
    private Slider springSlider;

    @FXML
    private Slider stepSlider;

    @FXML
    private void handleSettingsConfirmation(ActionEvent e) {
        delay = (int)delaySlider.getValue();
        maxNodes = (int)maxNodesSlider.getValue();

        window.hide();
    }

    @FXML
    private void handleResetSettings(ActionEvent e) {
        maxNodesSlider.setValue(40);
        stepSlider.setValue(120);
        repulseSlider.setValue(10);
        springSlider.setValue(10);
        delaySlider.setValue(10);

    }

    public Integer getMaxNodes() {
        return maxNodes;
    }

    public Integer getDelay() {
        return delay;
    }
    public Integer getRepulse() {
        return repulse;
    }
    public Integer getSpring() {
        return spring;
    }
    public Integer getStep() {
        return (step);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        maxNodesSlider.setValue(maxNodes);
        delaySlider.setValue(delay);

        maxNodesSlider.valueProperty().addListener((arg0, arg1, newVal) -> {
            maxNodes = newVal.intValue();
        });


        delaySlider.valueProperty().addListener((arg0, arg1, newVal) -> {
            delay = newVal.intValue();
        });
        repulseSlider.valueProperty().addListener((arg0, arg1, newVal) -> {
            repulse = newVal.intValue();
            //System.out.printf("Rep set to %1.2f \n",repulse);
        });
        springSlider.valueProperty().addListener((arg0, arg1, newVal) -> {
            spring = newVal.intValue();

        });
        stepSlider.valueProperty().addListener((arg0, arg1, newVal) -> {
            step = newVal.intValue();

        });

    }



    public SettingsController() {

    }

}
