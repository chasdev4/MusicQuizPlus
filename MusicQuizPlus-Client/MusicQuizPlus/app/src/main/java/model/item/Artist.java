package model.item;

import java.net.URI;

import model.Item;
import model.PhotoUrl;

public class Artist extends Item {
    public Artist(String id, String name, PhotoUrl[] photoUrl) {
        super(id, name, photoUrl);
    }
}
