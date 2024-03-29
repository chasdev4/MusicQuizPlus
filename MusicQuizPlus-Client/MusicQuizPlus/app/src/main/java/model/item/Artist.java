package model.item;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import model.ExternalLink;
import model.PhotoUrl;
import model.User;
import model.type.AlbumType;
import service.FirebaseService;
import service.ItemService;
import service.SpotifyService;
import utils.FormatUtil;
import utils.LogUtil;

// SUMMARY
// The Artist model stores artist information

public class Artist implements Serializable {
    private String id;
    private String name;
    private List<PhotoUrl> photoUrl;
    private String bio;
    private List<ExternalLink> externalLinks;
    private List<String> singleIds;
    private List<String> albumIds;
    private List<String> compilationIds;
    private int followers;
    private boolean followersKnown;
    private List<Integer> decades;

    // Exlcuded from database
    private List<Album> singles;
    private List<Album> albums;
    private List<Album> compilations;
    private List<Integer> sortedDecades;
    private Map<Integer, Integer> decadesMap;
    private Album latest;
    private boolean isInitializing;

    private static String TAG = "Artist.java";

    public Artist(String id, String name, List<PhotoUrl> photoUrl, List<String> singleIds,
                  List<String> albumIds, List<String> compilationIds, int followers, boolean followersKnown) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.singleIds = singleIds;
        this.albumIds = albumIds;
        this.compilationIds = compilationIds;
        this.followers = followers;
        this.followersKnown = followersKnown;
    }

    public Artist(JsonObject jsonObject, SpotifyService spotifyService) {
        extractArtist(jsonObject, spotifyService);
    }

    public Artist() {
        photoUrl = new ArrayList<>();
        externalLinks = new ArrayList<>();
        singleIds = new ArrayList<>();
        albumIds = new ArrayList<>();
        compilationIds = new ArrayList<>();
        singles = new ArrayList<>();
        albums = new ArrayList<>();
        compilations = new ArrayList<>();
        sortedDecades = new ArrayList<>();
        decadesMap = new HashMap<>();
    }

    //#region Accessors
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<PhotoUrl> getPhotoUrl() {
        return photoUrl;
    }

    public String getBio() {
        return bio;
    }

    public List<ExternalLink> getExternalLinks() {
        return externalLinks;
    }

    private void setLatest() {
        int newest = 0;
        if (albums != null) {
            for (Album a : albums) {
                if (a != null) {
                    Log.d(TAG, "setLatest: ");
                    int year = Integer.parseInt(a.getYear());
                    if (year > newest) {
                        newest = year;
                        latest = a;
                    }
                }
            }
        }
        if (singles != null) {
            for (Album a : singles) {
                if (a != null) {
                    int year = Integer.parseInt(a.getYear());
                    if (year > newest) {
                        newest = year;
                        latest = a;
                    }
                }
            }
        }
        if (compilations != null) {
            for (Album a : compilations) {
                if (a != null) {
                    int year = Integer.parseInt(a.getYear());
                    if (year > newest) {
                        newest = year;
                        latest = a;
                    }
                }
            }
        }
    }

    @Exclude
    public Album getLatest() {
        return latest;
    }

    public List<String> getSingleIds() {
        return singleIds;
    }

    public List<String> getAlbumIds() {
        return albumIds;
    }

    public List<String> getCompilationIds() {
        return compilationIds;
    }

    public int getFollowers() {
        return followers;
    }

    public boolean isFollowersKnown() {
        return followersKnown;
    }

    public List<Integer> getSortedDecades() {
        return sortedDecades;
    }

    public List<Integer> getDecades() {
        return decades;
    }

    @Exclude
    public List<Album> getSingles() {
        return singles;
    }

    @Exclude
    public List<Album> getAlbums() {
        return albums;
    }

    @Exclude
    public List<Album> getCompilations() {
        return compilations;
    }

    @Exclude
    public List<Track> getTracks() {
        return getAllTracks();
    }

    @Exclude
    public int getTrackPoolSize() {
        int count = 0;
        int size = 0;
        if (singleIds != null) {
            size = singleIds.size();
        }
        try {
            Thread.sleep(100 * size);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (singles != null) {
            for (Album album : singles) {
                if (album != null && album.getTracks() != null) {
                    count += album.getTracks().size();
                }
            }
        }
        if (albumIds != null) {
            size = albumIds.size();
        }
        try {
            Thread.sleep(100 * size);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (albums != null) {
            for (Album album : albums) {
                if (album != null && album.getTracks() != null) {
                    count += album.getTracks().size();
                }
            }
        }
        if (compilationIds != null) {
            size = compilationIds.size();
        }
        try {
            Thread.sleep(100 * size);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (compilations != null) {
            for (Album album : compilations) {
                if (album != null && album.getTracks() != null) {
                    count += album.getTracks().size();
                }
            }
        }

        return count;
    }

    @Exclude
    private List<Track> getAllTracks() {
        List<Track> tracks = new ArrayList<>();
        int size = 0;
        if (singleIds != null) {
            size = singleIds.size();
        }
        try {
            Thread.sleep(100 * size);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (singles != null) {
            for (Album album : singles) {
                if (album.isTrackIdsKnown() && album.getTracks() != null) {
                    tracks.addAll(album.getTracks());
                }
            }
        }
        if (albumIds != null) {
            size = albumIds.size();
        }
        try {
            Thread.sleep(100 * size);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (albums != null) {
            for (Album album : albums) {
                if (album.isTrackIdsKnown() && album.getTracks() != null && album.getTracks().size() > 0) {
                    tracks.addAll(album.getTracks());
                }
            }
        }
        if (compilationIds != null) {
            size = compilationIds.size();
        }
        try {
            Thread.sleep(100 * size);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (compilations != null) {
            for (Album album : compilations) {
                if (album.isTrackIdsKnown() && album.getTracks() != null && album.getTracks().size() > 0) {
                    tracks.addAll(album.getTracks());
                }
            }
        }

        return tracks;
    }

    @Exclude
    public List<String> getFeaturedArtists(List<Track> trackList) {
        List<String> featuredArtistNames = new ArrayList<>();

        for (Track track : trackList) {
            if (track.getArtistsMap().size() > 1) {
                for (Map.Entry<String, String> feat : track.getArtistsMap().entrySet()) {
                    if (!feat.getKey().equals(track.getArtistId()) && !featuredArtistNames.contains(feat.getValue())) {
                        featuredArtistNames.add(feat.getValue());
                    }
                }
            }
        }

        return featuredArtistNames;
    }

    @Exclude
    public int getAlbumTrackCount(String albumId) {
        for (Album album : albums) {
            if (album.getId().equals(albumId)) {
                return album.getTracks().size();
            }
        }
        for (Album album : compilations) {
            if (album.getId().equals(albumId)) {
                return album.getTracks().size();
            }
        }
        for (Album album : singles) {
            if (album.getId().equals(albumId)) {
                return album.getTracks().size();
            }
        }
        return -1;
    }

    @Exclude
    public int getAveragePopularity(List<Track> tracks) {
        int averagePopularity = 0;

        for (Track track : tracks) {
            averagePopularity += track.getPopularity();
        }

        return (tracks.size() == 0) ? 0 : averagePopularity / tracks.size();
    }

    @Exclude
    public Album getAlbum(String albumId) {
        for (Album a : albums) {
            if (a.getId().equals(albumId)) {
                return a;
            }
        }
        for (Album a : compilations) {
            if (a.getId().equals(albumId)) {
                return a;
            }
        }
        for (Album a : singles) {
            if (a.getId().equals(albumId)) {
                return a;
            }
        }
        return null;
    }

    @Exclude
    public String getRandomId() {
        Random rnd = new Random();
        if (compilationIds.size() > 0) {
            return compilationIds.get(rnd.nextInt(compilationIds.size()));
        } else if (albumIds.size() > 0) {
            return albumIds.get(rnd.nextInt(albumIds.size()));
        } else if (singleIds.size() > 0) {
            return singleIds.get(rnd.nextInt(singleIds.size()));
        }

        return null;
    }
    //#endregion

    //#region Mutators
    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public void setFollowersKnown(boolean followersKnown) {
        this.followersKnown = followersKnown;
    }

    public void setAlbumIds(List<String> albumIds) {
        this.albumIds = albumIds;
    }

    public void setSingleIds(List<String> singleIds) {
        this.singleIds = singleIds;
    }

    public void setCompilationIds(List<String> compilationIds) {
        this.compilationIds = compilationIds;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    public void setSingles(List<Album> singles) {
        this.singles = singles;
    }

    public void setCompilations(List<Album> compilations) {
        this.compilations = compilations;
    }
    //#endregion

    //#region Data Extraction
    // Extract information from the Artist Overview JsonObject into the model
    private void extractArtist(JsonObject jsonObject, SpotifyService spotifyService) {
        JsonObject jsonArtist = jsonObject.getAsJsonObject("data").getAsJsonObject("artist");
        id = jsonArtist.get("uri").getAsString();
        name = jsonArtist.getAsJsonObject().get("profile").getAsJsonObject().get("name").getAsString();

        JsonElement bioRaw = jsonArtist.getAsJsonObject()
                .get("profile")
                .getAsJsonObject()
                .get("biography")
                .getAsJsonObject()
                .get("text");
        if (!bioRaw.isJsonNull() && bioRaw != null) {
            // Remove HTML from bio
            bio = FormatUtil.removeHtml(bioRaw
                    .getAsString());


            if (bio.length() > 250) {
                bio = bio.substring(0, 250);
            }
        }


        JsonArray jsonArray = jsonArtist.getAsJsonObject()
                .get("profile")
                .getAsJsonObject()
                .getAsJsonObject("externalLinks")
                .getAsJsonArray("items");

        externalLinks = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            externalLinks.add(new ExternalLink(
                    jsonArray.get(i).getAsJsonObject().get("name").getAsString(),
                    jsonArray.get(i).getAsJsonObject().get("url").getAsString()
            ));
        }

        jsonArray = jsonArtist.getAsJsonObject()
                .getAsJsonObject("visuals")
                .getAsJsonObject("avatarImage")
                .getAsJsonArray("sources");

        photoUrl = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject image = jsonArray.get(i).getAsJsonObject();
            photoUrl.add(new PhotoUrl(
                    image.get("url").getAsString(),
                    image.get("width").getAsString(),
                    image.get("height").getAsString()
            ));
        }

        JsonObject discography = jsonArtist.getAsJsonObject("discography");

        singles = new ArrayList<>();
        singleIds = new ArrayList<>();
        albums = new ArrayList<>();
        albumIds = new ArrayList<>();
        compilations = new ArrayList<>();
        compilationIds = new ArrayList<>();
        decadesMap = new HashMap<>();


        // Save compilations from artist overview
        jsonArray = discography.getAsJsonObject("compilations")
                .getAsJsonArray("items");
        addReleases(jsonArray);
        jsonArray = retrieveArtistAlbums(AlbumType.ALBUM, spotifyService);
        addReleases(jsonArray);
        jsonArray = retrieveArtistAlbums(AlbumType.SINGLE, spotifyService);
        addReleases(jsonArray);

        setLatest();

        decades = new ArrayList<>();
        for (Map.Entry<Integer, Integer> d : decadesMap.entrySet()) {
            if (decades.size() == 0) {
                decades.add(d.getKey());
            } else {
                int index = decades.size();
                for (int i = 0; i < decades.size(); i++) {
                    if (d.getValue() < decadesMap.get(decades.get(i))) {
                        index = decades.indexOf(decades.get(i));
                        break;
                    }
                }
                decades.add(decades.size() - index, d.getKey());
            }
        }

    }

    private void addReleases(JsonArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonArray releases = jsonArray.get(i).getAsJsonObject().getAsJsonObject("releases").getAsJsonArray("items");
            for (int j = 0; j < releases.size(); j++) {
                Album album = extractAlbum(releases.get(j).getAsJsonObject());

                int year = Integer.parseInt(album.getYear());
                int decade = (year / 10) * 10;
                if (year % 10 == 9) {
                    decade += 10;
                }

                if (decadesMap.containsKey(decade)) {
                    int val = decadesMap.get(decade);
                    val++;
                    decadesMap.put(decade, val);
                } else {
                    decadesMap.put(decade, 1);
                }

                switch (album.getType()) {
                    case UNINITIALIZED:
                    case ALBUM:
                        albums.add(album);
                        albumIds.add(album.getId());
                        break;
                    case SINGLE:
                        singles.add(album);
                        singleIds.add(album.getId());
                        break;
                    case COMPILATION:
                        compilations.add(album);
                        compilationIds.add(album.getId());
                        break;

                }
            }
        }

    }

    private JsonArray retrieveArtistAlbums(AlbumType albumType, SpotifyService spotifyService) {
        return spotifyService.artistAlbums(id, albumType);
    }

    // Extract information from the album JsonObject created in extractArtist
    private Album extractAlbum(JsonObject album) {
        List<PhotoUrl> photos = new ArrayList<>();
        JsonArray jsonPhotos = album.getAsJsonObject("coverArt").getAsJsonArray("sources");
        for (int j = 0; j < jsonPhotos.size(); j++) {
            photos.add(new PhotoUrl(jsonPhotos.get(j).getAsJsonObject().get("url").getAsString(),
                    jsonPhotos.get(j).getAsJsonObject().get("width").getAsString(),
                    jsonPhotos.get(j).getAsJsonObject().get("height").getAsString()));
        }

        String artistId = id;
        Map<String, String> artistsMap = new HashMap<>() {
            {
                put(id, name);
            }
        };

        AlbumType albumType = AlbumType.UNINITIALIZED;
        String albumTypeStr = album.get("type").getAsString();
        for (AlbumType type : AlbumType.values()) {
            if (albumTypeStr.equals(type.toString())) {
                albumType = type;
                break;
            }
        }

        if (albumType == AlbumType.UNINITIALIZED) {
            albumType = AlbumType.ALBUM;
        }

        return new Album(
                album.get("uri").getAsString(),
                album.get("name").getAsString(),
                photos,
                artistId,
                artistsMap,
                albumType,
                null,
                false,
                0,
                false,
                album.getAsJsonObject("date").get("year").getAsString());
    }
    //#endregion

    //#region Collection Initialization
    public void initCollections(DatabaseReference db, User user) {
        isInitializing = true;
        LogUtil log = new LogUtil(TAG, "initCollections");

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                initCollection(db, user, AlbumType.SINGLE, singleIds);
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                initCollection(db, user, AlbumType.ALBUM, albumIds);

            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                initCollection(db, user, AlbumType.COMPILATION, compilationIds);

            }
        });
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.e(e.getMessage());
        }
        if (latest == null) {
            setLatest();
        }
        isInitializing = false;
    }

    public void initTracks(DatabaseReference db, User user) {
        LogUtil log = new LogUtil(TAG, "initTracks");
        List<Album> albumsToInit = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<Album> removeQueue = new ArrayList<>();
                for (Album single : singles) {
                    if (single != null) {
                        if (single.isTrackIdsKnown() && user.getAlbumIds().containsValue(single.getId())) {
                            albumsToInit.add(single);
                        }
                    } else {
                        removeQueue.add(single);
                    }
                }
                if (removeQueue.size() > 0) {
                    for (Album album : removeQueue) {
                        albums.remove(album);
                    }
                }
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<Album> removeQueue = new ArrayList<>();
                for (Album album : albums) {
                    if (album != null) {
                        if (album.isTrackIdsKnown() && user.getAlbumIds().containsValue(album.getId())) {
                            albumsToInit.add(album);
                        }
                    } else {
                        removeQueue.add(album);
                    }
                }
                if (removeQueue.size() > 0) {
                    for (Album album : removeQueue) {
                        albums.remove(album);
                    }
                }
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<Album> removeQueue = new ArrayList<>();
                for (Album compilation : compilations) {
                    if (compilation != null) {
                        if (compilation.isTrackIdsKnown() && user.getAlbumIds().containsValue(compilation.getId())) {
                            albumsToInit.add(compilation);
                        }
                    } else {
                        removeQueue.add(compilation);
                    }
                }
                if (removeQueue.size() > 0) {
                    for (Album album : removeQueue) {
                        albums.remove(album);
                    }
                }
            }
        });
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.e(e.getMessage());
        }
        CountDownLatch cdl = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Album album : albumsToInit) {
                    album.initCollection(db);
                }
                log.i("Albums initialized.");
                cdl.countDown();
            }
        }).start();
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void initCollection(DatabaseReference db, User user, AlbumType type, List<String> albumIdList) {
        List<Album> albumsList = new ArrayList<>();
        for (String albumId : albumIdList) {
            albumsList.add(FirebaseService.checkDatabase(db, "albums", albumId, Album.class));

            // If the user has the album hearted, fetch the tracks
            if (user.getAlbumIds().containsValue(albumId)) {
                //albumsList.get(albumsList.size() - 1).initCollection(db);
            }
        }

        switch (type) {
            case SINGLE:
                singles = albumsList;
                break;
            case ALBUM:
                albums = albumsList;
                break;
            case COMPILATION:
                compilations = albumsList;
                break;
        }
    }

    @Exclude
    public boolean isInitializing() {
        return isInitializing;
    }
    //#endregion

}
