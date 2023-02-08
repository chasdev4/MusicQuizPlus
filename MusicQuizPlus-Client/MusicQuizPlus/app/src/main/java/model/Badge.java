package model;

import android.content.Context;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import model.item.Album;
import model.item.Artist;
import model.item.Playlist;
import model.type.BadgeType;
import model.type.QuizType;
import service.FirebaseService;

public class Badge {

    private String badgeID;
    private String badgeName;
    private String description;
    private PhotoUrl photoURL;
    private BadgeType badgeType;
    private int badgeRank;

    /*
        Badge Ranks For Artist Badges:
        0 = Badge Rank Not Applicable (Completing an Album/Playlist)
        1 = 3-4 Correct On Artist Quiz
        2 = 5-6 Correct On Artist Quiz
        3 = 7-10 Correct On Artist Quiz

        Badge Ranks For Milestone Badges:
        Rank = Number of Quizzes Taken (Given for 1, 3, 10, 25, and every 50 after that)

        Performance Badges:
            Perfect Accuracy Badge's will have rank of 0

            Badge Ranks for Quick Reactions
            1 = 3-4 Quick Reactions On Quiz
            2 = 5-9 Quick Reactions On Quiz
            3 = 10 Quick Reactions On Quiz
    */

    private List<Badge> earnedBadges = new ArrayList<>();
    private User user;
    private Artist artist;
    private Playlist playlist;
    private Quiz quiz;
    private QuizType type;
    private List<String> badgeIds = new ArrayList<>();
    private boolean allowDuplicates;
    private boolean completedCollection;
    private String topicID;
    private String uid;
    private Badge badgeToAdd;

    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    GoogleSignIn googleSignIn = new GoogleSignIn();
    FirebaseUser firebaseUser = googleSignIn.getAuth().getCurrentUser();

    public Badge()
    {

    }

    public Badge(User user, Quiz quiz)
    {
        this.user = user;
        this.quiz = quiz;
        this.type = quiz.getType();
    }

    private Badge(String badgeID, String badgeName, String description, PhotoUrl photoURL, BadgeType badgeType, int badgeRank, boolean allowDuplicates)
    {
        this.badgeID = badgeID;
        this.badgeName = badgeName;
        this.description = description;
        this.photoURL = photoURL;
        this.badgeType = badgeType;
        this.badgeRank = badgeRank;
        this.allowDuplicates = allowDuplicates;
    }

    public List<Badge> getEarnedBadges(Context context)
    {
        switch(type)
        {
            case ARTIST:
                artist = quiz.getArtist();
                user.incrementArtistQuizCount();
                completedCollection = quiz.getCompletedCollection();
                topicID = artist.getId();
                photoURL = artist.getPhotoUrl().get(0);

                if(completedCollection)
                {
                    //Get completed album badge
                    List<String> completedCollectionIDs = quiz.getCompletedCollectionIDs();
                    List<Album> completedAlbums = new ArrayList<>();

                    for (String albumID : completedCollectionIDs){
                        completedAlbums.add(FirebaseService.checkDatabase(db, "albums", albumID, Album.class));
                    }

                    for (Album album : completedAlbums){
                        earnedBadges.add(getCompletedAlbumBadge(album));
                    }
                }

                Badge badge = getArtistBadge();

                if(badge != null)
                {
                    //There is an artist badge to be awarded
                    earnedBadges.add(badge);
                }

                break;

            case PLAYLIST:
                playlist = quiz.getPlaylist();
                user.incrementPlaylistQuizCount();
                completedCollection = quiz.getCompletedCollection();
                topicID = playlist.getId();
                photoURL = playlist.getPhotoUrl().get(0);

                if(completedCollection)
                {
                    //Get completed playlist badge
                    earnedBadges.add(getCompletedPlaylistBadge());
                }

                break;
        }

        badgeToAdd = checkForMilestoneBadge();
        List <Badge> performanceBadges = checkForPerformanceBadges();

        if (badgeToAdd != null)
        {
            //There Are Milestone Badges To Be Awarded
            earnedBadges.add(badgeToAdd);
        }

        earnedBadges.addAll(performanceBadges);

        if(earnedBadges.size() > 0)
        {
            for (Badge theBadge : earnedBadges)
            {
                String key = db.child("users").child(firebaseUser.getUid()).child("badgeIds").push().getKey();
                user.addBadgeId(key, theBadge.getBadgeID(), theBadge.allowDuplicates);

                badgeToAdd = null;
                badgeToAdd = (Badge) FirebaseService.checkDatabase(db, "badges", theBadge.getBadgeID(), Badge.class);

                if(badgeToAdd != null)
                {
                    //Badge is already in database
                    continue;
                }
                else
                {
                    db.child("badges").child(theBadge.getBadgeID()).setValue(theBadge);
                }
            }
        }

        updateBadgeIDsForUserInDatabase(context);

        return earnedBadges;
    }

