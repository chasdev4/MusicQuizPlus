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





//package model;
//
//import android.content.Context;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.Exclude;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//import java.util.Objects;
//
//import model.item.Album;
//import model.item.Artist;
//import model.item.Playlist;
//import model.type.BadgeType;
//import model.type.QuizType;
//import service.FirebaseService;
//
//public class Badge2 {
//
//    private String badgeID;
//    private String badgeName;
//    private String description;
//    private PhotoUrl photoURL;
//    private BadgeType type;
//    private int badgeRank;
//
//    /*
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
//            1 = 3-4 Quick Reactions On Quiz
//            2 = 5-9 Quick Reactions On Quiz
//            3 = 10 Quick Reactions On Quiz
//    */
//
//    private List<Badge2> earnedBadges = new ArrayList<>();
//    private User user;
//    private Artist artist;
//    private Playlist playlist;
//    private Quiz quiz;
//    private QuizType quizType;
//    private List<String> badgeIds = new ArrayList<>();
//    private boolean allowDuplicates;
//    private boolean completedCollection;
//    private String topicID;
//    private String uid;
//    private Badge2 badgeToAdd;
//    private int bonusXP;
//
//    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
//    GoogleSignIn googleSignIn = new GoogleSignIn();
//    FirebaseUser firebaseUser = googleSignIn.getAuth().getCurrentUser();
//
//    public Badge2()
//    {
//
//    }
//
//    public Badge2(BadgeType type) {
//        this.type = type;
//        createBadgeFromType();
//    }
//
//
//
//    public Badge2(User user, Quiz quiz)
//    {
//        this.user = user;
//        this.quiz = quiz;
//        this.quizType = quiz.getType();
//    }
//
//    private Badge2(String badgeID, String badgeName, String description, PhotoUrl photoURL, BadgeType badgeType, int badgeRank, boolean allowDuplicates)
//    {
//        this.badgeID = badgeID;
//        this.badgeName = badgeName;
//        this.description = description;
//        this.photoURL = photoURL;
//        this.type = badgeType;
//        this.badgeRank = badgeRank;
//        this.allowDuplicates = allowDuplicates;
//    }
//
//    public List<Badge2> getEarnedBadges(Context context)
//    {
//        switch(quizType)
//        {
//            case ARTIST:
//                artist = quiz.getArtist();
//                user.incrementArtistQuizCount();
//                completedCollection = quiz.getCompletedCollection();
//                topicID = artist.getId();
//                photoURL = artist.getPhotoUrl().get(0);
//                int totalCount = user.getArtistTrackCount();
//
//                badgeToAdd = checkForArtistBadge(totalCount);
//                if(badgeToAdd != null)
//                {
//                    earnedBadges.add(badgeToAdd);
//                }
//
//                if(completedCollection)
//                {
//                    //Get completed album badge
//                    List<String> completedCollectionIDs = quiz.getCompletedCollectionIDs();
//                    List<Album> completedAlbums = new ArrayList<>();
//
//                    for (String albumID : completedCollectionIDs){
//                        completedAlbums.add(FirebaseService.checkDatabase(db, "albums", albumID, Album.class));
//                    }
//
//                    for (Album album : completedAlbums){
//                        earnedBadges.add(getCompletedAlbumBadge(album));
//                    }
//                }
///*
//                badgeToAdd = getArtistBadge();
//
//                if(badgeToAdd != null)
//                {
//                    //There is an artist badge to be awarded
//                    earnedBadges.add(badgeToAdd);
//                }
// */
//                break;
//
//            case PLAYLIST:
//                playlist = quiz.getPlaylist();
//                user.incrementPlaylistQuizCount();
//                completedCollection = quiz.getCompletedCollection();
//                topicID = playlist.getId();
//                photoURL = playlist.getPhotoUrl().get(0);
//
//                if(completedCollection)
//                {
//                    //Get completed playlist badge
//                    earnedBadges.add(getCompletedPlaylistBadge());
//                }
//                break;
//        }
//
//        badgeToAdd = checkForMilestoneBadge();
//        List <Badge2> performanceBadges = checkForPerformanceBadges();
//
//        if (badgeToAdd != null)
//        {
//            //There Are Milestone Badges To Be Awarded
//            earnedBadges.add(badgeToAdd);
//        }
//
//        earnedBadges.addAll(performanceBadges);
//
//        if(earnedBadges.size() > 0)
//        {
//            for (Badge2 theBadge : earnedBadges)
//            {
//                String key = db.child("users").child(firebaseUser.getUid()).child("badgeIds").push().getKey();
//                user.addBadgeId(key, theBadge.getBadgeID(), theBadge.allowDuplicates);
//
//                badgeToAdd = null;
//                badgeToAdd = (Badge2) FirebaseService.checkDatabase(db, "badges", theBadge.getBadgeID(), Badge2.class);
//
//                if(badgeToAdd != null)
//                {
//                    //Badge is already in database
//                    continue;
//                }
//                else
//                {
//                    db.child("badges").child(theBadge.getBadgeID()).setValue(theBadge);
//                }
//            }
//        }
//
//        updateBadgeIDsForUserInDatabase(context);
//
//        return earnedBadges;
//    }
//
//    private Badge2 checkForArtistBadge(int totalCount) {
//        badgeToAdd = null;
//        allowDuplicates = false;
//        type = BadgeType.ARTIST;
//        String artistName = artist.getName();
//
//        if(user.getAllSongsKnown())
//        {
//            badgeName = String.format(Locale.ENGLISH, "True %s Fan", artistName);
//            description = String.format(Locale.ENGLISH, "You know ALL %s songs!", artistName);
//            uid = generateUniqueId(type, 4, topicID);
//            bonusXP += 1000;
//            badgeToAdd = new Badge2(uid, badgeName, description, photoURL, type, 4, allowDuplicates);
//        }
//        else if(totalCount == 3 || totalCount == 5 || totalCount == 10)
//        {
//            badgeName = String.format(Locale.ENGLISH, "I Know %s", artistName);
//            description = String.format(Locale.ENGLISH, "You know %d %s songs!", totalCount, artistName);
//            uid = generateUniqueId(type, 1, topicID);
//            bonusXP += 100;
//            badgeToAdd = new Badge2(uid, badgeName, description, photoURL, type, 1, allowDuplicates);
//        }
//        else if(totalCount == 25 || totalCount == 50)
//        {
//            badgeName = String.format(Locale.ENGLISH, "I Like %s", artistName);
//            description = String.format(Locale.ENGLISH, "You know %d %s songs!", totalCount, artistName);
//            uid = generateUniqueId(type, 2, topicID);
//            bonusXP += 250;
//            badgeToAdd = new Badge2(uid, badgeName, description, photoURL, type, 2, allowDuplicates);
//        }
//        else if (totalCount % 50 == 0)
//        {
//            badgeName = String.format(Locale.ENGLISH, "I Love %s", artistName);
//            description = String.format(Locale.ENGLISH, "You know %d %s songs!", totalCount, artistName);
//            uid = generateUniqueId(type, 3, topicID);
//            bonusXP += 500;
//            badgeToAdd = new Badge2(uid, badgeName, description, photoURL, type, 3, allowDuplicates);
//        }
//
//        return badgeToAdd;
//    }
//
//    private void updateBadgeIDsForUserInDatabase(Context context)
//    {
//        User updatedUser = new User(user);
//
//        db.child("users").child(firebaseUser.getUid()).setValue(updatedUser).
//                addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(context, "Updated (From Badge Model)..", Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(context, "Failed to update (From Badge Model)..", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private Badge2 getCompletedAlbumBadge(Album album)
//    {
//        allowDuplicates = false;
//        uid = generateUniqueId(BadgeType.ARTIST, 0, album.getId());
//        photoURL = album.getPhotoUrl().get(0);
//        badgeName = String.format(Locale.ENGLISH, "%s Master", album.getName());
//        description = String.format(Locale.ENGLISH, "User has completed all songs in %s", album.getName());
//        bonusXP += 500;
//        return new Badge2(uid, badgeName, description, photoURL, BadgeType.PLAYLIST, 0, allowDuplicates);
//    }
//
//    private Badge2 getCompletedPlaylistBadge()
//    {
//        allowDuplicates = false;
//        uid = generateUniqueId(BadgeType.PLAYLIST, 0, topicID);
//        badgeName = String.format(Locale.ENGLISH, "%s Master", playlist.getName());
//        description = String.format(Locale.ENGLISH, "User has completed all songs in %s", playlist.getName());
//        bonusXP += 500;
//        return new Badge2(uid, badgeName, description, photoURL, BadgeType.PLAYLIST, 0, allowDuplicates);
//    }
//
//    /*
//    private Badge getArtistBadge()
//    {
//        badgeToAdd = null;
//
//        int score = quiz.getNumCorrect();
//        String artistName = artist.getName();
//        allowDuplicates = true;
//
//        if(score > 2 && score < 5)
//        {
//            badgeName = String.format(Locale.ENGLISH, "I Like %s", artistName);
//            description = "User Has Got 3-4 Correct on an Artist Quiz";
//            uid = generateUniqueId(BadgeType.ARTIST, 1, artist.getId());
//            badgeToAdd = new Badge(uid, badgeName, description, photoURL, BadgeType.ARTIST, 1, allowDuplicates);
//        }
//        else if (score > 4 && score < 7)
//        {
//            badgeName = String.format(Locale.ENGLISH, "I Love %s", artistName);
//            description = "User Has Got 5-6 Correct on an Artist Quiz";
//            uid = generateUniqueId(BadgeType.ARTIST, 2, artist.getId());
//            badgeToAdd = new Badge(uid, badgeName, description, photoURL, BadgeType.ARTIST, 2, allowDuplicates);
//        }
//        else if (score > 6 && score <= 10)
//        {
//            badgeName = String.format(Locale.ENGLISH, "True %s Fan", artistName);
//            description = "User Has Got 7-10 Correct on an Artist Quiz";
//            uid = generateUniqueId(BadgeType.ARTIST, 3, artist.getId());
//            badgeToAdd = new Badge(uid, badgeName, description, photoURL, BadgeType.ARTIST, 3, allowDuplicates);
//        }
//
//        return badgeToAdd;
//    }
//     */
//
//    private Badge2 checkForMilestoneBadge()
//    {
//        badgeToAdd = null;
//
//        switch (quizType)
//        {
//            case ARTIST:
//
//                if(user.getArtistQuizCount() == 1)
//                {
//                    badgeToAdd = getMilestoneBadge(user.getArtistQuizCount());
//                    bonusXP += 100;
//                    break;
//                }
//                else if(user.getArtistQuizCount() == 3)
//                {
//                    badgeToAdd = getMilestoneBadge(user.getArtistQuizCount());
//                    bonusXP += 100;
//                    break;
//                }
//                else if(user.getArtistQuizCount() == 5)
//                {
//                    badgeToAdd = getMilestoneBadge(user.getArtistQuizCount());
//                    bonusXP += 100;
//                    break;
//                }
//                else if(user.getArtistQuizCount() == 10)
//                {
//                    badgeToAdd = getMilestoneBadge(user.getArtistQuizCount());
//                    bonusXP += 100;
//                    break;
//                }
//                else if(user.getArtistQuizCount() == 25 || user.getPlaylistQuizCount() == 50)
//                {
//                    badgeToAdd = getMilestoneBadge(user.getArtistQuizCount());
//                    bonusXP += 250;
//                    break;
//                }
//                else if((user.getArtistQuizCount() % 50) == 0)
//                {
//                    badgeToAdd = getMilestoneBadge(user.getArtistQuizCount());
//                    bonusXP += 500;
//                    break;
//                }
//
//            case PLAYLIST:
//
//                if(user.getPlaylistQuizCount() == 1)
//                {
//                    badgeToAdd = getMilestoneBadge(user.getPlaylistQuizCount());
//                    bonusXP += 100;
//                    break;
//                }
//                else if(user.getPlaylistQuizCount() == 3)
//                {
//                    badgeToAdd = getMilestoneBadge(user.getPlaylistQuizCount());
//                    bonusXP += 100;
//                    break;
//                }
//                else if(user.getPlaylistQuizCount() == 5)
//                {
//                    badgeToAdd = getMilestoneBadge(user.getPlaylistQuizCount());
//                    bonusXP += 100;
//                    break;
//                }
//                else if(user.getPlaylistQuizCount() == 10)
//                {
//                    badgeToAdd = getMilestoneBadge(user.getPlaylistQuizCount());
//                    bonusXP += 100;
//                    break;
//                }
//                else if(user.getPlaylistQuizCount() == 25 || user.getPlaylistQuizCount() == 50)
//                {
//                    badgeToAdd = getMilestoneBadge(user.getPlaylistQuizCount());
//                    bonusXP += 250;
//                    break;
//                }
//                else if((user.getPlaylistQuizCount() % 50) == 0)
//                {
//                    badgeToAdd = getMilestoneBadge(user.getPlaylistQuizCount());
//                    bonusXP += 500;
//                    break;
//                }
//        }
//
//        return badgeToAdd;
//    }
//
//    private List<Badge2> checkForPerformanceBadges()
//    {
//        List<Badge2> performanceBadges = new ArrayList<>();
//        int numQuickReactions = quiz.getQuickReactionCount();
//        String accuracy = quiz.getAccuracy();
//
//        if(Objects.equals(accuracy, "100.0%"))
//        {
//            performanceBadges.add(getPerfectAccuracyBadge());
//        }
//
//        if(numQuickReactions > 2)
//        {
//            performanceBadges.add(getQuickReactionBadge(numQuickReactions));
//        }
//
//        return performanceBadges;
//    }
//
//    private Badge2 getQuickReactionBadge(int numOfQuickReactions)
//    {
//        badgeToAdd = null;
//        allowDuplicates = true;
//        type = BadgeType.PERFORMANCE;
//
//        if(numOfQuickReactions > 2 && numOfQuickReactions < 5)
//        {
//            uid = generateUniqueId(type, 1, topicID);
//            badgeName = "3-4 Quick Reactions";
//            description = "User had 3-4 Quick Reactions";
//            bonusXP += 100;
//            badgeToAdd = new Badge2(uid, badgeName, description, photoURL, type, 1, allowDuplicates);
//        }
//        else if(numOfQuickReactions > 4 && numOfQuickReactions < 10)
//        {
//            uid = generateUniqueId(type, 2, topicID);
//            badgeName = "5-9 Quick Reactions";
//            description = "User had 5-9 Quick Reactions";
//            bonusXP += 250;
//            badgeToAdd = new Badge2(uid, badgeName, description, photoURL, type, 2, allowDuplicates);
//        }
//        else if(numOfQuickReactions == 10)
//        {
//            uid = generateUniqueId(type, 3, topicID);
//            badgeName = String.format(Locale.ENGLISH, "%d Quick Reactions", numOfQuickReactions);
//            description = String.format(Locale.ENGLISH, "User had %d Quick Reactions", numOfQuickReactions);
//            bonusXP += 500;
//            badgeToAdd = new Badge2(uid, badgeName, description, photoURL, type, 3, allowDuplicates);
//        }
//
//        return badgeToAdd;
//    }
//
//    private Badge2 getPerfectAccuracyBadge()
//    {
//        uid = generateUniqueId(BadgeType.PERFORMANCE, 0, topicID);
//        allowDuplicates = true;
//        bonusXP += 500;
//        return new Badge2(uid, "Perfect Accuracy", "User Obtained A Perfect Score On A Quiz", photoURL, BadgeType.PERFORMANCE, 0, allowDuplicates);
//    }
//
//    private Badge2 getMilestoneBadge(int quizCount)
//    {
//        String qzType;
//        String strQuiz;
//        allowDuplicates = false;
//
//        if(quizType == QuizType.PLAYLIST)
//        {
//            qzType = "Playlist";
//            type = BadgeType.PLAYLIST_MILESTONE;
//        }
//        else
//        {
//            qzType = "Artist";
//            type = BadgeType.ARTIST_MILESTONE;
//        }
//
//        if(quizCount == 1)
//        {
//            strQuiz = "Quiz";
//        }
//        else
//        {
//            strQuiz = "Quizzes";
//        }
//
//        uid = generateUniqueId(type, quizCount, topicID);
//        badgeName = String.format(Locale.ENGLISH, "%d %s %s Taken", quizCount, qzType, strQuiz);
//        description = String.format(Locale.ENGLISH, "User has taken %d %s %s", quizCount, qzType, strQuiz);
//        return new Badge2(uid, badgeName, description, null, type, quizCount, allowDuplicates);
//    }
//
//    private String generateUniqueId(BadgeType badgeType, int badgeRank, String topicId) {
//
//        if(badgeType == BadgeType.PLAYLIST_MILESTONE || badgeType == BadgeType.ARTIST_MILESTONE)
//        {
//            String uniqueID = badgeType.ordinal() + "-" + badgeRank + "-" + badgeType;
//            badgeIds.add(uniqueID);
//            return uniqueID;
//        }
//
//        uid = badgeType.ordinal() + "-" + badgeRank + "-" + topicId;
//        badgeIds.add(uid);
//        return uid;
//    }
//
//    @Exclude
//    public int getBonusXP() { return bonusXP; }
//
//    public String getBadgeID() {
//        return badgeID;
//    }
//
//    public PhotoUrl getPhotoURL() { return photoURL; }
//
//    public String getBadgeName() {
//        return badgeName;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public BadgeType getType() {
//        return type;
//    }
//
//    public int getBadgeRank() {
//        return badgeRank;
//    }
//
//}