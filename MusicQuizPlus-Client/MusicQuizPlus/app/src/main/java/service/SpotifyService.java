package service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

import model.Search;
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

    public Gson getGson() {
        return gson;
    }

    // Search endpoint
    public JsonObject search(String query, int limit, int offset) {
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
            JsonObject jsonObject = gson.fromJson(json, JsonElement.class).getAsJsonObject();

            // Populate Search Results model and return
            return jsonObject;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Artist Overview Endpoint
    public Artist artistOverview(String artistId) {
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
            JsonObject jsonObject = gson.fromJson(json, JsonElement.class).getAsJsonObject();

            // Populate Artist model and return
            return new Artist(jsonObject);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Album Tracks endpoint
    public JsonObject albumTracks(String albumId) {
        String[] albumIdArray = albumId.split(":");

        // Create the client and request
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("https://spotify23.p.rapidapi.com/album_tracks/?id="
                        + albumIdArray[2]
                )
                .get()
                .addHeader("X-RapidAPI-Key", _key)
                .addHeader("X-RapidAPI-Host", "spotify23.p.rapidapi.com")
                .build();

        try (Response response = client.newCall(request).execute())
        {
            // Use gson to get a JsonObject
            String json = response.body().string();
            return gson.fromJson(json, JsonElement.class).getAsJsonObject();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Playlist Tracks endpoint
    public JsonArray playlistTracks(String playlistId) {
        String[] playlistIdArray = playlistId.split(":");
        // Create the client and request
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("https://spotify23.p.rapidapi.com/playlist_tracks/?id="
                        + playlistIdArray[2]
                )
                .get()
                .addHeader("X-RapidAPI-Key", _key)
                .addHeader("X-RapidAPI-Host", "spotify23.p.rapidapi.com")
                .build();

        try (Response response = client.newCall(request).execute())
        {
            // Use gson to get a JsonObject
            String json = response.body().string();
            return gson.fromJson(json, JsonElement.class).getAsJsonObject().getAsJsonArray("items");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // User profile endpoint
    // ONLY USED TO RETRIEVE / UPDATE DEFAULT PLAYLISTS
    public JsonArray getDefaultPlaylists() {
        // Create the client and request
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("https://spotify23.p.rapidapi.com/user_profile/?id=spotify&playlistLimit=50&artistLimit=50")
                .get()
                .addHeader("X-RapidAPI-Key", _key)
                .addHeader("X-RapidAPI-Host", "spotify23.p.rapidapi.com")
                .build();

        try (Response response = client.newCall(request).execute())
        {
            // Use gson to get a JsonObject
            String json = response.body().string();
            return gson.fromJson(json, JsonElement.class).getAsJsonObject().getAsJsonArray("public_playlists");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
