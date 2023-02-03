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

import model.item.Artist;
import model.item.Playlist;
import model.type.BadgeType;
import model.type.QuizType;

public class Badge {

    private String badgeID;
    private String badgeName;
    private String description;
    private String photoURL;
    private BadgeType badgeType;
    private int badgeRank;

    /*
        Badge Ranks For Artist Badges:
        0 = Badge Rank Not Applicable
        1 = Bronze
        2 = Silver
        3 = Gold

        Badge Ranks For Milestone Badges:
        Rank = Number of Quizzes Taken (Given for 1, 3, 10, 25, and every 50 after that

        Perfect Accuracy Badge's will have rank of 0
    */

    private List<Badge> earnedBadges = new ArrayList<>();
    private User user;
    private Artist artist;
    private Playlist playlist;
    private Quiz quiz;
    private QuizType type;
    private List<String> badgeIds = new ArrayList<>();

    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    GoogleSignIn googleSignIn = new GoogleSignIn();
    FirebaseUser firebaseUser = googleSignIn.getAuth().getCurrentUser();

    public Badge(User user, Quiz quiz)
    {
        this.user = user;
        this.quiz = quiz;
        this.type = quiz.getType();
    }

    public Badge(String badgeID, String badgeName, String description, String photoURL, BadgeType badgeType, int badgeRank)
    {
        this.badgeID = badgeID;
        this.badgeName = badgeName;
        this.description = description;
        this.photoURL = photoURL;
        this.badgeType = badgeType;
        this.badgeRank = badgeRank;
    }

