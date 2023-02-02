package model;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import model.history.GeneratedQuizHistory;
import model.item.Artist;
import model.item.Playlist;
import model.item.Track;
import model.type.Difficulty;
import model.type.QuestionType;
import model.type.QuizType;
import model.type.Severity;
import service.FirebaseService;
import service.firebase.QuizService;
import utils.LogUtil;
import utils.ValidationUtil;


// SUMMARY
// The Quiz model holds data and methods for artist and playlist quizzes

public class Quiz implements Serializable {
    //#region Database members
    private QuizType type;
    private List<Question> questions;
    private String quizId;
    private Difficulty difficulty;
    //#endregion

    //#region Other members
    private User user;
    private String topicId;
    private Playlist playlist;
    private Artist artist;
    private List<Track> tracks = new ArrayList<>();
    private List<Track> history = new ArrayList<>();
    private int numQuestions;
    private int currentQuestionIndex;
    private int score;
    private int numCorrect;
    private int popularityThreshold;
    private List<String> featuredArtistsNames;
    private List<Track> featuredArtistTracks;
    private boolean isNewQuiz;
    private DatabaseReference db;
    private FirebaseUser firebaseUser;
    private int poolCount;
    //#endregion

    //#region Constants
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
    //#endregion

    //#region Constructors
    public Quiz(Playlist playlist, User user, DatabaseReference db, FirebaseUser firebaseUser) {
        topicId = playlist.getId();
        quizId = db.child("generated_quizzes").child(topicId).push().getKey();
        this.playlist = playlist;
        this.db = db;
        this.firebaseUser = firebaseUser;
        artist = null;
        this.user = user;
        type = QuizType.PLAYLIST;
        questions = new ArrayList<>();
        isNewQuiz = true;
        init();
    }

    public Quiz(Artist artist, User user, DatabaseReference db, FirebaseUser firebaseUser) {
        topicId = artist.getId();
        quizId = db.child("generated_quizzes").child(topicId).push().getKey();
        this.db = db;
        this.firebaseUser = firebaseUser;
        playlist = null;
        this.artist = artist;
        this.user = user;
        type = QuizType.ARTIST;
        questions = new ArrayList<>();
        isNewQuiz = true;
        init();
    }

    public Quiz() {

    }
    //#endregion

    //#region Accessors
    public QuizType getType() {
        return type;
    }

