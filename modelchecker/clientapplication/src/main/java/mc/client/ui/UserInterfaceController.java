package mc.client.ui;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mc.client.ModelView;
import mc.compiler.Compiler;
import mc.compiler.OperationResult;
import mc.exceptions.CompilationException;
import mc.util.expr.Expression;
import mc.webserver.Context;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static mc.client.ui.SyntaxHighlighting.computeHighlighting;

public class UserInterfaceController implements Initializable {
    private boolean holdHighlighting = false; // If there is an compiler issue, highlight the area. Dont keep applying highlighting it wipes it out
    private javafx.stage.Popup autocompleteBox = new javafx.stage.Popup();
    private ExecutorService executor; // Runs the highlighting in seperate thread
    private TrieNode<String> completionDictionary;

    @FXML private CodeArea userCodeInput;

    @FXML private TextArea compilerOutputDisplay;

    @FXML private SwingNode modelDisplay;

    @FXML private ComboBox<String> modelsList;

    private Stage window;


    private Scene scene;
    private boolean saveButton = false;
    private boolean dontSaveButton = false;
    private boolean cancel = false;
    private boolean hasBeenSavedBefore = false;

    private String buttonName;


    private int lengthEdgeValue = 10;
    private int maxNodeLabelValue = 10;
    private int operationFailureLabelValue = 10;
    private int operationPassLabelValue = 10;


    private boolean fairAbstractionSelected = false;
    private boolean autoSaveSelected = false;
    private boolean darkModeSelected = false;
    private boolean pruningSelected = false;
    private boolean liveCompillingSelected = false;


    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  <tt>null</tt> if the location is not known.
     * @param resources The resources used to localize the root object, or <tt>null</tt> if
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Have to initalise it or there is a delay between the graph becoming ready and actually displaying things
        SwingUtilities.invokeLater(() -> modelDisplay.setContent(ModelView.getInstance().updateGraph(modelDisplay)));

        //register a callback for whenever the list of automata is changed
        ModelView.getInstance().setListOfAutomataUpdater(this::updateModelsList);
        //register a callback for the output of the log
        ModelView.getInstance().setUpdateLog(this::updateLogText);
        //So the model viewer can modify the list of processes (For process model selection)


        //add all the syntax to the completion dictionary
        completionDictionary = new TrieNode<>(new ArrayList<>(Arrays.asList(SyntaxHighlighting.processTypes)));
        completionDictionary.add(new ArrayList<>(Arrays.asList(SyntaxHighlighting.functions)));
        completionDictionary.add(new ArrayList<>(Arrays.asList(SyntaxHighlighting.keywords)));

        //add style sheets
        userCodeInput.setStyle("-fx-background-color: #32302f;");
        userCodeInput.getStylesheets().add(getClass().getResource("/clientres/automata-keywords.css").toExternalForm());

        ListView<String> popupSelection = new ListView<String>();
        popupSelection.setStyle(
                        "-fx-background-color: #f7e1a0;" +
                        "-fx-text-fill:        black;" +
                        "-fx-padding:          5;"
        );


        popupSelection.setOnMouseClicked(event -> {
            String selectedItem = popupSelection.getSelectionModel().getSelectedItem();
            actOnSelect(popupSelection, selectedItem);
        });

