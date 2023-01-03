package model.item;

import java.net.URI;

import model.Item;
import model.PhotoUrl;

public class Album extends Item {

    private final String[] _artistName;
    private final String[] _artistId;

    public Album(String id, String name, PhotoUrl[] photoUrl, String[] artistName, String[] artistId) {
        super(id, name, photoUrl);
        _artistName = artistName;
        _artistId = artistId;
    }
}
