package model.item;

import model.Item;
import model.PhotoUrl;

public class Album extends Item {

    private final String[] _artistNames;
    private final String[] _artistIds;

    public Album(String id, String name, PhotoUrl[] photoUrl, String[] artistNames, String[] artistIds) {
        super(id, name, photoUrl);
        _artistNames = artistNames;
        _artistIds = artistIds;
    }

    public String[] get_artistNames() {
        return _artistNames;
    }

    public String[] get_artistIds() {
        return _artistIds;
    }
}
