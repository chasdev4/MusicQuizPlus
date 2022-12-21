package model.item;

import java.net.URI;

import model.Item;

public class Playlist extends Item {
    public Playlist(String id, String name, URI photoUrl) {
        super(id, name, photoUrl);
    }

    public Playlist(Item item) {
        super(item);
    }
}
