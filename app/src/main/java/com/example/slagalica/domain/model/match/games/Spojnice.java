package com.example.slagalica.domain.model.match.games;

import com.google.firebase.firestore.DocumentId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Spojnice {
    @DocumentId
    private String spojniceId;
    private List<String> questions;
    private List<String> answers;
}
