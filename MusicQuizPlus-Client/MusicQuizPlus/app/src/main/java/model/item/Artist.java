package model.item;

import java.net.URI;

import model.Item;

public class Artist extends Item {
    public Artist(String id, String name, URI photoUrl) {
        super(id, name, photoUrl);
    }

    public Artist(Item item) {
        super(item);
    }
}
