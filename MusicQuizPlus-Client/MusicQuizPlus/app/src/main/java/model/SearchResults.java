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

// SUMMARY
// The Search Results model maintains lists of items models retrieved from the Spotify API

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

    // Retrieve All Search Results
    private void Init(JsonObject json) {
        InitAlbums(json);
        InitArtists(json);
        InitPlaylists(json);
        InitTracks(json);
    }

    // Retrieve Album Search Results
    private void InitAlbums(JsonObject json) {
        albums = new ArrayList<>();

        // Loop through and store all the albums
        JsonArray jsonArray = json.getAsJsonObject("albums").getAsJsonArray("items");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("data");

            // Create an inner loop to get preview images
            JsonArray imageJsonArray = jsonObject.getAsJsonObject("coverArt").getAsJsonArray("sources");
            PhotoUrl[] photoUrls = new PhotoUrl[imageJsonArray.size()];
            for (int j = 0; j < imageJsonArray.size(); j++) {
                photoUrls[j] = new PhotoUrl(URI.create(imageJsonArray.get(j).getAsJsonObject().get("url").getAsString()),
                        imageJsonArray.get(j).getAsJsonObject().get("width").getAsDouble(),
                        imageJsonArray.get(j).getAsJsonObject().get("height").getAsDouble());
            }

            // Create an inner loop to get artists
            JsonArray artistJsonArray = jsonObject.getAsJsonObject("artists").getAsJsonArray("items");
            String[] artistNames = new String[artistJsonArray.size()];
            String[] artistIds = new String[artistJsonArray.size()];
            for (int j = 0; j < artistJsonArray.size(); j++) {
                artistNames[j] = artistJsonArray.get(j).getAsJsonObject().get("profile")
                        .getAsJsonObject().get("name").getAsString();
                artistIds[j] = artistJsonArray.get(j).getAsJsonObject().get("uri").getAsString();
            }

            // Add to collection
            albums.add(new Album(jsonObject.get("uri").getAsString(),
                    jsonObject.getAsJsonObject().get("name").getAsString(),
                    photoUrls, artistNames, artistIds));
        }
        Log.d(TAG, "Albums initialized.");
    }

    // Retrieve Artist Search Results
    private void InitArtists(JsonObject json) {
        artists = new ArrayList<>();

        // Loop through and store all the artists
        JsonArray jsonArray = json.getAsJsonObject("artists").getAsJsonArray("items");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("data");

            // Create an inner loop to get preview images
            JsonArray imageJsonArray = jsonObject.getAsJsonObject("visuals").getAsJsonObject("avatarImage")
                    .getAsJsonArray("sources");
            PhotoUrl[] photoUrls = new PhotoUrl[imageJsonArray.size()];
            for (int j = 0; j < imageJsonArray.size(); j++) {
                photoUrls[j] = new PhotoUrl(URI.create(imageJsonArray.get(j).getAsJsonObject().get("url").getAsString()),
                        imageJsonArray.get(j).getAsJsonObject().get("width").getAsDouble(),
                        imageJsonArray.get(j).getAsJsonObject().get("height").getAsDouble());
            }

            // Add to collection
            artists.add(new Artist(jsonObject.get("uri").getAsString(),
                    jsonObject.getAsJsonObject().get("profile").getAsJsonObject().get("name").getAsString(),
                    photoUrls));
        }
        Log.d(TAG, "Artists initialized.");
    }

    // Retrieve Playlist Search Results
    private void InitPlaylists(JsonObject json) {
        playlists = new ArrayList<>();

        // Loop through and store all the playlists
        JsonArray jsonArray = json.getAsJsonObject("playlists").getAsJsonArray("items");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("data");

            // Get the playlist preview image, there is only 1 for playlists
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

            // Add to collection
            playlists.add(new Playlist(jsonObject.get("uri").getAsString(),
                    jsonObject.get("name").getAsString(),
                    photoUrls, jsonObject.getAsJsonObject("owner").getAsJsonObject().get("name").getAsString(),
                    jsonObject.get("description").getAsString()));


        }
        Log.d(TAG, "Playlists initialized.");
    }

    // Retrieve Track Search Results
    private void InitTracks(JsonObject json) {
        tracks = new ArrayList<>();

        // Loop through and store all the tracks
        JsonArray jsonArray = json.getAsJsonObject("tracks").getAsJsonArray("items");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("data");

            // JsonObject to retrieve album information, image array is nested inside.
            JsonObject albumOfTrack = jsonObject.getAsJsonObject("albumOfTrack");

            // Create an inner loop to get preview images
            JsonArray imageJsonArray = albumOfTrack.getAsJsonObject("coverArt").getAsJsonArray("sources");
            PhotoUrl[] photoUrls = new PhotoUrl[imageJsonArray.size()];
            for (int j = 0; j < imageJsonArray.size(); j++) {
                photoUrls[j] = new PhotoUrl(URI.create(imageJsonArray.get(j).getAsJsonObject().get("url").getAsString()),
                        imageJsonArray.get(j).getAsJsonObject().get("width").getAsDouble(),
                        imageJsonArray.get(j).getAsJsonObject().get("height").getAsDouble());
            }

            // Create an inner loop to get artists
            JsonArray artistJsonArray = jsonObject.getAsJsonObject("artists").getAsJsonArray("items");
            String[] artistNames = new String[artistJsonArray.size()];
            String[] artistIds = new String[artistJsonArray.size()];
            for (int j = 0; j < artistJsonArray.size(); j++) {
                artistNames[j] = artistJsonArray.get(j).getAsJsonObject().get("profile")
                        .getAsJsonObject().get("name").getAsString();
                artistIds[j] = artistJsonArray.get(j).getAsJsonObject().get("uri").getAsString();
            }

            // Add to collection
            tracks.add(new Track(
                    jsonObject.get("uri").getAsString(),
                    jsonObject.get("name").getAsString(),
                    photoUrls,
                    albumOfTrack.get("name").getAsString(),
                    albumOfTrack.get("uri").getAsString(),
                    artistNames,
                    artistIds));


        }
        Log.d(TAG, "Tracks initialized.");
    }

}
