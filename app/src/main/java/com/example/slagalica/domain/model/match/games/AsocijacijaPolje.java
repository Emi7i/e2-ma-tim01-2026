package com.example.slagalica.domain.model.match.games;

public class AsocijacijaPolje {

    private final String text;
    private boolean opened;

    public AsocijacijaPolje(String text, boolean opened) {
        this.text = text;
        this.opened = opened;
    }

    public String getText() {
        return text;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }
}