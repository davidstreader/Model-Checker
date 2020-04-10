package mc.client.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.stage.Window;
import lombok.Setter;
import mc.Constant;
import mc.util.expr.MyAssert;


import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by smithjord3 on 30/01/18.
 */
public class SettingsController implements Initializable {
    private Integer maxNodes = 40;
    private Integer delay = 2;
    private Integer font = 14;
    private Integer repulse = 25;
    private Integer spring = 50;
    private Integer step = 150;
    private boolean showIds = false;
    private boolean showOptional = false;
    private boolean showOwners = false;
    private boolean showColor = true;
    private boolean Congruance = false;
    private boolean Symbolic = false;
    Collection<String> disp = new ArrayList<>();
    List<FontListener> fontListeners = new ArrayList<>(2);

    @Setter
    private Window window;

    @FXML
    private Slider maxNodesSlider;

    @FXML
    private Slider delaySlider;
    @FXML
    private Slider fontSlider;

    @FXML
    private Slider repulseSlider;
    @FXML
    private Slider springSlider;

    @FXML
    private Slider stepSlider;


    @FXML
    private CheckBox Ids = new CheckBox();
    @FXML
    private CheckBox Opt = new CheckBox();
    @FXML
    private CheckBox Own = new CheckBox();
    @FXML
    private CheckBox Col = new CheckBox();
    @FXML
    private CheckBox Cong = new CheckBox();
    @FXML
    private CheckBox Symb = new CheckBox();

    @FXML
    private ComboBox<String> displayList = new ComboBox<>();

    private void handleButtonAction(ActionEvent e) {

        showIds = Ids.isSelected();
        showOptional = Opt.isSelected();
        showOwners = Own.isSelected();
        showColor = Col.isSelected();

        Congruance = Cong.isSelected();
        Symbolic = Symb.isSelected();
        System.out.println(Runtime.class.getPackage().toString() + "\n" + Runtime.class.getPackage().getImplementationVersion());
        System.out.println("Symbolic = " + isSymbolic());

    }

    @FXML
    private void handleSettingsConfirmation(ActionEvent e) {
        delay = (int) delaySlider.getValue();
        maxNodes = (int) maxNodesSlider.getValue();

        window.hide();
    }

    @FXML
    private void handleResetSettings(ActionEvent e) {
        myReset();
    }

    public void addFontListener(FontListener fl) { // Listener pattern
        //System.out.println("FontListener "+  fl.getClass().getCanonicalName());
        fontListeners.add(fl);
        //System.out.println("FontListeners cnt "+fontListeners.size());
    }

    public void myReset() {
        maxNodesSlider.setValue(40);
        stepSlider.setValue(150);
        repulseSlider.setValue(25);
        springSlider.setValue(50);
        delaySlider.setValue(2);
        fontSlider.setValue(14);
        //Symb.setSelected(true);
        //displayList.setItems(disp);
        initDispType();
    }

    public Integer getMaxNodes() {
        return maxNodes;
    }

    public Integer getDelay() {
        return delay;
    }

    public Integer getFont() {
        return font;
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

    public boolean isShowIds() {
        return showIds;
    }

    public boolean isShowOptional() {
        return showOptional;
    }

    public boolean isShowOwners() {
        return showOwners;
    }

    public boolean isSymbolic() {
        return Symbolic;
    }

    public void updateDisplayList(Collection<String> displayType) {
        disp.clear();
        disp.add("All");
        disp.add(Constant.AUTOMATA);
        disp.add(Constant.PETRINET);
        displayList.getItems().clear();
        displayType.forEach(displayList.getItems()::add);
        displayList.getSelectionModel().selectFirst();
        // Symb.setSelected(true);
    }

    public String getDisplayType() {
        return displayList.getSelectionModel().getSelectedItem();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //System.out.println("initialise Settings "+Symbolic +"  and "+isSymbolic());
        Symb.setSelected(isSymbolic());
        Col.setSelected(isShowColor());
        maxNodesSlider.setValue(maxNodes);
        delaySlider.setValue(delay);
        fontSlider.setValue(font);

        maxNodesSlider.valueProperty().addListener((arg0, arg1, newVal) -> {
            maxNodes = newVal.intValue();
        });


        delaySlider.valueProperty().addListener((arg0, arg1, newVal) -> {
            delay = newVal.intValue();
        });
        fontSlider.valueProperty().addListener((arg0, arg1, newVal) -> {
            font = newVal.intValue();
            fontListeners.stream().forEach(x -> x.changeFontSize());
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

        Ids.setOnAction(e -> handleButtonAction(e));
        Opt.setOnAction(e -> handleButtonAction(e));
        Own.setOnAction(e -> handleButtonAction(e));
        Symb.setOnAction(e -> handleButtonAction(e));

        initDispType();

        // System.out.println("initialise Settings "+Symbolic +"  and "+isSymbolic());

    }

    private void initDispType() {
        updateDisplayList(disp);
    }

    public SettingsController() {
        initDispType();
    }

    @FXML
    public void initialize() {
    }
}
