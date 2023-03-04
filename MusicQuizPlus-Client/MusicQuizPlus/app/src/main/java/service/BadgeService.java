package service;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import model.PhotoUrl;
import model.Quiz;
import model.Badge;
import model.User;
import model.type.BadgeType;

public class BadgeService implements Serializable {

    private User user;

    public BadgeService(User user) {
        this.user = user;
    }

    //#region Accessors
    public static String getPath(Badge value) {
        String path = "";
        switch (value.getType()) {
            case PLAYLIST_KNOWLEDGE:
                path = "playlists/";
                break;
            case ARTIST_KNOWLEDGE_1:
            case ARTIST_KNOWLEDGE_2:
            case ARTIST_KNOWLEDGE_3:
            case ARTIST_KNOWLEDGE_4:
                path = "artists/";
                break;
            case STUDIO_ALBUM_KNOWLEDGE:
            case OTHER_ALBUM_KNOWLEDGE:
                path = "albums/";
                break;
        }
        path+=value.getId();

        return path;
    }
    public List<Badge> getBadges(Quiz quiz) {
        return getQuizBadges(quiz);
    }
    private List<Badge> getQuizBadges(Quiz quiz) {
        List<Badge> badges = new ArrayList<>();

        BadgeType badgeType = null;
        // Quick Reactor Rank 3
        if (quiz.getQuickReactionCount() >= 7) {
            badgeType = BadgeType.QUICK_REACTOR_3;
        }
        // Quick Reactor Rank 2
        else if(quiz.getQuickReactionCount() >= 5)
        {
            badgeType = BadgeType.QUICK_REACTOR_2;
        }
        // Quick Reactor Rank 1
        else if(quiz.getQuickReactionCount() >= 3)
        {
            badgeType = BadgeType.QUICK_REACTOR_1;
        }

        if (badgeType != null) {
            badges.add(new Badge(badgeType, null));
        }

        // Perfect Accuracy
        if (quiz.getNumQuestions() == quiz.getNumCorrect()) {
            badges.add(new Badge(BadgeType.PERFECT_ACCURACY, null));
        }

        // Get any badges on the user
        if (user.getEarnedBadges().size() > 0) {
            badges.addAll(user.getEarnedBadges());
            user.resetEarnedBadges();
        }

        return badges;
    }
    public static String getBadgeThumbnail(DatabaseReference db, String path) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String url = null;
        final DataSnapshot[] photoUrlSnapshot = {null};

        db.child(path + "/photoUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                photoUrlSnapshot[0] = snapshot;
                countDownLatch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PhotoUrl temp = null;
        for (DataSnapshot ds : photoUrlSnapshot[0].getChildren()) {
            PhotoUrl p = ds.getValue(PhotoUrl.class);
            if (temp == null) {
                temp = p;
            }
            if (temp.getWidth() > p.getWidth() && temp.getHeight() > p.getHeight()) {
                temp = p;
            }
        }
        if (temp != null) {
            url = temp.getUrl();
        }

        return url;
    }

