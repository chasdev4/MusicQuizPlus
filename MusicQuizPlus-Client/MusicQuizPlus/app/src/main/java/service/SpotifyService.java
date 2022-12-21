package service;

import static android.provider.Settings.System.getString;

import com.example.musicquizplus.R;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpotifyService {

    private final String spotifyKey;

    public SpotifyService(String key) {
        spotifyKey = key;
    }

    public void Search(String query, short limit, int offset) {
        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url("https://spotify23.p.rapidapi.com/search/?q=" + query + "&type=multi&offset=" + offset +"&limit=" + limit + "&numberOfTopResults=5")
                .get()
                .addHeader("X-RapidAPI-Key", spotifyKey)
                .addHeader("X-RapidAPI-Host", "spotify23.p.rapidapi.com")
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
