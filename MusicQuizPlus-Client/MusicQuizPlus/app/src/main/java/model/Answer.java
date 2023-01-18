package model;

public class Answer {
    private final String text;
    private final String artistId;
    private final String albumId;
    private final String playlistId;
    private final int index;

    public Answer(String text, String artistId, String albumId, String playlistId, int index) {
        this.text = text;
        this.artistId = artistId;
        this.albumId = albumId;
        this.playlistId = playlistId;
        this.index = index;
    }
}
