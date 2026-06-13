package com.example.slagalica.domain.model.match.games;

import com.google.firebase.firestore.DocumentId;
import java.util.List;

public class Spojnice {
    @DocumentId
    private String spojniceId;
    private String title;
    private List<String> questions;
    private List<String> answers;

    public Spojnice() {}

    public Spojnice(String spojniceId, String title, List<String> questions, List<String> answers) {
        this.spojniceId = spojniceId;
        this.title = title;
        this.questions = questions;
        this.answers = answers;
    }

    public String getSpojniceId() { return spojniceId; }
    public void setSpojniceId(String spojniceId) { this.spojniceId = spojniceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getQuestions() { return questions; }
    public void setQuestions(List<String> questions) { this.questions = questions; }

    public List<String> getAnswers() { return answers; }
    public void setAnswers(List<String> answers) { this.answers = answers; }
}
