package model;

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
import service.ItemService;
import service.SpotifyService;
import utils.LogUtil;
import utils.ValidationUtil;

// SUMMARY
// The Search Results model maintains lists of items models retrieved from the Spotify API

public class Search {

    private String searchTerm;
    private int limit;
    private SpotifyService spotifyService;

    private List<SearchResult> all;
    private List<SearchResult> albums;
    private List<SearchResult> artists;
    private List<SearchResult> playlists;
    private List<SearchResult> tracks;
    private List<SearchResult> trackAlbums;
    private SearchFilter currentFilter;

    final private static String TAG = "SearchResults.java";
    final private static Map<SearchFilter, String> TYPE = new HashMap<>() {
        {
            put(SearchFilter.ALL, "multi");
            put(SearchFilter.ARTIST, "artists");
            put(SearchFilter.ALBUM, "albums");
            put(SearchFilter.SONG, "tracks");
            put(SearchFilter.PLAYLIST, "playlists");
        }
    };

    public Search() {
        currentFilter = SearchFilter.ALL;
    }

    public Search(String searchTerm, int limit, SpotifyService spotifyService, SearchFilter filter) {
        this.searchTerm = searchTerm;
        this.limit = limit;
        this.spotifyService = spotifyService;
        this.currentFilter = filter;
    }

    public void execute(int offset) {
        JsonObject json = spotifyService.search(searchTerm, limit, offset, TYPE.get(currentFilter));
        init(json);
    }

    //#region Accessors
    public List<SearchResult> getAlbums() {
        return albums;
    }

    public List<SearchResult> getArtists() {
        return artists;
    }

    public List<SearchResult> getPlaylists() {
        return playlists;
    }

    public List<SearchResult> getTracks() {
        return tracks;
    }

    public SearchFilter getCurrentFilter() {
        return currentFilter;
    }

