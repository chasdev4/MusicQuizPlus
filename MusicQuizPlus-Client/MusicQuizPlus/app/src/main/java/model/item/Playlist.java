package model.item;

import java.net.URI;

import model.Item;
import model.PhotoUrl;

public class Playlist extends Item {
    public Playlist(String id, String name, PhotoUrl[] photoUrl) {
        super(id, name, photoUrl);
    }
}
