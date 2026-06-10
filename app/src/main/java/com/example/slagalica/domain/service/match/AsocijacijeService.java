package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.games.Asocijacija;
import com.example.slagalica.domain.model.match.games.AsocijacijaKolona;
import com.example.slagalica.domain.model.match.games.AsocijacijaPolje;

import java.util.List;

public class AsocijacijeService {

    private final Asocijacija asocijacija;

    public AsocijacijeService(Asocijacija asocijacija) {
        this.asocijacija = asocijacija;
    }

    public Asocijacija getAsocijacija() {
        return asocijacija;
    }

    public List<AsocijacijaKolona> getColumns() {
        return asocijacija.getColumns();
    }

    public boolean openField(AsocijacijaKolona column, AsocijacijaPolje field) {
        if (column == null || field == null) {
            return false;
        }

        if (field.isOpened()) {
            return false;
        }

        field.setOpened(true);
        return true;
    }

    public boolean submitColumnSolution(AsocijacijaKolona column, String enteredText) {
        if (column == null || enteredText == null) {
            return false;
        }

        String trimmed = enteredText.trim();
        if (trimmed.isEmpty()) {
            return false;
        }

        if (trimmed.equalsIgnoreCase(column.getSolution())) {
            column.setSolved(true);
            openAllFieldsInColumn(column);
            return true;
        }

        return false;
    }

    public boolean submitFinalSolution(String enteredText) {
        if (enteredText == null) {
            return false;
        }

        String trimmed = enteredText.trim();
        if (trimmed.isEmpty()) {
            return false;
        }

        if (trimmed.equalsIgnoreCase(asocijacija.getFinalSolution())) {
            openWholeBoard();
            return true;
        }

        return false;
    }

    public boolean isFinalSolved() {
        return asocijacija.isFinalSolved();
    }

    public void openAllFieldsInColumn(AsocijacijaKolona column) {
        for (AsocijacijaPolje field : column.getFields()) {
            field.setOpened(true);
        }
    }

    public void openWholeBoard() {
        for (AsocijacijaKolona column : asocijacija.getColumns()) {
            column.setSolved(true);
            for (AsocijacijaPolje field : column.getFields()) {
                field.setOpened(true);
            }
        }
        asocijacija.setFinalSolved(true);
    }
}