    public List<SearchResult> getAll() {
        LogUtil log = new LogUtil(TAG, "getAll");
        if (all != null) {
            all.clear();
        }
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                all.addAll(artists);
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                all.addAll(albums);
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                all.addAll(tracks);
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                all.addAll(playlists);
            }
        });

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.e(e.getMessage());
        }
        return all;
    }

    public TrackResult getTrackResult(Track track) {
        List<Album> titleMatch = new ArrayList<>();
        List<String> titleMatchIds = new ArrayList<>();
        List<Album> suggested = new ArrayList<>();
        List<String> suggestedIds = new ArrayList<>();
        for (SearchResult trackAlbum : trackAlbums) {
            if (track.getArtistId().equals(trackAlbum.getAlbum().getArtistId())) {
                if (ValidationUtil.namesMatch(track.getName(), trackAlbum.getAlbum().getTracks().get(0).getName(), TAG)) {
                    if (suggestedIds.contains(trackAlbum.getAlbum().getId())) {
                        suggestedIds.remove(trackAlbum.getAlbum().getId());
                        suggested.remove(trackAlbum);
                    }
                    titleMatchIds.add(trackAlbum.getAlbum().getId());
                    titleMatch.add(trackAlbum.getAlbum());
                } else {
                    if (!titleMatchIds.contains(trackAlbum.getAlbum().getId())) {
                        suggestedIds.add(trackAlbum.getAlbum().getId());
                        suggested.add(trackAlbum.getAlbum());
                    }
                }
            }
        }

        for (SearchResult album : albums) {
            for (Album suggest : suggested) {
                if (suggest.getArtistId().equals(album.getAlbum().getArtistId())
                        && !suggest.getId().equals(album.getAlbum().getId())
                        && !titleMatchIds.contains(album.getAlbum().getId())) {
                    suggested.add(album.getAlbum());
                }
            }
        }

        return new TrackResult(track.getName(),
                track.getId(),
                track.getArtistName(),
                titleMatch,
                suggested,
                ItemService.getSmallestPhotoUrl(track.getPhotoUrl()));
    }
    //#endregion

    //#region Mutators
    public void setCurrentFilter(SearchFilter currentFilter) {
        this.currentFilter = currentFilter;
    }

    public void setAll(List<SearchResult> all) {
        this.all = all;
    }
    //#endregion

    //#region Data Extraction
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
            albums.add(new SearchResult(SearchFilter.ALBUM, new Album(
                    jsonObject.get("uri").getAsString(),
                    jsonObject.getAsJsonObject().get("name").getAsString(),
                    photoUrls,
                    artistId,
                    artistsMap,
                    AlbumType.UNINITIALIZED,
                    null,
                    false, 0,
                    false,
                    jsonObject.getAsJsonObject("date").get("year").getAsString())));
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
            artists.add(new SearchResult(SearchFilter.ARTIST, new Artist(jsonObject.get("uri").getAsString(),
                    jsonObject.getAsJsonObject().get("profile").getAsJsonObject().get("name").getAsString(),
                    photoUrls, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0, false)));
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
            playlists.add(new SearchResult(SearchFilter.PLAYLIST, new Playlist(
                    jsonObject.get("uri").getAsString(),
                    jsonObject.get("name").getAsString(),
                    photoUrls,
                    jsonObject.getAsJsonObject("owner").getAsJsonObject().get("name").getAsString(),
                    jsonObject.get("description").getAsString())));

        }
        log.i("Playlist results extracted from JsonObject.");
    }

    // Retrieve Track Search Results
    private void extractTracks(JsonObject json) {
        LogUtil log = new LogUtil(TAG, "extractTracks");

        tracks = new ArrayList<>();
        trackAlbums = new ArrayList<>();

        // Loop through and store all the tracks
        JsonArray jsonArray = json.getAsJsonObject("tracks").getAsJsonArray("items");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("data");

            // JsonObject to retrieve album information, image array is nested inside.
            JsonObject albumOfTrack = jsonObject.getAsJsonObject("albumOfTrack");
            List<PhotoUrl> photoUrl = new ArrayList<>();
            JsonArray imageArray = albumOfTrack.getAsJsonObject("coverArt").getAsJsonArray("sources");
            for (int j = 0; j < imageArray.size(); j++) {
                JsonObject image = imageArray.get(i).getAsJsonObject();
                photoUrl.add(new PhotoUrl(
                        image.get("url").getAsString(),
                        image.get("width").getAsDouble(),
                        image.get("height").getAsDouble()
                ));
            }

            // Create an inner loop to get artists
            JsonArray artistsArray = jsonObject.getAsJsonObject("artists").getAsJsonArray("items");
            Map<String, String> artistsMap = new HashMap<>();
            String artistId = artistsArray.get(0).getAsJsonObject().get("uri").getAsString();
            for (int j = 0; j < artistsArray.size(); j++) {
                artistsMap.put(artistsArray.get(j).getAsJsonObject().get("uri").getAsString(),
                        artistsArray.get(j).getAsJsonObject().getAsJsonObject("profile").get("name").getAsString());
            }

            // Add to collection
            tracks.add(new SearchResult(SearchFilter.SONG, new Track(
                    jsonObject.get("uri").getAsString(),
                    jsonObject.get("name").getAsString(),
                    albumOfTrack.get("uri").getAsString(),
                    albumOfTrack.get("name").getAsString(),
                    false,
                    artistId,
                    artistsMap,
                    0,
                    false,
                    null,
                    null,
                    jsonObject.getAsJsonObject("playability").get("playable").getAsBoolean(),
                    photoUrl)));


            trackAlbums.add(new SearchResult(SearchFilter.ALBUM, new Album(
                    albumOfTrack.get("uri").getAsString(),
                    albumOfTrack.get("name").getAsString(),
                    photoUrl,
                    artistId,
                    artistsMap,
                    new ArrayList<>() {
                        {
                            add(tracks.get(tracks.size() - 1).getTrack());
                        }
                    }
            )));
        }
        log.v("Track results extracted from JsonObject.");
    }
    //#endregion


}
