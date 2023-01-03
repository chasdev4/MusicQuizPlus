package model.item;

import model.Item;
import model.PhotoUrl;

// SUMMARY
// The Artist item model stores artist information

public class Artist extends Item {
    public Artist(String id, String name, PhotoUrl[] photoUrl) {
        super(id, name, photoUrl);
    }
}
