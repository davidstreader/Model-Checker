package mc.client.ui;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;

import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import mc.client.ModelView;
import mc.compiler.CompilationObject;
import mc.compiler.CompilationObservable;
import mc.compiler.Compiler;
import mc.compiler.OperationResult;
import mc.exceptions.CompilationException;
import mc.util.LogAST;
import mc.util.expr.Expression;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static mc.client.ui.SyntaxHighlighting.computeHighlighting;

public class UserInterfaceController implements Initializable {
    private boolean holdHighlighting = false; // If there is an compiler issue, highlight the area. Dont keep applying highlighting it wipes it out
    private javafx.stage.Popup autocompleteBox = new javafx.stage.Popup();
    private ExecutorService executor; // Runs the highlighting in separate thread
    private TrieNode<String> completionDictionary;
    private SettingsController settingsController;

    @FXML
    private CodeArea userCodeInput;
    @FXML
    private TextArea compilerOutputDisplay;
    @FXML
    private SwingNode modelDisplay;
    @FXML
    private ComboBox<String> modelsList;
    @FXML
    private MenuItem newMenuItem;
    @FXML
    private MenuItem openMenuItem;
    @FXML
    private MenuItem saveMenuItem;
    @FXML
    private Menu openRecentTab;
    @FXML
    private Button compileButton;


    // for keep updating the file that has already been saved.
    private File currentOpenFile = null;
    private boolean modified = false;

    private Thread buildThread = new Thread();

    @Getter
    private ArrayDeque<String> recentFilePaths = new ArrayDeque<>();


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

