package gridwindow.top;

import com.google.gson.Gson;
import dto.EngineDTO;
import gridwindow.GridWindowController;
import javafx.application.Platform;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import utils.ClientConstants;
import utils.HttpClientUtil;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpreadsheetVersionRefresher  {
    private final GridWindowController mainController;
    private ScheduledExecutorService scheduler;
    private int lastKnownVersion;

    public SpreadsheetVersionRefresher(GridWindowController controller) {
        this.mainController = controller;
        this.lastKnownVersion = -1;  // Initialize to an invalid version number
    }

    public void startRefreshing() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::checkVersion, 0, 2, TimeUnit.SECONDS);  // Run every 2 seconds
    }

    public void stopRefreshing() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    private void checkVersion() {
        try {
            // Fetch the current engine data (similar to setSpreadsheetData but only checking version)
            String url = HttpUrl
                    .parse(ClientConstants.GET_ENGINE_DATA)
                    .newBuilder()
                    .addQueryParameter("userName", mainController.getUserName())
                    .addQueryParameter("spreadsheetName", mainController.getSpreadsheetName())
                    .build()
                    .toString();

            HttpClientUtil.runAsyncGet(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Handle failure (e.g., log the error)
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // Ensure the response body is closed after use
                    try (Response res = response) {
                        if (res.isSuccessful()) {
                            String responseBody = res.body().string(); // Make sure to read it before closing
                            Gson gson = new Gson();
                            EngineDTO engineDTO = gson.fromJson(responseBody, EngineDTO.class);

                            int currentVersion = engineDTO.getCurrentVersionNumber();

                            // Compare the version numbers
                            if (lastKnownVersion == -1) {
                                // First load, set the initial version
                                lastKnownVersion = currentVersion;
                            } else if (currentVersion > lastKnownVersion && !mainController.isSpreadsheetModified()) {
                                // Version has changed, and the user has not modified the spreadsheet
                                Platform.runLater(() -> {
                                    mainController.showNewVersionAvailable();
                                });
                            }
                            mainController.setSpreadsheetModified(false);
                            lastKnownVersion = currentVersion;  // Update the last known version
                        }
                    } catch (IOException e) {
                        // Handle IOException related to reading the response body
                    }
                }

            });
        } catch (Exception e) {
            // Handle any exceptions that occur during the request
        }
    }
}
