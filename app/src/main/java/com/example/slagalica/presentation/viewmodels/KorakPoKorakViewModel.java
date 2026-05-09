package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;

@HiltViewModel
public class KorakPoKorakViewModel extends ViewModel {
    @Inject
    public KorakPoKorakViewModel() {}

    private final MutableLiveData<Boolean> revealAllHints = new MutableLiveData<>(false);
    @Getter
    private final List<String> hints = List.of(
            "This is hint 1!", "This is hint 2!", "This is hint 3!", "This is hint 4!", "This is hint 5!",
            "This is hint 6!", "This is hint 7!");
    @Getter
    private final int[] points = {20, 18, 16, 14, 12, 10, 8};
    @Getter
    private int lastRevealedHint = -1;
    @Getter
    private final String answer = "sezame";

    public LiveData<Boolean> getRevealAllHints() { return revealAllHints; }

    public String getNextHint() {
        lastRevealedHint++;
        if (lastRevealedHint >= hints.size()) return null;
        return hints.get(lastRevealedHint);
    }

    public void revealAll() {
        lastRevealedHint = hints.size() - 1;
        revealAllHints.setValue(true);
    }
}
