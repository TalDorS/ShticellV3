package app;
import javafx.application.Application;
import javafx.stage.Stage;
import manager.SpreadsheetManager;


public class Main  extends Application {

    public static void main(String[] args) {
        Thread.currentThread().setName("Main");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        SpreadsheetManager manager = new SpreadsheetManager(stage);
        manager.runApp();
    }
}
