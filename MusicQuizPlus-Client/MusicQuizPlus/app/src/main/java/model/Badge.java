package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import model.type.QuizType;

public class Badge {

    private int badgeID; // Not sure how to create/use this
    private String badgeName;
    private String description;
    private String photoURL;
    private int badgeType;
    private int badgeRank;
    private List<Badge> earnedBadges = new ArrayList<>();
    private Badge performanceBadge;
    private User user;

    public Badge(User user)
    {
        this.user = user;
    }

    public Badge(String badgeName, String description, String photoURL, int badgeType, int badgeRank)
    {
        this.badgeName = badgeName;
        this.description = description;
        this.photoURL = photoURL;
        this.badgeType = badgeType;
        this.badgeRank = badgeRank;
    }

    public List<Badge> getEarnedBadges(Quiz quiz, QuizType quizType)
    {
        switch(quizType)
        {
            case ARTIST:

                //String artist = quiz.getArtist().getName();
                String artist = "Kanye West";
                Badge badge = getArtistBadge(quiz, artist);
                user.incrementArtistQuizCount();

                if(badge != null)
                {
                    //There is an artist badge to be awarded
                    earnedBadges.add(badge);
                }
                break;

            case PLAYLIST:

                //TODO: Get playlist badges
                //getPlaylistBadge(quiz);
                user.incrementPlaylistQuizCount();
                break;
        }

        Badge milestoneBadge = checkForMilestoneBadge(quizType);
        List <Badge> performanceBadges = checkForPerformanceBadges(quiz);

        if (milestoneBadge != null)
        {
            //There Are Milestone Badges To Be Awarded
            earnedBadges.add(milestoneBadge);
        }

        earnedBadges.addAll(performanceBadges);

        return earnedBadges;
    }

    private Badge getArtistBadge(Quiz quiz, String artist)
    {
        Badge badge = null;

        int score = quiz.getNumCorrect();

        if(score > 2 && score < 5)
        {
            String badgeName = String.format(Locale.ENGLISH, "I Like %s", artist);
            String description = "User Has Got 3-4 Correct on an Artist Quiz";
            badge = new Badge(badgeName, description, "photoURL", 0, 0);
        }
        else if (score > 4 && score < 7)
        {
            String badgeName = String.format(Locale.ENGLISH, "I Love %s", artist);
            String description = "User Has Got 5-6 Correct on an Artist Quiz";
            badge = new Badge(badgeName, description, "photoURL", 0, 0);
        }
        else if (score > 6 && score <= 10)
        {
            String badgeName = String.format(Locale.ENGLISH, "True %s Fan", artist);
            String description = "User Has Got 7-10 Correct on an Artist Quiz";
            badge = new Badge(badgeName, description, "photoURL", 0, 0);
        }

        return badge;
    }

    private Badge checkForMilestoneBadge(QuizType quizType)
    {
        Badge badge = null;

        switch (quizType)
        {
            case ARTIST:

                if(user.getArtistQuizCount() == 1)
                {
                    badge = getMilestoneBadge(user.getArtistQuizCount(), "Artist");
                    break;
                }
                else if(user.getArtistQuizCount() == 3)
                {
                    badge = getMilestoneBadge(user.getArtistQuizCount(), "Artist");
                    break;
                }
                else if(user.getArtistQuizCount() == 5)
                {
                    badge = getMilestoneBadge(user.getArtistQuizCount(), "Artist");
                    break;
                }
                else if(user.getArtistQuizCount() == 10)
                {
                    badge = getMilestoneBadge(user.getArtistQuizCount(), "Artist");
                    break;
                }
                else if(user.getArtistQuizCount() == 25)
                {
                    badge = getMilestoneBadge(user.getArtistQuizCount(), "Artist");
                    break;
                }
                else if((user.getArtistQuizCount() % 50) == 0)
                {
                    badge = getMilestoneBadge(user.getArtistQuizCount(), "Artist");
                    break;
                }

            case PLAYLIST:

                if(user.getPlaylistQuizCount() == 1)
                {
                    badge = getMilestoneBadge(user.getPlaylistQuizCount(), "Playlist");
                    break;
                }
                else if(user.getPlaylistQuizCount() == 3)
                {
                    badge = getMilestoneBadge(user.getPlaylistQuizCount(), "Playlist");
                    break;
                }
                else if(user.getPlaylistQuizCount() == 5)
                {
                    badge = getMilestoneBadge(user.getPlaylistQuizCount(), "Playlist");
                    break;
                }
                else if(user.getPlaylistQuizCount() == 10)
                {
                    badge = getMilestoneBadge(user.getPlaylistQuizCount(), "Playlist");
                    break;
                }
                else if(user.getPlaylistQuizCount() == 25)
                {
                    badge = getMilestoneBadge(user.getPlaylistQuizCount(), "Playlist");
                    break;
                }
                else if((user.getPlaylistQuizCount() % 50) == 0)
                {
                    badge = getMilestoneBadge(user.getPlaylistQuizCount(), "Playlist");
                    break;
                }
        }

        return badge;
    }

    private List<Badge> checkForPerformanceBadges(Quiz quiz)
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
        return new Badge("Perfect Accuracy", "User Obtained A Perfect Score On A Quiz", "photoURL", 0, 0);
    }

    private Badge getMilestoneBadge(int quizCount, String type)
    {
        String badgeName = String.format(Locale.ENGLISH, "%d %s Quizzes Taken", quizCount, type);
        String description = String.format(Locale.ENGLISH, "User has taken %d %s quizzes", quizCount, type);
        return new Badge(badgeName, description, "photoURL", 0, 0);
    }

    public int getBadgeID() {
        return badgeID;
    }

    public String getBadgeName() {
        return badgeName;
    }

    public String getDescription() {
        return description;
    }

    public int getBadgeType() {
        return badgeType;
    }

    public int getBadgeRank() {
        return badgeRank;
    }
}