    public String getQuizId() {
        return quizId;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Exclude
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

    @Exclude
    public int getScore() {
        return score;
    }

    @Exclude
    public String getAccuracy() {
        double accuracy = (double) numCorrect / numQuestions;
        return String.valueOf(accuracy * 100) + "%";
    }

    @Exclude
    public Question getFirstQuestion() {
        return questions.get(0);
    }

    @Exclude
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
                        } else if (collection == 1) {
                            randomIndex = rnd.nextInt(artist.getAlbums().size());
                            return artist.getAlbums().get(randomIndex).getName();
                        } else {
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
    //#endregion

    //#region Mutators

    //#endregion

    public void init() {
        // Initialize non-final members
        currentQuestionIndex = 0;
        score = 0;
        numCorrect = 0;
        numQuestions = 10;
        difficulty = user.getDifficulty();

        if (!retrieveQuiz()) {
            generateQuiz();
        }
    }

    // Checks the database for generated quizzes and whether or not a user has taken it yet
    private boolean retrieveQuiz() {
        // Get a map of generated quiz ids indexed under the topicId
        Map<String, GeneratedQuiz> generatedQuizzesByTopic = QuizService.retrieveGeneratedQuizzes(db, topicId);

        // If there are no generated quizzes, return to generate one
        if (generatedQuizzesByTopic == null || generatedQuizzesByTopic.isEmpty() || generatedQuizzesByTopic.size() == 0) {
            return false;
        }

        // Iterate over the generated quiz Id's and check against the user's generated quiz history
        for (Map.Entry<String, GeneratedQuiz> generatedQuizEntry : generatedQuizzesByTopic.entrySet()) {
            if (user.getGeneratedQuizHistory() == null) {
                user.setGeneratedQuizHistory(new HashMap<>());
                user.getGeneratedQuizHistory().put(topicId, new HashMap<>());
                Quiz quiz = FirebaseService.checkDatabase(db, "quizzes", generatedQuizEntry.getValue().getQuizId(), Quiz.class);
                if (quiz != null) {
                    newQuizInit(quiz);
                    return true;
                }
            }
            else {
                for (Map.Entry<String, String> generatedQuizHistoryEntry : user.getGeneratedQuizHistory().get(topicId).entrySet()) {
                    if (user.getGeneratedQuizHistory().get(topicId) == null
                            || (!generatedQuizHistoryEntry.getValue().equals(generatedQuizEntry.getValue().getQuizId())
                            || user.getGeneratedQuizHistory().isEmpty())
                            && generatedQuizEntry.getValue().getDifficulty() == user.getDifficulty()) {
                        // Found a new quiz, use it
                        Quiz quiz = FirebaseService.checkDatabase(db, "quizzes", generatedQuizEntry.getValue().getQuizId(), Quiz.class);
                        if (quiz != null) {
                            newQuizInit(quiz);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void newQuizInit(Quiz quiz) {
        isNewQuiz = false;
        type = quiz.getType();
        questions = quiz.questions;
        quizId = quiz.getQuizId();
        history = new ArrayList<>();
        for (Question question : quiz.getQuestions()) {
            history.add(new Track(question.getTrackId()));
        }

    }


    private void generateQuiz() {
        // For logging
        LogUtil log = new LogUtil(TAG, "generateQuiz");
        log.v(String.format("Creating a%s quiz.", (this.type == QuizType.PLAYLIST) ? " playlist" : "n artist"));

        featuredArtistTracks = new ArrayList<>();

        // Local variables that are set depending on the quiz type.
        List<ValidationObject> validationObjects = new ArrayList<>();   // For null checking
        boolean isPlaylistQuiz = false; // Boolean to track the type once
        Class cls = null;               // Class needed for validation object
        List<Track> rawTracks = null;   // All tracks from playlist or hearted albums of artist
        int averagePopularity = 0;      // Playlist or Artists' average popularity

        // Also changes dependent on quiz type
        double guessAlbumChance = GUESS_PLAYLIST_ALBUM_CHANCE;

        // Initialize local variables
        switch (type) {
            case PLAYLIST:
                cls = Playlist.class;
                validationObjects.add(new ValidationObject(playlist, cls, Severity.HIGH));
                rawTracks = playlist.getTracksListFromMap();
                averagePopularity = playlist.getAveragePopularity();
                isPlaylistQuiz = true;
                log.v("Playlist members initialized.");
                break;
            case ARTIST:
                cls = Artist.class;
                validationObjects.add(new ValidationObject(artist, cls, Severity.HIGH));
                rawTracks = artist.getTracks();
                averagePopularity = artist.getAveragePopularity(rawTracks);
                guessAlbumChance = GUESS_ARTIST_ALBUM_CHANCE;
                featuredArtistsNames = artist.getFeaturedArtists(rawTracks);
                log.v("Artist members initialized.");
                break;
        }
        // For creating a quiz history
        poolCount = rawTracks.size();

        // Null check
        if (ValidationUtil.nullCheck(validationObjects, log)) {
            return;
        }

        // Check to see if the tracks are known, they absolutely should be
        if (rawTracks.size() == 0) {
            log.e(String.format("%s tracks are unknown.", topicId));
            return;
        }

        // Should we ignore difficulty
        boolean ignoreDifficulty = true;
        // Enough data for the quiz, but not enough to be picky
        boolean insufficientData = false;

        // User's difficulty
        difficulty = user.getDifficulty();

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

        // TODO: Fix this after history is fixed
        // Prepare information for answers
        if (false)
//        if (insufficientData || ignoreDifficulty
//                || user.getQuizHistory() == null
//                || user.getQuizHistory().get(topicId) == null)
        {
            log.v("Creating quiz based on the entire track set.");
            tracks = rawTracks;
            getFeaturedArtistTracks(guessArtistCount);
        } else {
            log.v("Quiz history found. Separating tracks...");
            List<Track> oldTracks = new ArrayList<>();
            List<Track> hardTracks = new ArrayList<>();

            getFeaturedArtistTracks(guessArtistCount);

            // TODO: here
            Map<String, String> quizHistory = null;
            //   Map<String, String> quizHistory = user.getQuizHistory().get(topicId).getTrackIds();
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
                    if (quizHistory != null && quizHistory.containsValue(track.getId())) {
                        oldTracks.add(track);
                    } else {
                        tracks.add(track);
                    }
                } else {
                    hardTracks.add(track);
                }
            }

            // TODO: Update these when the FE validation plan in set
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
                log.e("There isn't enough data for this quiz");
                return;
            }

        }

        // Generate questions of each type
        generateQuestions(QuestionType.GUESS_TRACK, guessTrackCount, rnd);
        generateQuestions(QuestionType.GUESS_ALBUM, guessAlbumCount, rnd);
        generateQuestions(QuestionType.GUESS_ARTIST, guessArtistCount, rnd);
        generateQuestions(QuestionType.GUESS_YEAR, guessYearCount, rnd);
        Collections.shuffle(questions);
        log.i(String.format("%s Quiz with the id:%s created!", this.type.toString(), topicId));
    }

    private void generateQuestions(QuestionType type, int count, Random rnd) {
        boolean isFeaturedArtistQuestion = this.type == QuizType.ARTIST && type == QuestionType.GUESS_ARTIST;
        if (count < 1) {
            return;
        }
        for (int i = 0; i < count; i++) {
            List<String> answers = new ArrayList<>() {
                {
                    add("");
                    add("");
                    add("");
                    add("");
                }
            };

            // Pick a random index for the correct answer
            int answerIndex = rnd.nextInt(4);
            int randomIndex = 0;

            if (isFeaturedArtistQuestion) {
                randomIndex = rnd.nextInt(featuredArtistTracks.size());
            } else {
                randomIndex = rnd.nextInt(tracks.size());
            }

            String correctTrackId = null;
            String previewUrl = null;

            if (isFeaturedArtistQuestion) {
                answers.set(answerIndex, featuredArtistTracks.get(randomIndex).getFeaturedArtistName());

                // Remove the track from set
                correctTrackId = featuredArtistTracks.get(randomIndex).getId();
                previewUrl = featuredArtistTracks.get(randomIndex).getPreviewUrl();
                history.add(featuredArtistTracks.get(randomIndex));
                featuredArtistTracks.remove(randomIndex);
            } else {
                // Assign the correct answer
                answers.set(answerIndex, getAnswerText(type, randomIndex));

                // Remove the track from set
                correctTrackId = tracks.get(randomIndex).getId();
                previewUrl = tracks.get(randomIndex).getPreviewUrl();
                history.add(tracks.get(randomIndex));
                tracks.remove(randomIndex);
            }


            if (type == QuestionType.GUESS_YEAR) {
                int year = Integer.parseInt(answers.get(answerIndex));

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
                        answers.set(j, String.valueOf(years.get(0)));
                        years.remove(0);
                    }
                }

            } else {
                // Assign the other 3 answers
                for (int j = 0; j < 4; j++) {
                    rnd = new Random();
                    // Skip the correct answer
                    if (j != answerIndex) {
                        int randomIndex2 = 0;
                        if (isFeaturedArtistQuestion) {
                            randomIndex2 = rnd.nextInt(featuredArtistsNames.size());
                        } else {
                            randomIndex2 = rnd.nextInt(tracks.size());
                        }

                        String answerText = getAnswerText(type, randomIndex2);

                        // Validate the new answer
                        boolean tryAgain = false;
                        for (int k = 0; k < 4; k++) {
                            if (answerText != null && answers.get(k) != null) {
                                if (namesMatch(answers.get(k), answerText)) {
                                    tryAgain = true;
                                }
                            }
                        }
                        if (tryAgain) {
                            j--;
                        } else {
                            answers.set(j, answerText);
                        }
                    }
                }
            }
            questions.add(new Question(type, answers, answerIndex, correctTrackId, previewUrl));
        }
    }

    private boolean namesMatch(String a, String b) {
        LogUtil log = new LogUtil(TAG, "namesMatch");
        // Return if either are empty
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }

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
            } catch (Exception e) {
                log.e(e.getMessage());
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

    // Pass in the selected answer
    // Returns the next question
    public Question nextQuestion(int answerIndex) {
        LogUtil log = new LogUtil(TAG, "nextQuestion");
        if (!updateTest(answerIndex)) {
            log.i("No more questions!");
            return null;
        }
        Question question = questions.get(currentQuestionIndex);
        log.i(String.format("Returning question #%s: %s", String.valueOf(currentQuestionIndex + 1), question.getType().toString()));
        return question;
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

    // Call this method after the quiz is complete
    public void end() {
        updateDatabase();
    }

    private void updateDatabase() {
        String key = null;

        if (isNewQuiz) {
            // Save the new quiz up to the database
            db.child("quizzes").child(quizId).setValue(this);

            // Save a reference to this quiz to generated_quizzes
            key = db.child("generated_quizzes").child(topicId).push().getKey();
            db.child("generated_quizzes").child(topicId).child(key).setValue(
                    new GeneratedQuiz(quizId, difficulty));
        }

        user.updateHistoryIds(db, firebaseUser.getUid(), history);
        if (this.type == QuizType.PLAYLIST) {
            user.updatePlaylistHistory(db, firebaseUser.getUid(), topicId, history, poolCount);
        } else {
            user.updateArtistHistory(db, firebaseUser.getUid(), artist, history, poolCount);
        }
        user.updateGeneratedQuizHistory(db, firebaseUser.getUid(), topicId, quizId);
    }
}
