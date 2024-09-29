package services;

import com.google.gson.Gson;
import models.User;
import okhttp3.*;

import java.io.IOException;

public class LoginService {
    private static final String LOGIN_URL = "http://localhost:8080/shticell/login";
    private final OkHttpClient client;
    private final Gson gson;

    public LoginService() {
        client = new OkHttpClient();
        gson = new Gson();
    }

    public String login(User user) throws IOException {
        RequestBody body = RequestBody.create(gson.toJson(user), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isRedirect()) {
                return "Error: " + response.body().string();
            }

            return response.body().string();
        }
    }
}