    public List<Badge> getEarnedBadges(Context context)
    {
        switch(type)
        {
            case ARTIST:
                artist = quiz.getArtist();
                Badge badge = getArtistBadge(artist);
                user.incrementArtistQuizCount();

                if(badge != null)
                {
                    //There is an artist badge to be awarded
                    earnedBadges.add(badge);
                }
                break;

            case PLAYLIST:

                //TODO: Get playlist badges
                playlist = quiz.getPlaylist();
                //getPlaylistBadge(quiz);
                user.incrementPlaylistQuizCount();
                break;
        }

        Badge milestoneBadge = checkForMilestoneBadge();
        List <Badge> performanceBadges = checkForPerformanceBadges();

        if (milestoneBadge != null)
        {
            //There Are Milestone Badges To Be Awarded
            earnedBadges.add(milestoneBadge);
        }

        earnedBadges.addAll(performanceBadges);

        if(earnedBadges.size() > 0)
        {
            for (Badge theBadge : earnedBadges)
            {
                String key = db.child("users").child(firebaseUser.getUid()).child("badgeIds").push().getKey();
                user.addBadgeId(key, theBadge.getBadgeID());


                DatabaseReference badgeRef = db.child("badges");
                badgeRef.orderByValue().equalTo(theBadge.getBadgeID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            //add to data base
                            //String dbKey = db.child("badges").push().getKey();
                            db.child("badges").child(theBadge.getBadgeID()).setValue(theBadge);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



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
                        Toast.makeText(context, "User has been updated..", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to update the user data..", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Badge getArtistBadge(Artist artist)
    {
        Badge badge = null;

        int score = quiz.getNumCorrect();
        String photoURL = artist.getPhotoUrl().get(0).getUrl();
        String artistName = artist.getName();

        if(score > 2 && score < 5)
        {
            String badgeName = String.format(Locale.ENGLISH, "I Like %s", artistName);
            String description = "User Has Got 3-4 Correct on an Artist Quiz";
            String uid = generateUniqueId(BadgeType.ARTIST, 1, artist.getId());
            badge = new Badge(uid, badgeName, description, photoURL, BadgeType.ARTIST, 1);
        }
        else if (score > 4 && score < 7)
        {
            String badgeName = String.format(Locale.ENGLISH, "I Love %s", artistName);
            String description = "User Has Got 5-6 Correct on an Artist Quiz";
            String uid = generateUniqueId(BadgeType.ARTIST, 2, artist.getId());
            badge = new Badge(uid, badgeName, description, photoURL, BadgeType.ARTIST, 2);
        }
        else if (score > 6 && score <= 10)
        {
            String badgeName = String.format(Locale.ENGLISH, "True %s Fan", artistName);
            String description = "User Has Got 7-10 Correct on an Artist Quiz";
            String uid = generateUniqueId(BadgeType.ARTIST, 3, artist.getId());
            badge = new Badge(uid, badgeName, description, photoURL, BadgeType.ARTIST, 3);
        }

        return badge;
    }

    private Badge checkForMilestoneBadge()
    {
        Badge badge = null;

        switch (type)
        {
            case ARTIST:

                if(user.getArtistQuizCount() == 1)
                {
                    badge = getMilestoneBadge(user.getArtistQuizCount());
                    break;
                }
                else if(user.getArtistQuizCount() == 3)
                {
                    badge = getMilestoneBadge(user.getArtistQuizCount());
                    break;
                }
                else if(user.getArtistQuizCount() == 5)
                {
                    badge = getMilestoneBadge(user.getArtistQuizCount());
                    break;
                }
                else if(user.getArtistQuizCount() == 10)
                {
                    badge = getMilestoneBadge(user.getArtistQuizCount());
                    break;
                }
                else if(user.getArtistQuizCount() == 25)
                {
                    badge = getMilestoneBadge(user.getArtistQuizCount());
                    break;
                }
                else if((user.getArtistQuizCount() % 50) == 0)
                {
                    badge = getMilestoneBadge(user.getArtistQuizCount());
                    break;
                }

            case PLAYLIST:

                if(user.getPlaylistQuizCount() == 1)
                {
                    badge = getMilestoneBadge(user.getPlaylistQuizCount());
                    break;
                }
                else if(user.getPlaylistQuizCount() == 3)
                {
                    badge = getMilestoneBadge(user.getPlaylistQuizCount());
                    break;
                }
                else if(user.getPlaylistQuizCount() == 5)
                {
                    badge = getMilestoneBadge(user.getPlaylistQuizCount());
                    break;
                }
                else if(user.getPlaylistQuizCount() == 10)
                {
                    badge = getMilestoneBadge(user.getPlaylistQuizCount());
                    break;
                }
                else if(user.getPlaylistQuizCount() == 25)
                {
                    badge = getMilestoneBadge(user.getPlaylistQuizCount());
                    break;
                }
                else if((user.getPlaylistQuizCount() % 50) == 0)
                {
                    badge = getMilestoneBadge(user.getPlaylistQuizCount());
                    break;
                }
        }

        return badge;
    }

    private List<Badge> checkForPerformanceBadges()
    {
        List<Badge> performanceBadges = new ArrayList<>();

        String accuracy = quiz.getAccuracy();

        if(Objects.equals(accuracy, "100.0%"))
        {
            performanceBadges.add(getPerfectAccuracyBadge());
        }

        //TODO: Check for quick reaction times for more badges


        return performanceBadges;
    }

    private Badge getPerfectAccuracyBadge()
    {
        String uid;

        if(type == QuizType.PLAYLIST)
        {
            uid = generateUniqueId(BadgeType.PERFORMANCE, 0, playlist.getId());
        }
        else
        {
            uid = generateUniqueId(BadgeType.PERFORMANCE, 0, artist.getId());
        }

        return new Badge(uid, "Perfect Accuracy", "User Obtained A Perfect Score On A Quiz", "photoURL", BadgeType.PERFORMANCE, 0);
    }

    private Badge getMilestoneBadge(int quizCount)
    {
        String qzType;
        String strQuiz;
        String photoURL;
        String uid;

        if(type == QuizType.PLAYLIST)
        {
            qzType = "Playlist";
            photoURL = playlist.getPhotoUrl().get(0).getUrl();
            badgeType = BadgeType.PLAYLIST_MILESTONE;
            uid = generateUniqueId(badgeType, quizCount, playlist.getId());
        }
        else
        {
            qzType = "Artist";
            photoURL = artist.getPhotoUrl().get(0).getUrl();
            badgeType = BadgeType.ARTIST_MILESTONE;
            uid = generateUniqueId(badgeType, quizCount, artist.getId());
        }

        if(quizCount == 1)
        {
            strQuiz = "Quiz";
        }
        else
        {
            strQuiz = "Quizzes";
        }

        String badgeName = String.format(Locale.ENGLISH, "%d %s %s Taken", quizCount, qzType, strQuiz);
        String description = String.format(Locale.ENGLISH, "User has taken %d %s %s", quizCount, qzType, strQuiz);
        return new Badge(uid, badgeName, description, photoURL, badgeType, quizCount);
    }

    public String generateUniqueId(BadgeType badgeType, int badgeRank, String topicId) {

        if(badgeType == BadgeType.PLAYLIST_MILESTONE || badgeType == BadgeType.ARTIST_MILESTONE)
        {
            String uniqueID = "{ " + String.valueOf(badgeType.ordinal()) + " }-{ " + String.valueOf(badgeRank) + " }-{ " + badgeType + " }";
            badgeIds.add(uniqueID);
            return uniqueID;
        }

        String uid = "{ " + String.valueOf(badgeType.ordinal()) + " }-{ " + String.valueOf(badgeRank) + " }-{ " + topicId + " }";
        badgeIds.add(uid);
        return uid;
    }

    public String getBadgeID() {
        return badgeID;
    }

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