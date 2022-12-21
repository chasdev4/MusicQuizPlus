package model.item;

import java.net.URI;

import model.Item;

public class Track extends Item {
    public Track(String id, String name, URI photoUrl) {
        super(id, name, photoUrl);
    }

    public Track(Item item) {
        super(item);
    }
}
