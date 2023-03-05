package service.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import model.GeneratedQuiz;
import model.Quiz;

// SUMMARY
// Static methods for the quizzes

public class QuizService {

    public static Map<String, GeneratedQuiz> retrieveGeneratedQuizzes(DatabaseReference db, String topicId) {
//        CountDownLatch done = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Map<String, GeneratedQuiz> data = new HashMap<>();
        DatabaseReference generatedQuizzesRef = db.child("generated_quizzes").child(topicId);
        final DataSnapshot[] snap = {null};

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                generatedQuizzesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        snap[0] = snapshot;

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });



        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Log.e("QuizService", "retrieveGeneratedQuizzes: Error retrieving generated quizzes." );
        }

        if (snap[0] != null) {
            for (DataSnapshot ds : snap[0].getChildren()) {
                data.put(ds.getKey(), ds.getValue(GeneratedQuiz.class));
            }
        }

        return data;
    }
}
