package model.quiz;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.type.DateTime;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import model.Quiz;
import model.User;
import model.ValidationObject;
import model.item.Playlist;
import model.item.Track;
import model.type.Difficulty;
import model.type.QuestionType;
import model.type.QuizType;
import model.type.Severity;
import utils.FormatUtil;
import utils.ValidationUtil;

public class PlaylistQuiz extends Quiz {
    private Playlist playlist;

    private int popularityThreshold;
    // A modifiable list of tracks
    private List<Track> playlistTracks = new ArrayList<>();
    private List<Track> history = new ArrayList<>();

    private final String TAG = "PlaylistQuiz.java";
    private final double GUESS_TRACK_CHANCE = .6;
    private final double GUESS_ALBUM_CHANCE = .1;
    private final double GUESS_ARTIST_CHANCE = .2;
    private final double GUESS_YEAR_CHANCE = .1;
    private final int BUFFER = 10;
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

    public PlaylistQuiz(Playlist playlist, User user, FirebaseUser firebaseUser, QuizType type, String id, String queryId, int numQuestions) {
        super(user, firebaseUser, type, id, queryId, numQuestions);
        this.playlist = playlist;
        init();
    }

    // Generates the quiz
    private void init() {
        //#region Null check
        // For logging
        final String methodName = FormatUtil.formatMethodName("init");


        List<ValidationObject> validationObjects = new ArrayList<>() {
            {
                add(new ValidationObject(playlist, Playlist.class, Severity.HIGH));
            }
        };
        if (ValidationUtil.nullCheck(validationObjects, TAG, methodName)) {
            return;
        }
        //#endregion

        // Check to see if the tracks are known, they absolutely should be
        if (playlist.getTracks().size() == 0) {
            Log.e(TAG, String.format("%s %s tracks are unknown.", methodName, playlist.getId()));
            return;
        }


        // For random index selection
        Random rnd = new Random();

        boolean ignoreDifficulty = true;
        // Enough data for the quiz, but not enough to be picky
        boolean insufficientData = false;

        // Calculate the popularity threshold and update ignoreDifficulty
        if (getUser().getDifficulty() != Difficulty.HARD) {
            popularityThreshold = (int) (playlist.getAveragePopularity() * .6);
            ignoreDifficulty = false;
        }

        //  Get the number of tracks available
        int numTracks = playlist.getTracks().size();

        // If there are less tracks than questions available, update numQuestions
        if (numTracks < getNumQuestions() + BUFFER) {
            setNumQuestions(numTracks - BUFFER);
            insufficientData = true;
        }

        // Calculate the number of each question type
        int total = getNumQuestions();
        int guessTrackCount = (int) (getNumQuestions() * GUESS_TRACK_CHANCE);
        int guessAlbumCount = (int) (getNumQuestions() * GUESS_ALBUM_CHANCE);
        int guessArtistCount = (int) (getNumQuestions() * GUESS_ARTIST_CHANCE);
        int guessYearCount = (int) (getNumQuestions() * GUESS_YEAR_CHANCE);
        int newTotal = guessTrackCount + guessAlbumCount + guessArtistCount + guessYearCount;
        if (newTotal < total) {
            guessTrackCount += total - newTotal;
        } else if (newTotal > total) {
            guessTrackCount -= newTotal - total;
        }

        // Prepare information for answers
        if (insufficientData || ignoreDifficulty
                || getUser().getQuizHistory() == null
                || getUser().getQuizHistory().get(playlist.getId()) == null) {
            for (Track track : playlist.getTracks()) {
                playlistTracks.add(track);
            }

        } else {
            List<Track> oldTracks = new ArrayList<>();
            List<Track> hardTracks = new ArrayList<>();

            Map<String, String> quizHistory = getUser().getQuizHistory().get(playlist.getId()).getTrackIds();
            for (Track track : playlist.getTracks()) {
                boolean skip = false;
                if (!ignoreDifficulty) {
                    if (track.getPopularity() < popularityThreshold) {
                        if (getUser().getDifficulty() == Difficulty.EASY
                                || (getUser().getDifficulty() == Difficulty.MEDIUM
                                && rnd.nextInt(2) == 1))
                            skip = true;
                    }
                }
                if (!skip) {
                    if (quizHistory.containsValue(track.getId())) {
                        oldTracks.add(track);
                    } else {
                        playlistTracks.add(track);
                    }
                } else {
                    hardTracks.add(track);
                }
            }

            // While there isn't enough tracks, add old tracks
            int i = 0;
            while (playlistTracks.size() < getNumQuestions() + BUFFER
                    || i < oldTracks.size() - 1) {
                playlistTracks.add(oldTracks.get(i));
                i++;
            }

            // While there isn't enough tracks, add hard tracks
            i = 0;
            while (playlistTracks.size() < getNumQuestions() + BUFFER
                    || i < hardTracks.size() - 1) {
                playlistTracks.add(hardTracks.get(i));
                i++;
            }

            // This code block shouldn't execute
            if (playlistTracks.size() < getNumQuestions() + BUFFER) {
                Log.e(TAG, "There isn't enough data for this quiz");
                return;
            }

        }

        generateQuestions(QuestionType.GUESS_TRACK, guessTrackCount, rnd);
        generateQuestions(QuestionType.GUESS_ALBUM, guessAlbumCount, rnd);
        generateQuestions(QuestionType.GUESS_ARTIST, guessArtistCount, rnd);
        generateQuestions(QuestionType.GUESS_YEAR, guessYearCount, rnd);
        Collections.shuffle(getQuestions());
        Log.d("Debug", "Yipee");

    }

    private void generateQuestions(QuestionType type, int count, Random rnd) {

        for (int i = 0; i < count; i++) {
            String[] answers = new String[4];

            // Pick a random index for the correct answer
            int answerIndex = rnd.nextInt(4);

            // Pick a random index for choosing a playlist track
            int randomIndex = rnd.nextInt(playlistTracks.size());

            // Assign the correct answer
            answers[answerIndex] = getAnswerText(type, randomIndex);

            // Remove the track from set
            history.add(playlistTracks.get(randomIndex));
            playlistTracks.remove(randomIndex);

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
                        int randomIndex2 = rnd.nextInt(playlistTracks.size());
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
            getQuestions().add(new Question(type, answers, answerIndex));
            Log.d("Debug", "Yipee");
        }
        Log.d("Debug", "Yipee");
    }

    private String getAnswerText(QuestionType type, int randomIndex) {
        switch (type) {
            case GUESS_TRACK:
                return playlistTracks.get(randomIndex).getName();
            case GUESS_ALBUM:
                return playlistTracks.get(randomIndex).getAlbumName();
            case GUESS_ARTIST:
                return playlistTracks.get(randomIndex).getArtistName();
            case GUESS_YEAR:
                return playlistTracks.get(randomIndex).getYear();
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
        int length = (str1Longer) ? b.length() : a.length();

        for (int i = 0; i < length; i++) {
            if (str1[i] == str2[i]) {
                count++;
            } else {
                break;
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
}
