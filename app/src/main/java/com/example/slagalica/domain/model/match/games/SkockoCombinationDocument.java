package com.example.slagalica.domain.model.match.games;

import com.google.firebase.firestore.DocumentId;

import java.util.List;

public class SkockoCombinationDocument {

    @DocumentId
    private String skockoId;
    private List<String> combination;

    public SkockoCombinationDocument() {
    }

    public SkockoCombinationDocument(String skockoId, List<String> combination) {
        this.skockoId = skockoId;
        this.combination = combination;
    }

    public String getSkockoId() {
        return skockoId;
    }

    public void setSkockoId(String skockoId) {
        this.skockoId = skockoId;
    }

    public List<String> getCombination() {
        return combination;
    }

    public void setCombination(List<String> combination) {
        this.combination = combination;
    }
}