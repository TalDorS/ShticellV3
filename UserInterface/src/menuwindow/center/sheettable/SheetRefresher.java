package menuwindow.center.sheettable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import utils.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;

public class SheetRefresher extends TimerTask {
    private AvailableSheetTableController controller;

    public SheetRefresher(AvailableSheetTableController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        String url = "http://localhost:8080/Server_Web_exploded/sheet-details";

        HttpClientUtil.runAsyncGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> {
                    System.out.println("Failed to fetch sheet details: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // Use Gson to deserialize the response into a List of SheetDetails
                    Gson gson = new Gson();
                    Type sheetListType = new TypeToken<List<SheetDetails>>() {}.getType();
                    List<SheetDetails> newSheetDetails = gson.fromJson(responseBody, sheetListType);

                    // Compare the new data with the current table data
                    Platform.runLater(() -> {
                        if (!controller.isDataSame(newSheetDetails)) {
                            controller.updateTableWithSheetDetails(newSheetDetails);
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        System.out.println("Failed to fetch sheet details: " + response.message());
                    });
                }
            }
        });
    }
}

