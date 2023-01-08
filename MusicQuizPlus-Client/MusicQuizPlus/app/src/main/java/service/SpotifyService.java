package service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.PhotoUrl;
import model.SearchResults;
import model.item.Artist;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// SUMMARY
// The Spotify Service class retrieves data from the Spotify API

public class SpotifyService {

    private final String _key;
    private final Gson gson;

    public SpotifyService(String key) {
        _key = key;
        gson = new Gson();
    }

    // Search endpoint
    public SearchResults Search(String query, short limit, int offset) {
        // Create the client and request
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("https://spotify23.p.rapidapi.com/search/?q=" + query + "&type=multi&offset=" + offset +"&limit=" + limit + "&numberOfTopResults=5")
                .get()
                .addHeader("X-RapidAPI-Key", _key)
                .addHeader("X-RapidAPI-Host", "spotify23.p.rapidapi.com")
                .build();

        try (Response response = client.newCall(request).execute())
        {
            // Use gson to get a JsonObject
            String json = response.body().string();
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            // Populate Search Results model and return
            return new SearchResults(jsonObject, gson);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Artist ArtistOverview(String artistId) {
        String[] artistIdArray = artistId.split(":");

        // Create the client and request
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("https://spotify23.p.rapidapi.com/artist_overview/?id=" + artistIdArray[2])
                .get()
                .addHeader("X-RapidAPI-Key", _key)
                .addHeader("X-RapidAPI-Host", "spotify23.p.rapidapi.com")
                .build();

        try (Response response = client.newCall(request).execute())
        {
            // Use gson to get a JsonObject
            String json = response.body().string();
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            // Populate Search Results model and return
            return new Artist(jsonObject, gson);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
