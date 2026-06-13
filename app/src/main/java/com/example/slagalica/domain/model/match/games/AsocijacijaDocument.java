package com.example.slagalica.domain.model.match.games;

import com.google.firebase.firestore.DocumentId;

import java.util.List;

public class AsocijacijaDocument {

    @DocumentId
    private String asocijacijaId;
    private String finalSolution;
    private List<AsocijacijaColumnDocument> columns;

    public AsocijacijaDocument() {
    }

    public AsocijacijaDocument(String asocijacijaId, String finalSolution, List<AsocijacijaColumnDocument> columns) {
        this.asocijacijaId = asocijacijaId;
        this.finalSolution = finalSolution;
        this.columns = columns;
    }

    public String getAsocijacijaId() {
        return asocijacijaId;
    }

    public String getFinalSolution() {
        return finalSolution;
    }

    public List<AsocijacijaColumnDocument> getColumns() {
        return columns;
    }

    public void setAsocijacijaId(String asocijacijaId) {
        this.asocijacijaId = asocijacijaId;
    }

    public void setFinalSolution(String finalSolution) {
        this.finalSolution = finalSolution;
    }

    public void setColumns(List<AsocijacijaColumnDocument> columns) {
        this.columns = columns;
    }
}