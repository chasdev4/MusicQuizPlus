package model;

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

    public SearchResults(JsonObject json, Gson gson) {
        this.gson = gson;
        Init(json);
    }

    private void Init(JsonObject json) {
        InitAlbums(json);
    }

    private void InitAlbums(JsonObject json) {
        albums = new ArrayList<>();

        JsonArray jsonArray = json.getAsJsonObject("albums").getAsJsonArray("items");

        // Loop through and store all the albums
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("data");

            JsonArray imageJsonArray = jsonObject.getAsJsonObject("coverArt").getAsJsonArray("sources");
            PhotoUrl[] uris = new PhotoUrl[imageJsonArray.size()];
            for (int j = 0; j < imageJsonArray.size(); j++) {
                uris[j] = new PhotoUrl(URI.create(imageJsonArray.get(j).getAsJsonObject().get("url").getAsString()),
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
                    uris, artistNames, artistIds));
        }
    }

}
