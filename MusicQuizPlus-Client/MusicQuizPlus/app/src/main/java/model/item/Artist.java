package model.item;

import android.text.Html;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.ExternalLink;
import model.PhotoUrl;
import model.User;
import model.type.AlbumType;
import service.FirebaseService;

// SUMMARY
// The Artist model stores artist information

public class Artist implements Serializable {
    private String id;
    private String name;
    private List<PhotoUrl> photoUrl;
    private String bio;
    private List<ExternalLink> externalLinks;
    private String latest;
    private List<String> singleIds;
    private List<String> albumIds;
    private List<String> compilationIds;
    private int followers;
    private boolean followersKnown;

    // Exlcuded from database
    private List<Album> singles;
    private List<Album> albums;
    private List<Album> compilations;

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

    public Artist(JsonObject jsonObject) {
        extractArtist(jsonObject);
    }

    public Artist() {

    }

    // Extract information from the Artist Overview JsonObject into the model
    private void extractArtist(JsonObject jsonObject) {
        JsonObject jsonArtist = jsonObject.getAsJsonObject("data").getAsJsonObject("artist");
        id = jsonArtist.get("uri").getAsString();
        name = jsonArtist.getAsJsonObject().get("profile").getAsJsonObject().get("name").getAsString();

        // Remove HTML from bio
        bio = Html.fromHtml(
                jsonArtist.getAsJsonObject()
                        .get("profile")
                        .getAsJsonObject()
                        .get("biography")
                        .getAsJsonObject()
                        .get("text")
                        .getAsString()
        ).toString();

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
        latest = discography.getAsJsonObject("latest").get("uri").getAsString();

        singles = new ArrayList<>();
        singleIds = new ArrayList<>();
        albums = new ArrayList<>();
        albumIds = new ArrayList<>();
        compilations = new ArrayList<>();
        compilationIds = new ArrayList<>();

        List<String> discographyCollections = new ArrayList<>() {
            {
                add("singles");
                add("albums");
                add("compilations");
            }
        };

        // Loop thru to extract each album's info and add to it's collection
        for (int k = 0; k < discographyCollections.size(); k++) {
            jsonArray = discography.getAsJsonObject(discographyCollections.get(k).toString())
                    .getAsJsonArray("items");

            for (int i = 0; i < jsonArray.size(); i++) {
                Album album = extractAlbum(jsonArray.get(i).getAsJsonObject()
                        .getAsJsonObject("releases").getAsJsonArray("items").get(0)
                        .getAsJsonObject());
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

    // Extract information from the album JsonObject created in extractArtist
    private Album extractAlbum(JsonObject album) {
        List<PhotoUrl> photos = new ArrayList<>();
        JsonArray jsonPhotos = album.getAsJsonObject("coverArt").getAsJsonArray("sources");
        for (int j = 0; j < jsonPhotos.size(); j++) {
            photos.add(new PhotoUrl(jsonPhotos.get(j).getAsJsonObject().get("url").getAsString(),
                    jsonPhotos.get(j).getAsJsonObject().get("width").getAsString(),
                    jsonPhotos.get(j).getAsJsonObject().get("height").getAsString()));
        }
        List<String> artistName = new ArrayList<>() {
            {
                add(name);
            }
        };
        List<String> artistId = new ArrayList<>() {
            {
                add(id);
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

        return new Album(
                album.get("uri").getAsString(),
                album.get("name").getAsString(),
                photos,
                artistName,
                artistId,
                albumType,
                null,
                false,
                0,
                false,
                album.getAsJsonObject("date").get("year").getAsString());
    }

    public List<PhotoUrl> getPhotoUrl() {
        return photoUrl;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getBio() {
        return bio;
    }

    public List<ExternalLink> getExternalLinks() {
        return externalLinks;
    }

    public String getLatest() {
        return latest;
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

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public boolean isFollowersKnown() {
        return followersKnown;
    }

    public void setFollowersKnown(boolean followersKnown) {
        this.followersKnown = followersKnown;
    }

    @Exclude
    public List<Track> getTracks() {
        return getAllTracks();
    }

    private List<Track> getAllTracks() {
        List<Track> tracks = new ArrayList<>();

        if (singles != null) {
            for (Album album : singles) {
                if (album.isTrackIdsKnown() || album.getTracks() != null) {
                    tracks.addAll(album.getTracks());
                }
            }
        }
        if (albums != null) {
            for (Album album : albums) {
                if (album.isTrackIdsKnown() || album.getTracks() != null) {
                    tracks.addAll(album.getTracks());
                }
            }
        }
        if (compilations != null) {
            for (Album album : compilations) {
                if (album.isTrackIdsKnown() || album.getTracks() != null) {
                    tracks.addAll(album.getTracks());
                }
            }
        }

        return tracks;
    }

    public void initCollections(DatabaseReference db, User user) {
        initCollection(db, user, AlbumType.SINGLE, singleIds);
        initCollection(db, user, AlbumType.ALBUM, albumIds);
        initCollection(db, user, AlbumType.COMPILATION, compilationIds);
    }

    private void initCollection(DatabaseReference db, User user, AlbumType type, List<String> albumIdList) {
        List<Album> albumsList = new ArrayList<>();
        for (String albumId : albumIdList) {
            albumsList.add(FirebaseService.checkDatabase(db, "albums", albumId, Album.class));

            // If the user has the album hearted, fetch the tracks
            if (user.getAlbumIds().containsValue(albumId)) {
                albumsList.get(albumsList.size() - 1).initCollection(db);
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
        albumsList = null;
    }

    public int getAveragePopularity(List<Track> tracks) {
        int averagePopularity = 0;

        for (Track track : tracks) {
            averagePopularity += track.getPopularity();
        }

        return averagePopularity / tracks.size();
    }

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
}
