package com.example.slagalica.domain.model.match.games.common;

public interface OnSessionUpdateListener<T> {
    void onRemoteSessionUpdated(T data);
}