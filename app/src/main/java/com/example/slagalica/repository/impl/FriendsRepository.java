package com.example.slagalica.repository.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FriendsRepository {
    CompletableFuture<List<String>> getFriendIds(String userId);
    CompletableFuture<Void> addFriend(String userId, String friendId);
}
