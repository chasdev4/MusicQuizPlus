package model.quiz;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import model.Answer;
import model.Quiz;
import model.User;
import model.ValidationObject;
import model.item.Album;
import model.item.Playlist;
import model.item.Track;
import model.type.QuizType;
import model.type.Severity;
import utils.FormatUtil;
import utils.ValidationUtil;

public class PlaylistQuiz extends Quiz {
    private Playlist playlist;

    private int popularityThreshold;

    private final String TAG = "PlaylistQuiz.java";
    private final double MEDIUM_CHANCE = .5;
    private final double GUESS_TRACK_CHANCE = .5;
    private final double GUESS_ALBUM_CHANCE = .15;
    private final double GUESS_ARTIST_CHANCE = .25;
    private final double GUESS_YEAR_CHANCE = .1;

    public PlaylistQuiz(Playlist playlist, User user, FirebaseUser firebaseUser, QuizType type, String id, String queryId, int numQuestions) {
        super(user, firebaseUser, type, id, queryId, numQuestions);
        this.playlist = playlist;
        init();
    }

    // Generates the quiz
    private void init() {
        // For logging
        final String methodName = FormatUtil.formatMethodName("getQuestions");

        // Null check
        List<ValidationObject> validationObjects = new ArrayList<>() {
            {
                add(new ValidationObject(playlist, Playlist.class, Severity.HIGH));
            }
        };
        if (ValidationUtil.nullCheck(validationObjects, TAG, methodName)) {
            return;
        }

        // Check to see if the trackId's are known, they absolutely should be
        if (!playlist.isTrackIdsKnown()) {
            Log.e(TAG, String.format("%s %s track Id's are unknown.", methodName, playlist.getId()));
            return;
        }

        // Get the number of tracks available
        int numTracks = playlist.getTracks().size();

        // If there are less tracks than questions available, update numQuestions
        if (numTracks < getNumQuestions()) {
            setNumQuestions(numTracks);
        }


        // Check the user's Quiz history to separate the old from the new
        // Prepare information for answers
        Map<String, String> quizHistory = getUser().getQuizHistory().get(playlist.getId()).getTrackIds();
        if (quizHistory != null && quizHistory.size() > 0) {
            for (Track track : playlist.getTracks()) {
                if (quizHistory.containsValue(track.getId())) {
                    getOldTracks().add(track);
                }
                else {
                    getNewTracks().add(track);
                }
            }
        }
        // Else there is no history for this quiz
        else {
            setNewTracks(playlist.getTracks());
        }

        // Calculate the number of each question type
        int total = getNumQuestions();
        int guessTrackCount = (int) (getNumQuestions() * GUESS_TRACK_CHANCE);
        int guessAlbumCount = (int) (getNumQuestions() * GUESS_ALBUM_CHANCE);
        int guessArtistCount = (int) (getNumQuestions() * GUESS_ARTIST_CHANCE);
        int guessYearCount = (int) (getNumQuestions() * GUESS_YEAR_CHANCE);
        int newTotal = guessTrackCount+guessAlbumCount+guessArtistCount+guessYearCount;
        if (newTotal < total) {
            guessTrackCount += total - newTotal;
        }
        else if (newTotal > total) {
            guessTrackCount -= newTotal - total;
        }

        // Data for answers
        List<String> trackNamePool = new ArrayList<>();
        List<String> albumNamePool = new ArrayList<>();
        List<String> artistNamePool = new ArrayList<>();
        List<String> yearPool = new ArrayList<>();

        // Gather data for answers
        for (Track track : playlist.getTracks()) {
          //  Album album = getUser().getPlaylist(track.getAlbumId());
            // TODO: Fix me, user has a map of artists, each artist should have a list of albums
            Album album = null;
            trackNamePool.add(track.getName());
            albumNamePool.add(album.getName());
            artistNamePool.add(getUser().getArtist(track.getArtistIds().get(0)).getName());
            yearPool.add(album.getYear());
        }



        // Check to see if numTracks was reassigned or equal to numQuestions
        // If they're equal the user will be quizzed on the entire track set
        if (numTracks == getNumQuestions()) {
            Random rnd = new Random();
            List<Track> playlistTracks = playlist.getTracks();
            List<Track> currentTracks = new ArrayList<>();
            // Loop through and generate the quiz
            for (int i = 0; i < guessTrackCount; i++) {
                // An array for answers
                Answer[] answers = new Answer[4];
                // Pick a random index for the correct answer
                int answerIndex = rnd.nextInt(4);
                // Pick a random index for choosing a playlist track
                int randomTrackIndex = rnd.nextInt(playlistTracks.size());

                // Assign the correct answer
                answers[answerIndex] = new Answer(
                        playlist.getTracks().get(randomTrackIndex).getName(),
                        answerIndex
                        );
                // Remove the track from
                playlistTracks.remove(randomTrackIndex);

                for (int j = 0; j < 4; j++) {
                    if (j == answerIndex) {
                        j++;
                    }
                    else {
                    //    answers[j] =
                }

          //      Question question = new Question(QuestionType.GUESS_TRACK, , answerIndex)
            }
















//            for (int i = 0; i < numTracks; i++) {
//                Question question = null;
//                if (i <= guessTrackCount) {
//                    question = new Question(QuestionType.GUESS_TRACK, );
//                }
//                else if (i <= guessAlbumCount) {
//                   // question = new Question(QuestionType.GUESS_TRACK, );
//                }
//                else if (i <= guessArtistCount) {
//                   // question = new Question(QuestionType.GUESS_TRACK, );
//                }
//                else if  (i <= guessYearCount) {
//                  //  question = new Question(QuestionType.GUESS_TRACK, );
//                }
//
//                Track track = playlist.getTracks().get(i);
//                rnd.nextInt(i + 1);
//                switch (getUser().getDifficulty()) {
//                    case EASY:
//                        if (track.getPopularity() >= popularityThreshold) {
//
//                        }
//                        break;
//                    case MEDIUM:
//
//                        break;
//                    case HARD:
//
//                        break;
//                }
//
//                // Create an inner loop to get 4 answers
//                for (int j = 0; j < 4; j++) {
//
//                }
//            }
        }}
    }
}
