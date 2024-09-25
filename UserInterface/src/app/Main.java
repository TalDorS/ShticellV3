package app;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Request;
import javafx.application.Application;
import javafx.stage.Stage;
import manager.SpreadsheetManager;
import okhttp3.OkHttpClient;


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
