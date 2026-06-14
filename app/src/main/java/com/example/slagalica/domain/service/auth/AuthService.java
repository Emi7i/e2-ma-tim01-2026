package com.example.slagalica.domain.service.auth;

import com.example.slagalica.domain.model.auth.LoginDTO;
import com.example.slagalica.domain.model.auth.RegistrationDTO;
import com.example.slagalica.domain.model.auth.ResetPasswordDTO;
import com.example.slagalica.domain.model.profile.UserProfile;
import com.example.slagalica.repository.impl.UserProfileRepository;
import com.example.slagalica.util.AsyncUtils;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthService {

    private static final long DEFAULT_TOKENS = 5L;
    private static final long DEFAULT_STARS = 0L;
    private static final String DEFAULT_LEAGUE = "Bronze";
    private static final long DEFAULT_RANK = 0L;

    private final FirebaseAuth firebaseAuth;
    private final UserProfileRepository userProfileRepository;

    @Inject
    public AuthService(FirebaseAuth firebaseAuth, UserProfileRepository userProfileRepository) {
        this.firebaseAuth = firebaseAuth;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Registers a new user:
     * 1. Creates the Firebase Auth account
     * 2. Sends the verification email
     * 3. Creates the matching UserProfile document
     */
    public CompletableFuture<Void> registerUser(RegistrationDTO dto) {
        if (!dto.getPassword().equals(dto.getRepeatedPassword())) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("Passwords do not match"));
            return failed;
        }

        return AsyncUtils.toFuture(firebaseAuth.createUserWithEmailAndPassword(dto.getEmail(), dto.getPassword()))
                .thenCompose(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        CompletableFuture<Void> failed = new CompletableFuture<>();
                        failed.completeExceptionally(new IllegalStateException("Registration failed: no user returned"));
                        return failed;
                    }

                    // Send verification email
                    firebaseUser.sendEmailVerification();

                    UserProfile profile = new UserProfile(
                            firebaseUser.getUid(),
                            dto.getUsername(),
                            dto.getEmail(),
                            null,
                            DEFAULT_TOKENS,
                            DEFAULT_STARS,
                            DEFAULT_LEAGUE,
                            dto.getRegion(),
                            null,
                            DEFAULT_RANK
                    );

                    return userProfileRepository.saveProfile(profile);
                });
    }

    /**
     * Logs the user in. If the identifier is not an email, resolves the
     * matching email via UserProfile lookup by username first.
     */
    public CompletableFuture<AuthResult> loginUser(LoginDTO dto) {
        if (isEmail(dto.getIdentifier())) {
            return signIn(dto.getIdentifier(), dto.getPassword());
        }

        return userProfileRepository.findByUsername(dto.getIdentifier())
                .thenCompose(profile -> {
                    if (profile == null) {
                        CompletableFuture<AuthResult> failed = new CompletableFuture<>();
                        failed.completeExceptionally(new IllegalStateException("User not found"));
                        return failed;
                    }
                    return signIn(profile.getEmail(), dto.getPassword());
                });
    }

    private CompletableFuture<AuthResult> signIn(String email, String password) {
        return AsyncUtils.toFuture(firebaseAuth.signInWithEmailAndPassword(email, password));
    }

    /**
     * Resets the password for the currently logged-in user by
     * re-authenticating with the old password, then setting the new one.
     */
    public CompletableFuture<Void> resetPassword(ResetPasswordDTO dto) {
        if (!dto.getNewPassword().equals(dto.getRepeatedNewPassword())) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("New passwords do not match"));
            return failed;
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("No logged-in user"));
            return failed;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), dto.getOldPassword());

        return AsyncUtils.toFuture(user.reauthenticate(credential))
                .thenCompose(unused -> AsyncUtils.toFuture(user.updatePassword(dto.getNewPassword())));
    }

    private boolean isEmail(String identifier) {
        return identifier != null && identifier.contains("@");
    }
}