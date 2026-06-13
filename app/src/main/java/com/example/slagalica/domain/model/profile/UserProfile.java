package com.example.slagalica.domain.model.profile;

import com.google.firebase.firestore.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @DocumentId
    private String userId;
    private String username;
    private String email;
    private String avatar;
    private long numTokens;
    private long numStars;
    private String league;
    private String region;
    private String qrCode;
    private long rank;
}
