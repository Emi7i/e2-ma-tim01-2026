package com.example.slagalica.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slagalica.domain.model.auth.SessionManager;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.domain.model.social.MatchRequest;
import com.example.slagalica.repository.impl.MatchRequestRepository;
import com.google.firebase.firestore.ListenerRegistration;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MatchRequestViewModel extends ViewModel {

    private final MatchRequestRepository matchRequestRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<MatchRequest> outgoingRequest = new MutableLiveData<>(null);
    private final MutableLiveData<MatchRequest> incomingRequest = new MutableLiveData<>(null);

    private ListenerRegistration incomingListener;
    private ListenerRegistration outgoingListener;

    @Inject
    public MatchRequestViewModel(MatchRequestRepository matchRequestRepository,
                                 SessionManager sessionManager) {
        this.matchRequestRepository = matchRequestRepository;
        this.sessionManager = sessionManager;
    }

    public void startListeningForIncoming() {
        if (incomingListener != null) return;
        String userId = sessionManager.getCurrentUserId();
        if (userId == null) return;
        incomingListener = matchRequestRepository.listenForIncomingPending(
                userId, request -> incomingRequest.postValue(request));
    }

    public void sendRequest(String receiverId, String receiverUsername) {
        UserProfile profile = sessionManager.getCurrentProfile().getValue();
        if (profile == null) return;

        MatchRequest request = new MatchRequest(
                null,
                profile.getUserId(),
                receiverId,
                profile.getUsername(),
                receiverUsername,
                MatchRequest.STATUS_PENDING,
                System.currentTimeMillis()
        );

        matchRequestRepository.createRequest(request).thenAccept(id -> {
            request.setId(id);
            outgoingRequest.postValue(request);
            startListeningForOutgoing(id);
        });
    }

    public void cancelRequest() {
        MatchRequest current = outgoingRequest.getValue();
        if (current == null || current.getId() == null) return;
        if (outgoingListener != null) {
            outgoingListener.remove();
            outgoingListener = null;
        }
        matchRequestRepository.updateStatus(current.getId(), MatchRequest.STATUS_CANCELLED)
                .thenAccept(v -> outgoingRequest.postValue(null));
    }

    public void acceptIncoming() {
        MatchRequest current = incomingRequest.getValue();
        if (current == null) return;
        // Use a transaction so that a concurrent cancel cannot be overwritten by an accept.
        matchRequestRepository.acceptIfPending(current.getId())
                .thenAccept(accepted -> incomingRequest.postValue(null))
                .exceptionally(e -> { incomingRequest.postValue(null); return null; });
    }

    public void rejectIncoming() {
        MatchRequest current = incomingRequest.getValue();
        if (current == null) return;
        matchRequestRepository.updateStatus(current.getId(), MatchRequest.STATUS_REJECTED)
                .thenAccept(v -> incomingRequest.postValue(null));
    }

    public void expireIncoming() {
        MatchRequest current = incomingRequest.getValue();
        if (current == null) return;
        matchRequestRepository.updateStatus(current.getId(), MatchRequest.STATUS_EXPIRED)
                .thenAccept(v -> incomingRequest.postValue(null));
    }

    private void startListeningForOutgoing(String requestId) {
        if (outgoingListener != null) outgoingListener.remove();
        outgoingListener = matchRequestRepository.listenForRequest(requestId, request -> {
            if (request == null || !MatchRequest.STATUS_PENDING.equals(request.getStatus())) {
                if (outgoingListener != null) {
                    outgoingListener.remove();
                    outgoingListener = null;
                }
                outgoingRequest.postValue(null);
            }
        });
    }

    public LiveData<MatchRequest> getOutgoingRequest() { return outgoingRequest; }
    public LiveData<MatchRequest> getIncomingRequest() { return incomingRequest; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (incomingListener != null) incomingListener.remove();
        if (outgoingListener != null) outgoingListener.remove();
    }
}
