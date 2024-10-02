package app;
import javafx.application.Application;
import javafx.stage.Stage;
import manager.SpreadsheetManager;
import utils.HttpClientUtil;


public class Main extends Application {

    SpreadsheetManager manager;

    public static void main(String[] args) {
        Thread.currentThread().setName("Main");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        manager = new SpreadsheetManager(stage);
        manager.runApp();
    }

    @Override
    public void stop() {
        HttpClientUtil.shutdown();
        manager.closeApp();
    }
}
