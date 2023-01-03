package model.item;

import java.util.List;

import model.Item;
import model.PhotoUrl;

// SUMMARY
// The Artist item model stores artist information

public class Artist extends Item {
    public Artist(String id, String name, List<PhotoUrl> photoUrl) {
        super(id, name, photoUrl);
    }
}
