package model.item;

import java.net.URI;

import model.Item;

public class Album extends Item {

    public Album(String id, String name, URI photoUrl) {
        super(id, name, photoUrl);
    }

    public Album(Item item) {
        super(item);
    }
}
