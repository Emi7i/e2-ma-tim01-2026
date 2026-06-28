package com.example.slagalica.util;

import com.google.android.gms.tasks.Task;

import java.util.concurrent.CompletableFuture;

public final class AsyncUtils {

    private AsyncUtils() {
    }

    public static <T> CompletableFuture<T> toFuture(Task<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        task.addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                future.complete(t.getResult());
            } else {
                future.completeExceptionally(t.getException());
            }
        });
        return future;
    }
}