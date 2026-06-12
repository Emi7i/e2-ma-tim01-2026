package com.example.slagalica.domain.model.match.games;

import com.google.firebase.firestore.DocumentId;
import java.util.List;

public class KoZnaZna {
    @DocumentId
    private String koZnaZnaId;
    private String question;
    private String correctAnswer;
    private List<String> otherAnswers;

    public KoZnaZna() {}

    public KoZnaZna(String koZnaZnaId, String question, String correctAnswer, List<String> otherAnswers) {
        this.koZnaZnaId = koZnaZnaId;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.otherAnswers = otherAnswers;
    }

    public String getKoZnaZnaId() {
        return koZnaZnaId;
    }

    public void setKoZnaZnaId(String koZnaZnaId) {
        this.koZnaZnaId = koZnaZnaId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<String> getOtherAnswers() {
        return otherAnswers;
    }

    public void setOtherAnswers(List<String> otherAnswers) {
        this.otherAnswers = otherAnswers;
    }
}
