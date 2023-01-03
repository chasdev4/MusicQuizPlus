package service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import model.SearchResults;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SpotifyService {

    private final String _key;

    public SpotifyService(String key) {
        _key = key;
    }

    public SearchResults Search(String query, short limit, int offset) {
        SearchResults searchResults;
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("https://spotify23.p.rapidapi.com/search/?q=" + query + "&type=multi&offset=" + offset +"&limit=" + limit + "&numberOfTopResults=5")
                .get()
                .addHeader("X-RapidAPI-Key", _key)
                .addHeader("X-RapidAPI-Host", "spotify23.p.rapidapi.com")
                .build();

        try (Response response = client.newCall(request).execute())
        {
            String json = response.body().string();

            Gson gson = new Gson();
            TypeToken<SearchResults> mapType = new TypeToken<SearchResults>(){};

            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            searchResults = new SearchResults(jsonObject, gson);

            return null;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
