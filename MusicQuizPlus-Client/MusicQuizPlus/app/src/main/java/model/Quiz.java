package model;

import com.google.firebase.database.Exclude;

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
import java.util.Timer;
import java.util.TimerTask;

import model.history.TopicHistory;
import model.item.Album;
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
    private List<Question> questions;
    private String quizId;
    private Difficulty difficulty;
    //#endregion

    //#region Other members
    private User user;
    private QuizType type;
    private String topicId;
    private Playlist playlist;
    private Artist artist;
    private List<Track> tracks = new ArrayList<>();
    private List<Track> history = new ArrayList<>();
    private List<Track> wrong = new ArrayList<>();
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
    private boolean addedRemaining;
    private int remaining;
    private long questionTime;
    private Timer questionTimer;
    private long multiplierTime;
    private Timer multiplierTimer;
    private double currentMultiplier;
    private int xp;     // XP gained during the session
    private boolean timeUp = false;
    private boolean completedCollection;
    private List<String> completedCollectionIDs = new ArrayList<>();
    private int quickReaction;
    //#endregion

    //#region Constants
    private final String TAG = "Quiz.java";
    private final double GUESS_TRACK_CHANCE = .6;
    private final double GUESS_PLAYLIST_ALBUM_CHANCE = .1;
    private final double GUESS_ARTIST_ALBUM_CHANCE = .2;
    private final double GUESS_ARTIST_CHANCE = .2;
    private final double GUESS_YEAR_CHANCE = .1;
    private final double QUESTION_INTERVAL = 20;
    private final double MIN_MULTIPLIER = 1;
    private final double MAX_MULTIPLIER = 2;
    private final double MULTIPLIER_INTERVAL = 5;
    private final double MULTIPLIER_RATE = .25;
    private final int BUFFER = 5;
    private final int BASE_SCORE = 250;
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
        addedRemaining = false;
        remaining = 0;
        questionTime = -1;
        questionTimer = new Timer();
        multiplierTimer = new Timer();
        currentMultiplier = 1;
        quickReaction = 0;
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
        addedRemaining = false;
        remaining = 0;
        questionTime = -1;
        questionTimer = new Timer();
        multiplierTimer = new Timer();
        currentMultiplier = 1;
        quickReaction = 0;
        this.user.initArtistTrackCount();
        init();
    }

    public Quiz() {
        questionTimer = new Timer();
    }
    //#endregion

    //#region Accessors
    @Exclude
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
    public boolean getCompletedCollection() {
        return completedCollection;
    }

    @Exclude
    public List<String> getCompletedCollectionIDs() {
        return completedCollectionIDs;
    }

    @Exclude
    private int getFeaturedArtistTracks(int guessArtistCount) {
        if (type == QuizType.ARTIST) {
            boolean deleted = false;
            for (Track track : tracks) {
                if (track.getArtistsMap().size() == 2) {
                    featuredArtistTracks.add(track);
                }
            }
            Random rnd = new Random();
            List<Track> temp = new ArrayList<>();

            if (featuredArtistTracks.size() == 0) {
                return 0;
            }
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
        return featuredArtistTracks.size();
    }

    /*
    //USED FOR TESTING
    public void setNumQuestions (int numberOfQuestions) { numQuestions = numberOfQuestions; }
    public void setNumCorrect(int correct) { numCorrect = correct; }
    public void setQuickReactions(int num) { quickReactions = num; }
    */

    @Exclude
    public Playlist getPlaylist() { return playlist; }

    @Exclude
    public Artist getArtist() { return artist; }

    @Exclude
    public int getNumCorrect() { return numCorrect; }

    @Exclude
    public int getQuickReaction() { return quickReaction; }

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
    private boolean isIgnoreSettingsEnabled() {
        switch (type) {
            case PLAYLIST:
                return user.getSettings().isIgnorePlaylistDifficulty();
            case ARTIST:
                return user.getSettings().isIgnoreArtistDifficulty();
        }
        return false;
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
    private void addToHistory(Map<String, String> quizHistory, Track track) {
        if (!quizHistory.containsValue(track.getId())) {
            history.add(track);
        }
        else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    //#endregion

    //#region Pre-Quiz
    public void init() {
        // Initialize non-final members
        currentQuestionIndex = 0;
        score = 0;
        numCorrect = 0;
        numQuestions = 10;
        difficulty = user.getDifficulty();

        if (type == QuizType.ARTIST || !retrieveQuiz()) {
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
            } else {
                for (Map.Entry<String, String> generatedQuizHistoryEntry : user.getGeneratedQuizHistory().get(topicId).entrySet()) {
                    if (user.getGeneratedQuizHistory().get(topicId) == null
                            || (!user.getGeneratedQuizHistory().get(topicId).containsValue(generatedQuizEntry.getValue().getQuizId())
                            && generatedQuizEntry.getValue().getDifficulty() == user.getDifficulty())) {
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
        questions = quiz.getQuestions();
        quizId = quiz.getQuizId();
        history = new ArrayList<>();
        if (type == QuizType.PLAYLIST) {
            poolCount = playlist.getTrackIds().size();
        }
        else {
            poolCount = artist.getTracks().size();
        }
        String child = (type == QuizType.PLAYLIST) ? "playlists" : "artists";

        for (Question question : quiz.getQuestions()) {
            history.add(new Track(question.getTrackId(), question.getAlbumId()));
        }

    }

    private void generateQuiz() {
        // For logging
        LogUtil log = new LogUtil(TAG, "generateQuiz");
        boolean isPlaylistQuiz = this.type == QuizType.PLAYLIST; // Boolean to track the type once
        log.v(String.format("Creating a%s quiz.", (isPlaylistQuiz) ? " playlist" : "n artist"));

        featuredArtistTracks = new ArrayList<>();

        // Local variables that are set depending on the quiz type.
        List<ValidationObject> validationObjects = new ArrayList<>();   // For null checking
        Class cls = null;               // Class needed for validation object
        List<Track> rawTracks = new ArrayList<>();   // All tracks from playlist or hearted albums of artist
        Map<Integer, Track> playlistTracks = null;
        int averagePopularity = 0;      // Playlist or Artists' average popularity

        // Also changes dependent on quiz type
        double guessAlbumChance = GUESS_PLAYLIST_ALBUM_CHANCE;

        // Initialize local variables
        switch (type) {
            case PLAYLIST:
                cls = Playlist.class;
                validationObjects.add(new ValidationObject(playlist, cls, Severity.HIGH));
                playlistTracks = playlist.getTracks();
                for (int i = 0; i < playlistTracks.size(); i++) {
                    rawTracks.add(playlistTracks.get(i));
                }
                averagePopularity = playlist.getAveragePopularity();
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

        // User's difficulty
        difficulty = user.getDifficulty();

        // Calculate the popularity threshold and update ignoreDifficulty
        if (difficulty != Difficulty.HARD && !isIgnoreSettingsEnabled()) {
            popularityThreshold = (int) (averagePopularity * .6);
        }

        //  Get the number of tracks available
        int numTracks = rawTracks.size();

        // If there are isn't enough data
        if (numTracks < numQuestions + BUFFER) {
            numQuestions = numTracks - BUFFER;
        }

        // Calculate the number of each question type
        int guessTrackCount = (int) (numQuestions * GUESS_TRACK_CHANCE);
        int guessAlbumCount = (int) (numQuestions * guessAlbumChance);
        int guessArtistCount = 0;
        if (isPlaylistQuiz) {
            guessArtistCount = (int) (numQuestions * GUESS_ARTIST_CHANCE);
        }
        else {
            guessArtistCount = (int) featuredArtistsNames.size() / 4;
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

        Map<String, String> quizHistory = new HashMap<>();

        // Prepare information for answers
        if (!isPlaylistQuiz) {
            int result = getFeaturedArtistTracks(guessArtistCount);
            if (result < guessArtistCount) {
                for (int i = 0; i < guessArtistCount - result; i++) {
                    guessArtistCount--;
                    guessTrackCount++;
                }
            }
        }

        if (!isEnoughData(rawTracks.size())) {
            log.v("Creating quiz based on the entire track set.");
            tracks = rawTracks;
        } else {
            log.v("Separating track set...");
            // Old or hard tracks
            List<Track> skippedTracks = new ArrayList<>();

            boolean noQuizHistory = false;
            if (isPlaylistQuiz
                    && user.getPlaylistHistory() != null
                    && user.getPlaylistHistory().size() > 0
                    && user.getPlaylistHistory().containsKey(topicId)
                    && user.getPlaylistHistory().get(topicId).getCount() < user.getPlaylistHistory().get(topicId).getTotal()) {
                log.v("Playlist history exists.");
                quizHistory = user.getPlaylistHistory().get(topicId).getTrackIds();
            }
            else if (!isPlaylistQuiz
                    && user.getArtistHistory() != null
                    && user.getArtistHistory().size() > 0
                    && user.getArtistHistory().containsKey(topicId)) {
                log.v("Artist history exists.");
                for (Map.Entry<String, TopicHistory> album : user.getArtistHistory().get(artist.getId()).getAlbums().entrySet()) {
                    if (album.getValue().getCount() == album.getValue().getTotal()) {
                        Album artistAlbum = artist.getAlbum(album.getKey());
                        for (String trackId : artistAlbum.getTrackIds()) {
                            quizHistory.put(String.valueOf(artistAlbum.getTrackIds().indexOf(trackId)), trackId);
                        }
                    }
                    else {
                        quizHistory.putAll(album.getValue().getTrackIds());
                    }
                }
            }
            else {
                log.v("No quiz history found.");
                noQuizHistory = true;
            }

            if (!noQuizHistory && rawTracks.size() - quizHistory.size() <= numQuestions) {
                for (Track track : rawTracks) {
                    if (!quizHistory.containsValue(track.getId())) {
                        tracks.add(track);
                        remaining++;
                    }
                    else {
                        skippedTracks.add(track);
                    }
                    if (!addedRemaining) {
                        addedRemaining = true;
                    }
                }
                if (addedRemaining) {
                    for (Track track : tracks) {
                        rawTracks.remove(track);
                    }
                }
            }

            if (addedRemaining) {
                int size = tracks.size();
                for (int i = 0; i < numQuestions + BUFFER - size; i++) {
                    int random = rnd.nextInt(rawTracks.size());
                    tracks.add(rawTracks.get(random));
                    rawTracks.remove(random);
                }
            }
            else if (user.getDifficulty() == Difficulty.EASY && !isIgnoreSettingsEnabled()) {
                int size = tracks.size();
                for (int i = 0; i < numQuestions + BUFFER - size; i++) {
                    int random = rnd.nextInt(rawTracks.size());
                    Track track = rawTracks.get(random);
                    if (track.getPopularity() >= popularityThreshold) {
                        tracks.add(track);
                    } else {
                        skippedTracks.add(track);
                    }
                    rawTracks.remove(track);
                }
            }
            else if (user.getDifficulty() == Difficulty.MEDIUM && !isIgnoreSettingsEnabled()) {
                int size = tracks.size();
                for (int i = 0; i < numQuestions + BUFFER - size; i++) {
                    int random = rnd.nextInt(rawTracks.size());
                    Track track = rawTracks.get(random);
                    if (track.getPopularity() >= popularityThreshold && rnd.nextInt(2) == 1) {
                        tracks.add(track);
                    } else {
                        skippedTracks.add(track);
                    }
                    rawTracks.remove(track);
                }
            }
            else {
                int size = tracks.size();
                for (int i = 0; i < numQuestions + BUFFER - size; ) {
                    int random = rnd.nextInt(rawTracks.size());
                    Track track = rawTracks.get(random);
                    boolean added = false;
                    if (!quizHistory.containsValue(track.getId())) {
                        tracks.add(track);
                        rawTracks.remove(track);
                        added = true;
                    }
                    if (noQuizHistory || added) {
                        i++;
                    }
                }
            }

            if (numQuestions + BUFFER - tracks.size() > 0) {
                for (Track track : skippedTracks) {
                    tracks.add(track);
                    if (isEnoughData(tracks.size())) {
                        break;
                    }
                }
            }

            // This code block shouldn't execute
            if (!isEnoughData(tracks.size() + BUFFER)) {
                log.e("There isn't enough data for this quiz");
                return;
            }

        }

        // Generate questions of each type
        generateQuestions(QuestionType.GUESS_TRACK, guessTrackCount, rnd, quizHistory);
        generateQuestions(QuestionType.GUESS_ALBUM, guessAlbumCount, rnd, quizHistory);
        generateQuestions(QuestionType.GUESS_ARTIST, guessArtistCount, rnd, quizHistory);
        generateQuestions(QuestionType.GUESS_YEAR, guessYearCount, rnd, quizHistory);
        Collections.shuffle(questions);
        log.i(String.format("%s Quiz with the id:%s created!", this.type.toString(), topicId));
    }



    private void generateQuestions(QuestionType type, int count, Random rnd, Map<String, String> quizHistory) {
        boolean isFeaturedArtistQuestion = this.type == QuizType.ARTIST && type == QuestionType.GUESS_ARTIST && featuredArtistTracks.size() > 0;
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

            if (addedRemaining && !isFeaturedArtistQuestion) {
                randomIndex = 0;

                if (remaining == 0) {
                    addedRemaining = false;
                }
                remaining--;
            }

            String albumId = null;
            String correctTrackId = null;
            String previewUrl = null;

            Track removedTrack = tracks.get(randomIndex);
            if (isFeaturedArtistQuestion) {
                answers.set(answerIndex, featuredArtistTracks.get(randomIndex).getFeaturedArtistName());

                // Remove the track from set
                albumId = featuredArtistTracks.get(randomIndex).getAlbumId();
                correctTrackId = featuredArtistTracks.get(randomIndex).getId();
                previewUrl = featuredArtistTracks.get(randomIndex).getPreviewUrl();
                addToHistory(quizHistory, featuredArtistTracks.get(randomIndex));
                featuredArtistTracks.remove(randomIndex);
            } else {
                // Assign the correct answer
                answers.set(answerIndex, getAnswerText(type, randomIndex));

                // Remove the track from set
                albumId = tracks.get(randomIndex).getAlbumId();
                correctTrackId = tracks.get(randomIndex).getId();
                previewUrl = tracks.get(randomIndex).getPreviewUrl();
                addToHistory(quizHistory, tracks.get(randomIndex));
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
                    tempYear += rnd.nextInt(4) + 3;
                    years.add(tempYear);
                }
                tempYear = year;
                for (int j = 0; j < yearDown; j++) {
                    tempYear -= rnd.nextInt(4) + 3;
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
                        if (this.type == QuizType.PLAYLIST && type == QuestionType.GUESS_ARTIST
                        && removedTrack.getArtistsMap().containsValue(answerText)) {
                            for (Track track : tracks) {
                                if (!removedTrack.getArtistsMap().containsValue(track.getArtistsMap().get(track.getArtistId()))) {
                                    answerText = track.getArtistsMap().get(track.getArtistId());
                                }
                            }

                            if (removedTrack.getArtistsMap().containsValue(answerText)) {
                                for (Track track : history) {
                                    if (!removedTrack.getArtistsMap().containsValue(track.getArtistsMap().get(track.getArtistId()))) {
                                        answerText = track.getArtistsMap().get(track.getArtistId());
                                    }
                                }
                            }
                        }

                        for (int k = 0; k < 4; k++) {
                            if (answerText != null && answers.get(k) != null) {
                                if (ValidationUtil.namesMatch(answers.get(k), answerText, TAG)) {
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
            questions.add(new Question(type, answers, answerIndex, correctTrackId, albumId, previewUrl));
        }
    }

    private boolean isEnoughData(int size) {
        return size >= numQuestions + BUFFER;
    }
    //#endregion

    //#region During Quiz
    // Pass in the selected answer
    // Returns the next question
    public Question nextQuestion(int answerIndex) {
        LogUtil log = new LogUtil(TAG, "nextQuestion");
        questionTimer.cancel();
        multiplierTimer.cancel();
        questionTimer = new Timer();
        multiplierTimer = new Timer();
        if (timeUp) {
            answerIndex = -1;
            timeUp =false;
        }

        if (!updateTest(answerIndex)) {
            log.i("No more questions!");
            return null;
        }
        Question question = questions.get(currentQuestionIndex);
        questionTime = System.nanoTime();
        scheduleQuestionTimer();
        scheduleMultiplierTimer();
        log.i(String.format("Returning question #%s: %s", String.valueOf(currentQuestionIndex + 1), question.getType().toString()));
        return question;
    }

    private boolean updateTest(int answerIndex) {
        if (answerIndex == questions.get(currentQuestionIndex).getAnswerIndex()) {
            long elapsed = System.nanoTime() - questionTime;
            double seconds = (double)elapsed / 1_000_000_000.0;

            if(seconds <= 5.0)
            {
                quickReaction++;
            }

            multiplierTime += QUESTION_INTERVAL - seconds;
            if (multiplierTime > 25) {
                multiplierTime = 25;
            }
            double reactionTimeBonus = 1 - (seconds / QUESTION_INTERVAL);
            score += (int)(BASE_SCORE * (currentMultiplier + reactionTimeBonus));
            numCorrect++;
        } else {
            for (Track track : history) {
                if (track.getId().equals(questions.get(currentQuestionIndex).getTrackId())) {
                    wrong.add(track);
                    break;
                }
            }
        }
        if (currentQuestionIndex == numQuestions - 1) {
            return false;
        }
        currentQuestionIndex++;
        return true;
    }

    private void scheduleMultiplierTimer() {
        multiplierTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (multiplierTime > 0) {
                    multiplierTime--;
                }

                if (multiplierTime < 5) {
                    currentMultiplier = MIN_MULTIPLIER;
                }
                else if (multiplierTime < 10) {
                    currentMultiplier = 1.25;
                }
                else if (multiplierTime < 15) {
                    currentMultiplier = 1.5;
                }
                else if (multiplierTime < 20) {
                    currentMultiplier = 1.75;
                }
                else {
                    currentMultiplier = MAX_MULTIPLIER;
                }
            }
        }, 1000, 1000);
    }

    private void scheduleQuestionTimer() {
        int delay = (int)(QUESTION_INTERVAL * 1000) + 1;
        questionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeUp = true;
            }
        },delay,500);
    }

    // Call this method to begin the quiz
    public void start() {
        multiplierTime = (int)MAX_MULTIPLIER;
        questionTime = System.nanoTime();

        scheduleMultiplierTimer();
        scheduleQuestionTimer();
    }
    //#endregion

    //#region Post Quiz
    // Call this method after the quiz is complete
    public void end() {
        calculateXp();
        if (firebaseUser != null) {
            updateDatabase();
        }
    }

    private void calculateXp() {
        xp = (int)(score / 4 + 100);
        // TODO: Add bonus XP from badges to xp
    }

    private void updateDatabase() {

        // Update the user's xp / level
        user.addXP(db, firebaseUser, xp);

        String key = null;

        // Artist Quizzes aren't saved to database
        if (isNewQuiz && type == QuizType.PLAYLIST) {
            // Save the new quiz up to the database
            db.child("quizzes").child(quizId).setValue(this);

            // Save a reference to this quiz to generated_quizzes
            key = db.child("generated_quizzes").child(topicId).push().getKey();
            db.child("generated_quizzes").child(topicId).child(key).setValue(
                    new GeneratedQuiz(quizId, difficulty));
            user.updateGeneratedQuizHistory(db, firebaseUser.getUid(), topicId, quizId);
        }

        user.updateHistoryIds(db, firebaseUser.getUid(), history);
        for (Track track : wrong) {
            history.remove(track);
        }
        if (this.type == QuizType.PLAYLIST) {
            completedCollection = user.updatePlaylistHistory(db, firebaseUser.getUid(), playlist, history, poolCount);
        } else {
            completedCollection = user.updateArtistHistory(db, firebaseUser.getUid(), artist, history, poolCount);
            if(completedCollection)
            {
                completedCollectionIDs.addAll(user.getCompletedCollectionIDs());
            }
        }
    }
    //#endregion

    /*
    //USED FOR TESTING
    public void setNumQuestions (int numberOfQuestions) { numQuestions = numberOfQuestions; }
    public void setNumCorrect(int correct) { numCorrect = correct; }
 */
}
