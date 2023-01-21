package model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.AlbumType;

// SUMMARY
// The Search Results model maintains lists of items models retrieved from the Spotify API

public class SearchResults {

    final Gson gson;

    private List<Album> albums;
    private List<Artist> artists;
    private List<Playlist> playlists;
    private List<Track> tracks;

    final private static String TAG = "SearchResults.java";

    public SearchResults(JsonObject json, Gson gson) {
        this.gson = gson;
        Init(json);
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    // Retrieve All Search Results
    private void Init(JsonObject json) {
        extractAlbums(json);
        extractArtists(json);
        extractPlaylists(json);
        extractTracks(json);
    }

    // Retrieve Album Search Results
    private void extractAlbums(JsonObject json) {
        albums = new ArrayList<>();

        // Loop through and store all the albums
        JsonArray jsonArray = json.getAsJsonObject("albums").getAsJsonArray("items");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("data");

            // Create an inner loop to get preview images
            JsonArray imageJsonArray = jsonObject.getAsJsonObject("coverArt").getAsJsonArray("sources");
            List<PhotoUrl> photoUrls = new ArrayList<>();
            for (int j = 0; j < imageJsonArray.size(); j++) {
                photoUrls.add(new PhotoUrl(imageJsonArray.get(j).getAsJsonObject().get("url").getAsString(),
                        imageJsonArray.get(j).getAsJsonObject().get("width").getAsDouble(),
                        imageJsonArray.get(j).getAsJsonObject().get("height").getAsDouble()));
            }

            // Create an inner loop to get artists
            JsonArray artistJsonArray = jsonObject.getAsJsonObject("artists").getAsJsonArray("items");
            List<String> artistNames = new ArrayList<>();
            List<String> artistIds = new ArrayList<>();
            for (int j = 0; j < artistJsonArray.size(); j++) {
                artistNames.add(artistJsonArray.get(j).getAsJsonObject().get("profile")
                        .getAsJsonObject().get("name").getAsString());
                artistIds.add(artistJsonArray.get(j).getAsJsonObject().get("uri").getAsString());
            }

            // Add to collection
            albums.add(new Album(
                    jsonObject.get("uri").getAsString(),
                    jsonObject.getAsJsonObject().get("name").getAsString(),
                    photoUrls,
                    artistNames,
                    artistIds,
                    AlbumType.UNINITIALIZED,
                    null,
                    false, 0,
                    false,
                    jsonObject.getAsJsonObject("date").get("year").getAsString()));
        }
        Log.i(TAG, "Album results extracted from JsonObject.");
    }

    // Retrieve Artist Search Results
    private void extractArtists(JsonObject json) {
        artists = new ArrayList<>();

        // Loop through and store all the artists
        JsonArray jsonArray = json.getAsJsonObject("artists").getAsJsonArray("items");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("data");
            int imageJsonArraySize = 0;
            JsonArray imageJsonArray = new JsonArray(0);

            try {
                imageJsonArray = jsonObject.getAsJsonObject("visuals").getAsJsonObject("avatarImage")
                        .getAsJsonArray("sources");
                imageJsonArraySize = imageJsonArray.size();
            }
            catch (java.lang.ClassCastException e) {
                Log.i(TAG, "Artist Image Is Null");
            }


            // Create an inner loop to get preview images
            List<PhotoUrl> photoUrls = new ArrayList<>();
            for (int j = 0; j < imageJsonArraySize; j++) {
                photoUrls.add(new PhotoUrl(imageJsonArray.get(j).getAsJsonObject().get("url").getAsString(),
                        imageJsonArray.get(j).getAsJsonObject().get("width").getAsDouble(),
                        imageJsonArray.get(j).getAsJsonObject().get("height").getAsDouble()));
            }

            // Add to collection
            artists.add(new Artist(jsonObject.get("uri").getAsString(),
                    jsonObject.getAsJsonObject().get("profile").getAsJsonObject().get("name").getAsString(),
                    photoUrls, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0, false));
        }
        Log.i(TAG, "Artist results extracted from JsonObject.");
    }

    // Retrieve Playlist Search Results
    private void extractPlaylists(JsonObject json) {
        playlists = new ArrayList<>();

        // Loop through and store all the playlists
        JsonArray jsonArray = json.getAsJsonObject("playlists").getAsJsonArray("items");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("data");

            // Get the playlist preview image, there is only 1 for playlists
            JsonArray imageJsonArray = jsonObject.getAsJsonObject("images").getAsJsonArray("items");
            List<PhotoUrl> photoUrls = new ArrayList<>();
            JsonArray sourcesJsonArray = imageJsonArray.get(0).getAsJsonObject().getAsJsonArray("sources");

            double width =
                    (sourcesJsonArray.get(0).getAsJsonObject().get("width").isJsonNull())
                            ? 0
                            : sourcesJsonArray.get(0).getAsJsonObject().get("width").getAsDouble();

            double height =
                    (sourcesJsonArray.get(0).getAsJsonObject().get("height").isJsonNull())
                            ? 0
                            : sourcesJsonArray.get(0).getAsJsonObject().get("height").getAsDouble();

            photoUrls.add(new PhotoUrl(
                    sourcesJsonArray.get(0).getAsJsonObject().get("url").getAsString(), width, height));

            // Add to collection
            playlists.add(new Playlist(
                    jsonObject.get("uri").getAsString(),
                    jsonObject.get("name").getAsString(),
                    photoUrls,
                    jsonObject.getAsJsonObject("owner").getAsJsonObject().get("name").getAsString(),
                    jsonObject.get("description").getAsString()));

        }
        Log.i(TAG, "Playlist results extracted from JsonObject.");
    }

    // Retrieve Track Search Results
    private void extractTracks(JsonObject json) {
        tracks = new ArrayList<>();

        // Loop through and store all the tracks
        JsonArray jsonArray = json.getAsJsonObject("tracks").getAsJsonArray("items");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("data");

            // JsonObject to retrieve album information, image array is nested inside.
            JsonObject albumOfTrack = jsonObject.getAsJsonObject("albumOfTrack");

            // Create an inner loop to get artists
            JsonArray artistsArray = jsonObject.getAsJsonObject("artists").getAsJsonArray("items");
            Map<String, String> artistsMap = new HashMap<>();
            for (int j = 0; j < artistsArray.size(); j++) {
                artistsMap.put(artistsArray.get(j).getAsJsonObject().get("uri").toString(),
                        artistsArray.get(j).getAsJsonObject().getAsJsonObject("profile").get("name").toString());
            }

            // Add to collection
            tracks.add(new Track(
                    jsonObject.get("uri").getAsString(),
                    jsonObject.get("name").getAsString(),
                    albumOfTrack.get("uri").getAsString(),
                    albumOfTrack.get("name").getAsString(),
                    artistsMap,
                    0,
                    false,
                    null,
                    false,
                    null,
                    jsonObject.getAsJsonObject("playability").get("playable").getAsBoolean()));

        }
        Log.i(TAG, "Track results extracted from JsonObject.");
    }

}