        popupSelection.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                String selectedItem = popupSelection.getSelectionModel().getSelectedItem();
                actOnSelect(popupSelection, selectedItem);
            }

        });



        autocompleteBox.getContent().add(popupSelection);
        autocompleteBox.setAutoHide(true);

        executor = Executors.newSingleThreadExecutor();



        userCodeInput.setParagraphGraphicFactory(LineNumberFactory.get(userCodeInput)); // Add line numbers

        userCodeInput.setOnMouseClicked(event -> { // If the user clicks outside of the autocompletion box they probably dont want it there...
            autocompleteBox.hide();
        });

        userCodeInput.richChanges() // Set up syntax highlighting in another thread as regex finding can take a while.
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()) &&  !holdHighlighting) // Hold highlighting if we have an issue and have highlighted it, otherwise it gets wiped.
                .successionEnds(Duration.ofMillis(20))
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(userCodeInput.richChanges())
                .filterMap(t -> {
                    if(t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(this::applyHighlighting);

        userCodeInput.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()) && ch.getInserted().getStyleOfChar(0).isEmpty())
                .filter(ch -> ch.getInserted().getText().length() == 1)
                .subscribe((change) -> { // Hook for detecting user input, used for autocompletion as that happens quickly.

                if (change.getRemoved().getText().length() == 0) {  // If this isnt a backspace character

                    String currentUserCode = userCodeInput.getText();

                    if (userCodeInput.getCaretPosition() < currentUserCode.length()) {

                        char currentCharacter = userCodeInput.getText().charAt(userCodeInput.getCaretPosition());
                        switch (currentCharacter) {
                            case '\n':
                            case '\t':
                            case ' ': { // If the user has broken off a word, dont continue autocompleting it.
                                autocompleteBox.hide();

                                popupSelection.getItems().clear();
                            }
                            break;

                            default: {
                                popupSelection.getItems().clear();
                                String currentWord = getWordAtIndex(userCodeInput.getCaretPosition());
                                if (currentWord.length() > 0) {
                                    ArrayList<String> list = completionDictionary.getId(currentWord);

                                    if (list.size() != 0) {
                                        popupSelection.getItems().addAll(list);
                                        popupSelection.getSelectionModel().select(0);

                                        if(userCodeInput.getCaretBounds().isPresent())
                                            autocompleteBox.show(userCodeInput, userCodeInput.getCaretBounds()
                                                    .get().getMinX(), userCodeInput.getCaretBounds().get().getMaxY());

                                    } else { // If we dont have any autocomplete suggestions dont show the box
                                        autocompleteBox.hide();

                                    }
                                } else {
                                    autocompleteBox.hide();

                                }

                            }
                            break;
                        }
                    }

                } else { // Handles if there is a backspace
                    popupSelection.getItems().clear();
                    autocompleteBox.hide();

                }

            holdHighlighting = false;

        });

    }

    /**
     * This is a helper function to add an insert
     * @param popupSelection
     * @param selectedItem
     */
    private void actOnSelect(ListView<String> popupSelection, String selectedItem) {
        if(selectedItem != null) {

            String code = userCodeInput.getText();
            int wordPosition = userCodeInput.getCaretPosition()-1; // we know where the user word is, but we dont know the start or end

            int start;
            for(start = wordPosition;
                start > 0 &&
                        !Character.isWhitespace(code.charAt(start-1)) &&
                        Character.isLetterOrDigit(userCodeInput.getText().charAt(start-1));
                start--);

            int end;
            for(end = wordPosition;
                end < code.length() &&
                        !Character.isWhitespace(code.charAt(end)) &&
                        Character.isLetterOrDigit(userCodeInput.getText().charAt(end));
                end++);


            userCodeInput.replaceText(start, end, selectedItem);

            popupSelection.getItems().clear();
            autocompleteBox.hide();

            userCodeInput.setStyleSpans(0, computeHighlighting(userCodeInput.getText())); // Need to reupdate the styles when an insert has happened.
        }
    }

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = userCodeInput.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return computeHighlighting(text);
            }
        };
        executor.execute(task);
        return task;
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        userCodeInput.setStyleSpans(0, highlighting); // Fires a style event
    }


    private String getWordAtIndex(int pos) {
        String text = userCodeInput.getText().substring(0, pos);

        // keeping track of index
        int index;

        // get first whitespace "behind caret"
        for (index = text.length() - 1;
             index >= 0 &&
                     !Character.isWhitespace(text.charAt(index)) &&
                     Character.isLetterOrDigit(userCodeInput.getText().charAt(index));
             index--);

        // get prefix and startIndex of word
        String prefix = text.substring(index + 1, text.length());

        // get first whitespace forward from caret
        for (index = pos;
             index < userCodeInput.getLength() &&
                     !Character.isWhitespace(userCodeInput.getText().charAt(index)) &&
                     Character.isLetterOrDigit(userCodeInput.getText().charAt(index));
             index++);

        String suffix = userCodeInput.getText().substring(pos, index);

        // replace regex wildcards (literal ".") with "\.". Looks weird but
        // correct...
        prefix = prefix.replaceAll("\\.", "\\.");
        suffix = suffix.replaceAll("\\.", "\\.");

        // combine both parts of words
        prefix = prefix + suffix;

        // return current word being typed
        return prefix;
    }

    @FXML
    private void handleCreateNew(ActionEvent event) throws InterruptedException {
        // If the Code Area is not empty which means there are codes that we can save.
        //buttonName = "New";

        if (!(userCodeInput.getText().isEmpty())) {
            // Open a dialogue and give the user three options (SAVE, DON'TSAVE, CANCEL)
            createSceneFile("New");
            window.close();
            System.out.println("THIS IS BEFORE ANY OF THE INTERACTION");
            if (saveButton) {
                setBackFlags();
                // do these operations when the user click on SAVE button in the dialogue
                hasBeenSavedBefore = true;
                cleanTheCodeArea();


            } else if (dontSaveButton) {
                window.close();
                // do these operations when the user click on DON'TSAVE button in the dialogue
                cleanTheCodeArea();

            } else {
                // Close the dialogue if the user clicking on the CANCEL
                window.close();
            }

        }
        window.close();
    }

    @FXML
    private void handleOpen(ActionEvent event) {
        window = new Stage();
        //buttonName = "Open";
        // If the Code Area is not empty which means there are codes that we can save.
        if (!(userCodeInput.getText().isEmpty())) {
            // Open a dialogue and give the user three options (SAVE, DON'TSAVE, CANCEL)
            createSceneFile("Open");
            if (saveButton) {
                setBackFlags();
                // do these operations when the user click on SAVE button in the dialogue
                cleanTheCodeArea();
            } else if (dontSaveButton) {
                setBackFlags();
            }
        } else {
            dontSaveButtonFunctionality();
        }

    }

    @FXML
    private void handleOpenRecentAction(ActionEvent event) {
        /**/
    }

    @FXML
    private void handleFileClose(ActionEvent event) {
        if (!(userCodeInput.getText().equals(""))) {
            saveButtonFunctionality();
            System.exit(0);
        }
        System.exit(0);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        window = new Stage();
        if (!(hasBeenSavedBefore)) {
            saveButtonFunctionality();
        }
    }

    @FXML
    private void handleSaveAs(ActionEvent event) {
        window = new Stage();
        saveButtonFunctionality();
    }

    @FXML
    private void handleQuit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void handleAddSelectedModel(ActionEvent event) {
        if(modelsList.getSelectionModel().getSelectedItem() != null && modelsList.getSelectionModel().getSelectedItem() instanceof String) {

            ModelView.getInstance().addDisplayedModel(modelsList.getSelectionModel().getSelectedItem());
            SwingUtilities.invokeLater(() -> modelDisplay.setContent(ModelView.getInstance().updateGraph(modelDisplay)));
        }
    }

    @FXML
    private void handleAddallModels(ActionEvent event) {
        ModelView.getInstance().addAllModels();
        SwingUtilities.invokeLater(() -> modelDisplay.setContent(ModelView.getInstance().updateGraph(modelDisplay)));
    }

    @FXML
    private void handleClearGraph(ActionEvent event) {
        ModelView.getInstance().clearDisplayed();
        SwingUtilities.invokeLater(() -> modelDisplay.setContent(ModelView.getInstance().updateGraph(modelDisplay)));
    }

    @FXML
    private void handleFreeze(ActionEvent event) {
        String selecteditem = modelsList.getSelectionModel().getSelectedItem();
        if(selecteditem != null) {
            ModelView.getInstance().freezeProcessModel(selecteditem);
        }
    }

    @FXML
    private void handleUnfreeze(ActionEvent event) {
        String selecteditem = modelsList.getSelectionModel().getSelectedItem();
        if(selecteditem != null) {
            ModelView.getInstance().unfreezeProcessModel(selecteditem);
        }
    }

    @FXML
    private void handleFreezeAll(ActionEvent event) {
        String selecteditem = modelsList.getSelectionModel().getSelectedItem();
        if(selecteditem != null) {
            ModelView.getInstance().freezeAllCurrentlyDisplayed();
        }
    }

    @FXML
    private void handleUnfreezeAll(ActionEvent event) {
        String selecteditem = modelsList.getSelectionModel().getSelectedItem();
        if(selecteditem != null) {
            ModelView.getInstance().unfreezeAllCurrentlyDisplayed();
        }
    }


    @FXML
    private void handOptionsRequest(ActionEvent event) {
        creatSceneOptions();
    }

    //TODO: make this a better concurrent process
    @FXML
    private void handleCompileRequest(ActionEvent event) {
        String userCode = userCodeInput.getText();
        if(!userCode.isEmpty()) {
            compilerOutputDisplay.clear();
            modelsList.getItems().clear();

            try {
                Compiler codeCompiler = new Compiler();

                // This follows the observer pattern.
                // Within the compile function the code is then told to update an observer
                codeCompiler.compile(userCode, new Context(), Expression.mkCtx(), new LinkedBlockingQueue<>());

                compilerOutputDisplay.insertText(0,"Compiling completed sucessfully!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (CompilationException e) {
                holdHighlighting = true;
                compilerOutputDisplay.insertText(0,e.toString() + "\n" + e.getLocation());


                if(e.getLocation().getStartIndex() > 0 && e.getLocation().getStartIndex() < userCodeInput.getText().length())
                    userCodeInput.setStyleClass(e.getLocation().getStartIndex(), e.getLocation().getEndIndex(), "issue");

            }
        }
    }


    //helpers for ModelView

    /**
     * This recieves a list of all valid models and registers them with the combobox
     * @param models a collection of the processIDs of all valid models
     */
    private void updateModelsList(Collection<String> models){
        modelsList.getItems().clear();
        models.forEach(modelsList.getItems()::add);
        modelsList.getSelectionModel().selectFirst();
    }

    private void updateLogText(List<OperationResult> opRes, List<OperationResult> eqRes){
        if(opRes.size() > 0)
            compilerOutputDisplay.appendText("\n##Operation Results##\n");

        opRes.forEach(o -> compilerOutputDisplay.appendText(o.getProcess1().getIdent() + " " + o.getOperation() + " " +
                                                            o.getProcess2().getIdent() + " = " + o.getResult() + "\n"));

        if(eqRes.size() > 0)
            compilerOutputDisplay.appendText("\n##Operation Results##\n");

        eqRes.forEach(o -> compilerOutputDisplay.appendText(o.getProcess1().getIdent() + " " + o.getOperation() + " " +
                                                            o.getProcess2().getIdent() + " = " + o.getResult() + "\n" +
                                                            "Simulations passed: "+ o.getExtra() + "\n"));


    }




    private boolean saveButtonFunctionality() {
        dontSaveButton = false;
        cancel = false;
        saveButton = true;
        FileChooser fileChooser;
        PrintStream readTo;
        File selectedFile;
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("TXT", "*.txt"));
        fileChooser.setTitle("Save file into a directory");
        selectedFile = fileChooser.showSaveDialog(window);

        try {
            if (selectedFile != null) {
                readTo = new PrintStream(selectedFile);
                readTo.println(userCodeInput.getText());
                readTo = readTheOptionsIntegers(readTo);
                readTo = readTheOptionsBooleans(readTo);
                readTo.close();
                hasBeenSavedBefore = true;
            }
        } catch (IOException message) {
            System.out.println(message);
        }
        window.close();
        return true;
    }

    private void dontSaveButtonFunctionality() {
        saveButton = false;
        dontSaveButton = true;
        cancel = false;
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("TXT", "*.txt"));
        fileChooser.setTitle("Open Resource File");

        File selectedFile = fileChooser.showOpenDialog(window);
        String theCode = "";
        Scanner scanner;

        try {
            if (selectedFile != null) {
                scanner = new Scanner(selectedFile);
                while (scanner.hasNext() && !scanner.hasNext("lengthEdgeValue:")) {
                    theCode = theCode + scanner.nextLine() + "\n";
                }
                readOptions(scanner);
                scanner.close();
                String length = userCodeInput.getText();
                int size = length.length();
                userCodeInput.deleteText(0, size);
                userCodeInput.replaceSelection(theCode);
            }
        } catch (IOException message) {
            System.out.println(message);
        }

        window.close();
    }

    private GridPane createGrideOptions() {
        final Label lengthEdgeLabel = new Label("Length of the Edge:");
        Slider lengthEdge = createSlider();
        lengthEdge.setValue((double) lengthEdgeValue);
        lengthEdge.valueProperty().addListener((ChangeListener) (arg0, arg1, arg2) -> {
            lengthEdgeValue = (int) lengthEdge.getValue();
        });


        final Label maxNodeLabel = new Label("Automa Max Node:");
        Slider maxNode = createSlider();
        maxNode.setValue((double) maxNodeLabelValue);
        maxNode.valueProperty().addListener((ChangeListener) (arg0, arg1, arg2) -> {
            maxNodeLabelValue = (int) maxNode.getValue();
        });


        final Label operationFailureLabel = new Label("Operation failure count:");
        Slider operationFailure = createSlider();
        operationFailure.setValue((double) operationFailureLabelValue);
        operationFailure.valueProperty().addListener((ChangeListener) (arg0, arg1, arg2) -> {
            operationFailureLabelValue = (int) operationFailure.getValue();
        });

        final Label operationPassLabel = new Label("Operation pass count:");
        Slider operationPass = createSlider();
        operationPass.setValue((double) operationPassLabelValue);
        operationPass.valueProperty().addListener((ChangeListener) (arg0, arg1, arg2) -> {
            operationPassLabelValue = (int) operationPass.getValue();
        });


        CheckBox fairAbstraction;
        fairAbstraction = new CheckBox("Fair Abstraction");
        fairAbstraction.setOnAction(e -> fairAbstractionFunctionality());
        if (!(fairAbstractionSelected)) {

        } else {
            fairAbstraction.setSelected(true);
        }

        CheckBox autoSave;
        if (!(autoSaveSelected)) {
            autoSave = new CheckBox("Autosave");
            autoSave.setOnAction(e -> autoSaveFunctionality());
        } else {
            autoSave = new CheckBox("Autosave");
            autoSave.setOnAction(e -> autoSaveFunctionality());
            autoSave.setSelected(true);
        }

        CheckBox darkMode;
        if (!(darkModeSelected)) {
            darkMode = new CheckBox("Dark Mode");
            darkMode.setOnAction(e -> darkModeFunctionality());
        } else {
            darkMode = new CheckBox("Dark Mode");
            darkMode.setOnAction(e -> darkModeFunctionality());
            darkMode.setSelected(true);

        }

        CheckBox pruning;
        if (!(pruningSelected)) {
            pruning = new CheckBox("Pruning");
            pruning.setOnAction(e -> pruningFunctionality());
        } else {
            pruning = new CheckBox("Pruning");
            pruning.setOnAction(e -> pruningFunctionality());
            pruning.setSelected(true);
        }

        CheckBox liveCompilling;
        if (!(liveCompillingSelected)) {
            liveCompilling = new CheckBox("Live Compilling");
            liveCompilling.setOnAction(e -> liveCompilingFunctionality());
        } else {
            liveCompilling = new CheckBox("Live Compilling");
            liveCompilling.setOnAction(e -> liveCompilingFunctionality());
            liveCompilling.setSelected(true);
        }

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> window.close());


        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(25);
        grid.setHgap(10);


        GridPane.setConstraints(lengthEdgeLabel, 0, 0);
        GridPane.setConstraints(lengthEdge, 1, 0);

        GridPane.setConstraints(maxNodeLabel, 0, 1);
        GridPane.setConstraints(maxNode, 1, 1);

        GridPane.setConstraints(operationFailureLabel, 0, 2);
        GridPane.setConstraints(operationFailure, 1, 2);

        GridPane.setConstraints(operationPassLabel, 0, 3);
        GridPane.setConstraints(operationPass, 1, 3);

        GridPane.setConstraints(fairAbstraction, 0, 5);
        GridPane.setConstraints(autoSave, 0, 4);
        GridPane.setConstraints(darkMode, 0, 6);

        GridPane.setConstraints(pruning, 1, 4);
        GridPane.setConstraints(liveCompilling, 1, 5);
        GridPane.setConstraints(closeButton, 1, 6);


        grid.getChildren().addAll(lengthEdgeLabel, lengthEdge, maxNodeLabel, maxNode, operationFailureLabel,
                operationFailure, operationPassLabel, operationPass, fairAbstraction, autoSave, darkMode, pruning,
                liveCompilling, closeButton);
        return grid;
    }

    private GridPane createBoxForFile(String buttonName) {
        Label label = new Label("Do you want to save changes?");

        Button saveButton = createSaveButton();
        Button dontSaveButton = createDontSaveButtonForNew();
        Button cancelButton = createCancelButton();

        switch (buttonName){
            case "Open":
                saveButton = createSaveButtonForOpen();
                break;

            case "Close":
                dontSaveButton = createDontSaveButtonForNew();
                break;

            case "Quit":
                System.exit(0);
                break;
            default:
        }

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(25);
        grid.setHgap(10);

        GridPane.setConstraints(label, 0, 0);
        GridPane.setConstraints(saveButton, 1, 0);
        GridPane.setConstraints(dontSaveButton, 2, 0);
        GridPane.setConstraints(cancelButton, 3, 0);

        grid.getChildren().addAll(label, saveButton, dontSaveButton, cancelButton);
        return grid;
    }

    private Button createSaveButton() {
        Button saveButtonTemp = new Button();
        saveButtonTemp.setText("Save");
        saveButtonTemp.setOnAction(e -> saveButtonFunctionality());
        return saveButtonTemp;

    }

    private Button createDontSaveButton() {
        Button dontSaveButton = new Button();
        dontSaveButton.setText("Don't Save");
        dontSaveButton.setOnAction(e -> dontSaveButtonFunctionality());
        return dontSaveButton;
    }

    private Button createCancelButton() {
        Button cancelButton = new Button();
        cancelButton.setText("Cancel");
        cancelButton.setOnAction(e -> cancelButtonFunctionality());
        return cancelButton;
    }

    private Button createDontSaveButtonForNew() {
        Button dontSaveTemp = new Button();
        dontSaveTemp.setText("Don'tSave");
        dontSaveTemp.setOnAction(e -> cleanTheCodeArea());
        return dontSaveTemp;
    }

    private Button createSaveButtonForOpen() {
        Button dontSaveButton = new Button();
        dontSaveButton.setText("Save");
        dontSaveButton.setOnAction(e -> dontSaveButtonFunctionality());
        return dontSaveButton;
    }

    private void cancelButtonFunctionality() {
        saveButton = false;
        dontSaveButton = false;
        cancel = true;
        window.close();
    }

    private void creatSceneOptions() {
        window = new Stage();
        scene = sceneGeneratorOptions();
        window.setScene(scene);
        window.setMaxWidth(350);
        window.setMaxHeight(410);
        window.setMinWidth(350);
        window.setMinHeight(410);
        window.initModality(Modality.APPLICATION_MODAL);
        window.show();
    }

    private void createSceneFile(String buttonName) {
        window = new Stage();
        scene = sceneGeneratorFile(buttonName);
        window.setScene(scene);
        window.setMaxWidth(460);
        window.setMaxHeight(85);
        window.setMinWidth(460);
        window.setMinHeight(85);
        window.initModality(Modality.APPLICATION_MODAL);
        window.showAndWait();
    }

    private Slider createSlider() {
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(40);
        slider.setValue(20);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(20);
        slider.setMinorTickCount(5);
        slider.setBlockIncrement(4);
        return slider;
    }

    private PrintStream readTheOptionsBooleans(PrintStream readTo) {
        readTo.println();

/*
        List<Boolean> theOptionChangesForBooleanCheckBoxes = new ArrayList<>();
        theOptionChangesForBooleanCheckBoxes.add(0, fairAbstractionSelected);
        theOptionChangesForBooleanCheckBoxes.add(1, autoSaveSelected);
        theOptionChangesForBooleanCheckBoxes.add(2, darkModeSelected);
        theOptionChangesForBooleanCheckBoxes.add(3, pruningSelected);
        theOptionChangesForBooleanCheckBoxes.add(4, liveCompillingSelected);
*/

        readTo.println("fairAbstractionSelected: " + fairAbstractionSelected);
        readTo.println("autoSaveSelected: " + autoSaveSelected);
        readTo.println("darkModeSelected: " + darkModeSelected);
        readTo.println("pruningSelected: " + pruningSelected);
        readTo.println("liveCompillingSelected: " + liveCompillingSelected);

        return readTo;
    }


    private PrintStream readTheOptionsIntegers(PrintStream readTo) {
        readTo.println();

/*
        List<Integer> theOptionChangesForIntegereSliders = new ArrayList<>();
        theOptionChangesForIntegereSliders.add(0, lengthEdgeValue);
        theOptionChangesForIntegereSliders.add(1, maxNodeLabelValue);
        theOptionChangesForIntegereSliders.add(2, operationFailureLabelValue);
        theOptionChangesForIntegereSliders.add(3, operationPassLabelValue);
*/

        readTo.println("lengthEdgeValue: " + lengthEdgeValue);
        readTo.println("maxNodeLabelValue: " + maxNodeLabelValue);
        readTo.println("operationFailureLabelValue: " + operationFailureLabelValue);
        readTo.println("operationPassLabelValue: " + operationPassLabelValue);

        return readTo;
    }

    private void readOptions(Scanner scanner) {

        while (scanner.hasNext()){
            switch (scanner.next()){
                case "lengthEdgeValue:":
                    lengthEdgeValue = scanner.nextInt();
                    break;

                case "maxNodeLabelValue:":
                    maxNodeLabelValue = scanner.nextInt();
                    break;

                case "operationFailureLabelValue:":
                    operationFailureLabelValue = scanner.nextInt();
                    break;

                case "operationPassLabelValue:":
                    operationPassLabelValue = scanner.nextInt();
                    break;

                case "fairAbstractionSelected:":
                    fairAbstractionSelected = scanner.nextBoolean();
                    break;

                case "darkModeSelected:":
                    darkModeSelected = scanner.nextBoolean();
                    break;

                case "pruningSelected:":
                    pruningSelected = scanner.nextBoolean();
                    break;

                case "liveCompillingSelected:":
                    liveCompillingSelected = scanner.nextBoolean();
                    break;
            }
        }
    }

    private Scene sceneGeneratorOptions() {
        GridPane grid = createGrideOptions();
        scene = new Scene(grid, 500, 100);
        return scene;
    }

    private Scene sceneGeneratorFile(String buttonName) {
        GridPane grid = createBoxForFile(buttonName);
        scene = new Scene(grid, 460, 85);
        return scene;
    }

    private void setBackFlags() {
        saveButton = false;
        dontSaveButton = false;
        cancel = false;
    }

    private void cleanTheCodeArea() {
        dontSaveButton = true;
        String length = userCodeInput.getText();
        int size = length.length();
        userCodeInput.deleteText(0, size);
        this.window.close();
    }


    private void fairAbstractionFunctionality() {fairAbstractionSelected = (!fairAbstractionSelected);}
    private void autoSaveFunctionality() {
        autoSaveSelected = (!autoSaveSelected);
    }
    private void darkModeFunctionality() {
        darkModeSelected = (!darkModeSelected);
    }
    private void pruningFunctionality() {
        pruningSelected = (!pruningSelected);
    }
    private void liveCompilingFunctionality() {
        liveCompillingSelected = (!liveCompillingSelected);
    }


/*    public boolean isAutoSaveSelected() { return autoSaveSelected;}
    public boolean isFairAbstractionSelected() {
        return fairAbstractionSelected;
    }
    public boolean isDarkModeSelected() {
        return darkModeSelected;
    }
    public boolean isPruningSelected() {
        return pruningSelected;
    }
    public boolean isLiveCompillingSelected() {
        return liveCompillingSelected;
    }*/
}





