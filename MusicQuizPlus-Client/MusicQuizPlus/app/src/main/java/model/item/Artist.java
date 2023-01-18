package model.item;

import android.text.Html;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import model.ExternalLink;
import model.PhotoUrl;
import model.type.AlbumType;

// SUMMARY
// The Artist model stores artist information

public class Artist {
    private String id;
    private String name;
    private List<PhotoUrl> photoUrl;
    private String bio;
    private List<ExternalLink> externalLinks;
    private String latest;
    private List<Album> singles;
    private List<Album> albums;
    private List<Album> compilations;

    public Artist(String id, String name, List<PhotoUrl> photoUrl) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public Artist(JsonObject jsonObject) {
        extractArtist(jsonObject);
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
        albums = new ArrayList<>();
        compilations = new ArrayList<>();

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
                        break;
                    case SINGLE:
                        singles.add(album);
                        break;
                    case COMPILATION:
                        compilations.add(album);
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
                null);
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

    public List<Album> getSingles() {
        return singles;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public List<Album> getCompilations() {
        return compilations;
    }
}
