package model;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.Difficulty;
import model.type.QuestionType;
import model.type.QuizType;
import model.type.Severity;
import utils.FormatUtil;
import utils.ValidationUtil;

public class Quiz implements Serializable {

    // Final members
    private final User user;          // Difficulty, level and xp
    private final QuizType type;
  //  private final String id; // TODO: Quiz's ID: What if we saved the quiz to firebase for reuse? B or C Feature
    private final List<Question> questions;
    private final Playlist playlist;
    private final Artist artist;

    // Non-final members
    private List<Track> tracks = new ArrayList<>();
    private List<Track> history = new ArrayList<>();
    private int numQuestions;
    private int currentQuestionIndex;
    private int score;
    private int numCorrect;
    private int popularityThreshold;
    private List<String> featuredArtistsNames;
    private List<Track> featuredArtistTracks;

    // Constants
    private final String TAG = "Quiz.java";
    private final Map<Integer, List<String>> WORDS = new HashMap<>() {
        {
            put(1, new ArrayList<>() {
                {

                }
            });
            put(2, new ArrayList<>() {
                {
                    add("an");
                    add("as");
                    add("at");
                    add("be");
                    add("by");
                    add("do");
                    add("he");
                    add("if");
                    add("in");
                    add("is");
                    add("it");
                    add("my");
                    add("no");
                    add("of");
                    add("on");
                    add("or");
                    add("so");
                    add("to");
                    add("up");
                    add("us");
                    add("we");
                }
            });
            put(3, new ArrayList<>() {
                {
                    add("act");
                    add("all");
                    add("and");
                    add("any");
                    add("are");
                    add("but");
                    add("can");
                    add("day");
                    add("did");
                    add("end");
                    add("far");
                    add("few");
                    add("for");
                    add("get");
                    add("god");
                    add("had");
                    add("has");
                    add("her");
                    add("him");
                    add("his");
                    add("how");
                    add("its");
                    add("law");
                    add("man");
                    add("may");
                    add("men");
                    add("not");
                    add("now");
                    add("off");
                    add("old");
                    add("one");
                    add("our");
                    add("own");
                    add("per");
                    add("say");
                    add("see");
                    add("set");
                    add("she");
                    add("the");
                    add("too");
                    add("two");
                    add("use");
                    add("war");
                    add("was");
                    add("way");
                    add("who");
                    add("why");
                    add("yet");
                    add("you");
                }
            });
            put(4, new ArrayList<>() {
                {
                    add("also");
                    add("baby");
                    add("back");
                    add("been");
                    add("both");
                    add("case");
                    add("does");
                    add("down");
                    add("each");
                    add("even");
                    add("from");
                    add("good");
                    add("have");
                    add("here");
                    add("into");
                    add("just");
                    add("life");
                    add("like");
                    add("long");
                    add("made");
                    add("make");
                    add("many");
                    add("more");
                    add("most");
                    add("much");
                    add("must");
                    add("only");
                    add("over");
                    add("part");
                    add("said");
                    add("same");
                    add("some");
                    add("such");
                    add("than");
                    add("that");
                    add("them");
                    add("then");
                    add("they");
                    add("this");
                    add("time");
                    add("upon");
                    add("used");
                    add("very");
                    add("were");
                    add("what");
                    add("when");
                    add("well");
                    add("will");
                    add("with");
                    add("work");
                    add("your");
                }
            });
            put(5, new ArrayList<>() {
                {
                    add("about");
                    add("above");
                    add("after");
                    add("again");
                    add("among");
                    add("being");
                    add("could");
                    add("early");
                    add("every");
                    add("first");
                    add("found");
                    add("given");
                    add("great");
                    add("group");
                    add("house");
                    add("human");
                    add("large");
                    add("later");
                    add("means");
                    add("might");
                    add("never");
                    add("often");
                    add("order");
                    add("other");
                    add("place");
                    add("point");
                    add("power");
                    add("right");
                    add("shall");
                    add("since");
                    add("small");
                    add("state");
                    add("still");
                    add("their");
                    add("there");
                    add("these");
                    add("think");
                    add("those");
                    add("three");
                    add("under");
                    add("until");
                    add("water");
                    add("where");
                    add("which");
                    add("while");
                    add("whole");
                    add("women");
                    add("world");
                    add("would");
                    add("years");
                }
            });
        }
    };
    private final double GUESS_TRACK_CHANCE = .6;
    private final double GUESS_PLAYLIST_ALBUM_CHANCE = .1;
    private final double GUESS_ARTIST_ALBUM_CHANCE = .2;
    private final double GUESS_ARTIST_CHANCE = .2;
    private final double GUESS_YEAR_CHANCE = .1;
    private final int BUFFER = 5;
    private final int BASE_SCORE = 100;

