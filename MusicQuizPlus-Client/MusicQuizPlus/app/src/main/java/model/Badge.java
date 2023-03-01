package model;

import com.google.firebase.database.Exclude;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;

import model.type.BadgeType;

/*
//        Badge Ranks For Artist Badges:
//        0 = Badge Rank Not Applicable (Completing an Album/Playlist)
//        1 = 3,5,10 Songs Known
//        2 = 25 and 50 Songs Known
//        3 = Every 50 Songs Known after 50
//
//        Badge Ranks For Milestone Badges:
//        Rank = Number of Quizzes Taken (Given for 1, 3, 10, 25, and every 50 after that)
//
//        Performance Badges:
//            Perfect Accuracy Badge's will have rank of 0
//
//            Badge Ranks for Quick Reactions
//            1 = 3 Quick Reactions On Quiz
//            2 = 5 Quick Reactions On Quiz
//            3 = 7 Quick Reactions On Quiz
//    */

public class Badge implements Serializable {
    private BadgeType type;
    private String id;
    private String photoUrl;
    private String name;
    private int number;

    public Badge(@NonNull BadgeType type) {
        this.type = type;
    }

    public Badge(@NonNull BadgeType type, String id) {
        this.type = type;
        this.id = id;
    }

    public Badge(@NonNull BadgeType type, String id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }

    public Badge(@NonNull BadgeType type, int number) {
        this.type = type;
        this.number = number;
    }

    public Badge(@NonNull BadgeType type, String id, String name, int number) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.number = number;
    }

    public Badge() {}

    public BadgeType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    @Exclude
    public String getPhotoUrl() {
        return photoUrl;
    }
    @Exclude
    public String getName() { return name; }
    public int getNumber() { return number; }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setName(String name) { this.name = name; }
}