    private void updateBadgeIDsForUserInDatabase(Context context)
    {
        User updatedUser = new User(user);

        db.child("users").child(firebaseUser.getUid()).setValue(updatedUser).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Updated (From Badge Model)..", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to update (From Badge Model)..", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Badge getCompletedAlbumBadge(Album album)
    {
        allowDuplicates = false;

        uid = generateUniqueId(BadgeType.ARTIST, 0, album.getId());
        photoURL = album.getPhotoUrl().get(0);
        badgeName = String.format(Locale.ENGLISH, "%s Master", album.getName());
        description = String.format(Locale.ENGLISH, "User has completed all songs in %s", album.getName());

        return new Badge(uid, badgeName, description, photoURL, BadgeType.PLAYLIST, 0, allowDuplicates);
    }

    private Badge getCompletedPlaylistBadge()
    {
        allowDuplicates = false;

        uid = generateUniqueId(BadgeType.PLAYLIST, 0, topicID);
        badgeName = String.format(Locale.ENGLISH, "%s Master", playlist.getName());
        description = String.format(Locale.ENGLISH, "User has completed all songs in %s", playlist.getName());

        return new Badge(uid, badgeName, description, photoURL, BadgeType.PLAYLIST, 0, allowDuplicates);
    }

    private Badge getArtistBadge()
    {
        badgeToAdd = null;

        int score = quiz.getNumCorrect();
        String artistName = artist.getName();
        allowDuplicates = true;

        if(score > 2 && score < 5)
        {
            badgeName = String.format(Locale.ENGLISH, "I Like %s", artistName);
            description = "User Has Got 3-4 Correct on an Artist Quiz";
            uid = generateUniqueId(BadgeType.ARTIST, 1, artist.getId());
            badgeToAdd = new Badge(uid, badgeName, description, photoURL, BadgeType.ARTIST, 1, allowDuplicates);
        }
        else if (score > 4 && score < 7)
        {
            badgeName = String.format(Locale.ENGLISH, "I Love %s", artistName);
            description = "User Has Got 5-6 Correct on an Artist Quiz";
            uid = generateUniqueId(BadgeType.ARTIST, 2, artist.getId());
            badgeToAdd = new Badge(uid, badgeName, description, photoURL, BadgeType.ARTIST, 2, allowDuplicates);
        }
        else if (score > 6 && score <= 10)
        {
            badgeName = String.format(Locale.ENGLISH, "True %s Fan", artistName);
            description = "User Has Got 7-10 Correct on an Artist Quiz";
            uid = generateUniqueId(BadgeType.ARTIST, 3, artist.getId());
            badgeToAdd = new Badge(uid, badgeName, description, photoURL, BadgeType.ARTIST, 3, allowDuplicates);
        }

        return badgeToAdd;
    }

    private Badge checkForMilestoneBadge()
    {
        badgeToAdd = null;

        switch (type)
        {
            case ARTIST:

                if(user.getArtistQuizCount() == 1)
                {
                    badgeToAdd = getMilestoneBadge(user.getArtistQuizCount());
                    break;
                }
                else if(user.getArtistQuizCount() == 3)
                {
                    badgeToAdd = getMilestoneBadge(user.getArtistQuizCount());
                    break;
                }
                else if(user.getArtistQuizCount() == 5)
                {
                    badgeToAdd = getMilestoneBadge(user.getArtistQuizCount());
                    break;
                }
                else if(user.getArtistQuizCount() == 10)
                {
                    badgeToAdd = getMilestoneBadge(user.getArtistQuizCount());
                    break;
                }
                else if(user.getArtistQuizCount() == 25)
                {
                    badgeToAdd = getMilestoneBadge(user.getArtistQuizCount());
                    break;
                }
                else if((user.getArtistQuizCount() % 50) == 0)
                {
                    badgeToAdd = getMilestoneBadge(user.getArtistQuizCount());
                    break;
                }

            case PLAYLIST:

                if(user.getPlaylistQuizCount() == 1)
                {
                    badgeToAdd = getMilestoneBadge(user.getPlaylistQuizCount());
                    break;
                }
                else if(user.getPlaylistQuizCount() == 3)
                {
                    badgeToAdd = getMilestoneBadge(user.getPlaylistQuizCount());
                    break;
                }
                else if(user.getPlaylistQuizCount() == 5)
                {
                    badgeToAdd = getMilestoneBadge(user.getPlaylistQuizCount());
                    break;
                }
                else if(user.getPlaylistQuizCount() == 10)
                {
                    badgeToAdd = getMilestoneBadge(user.getPlaylistQuizCount());
                    break;
                }
                else if(user.getPlaylistQuizCount() == 25)
                {
                    badgeToAdd = getMilestoneBadge(user.getPlaylistQuizCount());
                    break;
                }
                else if((user.getPlaylistQuizCount() % 50) == 0)
                {
                    badgeToAdd = getMilestoneBadge(user.getPlaylistQuizCount());
                    break;
                }
        }

        return badgeToAdd;
    }

    private List<Badge> checkForPerformanceBadges()
    {
        List<Badge> performanceBadges = new ArrayList<>();
        int numQuickReactions = quiz.getQuickReaction();
        String accuracy = quiz.getAccuracy();

        if(Objects.equals(accuracy, "100.0%"))
        {
            performanceBadges.add(getPerfectAccuracyBadge());
        }

        if(numQuickReactions > 2)
        {
            performanceBadges.add(getQuickReactionBadge(numQuickReactions));
        }

        return performanceBadges;
    }

    private Badge getQuickReactionBadge(int numOfQuickReactions)
    {
        badgeToAdd = null;
        allowDuplicates = true;
        badgeType = BadgeType.PERFORMANCE;

        if(numOfQuickReactions > 2 && numOfQuickReactions < 5)
        {
            uid = generateUniqueId(badgeType, 1, topicID);
            badgeName = "3-4 Quick Reactions";
            description = "User had 3-4 Quick Reactions";
            badgeToAdd = new Badge(uid, badgeName, description, photoURL, badgeType, 1, allowDuplicates);
        }
        else if(numOfQuickReactions > 4 && numOfQuickReactions < 10)
        {
            uid = generateUniqueId(badgeType, 2, topicID);
            badgeName = "5-9 Quick Reactions";
            description = "User had 5-9 Quick Reactions";
            badgeToAdd = new Badge(uid, badgeName, description, photoURL, badgeType, 2, allowDuplicates);
        }
        else if(numOfQuickReactions == 10)
        {
            uid = generateUniqueId(badgeType, 3, topicID);
            badgeName = String.format(Locale.ENGLISH, "%d Quick Reactions", numOfQuickReactions);
            description = String.format(Locale.ENGLISH, "User had %d Quick Reactions", numOfQuickReactions);
            badgeToAdd = new Badge(uid, badgeName, description, photoURL, badgeType, 3, allowDuplicates);
        }

        return badgeToAdd;
    }

    private Badge getPerfectAccuracyBadge()
    {
        uid = generateUniqueId(BadgeType.PERFORMANCE, 0, topicID);
        allowDuplicates = true;
        return new Badge(uid, "Perfect Accuracy", "User Obtained A Perfect Score On A Quiz", photoURL, BadgeType.PERFORMANCE, 0, allowDuplicates);
    }

    private Badge getMilestoneBadge(int quizCount)
    {
        String qzType;
        String strQuiz;
        allowDuplicates = false;

        if(type == QuizType.PLAYLIST)
        {
            qzType = "Playlist";
            badgeType = BadgeType.PLAYLIST_MILESTONE;
            uid = generateUniqueId(badgeType, quizCount, topicID);
        }
        else
        {
            qzType = "Artist";
            badgeType = BadgeType.ARTIST_MILESTONE;
            uid = generateUniqueId(badgeType, quizCount, topicID);
        }

        if(quizCount == 1)
        {
            strQuiz = "Quiz";
        }
        else
        {
            strQuiz = "Quizzes";
        }

        badgeName = String.format(Locale.ENGLISH, "%d %s %s Taken", quizCount, qzType, strQuiz);
        description = String.format(Locale.ENGLISH, "User has taken %d %s %s", quizCount, qzType, strQuiz);
        return new Badge(uid, badgeName, description, null, badgeType, quizCount, allowDuplicates);
    }

    private String generateUniqueId(BadgeType badgeType, int badgeRank, String topicId) {

        if(badgeType == BadgeType.PLAYLIST_MILESTONE || badgeType == BadgeType.ARTIST_MILESTONE)
        {
            String uniqueID = badgeType.ordinal() + "-" + badgeRank + "-" + badgeType;
            badgeIds.add(uniqueID);
            return uniqueID;
        }

        uid = badgeType.ordinal() + "-" + badgeRank + "-" + topicId;
        badgeIds.add(uid);
        return uid;
    }

    public String getBadgeID() {
        return badgeID;
    }

    public PhotoUrl getPhotoURL() { return photoURL; }

    public String getBadgeName() {
        return badgeName;
    }

    public String getDescription() {
        return description;
    }

    public BadgeType getBadgeType() {
        return badgeType;
    }

    public int getBadgeRank() {
        return badgeRank;
    }

}