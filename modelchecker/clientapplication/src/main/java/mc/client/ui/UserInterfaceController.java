package mc.client.ui;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import mc.client.ModelView;
import mc.compiler.Compiler;
import mc.exceptions.CompilationException;
import mc.util.expr.Expression;
import mc.webserver.Context;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import javax.swing.*;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mc.client.ui.SyntaxHighlighting.computeHighlighting;

public class UserInterfaceController implements Initializable {
    private javafx.stage.Popup autocompleteBox = new javafx.stage.Popup();
    private ExecutorService executor;
    private TrieNode<String> completionDictionary;

    @FXML private CodeArea userCodeInput;

    @FXML private SwingNode modelDisplay;

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
        SwingUtilities.invokeLater(() -> modelDisplay.setContent(ModelView.getInstance().getGraphComponent()));
        completionDictionary = new TrieNode<>(  new ArrayList<>(Arrays.asList(SyntaxHighlighting.processTypes)) );
        completionDictionary.add(new ArrayList<>(Arrays.asList(SyntaxHighlighting.functions)));
        completionDictionary.add(new ArrayList<>(Arrays.asList(SyntaxHighlighting.keywords)));

        userCodeInput.setStyle("-fx-background-color: #32302f;");
        userCodeInput.getStylesheets().add(getClass().getResource("/clientres/automata-keywords.css").toExternalForm());

        ListView popupSelection = new ListView();
        popupSelection.setStyle(
                "-fx-background-color: #f7e1a0;" +
                        "-fx-text-fill: black;" +
                        "-fx-padding: 5;"
        );


        popupSelection.setOnMouseClicked(event -> {
            String selectedItem = (String)popupSelection.getSelectionModel().getSelectedItem();
            if(selectedItem != null) {

                String code = userCodeInput.getText();
                int wordPosition = userCodeInput.getCaretPosition()-1; // we know where the user word is, but we dont know the start or end

                int start = 0;
                for(start = wordPosition; start > 0 && !Character.isWhitespace(code.charAt(start-1)) && Character.isLetterOrDigit(userCodeInput.getText().charAt(start-1)); start--);


                int end = 0;
                for(end = wordPosition; end < code.length() && !Character.isWhitespace(code.charAt(end)) && Character.isLetterOrDigit(userCodeInput.getText().charAt(end)) ; end++);


                userCodeInput.replaceText(start, end, selectedItem);

                popupSelection.getItems().clear();
                autocompleteBox.hide();

                userCodeInput.setStyleSpans(0, computeHighlighting(userCodeInput.getText())); // Need to reupdate the styles when an insert has happened.
            }
        });

        popupSelection.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                String selectedItem = (String)popupSelection.getSelectionModel().getSelectedItem();
                if(selectedItem != null) {


                    String code = userCodeInput.getText();
                    int wordPosition = userCodeInput.getCaretPosition()-1; // we know where the user word is, but we dont know the start or end

                    //Find the first whitespace or special character, so statements like for(int will give an autocomplete option
                    int start = 0;
                    for(start = wordPosition; start > 0 && !Character.isWhitespace(code.charAt(start-1)) && Character.isLetterOrDigit(userCodeInput.getText().charAt(start-1)); start--);

                    int end = 0;
                    for(end = wordPosition; end < code.length() && !Character.isWhitespace(code.charAt(end)) && Character.isLetterOrDigit(userCodeInput.getText().charAt(end)); end++);


                    userCodeInput.replaceText(start, end, selectedItem);

                    popupSelection.getItems().clear();
                    autocompleteBox.hide();

                    userCodeInput.setStyleSpans(0, computeHighlighting(userCodeInput.getText())); // Need to reupdate the styles when an insert has happened.
                }
            }

        });

        autocompleteBox.getContent().add(popupSelection);

        executor = Executors.newSingleThreadExecutor();

        userCodeInput.setParagraphGraphicFactory(LineNumberFactory.get(userCodeInput)); // Add line numbers

        userCodeInput.richChanges() // Set up syntax highlighting in another thread as regex finding can take a while.
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
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

        userCodeInput.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(( change) -> { // Hook for detecting user input, used for autocompletion as that happens quickly.
            if(change.getInserted().getStyleOfChar(0).isEmpty()) { // If this isnt a style event rather than the user typing.
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

                                        autocompleteBox.show(userCodeInput, userCodeInput.getCaretBounds().get().getMinX(), userCodeInput.getCaretBounds().get().getMaxY());

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

            }

        });

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
        for (index = text.length() - 1; index >= 0 && !Character.isWhitespace(text.charAt(index)) && Character.isLetterOrDigit(userCodeInput.getText().charAt(index)); index--);

        // get prefix and startIndex of word
        String prefix = text.substring(index + 1, text.length());

        // get first whitespace forward from caret
        for (index = pos; index < userCodeInput.getLength() && !Character.isWhitespace(userCodeInput.getText().charAt(index)) && Character.isLetterOrDigit(userCodeInput.getText().charAt(index)); index++);

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

    }

    @FXML
    private void handleAddallModels(ActionEvent event) {

    }

    @FXML
    private void handleClearCanvas(ActionEvent event) {

    }

    @FXML
    private void handlExportImage(ActionEvent event) {

    }

    @FXML
    private void handOptionsRequest(ActionEvent event) {

    }

    @FXML
    private void handleCompileRequest(ActionEvent event) {
        String userCode = userCodeInput.getText();
        if(!userCode.isEmpty()) {
            System.out.println("Handling compile request!");

            try {
                Compiler codeCompiler = new Compiler();
                codeCompiler.compile(userCode, new Context(), Expression.mkCtx(), new LinkedBlockingQueue<>()); // This follows the observer pattern. Within the compile function the code is then told to update an observer
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (CompilationException e) {
                e.printStackTrace();
            }

            SwingUtilities.invokeLater(() -> modelDisplay.setContent(ModelView.getInstance().getGraphComponent()));
        }
    }

    @FXML
    private void handleSaveAs(ActionEvent event) {


    }

}
