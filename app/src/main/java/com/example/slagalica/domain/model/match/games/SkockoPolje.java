package com.example.slagalica.domain.model.match.games;

public class SkockoPolje {

    private String symbol;

    public SkockoPolje() {
        this.symbol = "";
    }

    public SkockoPolje(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public boolean isEmpty() {
        return symbol == null || symbol.trim().isEmpty();
    }
}