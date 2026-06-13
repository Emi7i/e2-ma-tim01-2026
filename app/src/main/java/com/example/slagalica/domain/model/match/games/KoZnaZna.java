package com.example.slagalica.domain.model.match.games;

import com.google.firebase.firestore.DocumentId;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KoZnaZna {
    @DocumentId
    private String koZnaZnaId;
    private String question;
    private String correctAnswer;
    private List<String> otherAnswers;
}
