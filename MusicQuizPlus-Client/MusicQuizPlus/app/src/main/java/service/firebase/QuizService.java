package service.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import model.GeneratedQuiz;
import model.Quiz;

// SUMMARY
// Static methods for the quizzes

public class QuizService {

    public static Map<String, GeneratedQuiz> retrieveGeneratedQuizzes(DatabaseReference db, String topicId) {
        CountDownLatch done = new CountDownLatch(1);
        Map<String, GeneratedQuiz> data = new HashMap<>();
        DatabaseReference generatedQuizzesRef = db.child("generated_quizzes").child(topicId);
        generatedQuizzesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    data.put(ds.getKey(), (GeneratedQuiz) ds.getValue(GeneratedQuiz.class));
                }
                done.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        try {
            done.await();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        return data;
    }
}