    public static String getBadgeName(DatabaseReference db, String path) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] name = {null};

        db.child(path + "/name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    name[0] = snapshot.getValue().toString();
                }
                countDownLatch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return name[0];
    }

    public static String getBadgeName(BadgeType type) { return BADGE_NAMES.get(type); }
    public static String getBadgeText(BadgeType type, String name, int number) {
        String text = BADGE_TEXTS.get(type);
        if (isMilestone50(type)) {
            return String.format(text, String.valueOf(number));
        }
        else if (hasThumbnail(type)) {
            return String.format(text, name);
        }
        return text;
    }
    public static String getBadgeDescription(BadgeType type, String name, int number) {
        String description = BADGE_DESCRIPTIONS.get(type);
        if (isMilestone50(type)) {
            return String.format(description, String.valueOf(number));
        }
        else if (hasTwoArguments(type)) {
            return String.format(description, String.valueOf(number), name);
        }
        else if (hasOneArgument(type)) {
            return String.format(description, name);
        }
        return description;
    }
    public static Integer getBadgeXp(BadgeType type) { return BADGE_XP.get(type); }
    //#endregion
    public static boolean hasThumbnail(BadgeType type) {
        return type == BadgeType.PLAYLIST_KNOWLEDGE ||
                type == BadgeType.ARTIST_KNOWLEDGE_1 ||
                type == BadgeType.ARTIST_KNOWLEDGE_2 ||
                type == BadgeType.ARTIST_KNOWLEDGE_3 ||
                type == BadgeType.ARTIST_KNOWLEDGE_4 ||
                type == BadgeType.STUDIO_ALBUM_KNOWLEDGE ||
                type == BadgeType.OTHER_ALBUM_KNOWLEDGE;
    }
    private static boolean isMilestone50(BadgeType type) {
        return type == BadgeType.ARTIST_QUIZ_MILESTONE_50 ||
                type == BadgeType.PLAYLIST_QUIZ_MILESTONE_50;
    }

    private static boolean hasTwoArguments(BadgeType type) {
        return type == BadgeType.ARTIST_KNOWLEDGE_1 ||
                type == BadgeType.ARTIST_KNOWLEDGE_2 ||
                type == BadgeType.ARTIST_KNOWLEDGE_3;
    }
    private static boolean hasOneArgument(BadgeType type) {
        return type == BadgeType.PLAYLIST_KNOWLEDGE ||
                type == BadgeType.ARTIST_KNOWLEDGE_4 ||
                type == BadgeType.STUDIO_ALBUM_KNOWLEDGE ||
                type == BadgeType.OTHER_ALBUM_KNOWLEDGE;
    }

    // The official names for the badges
    private static final Map<BadgeType, String> BADGE_NAMES = new HashMap<>() {
        {
            put(BadgeType.QUICK_REACTOR_1, "Quick Reactor I");
            put(BadgeType.QUICK_REACTOR_2, "Quick Reactor II");
            put(BadgeType.QUICK_REACTOR_3, "Quick Reactor III");
            put(BadgeType.PERFECT_ACCURACY, "Perfect Accuracy");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_1, "Artist Quiz Milestone I");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_3, "Artist Quiz Milestone II");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_5, "Artist Quiz Milestone III");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_10, "Artist Quiz Milestone IV");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_25, "Artist Quiz Milestone V");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_50, "Artist Quiz Milestone VI");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_1, "Playlist Quiz Milestone I");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_3, "Playlist Quiz Milestone II");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_5, "Playlist Quiz Milestone III");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_10, "Playlist Quiz Milestone IV");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_25, "Playlist Quiz Milestone V");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_50, "Playlist Quiz Milestone VI");
            put(BadgeType.PLAYLIST_KNOWLEDGE, "Knows Playlist");
            put(BadgeType.ARTIST_KNOWLEDGE_1, "Knows Artist");
            put(BadgeType.ARTIST_KNOWLEDGE_2, "Likes Artist");
            put(BadgeType.ARTIST_KNOWLEDGE_3, "Loves Artist");
            put(BadgeType.ARTIST_KNOWLEDGE_4, "True Fan");
            put(BadgeType.STUDIO_ALBUM_KNOWLEDGE, "Knows Album");
            put(BadgeType.OTHER_ALBUM_KNOWLEDGE, "Knows Album");
        }
    };

    // The text to display
    private static final Map<BadgeType, String> BADGE_TEXTS = new HashMap<>() {
        {
            put(BadgeType.QUICK_REACTOR_1, "Quick Reactor I");
            put(BadgeType.QUICK_REACTOR_2, "Quick Reactor II");
            put(BadgeType.QUICK_REACTOR_3, "Quick Reactor III");
            put(BadgeType.PERFECT_ACCURACY, "Perfect Accuracy");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_1, "First Artist Quiz!");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_3, "3 Artist Quizzes!");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_5, "5 Artist Quizzes!");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_10, "10 Artist Quizzes!");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_25, "25 Artist Quizzes!");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_50, "%s Artist Quizzes!"); // dynamic number
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_1, "First Playlist Quiz!");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_3, "3 Playlist Quizzes!");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_5, "5 Playlist Quizzes!");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_10, "10 Playlist Quizzes!");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_25, "25 Playlist Quizzes!");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_50, "%s Playlist Quizzes!"); // dynamic number
            put(BadgeType.PLAYLIST_KNOWLEDGE, "Knows %s");          // playlist name
            put(BadgeType.ARTIST_KNOWLEDGE_1, "Knows %s");          // artist name
            put(BadgeType.ARTIST_KNOWLEDGE_2, "Likes %s");          // artist name
            put(BadgeType.ARTIST_KNOWLEDGE_3, "Loves %s");          // artist name
            put(BadgeType.ARTIST_KNOWLEDGE_4, "True %s Fan");       // artist name
            put(BadgeType.STUDIO_ALBUM_KNOWLEDGE, "Knows %s");      // album name
            put(BadgeType.OTHER_ALBUM_KNOWLEDGE, "Knows %s");       // album name
        }
    };

    private static final Map<BadgeType, String> BADGE_DESCRIPTIONS = new HashMap<>() {
        {
            put(BadgeType.QUICK_REACTOR_1, "You got at least 3 quick reactions!");
            put(BadgeType.QUICK_REACTOR_2, "You got at least 5 quick reactions!");
            put(BadgeType.QUICK_REACTOR_3, "You got at least 7 quick reactions!");
            put(BadgeType.PERFECT_ACCURACY, "You answered all questions correctly!");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_1, "Keep playing to earn more badges!");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_3, "Looks like you're getting the hang of it!");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_5, "You've taken 5 artist quizzes!");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_10, "You've taken 10 artist quizzes!");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_25, "You've taken 25 artist quizzes!");
            put(BadgeType.ARTIST_QUIZ_MILESTONE_50, "You've taken %s artist quizzes!"); // dynamic number
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_1, "Keep playing to earn more badges!");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_3, "Looks like you're getting the hang of it!");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_5, "You've taken 5 artist quizzes!");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_10, "You've taken 10 artist quizzes!");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_25, "You've taken 25 artist quizzes!");
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_50, "You've taken %s artist quizzes!"); // dynamic number
            put(BadgeType.PLAYLIST_KNOWLEDGE, "You know all the songs in %s"); // Playlist name
            put(BadgeType.ARTIST_KNOWLEDGE_1, "You know %s %s songs!"); // number, artist name
            put(BadgeType.ARTIST_KNOWLEDGE_2, "You know %s %s songs!"); // number, artist name
            put(BadgeType.ARTIST_KNOWLEDGE_3, "You know %s %s songs!"); // number, artist name
            put(BadgeType.ARTIST_KNOWLEDGE_4, "You know every song by %s!"); // artist name
            put(BadgeType.STUDIO_ALBUM_KNOWLEDGE, "You know every song on %s"); // album name
            put(BadgeType.OTHER_ALBUM_KNOWLEDGE, "You know every song on %s"); // album name
        }
    };

    private static final Map<BadgeType, Integer> BADGE_XP = new HashMap<>(){
        {
            put(BadgeType.QUICK_REACTOR_1, 100);
            put(BadgeType.QUICK_REACTOR_2, 300);
            put(BadgeType.QUICK_REACTOR_3, 500);
            put(BadgeType.PERFECT_ACCURACY, 500);
            put(BadgeType.ARTIST_QUIZ_MILESTONE_1, 100);
            put(BadgeType.ARTIST_QUIZ_MILESTONE_3, 100);
            put(BadgeType.ARTIST_QUIZ_MILESTONE_5, 100);
            put(BadgeType.ARTIST_QUIZ_MILESTONE_10, 100);
            put(BadgeType.ARTIST_QUIZ_MILESTONE_25, 200);
            put(BadgeType.ARTIST_QUIZ_MILESTONE_50, 300);
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_1, 100);
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_3, 100);
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_5, 100);
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_10, 100);
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_25, 200);
            put(BadgeType.PLAYLIST_QUIZ_MILESTONE_50, 300);
            put(BadgeType.PLAYLIST_KNOWLEDGE, 300);
            put(BadgeType.ARTIST_KNOWLEDGE_1, 100);
            put(BadgeType.ARTIST_KNOWLEDGE_2, 100);
            put(BadgeType.ARTIST_KNOWLEDGE_3, 300);
            put(BadgeType.ARTIST_KNOWLEDGE_4, 1000);
            put(BadgeType.STUDIO_ALBUM_KNOWLEDGE, 100);
            put(BadgeType.OTHER_ALBUM_KNOWLEDGE, 100);
        }
    };
}
