package mc.client.ui;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import mc.client.ModelView;
import mc.compiler.Compiler;
import mc.compiler.OperationResult;
import mc.exceptions.CompilationException;
import mc.util.Location;
import mc.util.expr.Expression;
import mc.webserver.Context;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;

import javax.swing.*;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static mc.client.ui.SyntaxHighlighting.computeHighlighting;

public class UserInterfaceController implements Initializable {
    private boolean holdHighlighting = false;
    private javafx.stage.Popup autocompleteBox = new javafx.stage.Popup();
    private ExecutorService executor;
    private TrieNode<String> completionDictionary;

    @FXML private CodeArea userCodeInput;

    @FXML private TextArea compilerOutputDisplay;

    @FXML private SwingNode modelDisplay;

    @FXML private ComboBox modelsList;


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


        //add all the syntax to the completion dictionary
        completionDictionary = new TrieNode<>(new ArrayList<>(Arrays.asList(SyntaxHighlighting.processTypes)));
        completionDictionary.add(new ArrayList<>(Arrays.asList(SyntaxHighlighting.functions)));
        completionDictionary.add(new ArrayList<>(Arrays.asList(SyntaxHighlighting.keywords)));

        //add style sheets
        userCodeInput.setStyle("-fx-background-color: #32302f;");
        userCodeInput.getStylesheets().add(getClass().getResource("/clientres/automata-keywords.css").toExternalForm());

        ListView popupSelection = new ListView();
        popupSelection.setStyle(
                        "-fx-background-color: #f7e1a0;" +
                        "-fx-text-fill:        black;" +
                        "-fx-padding:          5;"
        );


        popupSelection.setOnMouseClicked(event -> {
            String selectedItem = (String)popupSelection.getSelectionModel().getSelectedItem();
            actOnSelect(popupSelection, selectedItem);
        });

        popupSelection.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                String selectedItem = (String)popupSelection.getSelectionModel().getSelectedItem();
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
    private void actOnSelect(ListView popupSelection, String selectedItem) {
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
    private void handleQuit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void handleCreateNew(ActionEvent event) {

    }

    @FXML
    private void handleOpenRecentAction(ActionEvent event) {


    }

    @FXML
    private void handleOpen(ActionEvent event) {

    }

    @FXML
    private void handleFileClose(ActionEvent event) {

    }

    @FXML
    private void handleSave(ActionEvent event) {

    }

    @FXML
    private void handleAddSelectedModel(ActionEvent event) {
        if(modelsList.getSelectionModel().getSelectedItem() != null && modelsList.getSelectionModel().getSelectedItem() instanceof String) {

            ModelView.getInstance().addDisplayedAutomata((String) modelsList.getSelectionModel().getSelectedItem());
            SwingUtilities.invokeLater(() -> modelDisplay.setContent(ModelView.getInstance().updateGraph(modelDisplay)));
        }
    }

    @FXML
    private void handleAddallModels(ActionEvent event) {
        ModelView.getInstance().addAllAutomata();
        SwingUtilities.invokeLater(() -> modelDisplay.setContent(ModelView.getInstance().updateGraph(modelDisplay)));
    }

    @FXML
    private void handleClearGraph(ActionEvent event) {
        ModelView.getInstance().clearDisplayed();
        SwingUtilities.invokeLater(() -> modelDisplay.setContent(ModelView.getInstance().updateGraph(modelDisplay)));
    }

    @FXML
    private void handlExportImage(ActionEvent event) {

    }

    @FXML
    private void handOptionsRequest(ActionEvent event) {

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
                compilerOutputDisplay.insertText(0,e.toString());
                Location compilerIssue = e.getLocation();
                userCodeInput.setStyleClass(compilerIssue.getStartIndex(), compilerIssue.getEndIndex(), "issue");

            }
        }
    }

    @FXML
    private void handleSaveAs(ActionEvent event) {


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
                                                            o.getProcess2().getIdent() + " = " + o.getResult() + "\n"));
    }

}
