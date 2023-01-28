package model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.AlbumType;
import model.type.SearchFilter;
import service.SpotifyService;
import utils.LogUtil;

// SUMMARY
// The Search Results model maintains lists of items models retrieved from the Spotify API

public class Search {

    private String searchTerm;
    private int limit;
    private SpotifyService spotifyService;

    private List<SearchResult> all;
    private List<Album> albums;
    private List<Artist> artists;
    private List<Playlist> playlists;
    private List<Track> tracks;
    private SearchFilter currentFilter;

    final private static String TAG = "SearchResults.java";

    public Search(String searchTerm, int limit, SpotifyService spotifyService) {
        this.searchTerm = searchTerm;
        this.limit = limit;
        this.spotifyService = spotifyService;

    }

    public void search(int offset) {
        JsonObject json = spotifyService.search(searchTerm, limit, offset);
        init(json);
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
    private void init(JsonObject json) {
        LogUtil log = new LogUtil(TAG, "init");
        all = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                extractAlbums(json);
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                extractArtists(json);
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                extractPlaylists(json);
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                extractTracks(json);
            }
        });
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.e(e.getMessage());
        }
    }

    // Retrieve Album Search Results
    private void extractAlbums(JsonObject json) {
        LogUtil log = new LogUtil(TAG, "extractAlbums");
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

            String artistId = artistJsonArray.get(0).getAsJsonObject().get("uri").getAsString();
            Map<String, String> artistsMap = new HashMap<>();
            for (int j = 0; j < artistJsonArray.size(); j++) {
                artistsMap.put(artistJsonArray.get(j).getAsJsonObject().get("uri").getAsString(),
                        artistJsonArray.get(j).getAsJsonObject().get("profile")
                                .getAsJsonObject().get("name").getAsString());
            }

            // Add to collection
            albums.add(new Album(
                    jsonObject.get("uri").getAsString(),
                    jsonObject.getAsJsonObject().get("name").getAsString(),
                    photoUrls,
                    artistId,
                    artistsMap,
                    AlbumType.UNINITIALIZED,
                    null,
                    false, 0,
                    false,
                    jsonObject.getAsJsonObject("date").get("year").getAsString()));
        }
        log.i("Album results extracted from JsonObject.");
    }

    // Retrieve Artist Search Results
    private void extractArtists(JsonObject json) {
        LogUtil log = new LogUtil(TAG, "extractArtists");

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
            } catch (java.lang.ClassCastException e) {
                log.i("Artist Image Is Null");
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
        log.i("Artist results extracted from JsonObject.");
    }

    // Retrieve Playlist Search Results
    private void extractPlaylists(JsonObject json) {
        LogUtil log = new LogUtil(TAG, "extractPlaylists");
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
        log.i("Playlist results extracted from JsonObject.");
    }

    // Retrieve Track Search Results
    private void extractTracks(JsonObject json) {
        LogUtil log = new LogUtil(TAG, "extractTracks");

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
            String artistId = artistsArray.get(0).getAsJsonObject().get("uri").toString();
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
                    artistId,
                    artistsMap,
                    0,
                    false,
                    null,
                    false,
                    null,
                    jsonObject.getAsJsonObject("playability").get("playable").getAsBoolean()));

        }
        log.v("Track results extracted from JsonObject.");
    }

    public SearchFilter getCurrentFilter() {
        return currentFilter;
    }

    public void setCurrentFilter(SearchFilter currentFilter) {
        this.currentFilter = currentFilter;
    }

    public List<SearchResult> getAll() {
        LogUtil log = new LogUtil(TAG, "getAll");
        if (all.size() == 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(4);
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (Artist artist : artists) {
                        all.add(new SearchResult(SearchFilter.ARTIST, artist));
                    }
                }
            });
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (Album album : albums) {
                        all.add(new SearchResult(SearchFilter.ALBUM, album));
                    }
                }
            });
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (Track track : tracks) {
                        all.add(new SearchResult(SearchFilter.TRACK, track));
                    }
                }
            });
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (Playlist playlist : playlists) {
                        all.add(new SearchResult(SearchFilter.PLAYLIST, playlist));
                    }
                }
            });

            executorService.shutdown();
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                log.e(e.getMessage());
            }
        }
        return all;
    }

    public void setAll(List<SearchResult> all) {
        this.all = all;
    }
}
