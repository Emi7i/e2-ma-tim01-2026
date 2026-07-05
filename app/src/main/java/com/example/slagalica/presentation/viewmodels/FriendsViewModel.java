package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.repository.impl.FriendsRepository;
import com.example.slagalica.repository.impl.UserProfileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FriendsViewModel extends ViewModel {

    private final FriendsRepository friendsRepository;
    private final UserProfileRepository userProfileRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<List<UserProfile>> friends = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<String> message = new MutableLiveData<>(null);

    @Inject
    public FriendsViewModel(FriendsRepository friendsRepository,
                             UserProfileRepository userProfileRepository,
                             SessionManager sessionManager) {
        this.friendsRepository = friendsRepository;
        this.userProfileRepository = userProfileRepository;
        this.sessionManager = sessionManager;
    }

    public void loadFriends() {
        String currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId == null) return;

        isLoading.postValue(true);
        friendsRepository.getFriendIds(currentUserId)
                .thenCompose(ids -> {
                    if (ids.isEmpty()) {
                        return CompletableFuture.completedFuture(new ArrayList<UserProfile>());
                    }
                    List<CompletableFuture<UserProfile>> futures = new ArrayList<>();
                    for (String id : ids) {
                        futures.add(userProfileRepository.getProfile(id));
                    }
                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> {
                                List<UserProfile> profiles = new ArrayList<>();
                                for (CompletableFuture<UserProfile> f : futures) {
                                    try {
                                        UserProfile profile = f.get();
                                        if (profile != null) profiles.add(profile);
                                    } catch (Exception ignored) {
                                    }
                                }
                                return profiles;
                            });
                })
                .thenCompose(profiles -> {
                    if (profiles.isEmpty()) return CompletableFuture.completedFuture(profiles);
                    List<CompletableFuture<Void>> rankFutures = new ArrayList<>();
                    for (UserProfile profile : profiles) {
                        rankFutures.add(
                            userProfileRepository.getPlayerRank(profile.getNumStars())
                                .thenAccept(r -> profile.setRank(r))
                                .exceptionally(e -> null)
                        );
                    }
                    return CompletableFuture.allOf(rankFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> profiles);
                })
                .thenAccept(profiles -> {
                    friends.postValue(profiles);
                    isLoading.postValue(false);
                })
                .exceptionally(e -> {
                    error.postValue("Greška pri učitavanju prijatelja");
                    isLoading.postValue(false);
                    return null;
                });
    }

    public void searchAndAddFriend(String username) {
        String currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId == null) return;
        String trimmed = username != null ? username.trim() : "";
        if (trimmed.isEmpty()) {
            error.postValue("Unesite korisničko ime");
            return;
        }

        isLoading.postValue(true);
        userProfileRepository.findByUsername(trimmed)
                .thenAccept(profile -> {
                    if (profile == null) {
                        error.postValue("Korisnik nije pronađen");
                        isLoading.postValue(false);
                        return;
                    }
                    if (profile.getUserId().equals(currentUserId)) {
                        error.postValue("Ne možete dodati sebe kao prijatelja");
                        isLoading.postValue(false);
                        return;
                    }
                    friendsRepository.addFriend(currentUserId, profile.getUserId())
                            .thenAccept(v -> {
                                message.postValue("Prijatelj dodat!");
                                loadFriends();
                            })
                            .exceptionally(e -> {
                                error.postValue("Greška pri dodavanju prijatelja");
                                isLoading.postValue(false);
                                return null;
                            });
                })
                .exceptionally(e -> {
                    error.postValue("Greška pri pretraživanju");
                    isLoading.postValue(false);
                    return null;
                });
    }

    public void addFriendByScannedContent(String content) {
        String currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId == null || content == null || content.trim().isEmpty()) return;

        isLoading.postValue(true);
        userProfileRepository.getProfile(content.trim())
                .thenCompose(profile -> {
                    if (profile != null) {
                        return CompletableFuture.completedFuture(profile);
                    }
                    return userProfileRepository.findByUsername(content.trim());
                })
                .thenAccept(profile -> {
                    if (profile == null) {
                        error.postValue("Korisnik nije pronađen");
                        isLoading.postValue(false);
                        return;
                    }
                    if (profile.getUserId().equals(currentUserId)) {
                        error.postValue("Ne možete dodati sebe kao prijatelja");
                        isLoading.postValue(false);
                        return;
                    }
                    friendsRepository.addFriend(currentUserId, profile.getUserId())
                            .thenAccept(v -> {
                                message.postValue("Prijatelj dodat!");
                                loadFriends();
                            })
                            .exceptionally(e -> {
                                error.postValue("Greška pri dodavanju prijatelja");
                                isLoading.postValue(false);
                                return null;
                            });
                })
                .exceptionally(e -> {
                    error.postValue("Greška pri pretraživanju");
                    isLoading.postValue(false);
                    return null;
                });
    }

    public LiveData<List<UserProfile>> getFriends() { return friends; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getMessage() { return message; }
    public void clearMessage() { message.postValue(null); }
    public void clearError() { error.postValue(null); }
}
