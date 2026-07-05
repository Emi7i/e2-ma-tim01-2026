package com.example.slagalica.domain.model.social;

import com.google.firebase.firestore.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequest {

    public static final String STATUS_PENDING   = "PENDING";
    public static final String STATUS_ACCEPTED  = "ACCEPTED";
    public static final String STATUS_REJECTED  = "REJECTED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_EXPIRED   = "EXPIRED";

    @DocumentId
    private String id;
    private String senderId;
    private String receiverId;
    private String senderUsername;
    private String receiverUsername;
    private String status;
    private long   timestamp;
    @Getter @Setter
    private String matchId;
}
