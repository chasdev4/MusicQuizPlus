package model;

import java.io.Serializable;
import java.util.List;

import model.type.QuestionType;

// SUMMARY
// The question model stores information about a single question in a quiz

public class Question implements Serializable {

    private QuestionType type;
    private List<String> answers;
    private int answerIndex;
    private String trackId;
    private String albumId;
    private String previewUrl;

    public Question(QuestionType type, List<String> answers, int answerIndex, String trackId, String albumId, String previewUrl) {
        this.type = type;
        this.answers = answers;
        this.answerIndex = answerIndex;
        this.trackId = trackId;
        this.albumId = albumId;
        this.previewUrl = previewUrl;

    }
    public Question() {}

    public QuestionType getType() {
        return type;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public int getAnswerIndex() {
        return answerIndex;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }


    public String getTrackId() {
        return trackId;
    }

    public String getAlbumId() {
        return albumId;
    }
}
