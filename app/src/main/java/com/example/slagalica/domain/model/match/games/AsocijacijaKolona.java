package com.example.slagalica.domain.model.match.games;

import java.util.List;

public class AsocijacijaKolona {

    private final String label;
    private final List<AsocijacijaPolje> fields;
    private final String solution;
    private boolean solved;

    public AsocijacijaKolona(String label,
                             List<AsocijacijaPolje> fields,
                             String solution,
                             boolean solved) {
        this.label = label;
        this.fields = fields;
        this.solution = solution;
        this.solved = solved;
    }

    public String getLabel() {
        return label;
    }

    public List<AsocijacijaPolje> getFields() {
        return fields;
    }

    public String getSolution() {
        return solution;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }
}