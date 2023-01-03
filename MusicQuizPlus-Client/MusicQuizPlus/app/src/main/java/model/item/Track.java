package model.item;

import java.net.URI;

import model.Item;
import model.PhotoUrl;

public class Track extends Item {
    public Track(String id, String name, PhotoUrl[] photoUrl) {
        super(id, name, photoUrl);
    }
}
