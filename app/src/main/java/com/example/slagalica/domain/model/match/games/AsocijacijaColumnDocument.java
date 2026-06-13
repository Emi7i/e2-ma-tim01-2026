package com.example.slagalica.domain.model.match.games;

import java.util.List;

public class AsocijacijaColumnDocument {

    private String label;
    private String solution;
    private List<String> fields;

    public AsocijacijaColumnDocument() {
    }

    public AsocijacijaColumnDocument(String label, String solution, List<String> fields) {
        this.label = label;
        this.solution = solution;
        this.fields = fields;
    }

    public String getLabel() {
        return label;
    }

    public String getSolution() {
        return solution;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}