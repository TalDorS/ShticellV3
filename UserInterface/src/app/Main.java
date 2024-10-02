package app;
import javafx.application.Application;
import javafx.stage.Stage;
import manager.AppManager;
import utils.HttpClientUtil;


public class Main extends Application {

    AppManager manager;

    public static void main(String[] args) {
        Thread.currentThread().setName("Main");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Start the first client when the application launches
        manager = new AppManager(primaryStage);
        manager.launchNewClient();
    }

    @Override
    public void stop() {
        HttpClientUtil.shutdown();
        manager.closeApp();
    }

}
