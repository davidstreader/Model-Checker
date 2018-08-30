package mc.client.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.stage.Window;
import lombok.Setter;
import mc.Constant;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by smithjord3 on 30/01/18.
 */
public class SettingsController implements Initializable{
    private Integer maxNodes = 40;
    private Integer delay = 2;
    private Integer repulse = 25;
    private Integer spring = 50;
    private Integer step = 150;
    private boolean showOwners = false;
    private boolean showColor = false;
    private boolean Congruance = false;
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


    @FXML private CheckBox Own = new CheckBox();
    @FXML private CheckBox Col = new CheckBox();
    @FXML private CheckBox Cong = new CheckBox();



    private void handleButtonAction(ActionEvent e) {

        showOwners = Own.isSelected();
        showColor = Col.isSelected();
        Congruance = Cong.isSelected();
        //System.out.println("Owners = "+showOwners+"  Col = "+isShowColor());
    }

    @FXML
    private void handleSettingsConfirmation(ActionEvent e) {
        delay = (int)delaySlider.getValue();
        maxNodes = (int)maxNodesSlider.getValue();

        window.hide();
    }

    @FXML
    private void handleResetSettings(ActionEvent e) {
        maxNodesSlider.setValue(40);
        stepSlider.setValue(150);
        repulseSlider.setValue(25);
        springSlider.setValue(50);
        delaySlider.setValue(2);

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

    public boolean isShowColor() {
        return showColor;
    }

    public boolean isShowOwners() {
        return showOwners;
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
        Own.setOnAction(e -> handleButtonAction(e));

    }



    public SettingsController() {

    }

}
