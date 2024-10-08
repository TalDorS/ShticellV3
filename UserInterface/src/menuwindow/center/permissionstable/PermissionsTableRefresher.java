package menuwindow.center.permissionstable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.PermissionsManagerDTO;
import javafx.application.Platform;
import menuwindow.MenuWindowController;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import utils.AlertUtils;
import utils.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.TimerTask;

public class PermissionsTableRefresher extends TimerTask {
    private final MenuWindowController mainController;
    private final PermissionsTableController permissionsTableController;

    public PermissionsTableRefresher(MenuWindowController mainController) {
        this.mainController = mainController;
        this.permissionsTableController = mainController.getPermissionsTableComponentController();
    }

    @Override
    public void run() {
        String selectedSpreadsheetName = mainController.getLastSelectedSpreadsheetName();

        // No spreadsheet selected
        if (selectedSpreadsheetName == null) {
            return;
        }

        // Http request to get the permissions for the selected spreadsheet
        String finalUrl = "http://localhost:8080/Server_Web_exploded/getPermissions?spreadsheetName=" + selectedSpreadsheetName;

        HttpClientUtil.runAsyncGet(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    permissionsTableController.resetPermissionsTableToDefault();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // Use Gson to deserialize the JSON response into a PermissionsManagerDTO
                    Gson gson = new Gson();
                    Type permissionsType = new TypeToken<PermissionsManagerDTO>() {}.getType();
                    PermissionsManagerDTO newPermissionsData = gson.fromJson(responseBody, permissionsType);

                    // Compare new data with current table data
                    Platform.runLater(() -> {
                        if (!mainController.getPermissionsTableComponentController().isDataSame(newPermissionsData)) {
                            // Update the table if the data is different
                            mainController.getPermissionsTableComponentController().updatePermissionsTable(newPermissionsData);
                        }
                    });
                } else {
                    permissionsTableController.resetPermissionsTableToDefault();
                }
            }
        });
    }
}
