package model;

import java.util.List;
import java.util.Objects;

import model.type.QuizType;

public class Badge {

    private int badgeID; // Not sure how to create/use this
    private String badgeName;
    private String description;
    private String photoURL;
    private int badgeType;
    private int badgeRank;
    private List<Badge> earnedBadges;
    private Badge performanceBadge;

    public Badge()
    {

    }

    public Badge(String badgeName, String description, String photoURL, int badgeType, int badgeRank)
    {
        this.badgeName = badgeName;
        this.description = description;
        this.photoURL = photoURL;
        this.badgeType = badgeType;
        this.badgeRank = badgeRank;
    }

    public List<Badge> getEarnedBadges(User user, Quiz quiz, QuizType quizType)
    {

        switch(quizType)
        {
            case ARTIST:

                getArtistBadge(quiz);
                user.incrementArtistQuizCount();

            case PLAYLIST:

                //getPlaylistBadge(quiz);
                user.incrementPlaylistQuizCount();
        }

        Badge milestoneBadge = checkForMilestoneBadge(user, quizType);
        List <Badge> performanceBadges = checkForPerformanceBadges(quiz);

        if (milestoneBadge != null)
        {
            //There Are Milestone Badges To Be Awarded
            earnedBadges.add(milestoneBadge);
        }

        return earnedBadges;
    }

    private Badge getArtistBadge(Quiz quiz)
    {
        Badge badge = new Badge();

        int score = quiz.getScore();

        if(score > 2 && score < 5)
        {
            //TODO: Add "I LIKE {ARTIST}" badge
        }
        else if (score > 4 && score < 7)
        {
            //TODO: Add "I LOVE {ARTIST}" badge
        }
        else if (score > 6 && score <= 10)
        {
            //TODO: Add "TRUE {ARTIST} FAN" badge
        }

        return badge;
    }

    private Badge checkForMilestoneBadge(User user, QuizType quizType)
    {
        Badge badge = null;

        switch (quizType)
        {
            case ARTIST:

                if(user.getArtistQuizCount() == 1)
                {
                    //TODO: Add Milestone Badge
                }
                else if(user.getArtistQuizCount() == 3)
                {
                    //TODO: Add Milestone Badge
                }
                else if(user.getArtistQuizCount() == 5)
                {
                    //TODO: Add Milestone Badge
                }
                else if(user.getArtistQuizCount() == 10)
                {
                    //TODO: Add Milestone Badge
                }
                else if(user.getArtistQuizCount() == 25)
                {
                    //TODO: Add Milestone Badge
                }
                else if((user.getArtistQuizCount() % 50) == 0)
                {
                    //TODO: Add Milestone Badge
                }

            case PLAYLIST:

                if(user.getPlaylistQuizCount() == 1)
                {
                    //TODO: Add Milestone Badge
                }
                else if(user.getPlaylistQuizCount() == 3)
                {
                    //TODO: Add Milestone Badge
                }
                else if(user.getPlaylistQuizCount() == 5)
                {
                    //TODO: Add Milestone Badge
                }
                else if(user.getPlaylistQuizCount() == 10)
                {
                    //TODO: Add Milestone Badge
                }
                else if(user.getPlaylistQuizCount() == 25)
                {
                    //TODO: Add Milestone Badge
                }
                else if((user.getPlaylistQuizCount() % 50) == 0)
                {
                    //TODO: Add Milestone Badge
                }
        }

        return badge;
    }

    private List<Badge> checkForPerformanceBadges(Quiz quiz)
    {
        List<Badge> performanceBadges = null;

        if(Objects.equals(quiz.getAccuracy(), "100%"))
        {
            //TODO: Create photo URL for perfect accuracy badge
            Badge perfectAccuracy = new Badge("Perfect Accuracy", "User Obtained A Perfect Score On A Quiz", "photoURL", 0, 0);
            performanceBadges.add(perfectAccuracy);
        }

        return performanceBadges;
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