    public Quiz(Playlist playlist, User user) {
        this.playlist = playlist;
        artist = null;
        this.user = user;
        type = QuizType.PLAYLIST;
        questions = new ArrayList<>();
        init();
    }
    public Quiz(Artist artist, User user) {
        playlist = null;
        this.artist = artist;
        this.user = user;
        type = QuizType.ARTIST;
        questions = new ArrayList<>();
        init();
    }

    public void init() {
        generateQuiz();
    }

    private void generateQuiz() {
        // For logging
        final String methodName = FormatUtil.formatMethodName("init");

        // Initialize non-final members
        currentQuestionIndex = 0;
        score = 0;
        numCorrect = 0;
        numQuestions = 10;
        featuredArtistTracks = new ArrayList<>();

        // Local variables that are set depending on the quiz type.
        List<ValidationObject> validationObjects = new ArrayList<>();   // For null checking
        boolean isPlaylistQuiz = false; // Boolean to track the type once
        Class cls = null;               // Class needed for validation object
        List<Track> rawTracks = null;   // All tracks from playlist or hearted albums of artist
        String subjectId = null;        // Playlist or Artist ID
        int averagePopularity = 0;      // Playlist or Artists' average popularity
        // Also changes dependent on quiz type
        double guessAlbumChance = GUESS_PLAYLIST_ALBUM_CHANCE;

        // Initialize local variables
        switch (type) {
            case PLAYLIST:
                cls = Playlist.class;
                validationObjects.add(new ValidationObject(playlist, cls, Severity.HIGH));
                rawTracks = playlist.getTracks();
                subjectId = playlist.getId();
                averagePopularity = playlist.getAveragePopularity();
                isPlaylistQuiz = true;
                break;
            case ARTIST:
                cls = Artist.class;
                validationObjects.add(new ValidationObject(artist, cls, Severity.HIGH));
                rawTracks = artist.getTracks();
                subjectId = artist.getId();
                averagePopularity = artist.getAveragePopularity(rawTracks);
                guessAlbumChance = GUESS_ARTIST_ALBUM_CHANCE;
                featuredArtistsNames = artist.getFeaturedArtists(rawTracks);
                break;
        }

        // Null check
        if (ValidationUtil.nullCheck(validationObjects, TAG, methodName)) {
            return;
        }

        // Check to see if the tracks are known, they absolutely should be
        if (rawTracks.size() == 0) {
            Log.e(TAG, String.format("%s %s tracks are unknown.", methodName, subjectId));
            return;
        }

        // Should we ignore difficulty
        boolean ignoreDifficulty = true;
        // Enough data for the quiz, but not enough to be picky
        boolean insufficientData = false;

        // User's difficulty
        Difficulty difficulty = user.getDifficulty();

        // Calculate the popularity threshold and update ignoreDifficulty
        if (difficulty != Difficulty.HARD) {
            popularityThreshold = (int) (averagePopularity * .6);
            ignoreDifficulty = false;
        }

        //  Get the number of tracks available
        int numTracks = rawTracks.size();

        // TODO: Update and rethink this to work with the front-end quiz validation plan
        // If there are isn't enough data
        if (numTracks < numQuestions + BUFFER) {
            // If a playlist quiz doesn't have enough data
            if (isPlaylistQuiz) {
                numQuestions = numTracks - BUFFER;
                insufficientData = true;
            }
            // If an artist quiz doesn't have enough data
            else {

            }

        }

        // Calculate the number of each question type
        int guessTrackCount = (int) (numQuestions * GUESS_TRACK_CHANCE);
        int guessAlbumCount = (int) (numQuestions * guessAlbumChance);
        int guessArtistCount = 0;
        switch (this.type) {
            case PLAYLIST:
                guessArtistCount = (int) (numQuestions * GUESS_ARTIST_CHANCE);
                break;
            case ARTIST:
                guessArtistCount = (int) featuredArtistsNames.size() / 4;
                break;
        }
        int guessYearCount = (int) (numQuestions * GUESS_YEAR_CHANCE);
        int newTotal = guessTrackCount + guessAlbumCount + guessArtistCount + guessYearCount;




        if (newTotal < numQuestions) {
            guessTrackCount += numQuestions - newTotal;
        } else if (newTotal > numQuestions) {
            guessTrackCount -= newTotal - numQuestions;
        }

        // For random index selection
        Random rnd = new Random();

        // Prepare information for answers
        if (insufficientData || ignoreDifficulty
                || user.getQuizHistory() == null
                || user.getQuizHistory().get(subjectId) == null) {
            tracks = rawTracks;
            getFeaturedArtistTracks(guessArtistCount);

        } else {
            List<Track> oldTracks = new ArrayList<>();
            List<Track> hardTracks = new ArrayList<>();

            getFeaturedArtistTracks(guessArtistCount);

            Map<String, String> quizHistory = user.getQuizHistory().get(subjectId).getTrackIds();
            for (Track track : rawTracks) {
                boolean skip = false;
                if (!ignoreDifficulty) {
                    if (track.getPopularity() < popularityThreshold) {
                        if (difficulty == Difficulty.EASY
                                || (difficulty == Difficulty.MEDIUM
                                && rnd.nextInt(2) == 1))
                            skip = true;
                    }
                }
                if (!skip) {
                    if (quizHistory.containsValue(track.getId())) {
                        oldTracks.add(track);
                    } else {
                        tracks.add(track);
                    }
                } else {
                    hardTracks.add(track);
                }
            }

            // While there isn't enough tracks, add old tracks
            int i = 0;
            while (tracks.size() < numQuestions + BUFFER
                    || i < oldTracks.size() - 1) {
                tracks.add(oldTracks.get(i));
                i++;
            }

            // While there isn't enough tracks, add hard tracks
            i = 0;
            while (tracks.size() < numQuestions + BUFFER
                    || i < hardTracks.size() - 1) {
                tracks.add(hardTracks.get(i));
                i++;
            }

            // This code block shouldn't execute
            if (tracks.size() < numQuestions + BUFFER) {
                Log.e(TAG, "There isn't enough data for this quiz");
                return;
            }

        }


        generateQuestions(QuestionType.GUESS_TRACK, guessTrackCount, rnd);
        generateQuestions(QuestionType.GUESS_ALBUM, guessAlbumCount, rnd);
        generateQuestions(QuestionType.GUESS_ARTIST, guessArtistCount, rnd);
        generateQuestions(QuestionType.GUESS_YEAR, guessYearCount, rnd);
        Collections.shuffle(questions);
        Log.d("Debug", "Yipee");

    }

