package com.example.musicquizplus;

public class GridViewItems {

    private int image_id;
    private String text;

    public int getImage_id() {
        return image_id;
    }

    public void setImage_id(int image_id) {
        this.image_id = image_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    GridViewItems(int img, String text) {
        image_id = img;
        this.text = text;
    }

}
