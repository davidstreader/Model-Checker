package mc.client.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

public class UserInterfaceApplication extends Application {
    @Getter
    @Setter
    static private Stage primaryStage;


    @Setter
    @Getter
    static private UserInterfaceController controller;

    static private Parent root;

    public static void resetShapes() {


    }

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     * <p>
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage_ the primary stage for this application, onto which
     *                     the application scene can be set. The primary stage will be embedded in
     *                     the browser if the application was launched as an applet.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages and will not be embedded in the browser.
     */
    @Override
    public void start(Stage primaryStage_)  {
        primaryStage = primaryStage_;
        setMacDockIcon(new Image(getClass().getResourceAsStream("/clientres/icon.jpg")));
        Font.loadFont(getClass().getResource("/clientres/hasklig.otf").toExternalForm(), 10);

        try {
            root = FXMLLoader.load(getClass().getResource("/clientres/UserInterface.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        primaryStage.setTitle("Process Modeller - New File");
        Scene windowScene = new Scene(root, 1000, 700);
        primaryStage.setScene(windowScene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/clientres/icon.jpg")));
        primaryStage.setResizable(false);
        primaryStage.show();




    }



    @Override
    public void stop() {
        if (controller.saveUserChanges()) {

            ArrayDeque<String> filePaths = controller.getRecentFilePaths();
            try {
                System.out.print("\nwriting settings file...");
                BufferedWriter writer = new BufferedWriter(new FileWriter("recentfiles.conf"));

                while (!filePaths.isEmpty())
                    writer.write(filePaths.pollLast() + "\n");

                writer.close();
                System.out.print("Done\n");

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        System.exit(0);
    }


    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        launch(args);
    }

    private void setMacDockIcon(Image img) {
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