    private void generateQuestions(QuestionType type, int count, Random rnd) {
        boolean isFeaturedArtistQuestion = this.type == QuizType.ARTIST && type == QuestionType.GUESS_ARTIST;
        if (count < 1) {
            return;
        }
        for (int i = 0; i < count; i++) {
            String[] answers = new String[4];

            // Pick a random index for the correct answer
            int answerIndex = rnd.nextInt(4);
            int randomIndex = 0;

            if (isFeaturedArtistQuestion) {
                randomIndex = rnd.nextInt(featuredArtistTracks.size());
            }
            else {
                randomIndex = rnd.nextInt(tracks.size());
            }

            String previewUrl = null;

            if (isFeaturedArtistQuestion) {
                answers[answerIndex] = featuredArtistTracks.get(randomIndex).getFeaturedArtistName();

                // Remove the track from set
                previewUrl = featuredArtistTracks.get(randomIndex).getPreviewUrl();
                history.add(featuredArtistTracks.get(randomIndex));
                featuredArtistTracks.remove(randomIndex);
            }
            else {
                // Assign the correct answer
                answers[answerIndex] = getAnswerText(type, randomIndex);

                // Remove the track from set
                previewUrl = tracks.get(randomIndex).getPreviewUrl();
                history.add(tracks.get(randomIndex));
                tracks.remove(randomIndex);
            }


            if (type == QuestionType.GUESS_YEAR) {
                int year = Integer.parseInt(answers[answerIndex]);

                int yearDifference = Calendar.getInstance().get(Calendar.YEAR) - year;
                int yearUp = 0;
                int yearDown = 0;
                if (yearDifference > 16) {
                    yearUp = rnd.nextInt(3) + 1;
                    yearDown = 3 - yearUp;
                } else if (yearDifference > 8) {
                    yearUp = rnd.nextInt(2) + 1;
                    yearDown = 3 - yearUp;
                } else {
                    yearDown = 3;
                }
                List<Integer> years = new ArrayList<>();
                int tempYear = year;
                for (int j = 0; j < yearUp; j++) {
                    tempYear += rnd.nextInt(4) + 1;
                    years.add(tempYear);
                }
                tempYear = year;
                for (int j = 0; j < yearDown; j++) {
                    tempYear -= rnd.nextInt(4) + 1;
                    years.add(tempYear);
                }

                Collections.shuffle(years);
                for (int j = 0; j < 4; j++) {
                    if (j != answerIndex) {
                        answers[j] = String.valueOf(years.get(0));
                        years.remove(0);
                    }
                }

            }
            else {
                // Assign the other 3 answers
                for (int j = 0; j < 4; j++) {
                    rnd = new Random();
                    // Skip the correct answer
                    if (j != answerIndex) {
                        int randomIndex2 = 0;
                        if (isFeaturedArtistQuestion) {
                             randomIndex2 = rnd.nextInt(featuredArtistsNames.size());
                        }
                        else {
                             randomIndex2 = rnd.nextInt(tracks.size());
                        }

                        String answerText = getAnswerText(type, randomIndex2);

                        // Validate the new answer
                        boolean tryAgain = false;
                        for (int k = 0; k < 4; k++) {
                            if (answerText != null && answers[k] != null) {
                                if (namesMatch(answers[k], answerText)) {
                                    tryAgain = true;
                                }
                            }
                        }
                        if (tryAgain) {
                            j--;
                        } else {
                            answers[j] = answerText;
                        }
                    }
                }
            }
            questions.add(new Question(type, answers, answerIndex, previewUrl));
        }
    }

