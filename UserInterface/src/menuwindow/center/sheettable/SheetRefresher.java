package menuwindow.center.sheettable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import menuwindow.center.sheettable.models.SheetDetails;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import utils.AlertUtils;
import utils.ClientConstants;
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
        String url = ClientConstants.GET_SHEET_DETAILS;

        HttpClientUtil.runAsyncGet(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call,@NotNull IOException e) {
                Platform.runLater(() -> {
                    AlertUtils.showAlert(Alert.AlertType.ERROR,"Failed to fetch sheet details: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call,@NotNull Response response) throws IOException {
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
                        AlertUtils.showAlert(Alert.AlertType.ERROR,"Failed to fetch sheet details: " + response.message());
                    });
                }
            }
        });
    }
}

