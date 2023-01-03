package model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;

public class SearchResults {

    final Gson gson;

    List<Album> albums;
    List<Artist> artists;
    List<Playlist> playlists;
    List<Track> tracks;

    final private String TAG = "SearchResults.java";

    public SearchResults(JsonObject json, Gson gson) {
        this.gson = gson;
        Init(json);
    }

    private void Init(JsonObject json) {
        InitAlbums(json);
        InitArtists(json);
        InitPlaylists(json);
    }

    private void InitAlbums(JsonObject json) {
        albums = new ArrayList<>();

        JsonArray jsonArray = json.getAsJsonObject("albums").getAsJsonArray("items");

        // Loop through and store all the albums
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("data");

            JsonArray imageJsonArray = jsonObject.getAsJsonObject("coverArt").getAsJsonArray("sources");
            PhotoUrl[] photoUrls = new PhotoUrl[imageJsonArray.size()];
            for (int j = 0; j < imageJsonArray.size(); j++) {
                photoUrls[j] = new PhotoUrl(URI.create(imageJsonArray.get(j).getAsJsonObject().get("url").getAsString()),
                        imageJsonArray.get(j).getAsJsonObject().get("width").getAsDouble(),
                        imageJsonArray.get(j).getAsJsonObject().get("height").getAsDouble());
            }

            JsonArray artistJsonArray = jsonObject.getAsJsonObject("artists").getAsJsonArray("items");
            String[] artistNames = new String[artistJsonArray.size()];
            String[] artistIds = new String[artistJsonArray.size()];
            for (int j = 0; j < artistJsonArray.size(); j++) {
                artistNames[j] = artistJsonArray.get(j).getAsJsonObject().get("profile")
                        .getAsJsonObject().get("name").getAsString();
                artistIds[j] = artistJsonArray.get(j).getAsJsonObject().get("uri").getAsString();
            }


            albums.add(new Album(jsonObject.get("uri").getAsString(),
                    jsonObject.getAsJsonObject().get("name").getAsString(),
                    photoUrls, artistNames, artistIds));
        }
        Log.d(TAG, "Albums initialized.");
    }

    private void InitArtists(JsonObject json) {
        artists = new ArrayList<>();

        JsonArray jsonArray = json.getAsJsonObject("artists").getAsJsonArray("items");

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("data");

            JsonArray imageJsonArray = jsonObject.getAsJsonObject("visuals").getAsJsonObject("avatarImage")
                    .getAsJsonArray("sources");
            PhotoUrl[] photoUrls = new PhotoUrl[imageJsonArray.size()];
            for (int j = 0; j < imageJsonArray.size(); j++) {
                photoUrls[j] = new PhotoUrl(URI.create(imageJsonArray.get(j).getAsJsonObject().get("url").getAsString()),
                        imageJsonArray.get(j).getAsJsonObject().get("width").getAsDouble(),
                        imageJsonArray.get(j).getAsJsonObject().get("height").getAsDouble());
            }

            artists.add(new Artist(jsonObject.get("uri").getAsString(),
                    jsonObject.getAsJsonObject().get("profile").getAsJsonObject().get("name").getAsString(),
                    photoUrls));
        }
        Log.d(TAG, "Artists initialized.");
    }

    private void InitPlaylists(JsonObject json) {
        playlists = new ArrayList<>();

        JsonArray jsonArray = json.getAsJsonObject("playlists").getAsJsonArray("items");

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("data");

            JsonArray imageJsonArray = jsonObject.getAsJsonObject("images").getAsJsonArray("items");
            PhotoUrl[] photoUrls = new PhotoUrl[1];

            JsonArray sourcesJsonArray = imageJsonArray.get(0).getAsJsonObject().getAsJsonArray("sources");


            double width =
                    (sourcesJsonArray.get(0).getAsJsonObject().get("width").isJsonNull())
                            ? 0
                            : sourcesJsonArray.get(0).getAsJsonObject().get("width").getAsDouble();


            double height =
                    (sourcesJsonArray.get(0).getAsJsonObject().get("height").isJsonNull())
                            ? 0
                            : sourcesJsonArray.get(0).getAsJsonObject().get("height").getAsDouble();


            photoUrls[0] = new PhotoUrl(
                    URI.create(
                            sourcesJsonArray.get(0).getAsJsonObject().get("url").getAsString()
                    ), width, height);


            playlists.add(new Playlist(jsonObject.get("uri").getAsString(),
                    jsonObject.get("name").getAsString(),
                    photoUrls, jsonObject.getAsJsonObject("owner").getAsJsonObject().get("name").getAsString(),
                    jsonObject.get("description").getAsString()));


        }
        Log.d(TAG, "Playlists initialized.");
    }

}
