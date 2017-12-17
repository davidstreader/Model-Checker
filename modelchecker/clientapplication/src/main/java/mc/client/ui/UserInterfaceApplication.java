package mc.client.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.lang.reflect.Method;

public class UserInterfaceApplication extends Application {

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     * <p>
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set. The primary stage will be embedded in
     *                     the browser if the application was launched as an applet.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages and will not be embedded in the browser.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        setMacDockIcon(new Image(getClass().getResourceAsStream("/clientres/icon.jpg")));
        Font.loadFont(getClass().getResource("/clientres/hasklig.otf").toExternalForm(),10);
        Parent root = FXMLLoader.load(getClass().getResource("/clientres/UserInterface.fxml"));

        primaryStage.setTitle("Automata Modeller");
        Scene windowScene = new Scene(root, 1000, 700);
        primaryStage.setScene(windowScene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/clientres/icon.jpg")));
        primaryStage.show();
        primaryStage.setOnHiding(e -> System.exit(0));
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void setMacDockIcon(Image img) {
        try {
            Class<?> applicationClass = Class.forName("com.apple.eawt.Application");

            Method getApplicationMethod = applicationClass.getMethod("getApplication");
            Method setDockIconMethod = applicationClass.getMethod("setDockIconImage", java.awt.Image.class);

            Object macOSXApplication = getApplicationMethod.invoke(null);
            setDockIconMethod.invoke(macOSXApplication, img);
        }
        catch(Exception ignored) {

        }
    }
}