    private void getFeaturedArtistTracks(int guessArtistCount) {
        if (type == QuizType.ARTIST) {
            boolean deleted = false;
            for (Track track : tracks) {
                if (track.getArtistsMap().size() == 2) {
                    featuredArtistTracks.add(track);
                }
            }
            Random rnd = new Random();
            List<Track> temp = new ArrayList<>();

            for (int i = 0; i < guessArtistCount; i++) {
                temp.add(featuredArtistTracks.get(rnd.nextInt(featuredArtistTracks.size())));
            }
            featuredArtistTracks = temp;

            for (int i = 0; i < featuredArtistTracks.size(); i++) {
                featuredArtistsNames.remove(featuredArtistsNames.indexOf(featuredArtistTracks.get(i).getFeaturedArtistName()));
            }

            for (Track track : featuredArtistTracks) {
                tracks.remove(track);
            }
        }
    }


    private String getAnswerText(QuestionType type, int randomIndex) {
        switch (type) {
            case GUESS_TRACK:
                return tracks.get(randomIndex).getName();
            case GUESS_ALBUM:
                switch (this.type) {
                    case PLAYLIST:
                        return tracks.get(randomIndex).getAlbumName();
                    case ARTIST:
                        Random rnd = new Random();
                        int collection = rnd.nextInt(3);
                        if (collection == 0) {
                            randomIndex = rnd.nextInt(artist.getSingles().size());
                            return artist.getSingles().get(randomIndex).getName();
                        }
                        else if (collection == 1) {
                            randomIndex = rnd.nextInt(artist.getAlbums().size());
                            return artist.getAlbums().get(randomIndex).getName();
                        }
                        else {
                            randomIndex = rnd.nextInt(artist.getCompilations().size());
                            return artist.getCompilations().get(randomIndex).getName();
                        }
                }

            case GUESS_ARTIST:
                switch (this.type) {
                    case PLAYLIST:
                        return tracks.get(randomIndex).getArtistName();
                    case ARTIST:
                        return featuredArtistsNames.get(randomIndex);
                }
            case GUESS_YEAR:
                return tracks.get(randomIndex).getYear();
        }
        return null;
    }

    private boolean namesMatch(String a, String b) {
        // Return if they're equal
        if (a.equals(b)) {
            return true;
        }

        int count = 0;
        char[] str1 = a.toCharArray();
        char[] str2 = b.toCharArray();
        boolean str1Longer = a.length() > a.length();
        int length = (!str1Longer) ? b.length() : a.length();

        for (int i = 0; i < length; i++) {
            // TODO: Add a breakpoint to the catch block if it doesn't exist. Solve why the if statement throws an ArrayIndexOutOfBoundsException
            try {
                if (str1[i] == str2[i]) {
                    count++;
                } else {
                    break;
                }
            }
            catch (Exception e) {
                Log.d("Quiz", "Tricky bug");
            }

        }

        // Return if there were no matches
        if (count == 0) {
            return false;
        } else if (count == length) {
            return true;
        }

        String strResult = a.substring(0, count);

        // Remove trailing spaces
        if (str1[count - 1] == ' ') {
            strResult = strResult.trim();
        }

        if (strResult.length() > 1 && strResult.length() <= 5) {
            if (WORDS.get(strResult.length()).contains(strResult.toLowerCase())) {
                return false;
            }
        }

        if (count < 3 && count > length - 3) {
            return true;
        }

        return false;
    }

    public Question getFirstQuestion() {
        return questions.get(0);
    }

    public int getScore() {
        return score;
    }

    public String getAccuracy() {
        double accuracy = (double)numCorrect / numQuestions;
        return String.valueOf(accuracy  * 100)  + "%";
    }

    // Pass in the selected answer
    // Returns the next question
    public Question nextQuestion(int answerIndex) {
        if (!updateTest(answerIndex)) {
            return null;
        }
        return questions.get(currentQuestionIndex);
    }

    private boolean updateTest(int answerIndex) {
        if (answerIndex == questions.get(currentQuestionIndex).getAnswerIndex()) {
            score += BASE_SCORE;
            numCorrect++;
        }
        if (currentQuestionIndex == numQuestions - 1) {
            return false;
        }
        currentQuestionIndex++;
        return true;
    }
}