        try {
            //Load recent files
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("recentfiles.conf")));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isEmpty())
                    addRecentFile(line);
            }
            bufferedReader.close();

        } catch (IOException e) {
            System.out.println("Error reading the settings file.");
        }

        settingsController = new SettingsController();

        ModelView.getInstance().setSettings(settingsController);

        // Have to initialise it or there is a delay between the graph becoming ready and actually displaying things
        SwingUtilities.invokeLater(() -> modelDisplay.setContent(ModelView.getInstance().updateGraph(modelDisplay)));

        //register a callback for whenever the list of automata is changed
        ModelView.getInstance().setListOfAutomataUpdater(this::updateModelsList);
        //register a callback for the output of the log
        ModelView.getInstance().setUpdateLog(this::updateLogText);

        //Add the key combinations
        newMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        saveMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        openMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));

        //So the controller can save the settings we perhaps generate, we need to give it a reference to here
        UserInterfaceApplication.setController(this);


        //add all the syntax to the completion dictionary
        completionDictionary = new TrieNode<>(new ArrayList<>(Arrays.asList(SyntaxHighlighting.processTypes)));
        completionDictionary.add(new ArrayList<>(Arrays.asList(SyntaxHighlighting.functions)));
        completionDictionary.add(new ArrayList<>(Arrays.asList(SyntaxHighlighting.keywords)));

        //add style sheets
        userCodeInput.setStyle("-fx-background-color: #32302f;");
        userCodeInput.getStylesheets().add(getClass().getResource("/clientres/automata-keywords.css").toExternalForm());

        ListView<String> popupSelection = new ListView<>();
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
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
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
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()) && !holdHighlighting) // Hold highlighting if we have an issue and have highlighted it, otherwise it gets wiped.
                .successionEnds(Duration.ofMillis(20))
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(userCodeInput.richChanges())
                .filterMap(t -> {
                    if (t.isSuccess()) {
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
                    modified = true;
                    holdHighlighting = false;
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

                                            if (userCodeInput.getCaretBounds().isPresent())
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


                });
    }

    /**
     * This is a helper function to add an insert
     *
     * @param popupSelection
     * @param selectedItem
     */
    private void actOnSelect(ListView<String> popupSelection, String selectedItem) {
        if (selectedItem != null) {

            String code = userCodeInput.getText();
            int wordPosition = userCodeInput.getCaretPosition() - 1; // we know where the user word is, but we dont know the start or end

            int start;
            for (start = wordPosition;
                 start > 0 &&
                         !Character.isWhitespace(code.charAt(start - 1)) &&
                         Character.isLetterOrDigit(userCodeInput.getText().charAt(start - 1));
                 start--)
                ;

            int end;
            for (end = wordPosition;
                 end < code.length() &&
                         !Character.isWhitespace(code.charAt(end)) &&
                         Character.isLetterOrDigit(userCodeInput.getText().charAt(end));
                 end++)
                ;


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
             index--)
            ;

        // get prefix and startIndex of word
        String prefix = text.substring(index + 1, text.length());

        // get first whitespace forward from caret
        for (index = pos;
             index < userCodeInput.getLength() &&
                     !Character.isWhitespace(userCodeInput.getText().charAt(index)) &&
                     Character.isLetterOrDigit(userCodeInput.getText().charAt(index));
             index++)
            ;

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

     boolean saveUserChanges() {
        if (modified) {

            Alert save = new Alert(Alert.AlertType.NONE);

            save.setTitle("Current file is modified");
            save.setContentText("Would you like to save changes?");

            ButtonType confirmSave = new ButtonType("Save", ButtonBar.ButtonData.YES);
            ButtonType dismissSave = new ButtonType("Dont save", ButtonBar.ButtonData.NO);
            ButtonType cancelOperation = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            save.getButtonTypes().setAll(confirmSave, dismissSave, cancelOperation);
            save.initModality(Modality.APPLICATION_MODAL); /* *** */
            save.initOwner(modelDisplay.getScene().getWindow());

            save.showAndWait();

            ButtonType result = save.getResult();
            if (result == confirmSave) {
                File selectedFile = currentOpenFile;

                if (selectedFile == null) {

                    FileChooser chooser = new FileChooser();
                    chooser.setTitle("Save file");
                    selectedFile = chooser.showSaveDialog(modelDisplay.getScene().getWindow());
                }

                if (selectedFile != null) { // Can still be null if they dont select anything in the saveDialog
                    try {
                        PrintStream writeTo = new PrintStream(selectedFile, "UTF-8");
                        writeTo.println(userCodeInput.getText());
                        writeTo.close();

                    } catch (IOException message) {
                        Alert saveFailed = new Alert(Alert.AlertType.ERROR);
                        saveFailed.setTitle("Error encountered when saving file");
                        saveFailed.setContentText("Error: " + message.getMessage());

                        saveFailed.getButtonTypes().setAll(new ButtonType("Okay", ButtonBar.ButtonData.CANCEL_CLOSE));
                        saveFailed.initModality(Modality.APPLICATION_MODAL);
                        saveFailed.initOwner(modelDisplay.getScene().getWindow());
                        saveFailed.show();
                    }
                }

            } else if (result != dismissSave) { // If the user has pressed cancel
                return false;
            }
        }

        return true;
    }


    @FXML
    private void handleCreateNew(ActionEvent event) {
        if (saveUserChanges()) {
            currentOpenFile = null;
            userCodeInput.clear();
            modified = false;
            holdHighlighting = false;
            UserInterfaceApplication.getPrimaryStage().setTitle("Process Modeller - New File");
        }
    }

    private void openFile(String filePath) {

        if (saveUserChanges()) {
            try {
                File selectedFile;

                if (filePath != null) {
                    selectedFile = new File(filePath);
                } else {
                    FileChooser openDialog = new FileChooser();
                    openDialog.setTitle("Open file");
                    selectedFile = openDialog.showOpenDialog(modelDisplay.getScene().getWindow());
                }

                if (selectedFile != null) {
                    String data = Files.toString(selectedFile, Charsets.UTF_8);

                    userCodeInput.replaceText(data);
                    currentOpenFile = selectedFile;
                    UserInterfaceApplication.getPrimaryStage().setTitle("Process Modeller - " + currentOpenFile.getName());
                    modified = false;
                    holdHighlighting = false;
                    addRecentFile(selectedFile.getAbsolutePath());
                }

            } catch (IOException e) {
                Alert saveFailed = new Alert(Alert.AlertType.ERROR);
                saveFailed.setTitle("Error encountered when reading file");
                saveFailed.setContentText("Error: " + e.getMessage());

                saveFailed.getButtonTypes().setAll(new ButtonType("Okay", ButtonBar.ButtonData.CANCEL_CLOSE));
                saveFailed.initModality(Modality.APPLICATION_MODAL);
                saveFailed.initOwner(modelDisplay.getScene().getWindow());
                saveFailed.show();
            }
        }
    }


    @FXML
    private void handleOpen(ActionEvent event) {
        openFile(null);
    }

    @FXML
    private void handleFileClose(ActionEvent event) {
        if (saveUserChanges()) {
            userCodeInput.clear();
            currentOpenFile = null;
            modified = false;

            UserInterfaceApplication.getPrimaryStage().setTitle("Process Modeller - New File");
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        File selectedFile = currentOpenFile;

        if (selectedFile == null) {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save file");
            selectedFile = chooser.showSaveDialog(modelDisplay.getScene().getWindow());
        }

        if (selectedFile != null) {
            try {
                PrintStream writeTo = new PrintStream(selectedFile, "UTF-8");
                writeTo.println(userCodeInput.getText());
                modified = false;
                currentOpenFile = selectedFile;
                UserInterfaceApplication.getPrimaryStage().setTitle("Process Modeller - " + currentOpenFile.getName());
                addRecentFile(selectedFile.getAbsolutePath());
            } catch (IOException e) {
                Alert saveFailed = new Alert(Alert.AlertType.ERROR);
                saveFailed.setTitle("Error encountered when saving file");
                saveFailed.setContentText("Error: " + e.getMessage());

                saveFailed.initModality(Modality.APPLICATION_MODAL);
                saveFailed.initOwner(modelDisplay.getScene().getWindow());

                saveFailed.getButtonTypes().setAll(new ButtonType("Okay", ButtonBar.ButtonData.CANCEL_CLOSE));
                saveFailed.show();
            }
        }
    }


    @FXML
    private void handleSaveAs(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save as");
        File selectedFile = chooser.showSaveDialog(modelDisplay.getScene().getWindow());

        if (selectedFile != null) {
            try {
                PrintStream writeTo = new PrintStream(selectedFile, "UTF-8");
                writeTo.println(userCodeInput.getText());
                currentOpenFile = selectedFile;
                UserInterfaceApplication.getPrimaryStage().setTitle("Process Modeller - " + currentOpenFile.getName());
                modified = false;
                addRecentFile(selectedFile.getAbsolutePath());

            } catch (IOException e) {
                Alert saveFailed = new Alert(Alert.AlertType.ERROR);
                saveFailed.setTitle("Error encountered when saving file");
                saveFailed.setContentText("Error: " + e.getMessage());

                saveFailed.initModality(Modality.APPLICATION_MODAL);
                saveFailed.initOwner(modelDisplay.getScene().getWindow());

                saveFailed.getButtonTypes().setAll(new ButtonType("Okay", ButtonBar.ButtonData.CANCEL_CLOSE));
                saveFailed.show();
            }

        }

    }

    @FXML
    private void handleQuit(ActionEvent event) {
        if (saveUserChanges()) {
            UserInterfaceApplication.getPrimaryStage().hide();
        }
    }


    @FXML
    private void handleAddSelectedModel(ActionEvent event) {
        if (modelsList.getSelectionModel().getSelectedItem() != null && modelsList.getSelectionModel().getSelectedItem() instanceof String) {

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
        if (selecteditem != null) {
            ModelView.getInstance().freezeProcessModel(selecteditem);
        }
    }

    @FXML
    private void handleUnfreeze(ActionEvent event) {
        String selecteditem = modelsList.getSelectionModel().getSelectedItem();
        if (selecteditem != null) {
            ModelView.getInstance().unfreezeProcessModel(selecteditem);
        }
    }

    @FXML
    private void handleFreezeAll(ActionEvent event) {
        String selecteditem = modelsList.getSelectionModel().getSelectedItem();
        if (selecteditem != null) {
            ModelView.getInstance().freezeAllCurrentlyDisplayed();
        }
    }

    @FXML
    private void handleUnfreezeAll(ActionEvent event) {
        String selecteditem = modelsList.getSelectionModel().getSelectedItem();
        if (selecteditem != null) {
            ModelView.getInstance().unfreezeAllCurrentlyDisplayed();
        }
    }


    @FXML
    private void handleOptions(ActionEvent event) {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientres/SettingsInterface.fxml"));

        loader.setController(settingsController);
        try {
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Settings");

            Scene windowScene = new Scene(loader.load(), 402, 326);
            settingsStage.setScene(windowScene);

            settingsController.setWindow(settingsStage.getScene().getWindow());
            settingsStage.initOwner(UserInterfaceApplication.getPrimaryStage());
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.setResizable(false);
            settingsStage.show();

        } catch (IOException e) {
            Alert optionsLayoutLoadFailed = new Alert(Alert.AlertType.ERROR);
            optionsLayoutLoadFailed.setTitle("Error encountered when loading options panel layout");
            optionsLayoutLoadFailed.setContentText("Error: " + e.getMessage());

            optionsLayoutLoadFailed.initModality(Modality.APPLICATION_MODAL);
            optionsLayoutLoadFailed.initOwner(modelDisplay.getScene().getWindow());

            optionsLayoutLoadFailed.getButtonTypes().setAll(new ButtonType("Okay", ButtonBar.ButtonData.CANCEL_CLOSE));
            optionsLayoutLoadFailed.show();
        }
    }

    //TODO: make this a better concurrent process
    @FXML
    private void handleCompileRequest(ActionEvent event) {
        String userCode = userCodeInput.getText();

        if (!userCode.isEmpty()) {

            if(buildThread.isAlive()) {
                buildThread.stop();
                compilerOutputDisplay.appendText("Build cancelled" + "\n");
                compileButton.setText("Compile");

            } else {

                compilerOutputDisplay.appendText("Starting build..." + "\n");

                compilerOutputDisplay.clear();
                modelsList.getItems().clear();

                buildThread = new Thread(() -> {
                    BlockingQueue<Object> messageLog = new LinkedBlockingQueue<>();
                    try {
                        // This follows the observer pattern.
                        // Within the compile function the code is then told to update an observer
                        Compiler codeCompiler = new Compiler();

                        Thread logThread = new Thread(() -> {
                            while (true) { // Realitively expensive spinning lock

                                if (!messageLog.isEmpty()) {
                                    LogAST t = ((LogAST) messageLog.poll());
                                    Platform.runLater(() -> compilerOutputDisplay.appendText(t.getMessage() + "\n"));
                                }
                                try {
                                    Thread.sleep(10); // To stop free spinning eating cpu
                                } catch (InterruptedException e) {
                                }
                            }
                        });

                        logThread.setDaemon(true); // Means the thread doesnt hang the appication on close
                        logThread.start();


                        //Keep the actual compilition outside the javafx thread otherwise we get hanging
                        CompilationObject compilerOutput = codeCompiler.compile(userCode, Expression.mkCtx(), messageLog);

                        Platform.runLater(() -> {
                            CompilationObservable.getInstance().updateClient(compilerOutput); // If this is run outside the fx thread then exceptions occur and weirdness with threads updating combox box and whatnot
                            compilerOutputDisplay.appendText("Compiling completed sucessfully!\n" + new Date().toString());
                        });
                        logThread.stop();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (CompilationException e) {

                        Platform.runLater(() -> { // Update anything that goes wrong on the main fx thread.

                            holdHighlighting = true;
                            compilerOutputDisplay.appendText(e.toString());
                            if (e.getLocation() != null) {
                                compilerOutputDisplay.appendText("\n" + e.getLocation());
                                if (e.getLocation().getStartIndex() > 0 && e.getLocation().getStartIndex() < userCodeInput.getText().length())
                                    userCodeInput.setStyleClass(e.getLocation().getStartIndex(), e.getLocation().getEndIndex(), "issue");
                            }
                        });
                    }

                    Platform.runLater(() -> {
                        compileButton.setText("Compile");
                    });


                });

                buildThread.setDaemon(true);
                buildThread.start();

                compileButton.setText("Stop Build");

            }

        }
    }


    //helpers for ModelView

    /**
     * This recieves a list of all valid models and registers them with the combobox
     *
     * @param models a collection of the processIDs of all valid models
     */
    private void updateModelsList(Collection<String> models) {
        modelsList.getItems().clear();
        models.forEach(modelsList.getItems()::add);
        modelsList.getSelectionModel().selectFirst();
    }

    private void updateLogText(List<OperationResult> opRes, List<OperationResult> eqRes) {
        if (opRes.size() > 0)
            compilerOutputDisplay.appendText("\n##Operation Results##\n");

        opRes.forEach(o -> compilerOutputDisplay.appendText(o.getProcess1().getIdent() + " " + o.getOperation() + " " +
                o.getProcess2().getIdent() + " = " + o.getResult() + "\n"));


        if (eqRes.size() > 0) {
            compilerOutputDisplay.appendText("\n##Equation Results##\n");

            for (OperationResult result : eqRes) {
                compilerOutputDisplay.appendText(result.getProcess1().getIdent() + " " + result.getOperation() + " " +
                        result.getProcess2().getIdent() + " = " + result.getResult() + "\n");


                if (result.getFailures().size() > 0) {
                    compilerOutputDisplay.appendText("\tFailing Combinations: \n");

                    for (String failure : result.getFailures())
                        compilerOutputDisplay.appendText("\t\t" + failure + " " );
                }

                compilerOutputDisplay.appendText("\tSimulations passed: " + result.getExtra() + "\n");

            }
        }

    }

    private void addRecentFile(String filePath) {
        if (!recentFilePaths.contains(filePath)) {
            while (recentFilePaths.size() > 5) { // Incase someone adds a shit ton of entries into the settings file
                recentFilePaths.pollLast();
            }


            recentFilePaths.offerFirst(filePath);


            openRecentTab.getItems().clear();

            for (String path : recentFilePaths) {
                MenuItem newItem = new MenuItem(path);
                newItem.setOnAction(e -> {
                    openFile(path);
                });
                openRecentTab.getItems().add(newItem);
            }
        }

    }


}
