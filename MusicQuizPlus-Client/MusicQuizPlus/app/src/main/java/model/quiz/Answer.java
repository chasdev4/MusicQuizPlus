package model.quiz;

public class Answer {
    private final String text;
    private final int index;

    public Answer(String text, int index) {
        this.text = text;
        this.index = index;
    }

    public String getText() {
        return text;
    }

    public int getIndex() {
        return index;
    }
}
