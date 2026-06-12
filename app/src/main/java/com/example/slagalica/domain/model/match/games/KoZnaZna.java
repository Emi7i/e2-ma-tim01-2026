package com.example.slagalica.domain.model.match.games;

import com.google.firebase.firestore.DocumentId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KoZnaZna {
    @DocumentId
    private String koZnaZnaId;
    private String question;
    private String correctAnswer;
    private List<String> otherAnswers;
}
