package service.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import utils.LogUtil;

public class UserService {
    private final static String TAG = "UserService.java";

    // Create a new user on both databases
    public static void createUser(FirebaseUser firebaseUser, FirebaseFirestore firestore,
                                  DatabaseReference db) {
        LogUtil log = new LogUtil(TAG, "createUser");
        // Create a new user with a first and last name
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", firebaseUser.getDisplayName());
        userMap.put("email", firebaseUser.getEmail());
        userMap.put("photo_url", firebaseUser.getPhotoUrl());

        firestore.collection("users").document(firebaseUser.getUid())
                .set(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        log.d("DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        log.w("Error writing document", e);
                    }
                });

        userMap.clear();
        userMap = new HashMap<>();
        userMap.put("xp", 0);
        userMap.put("level", 1);

        db.child("users").child(firebaseUser.getUid()).setValue(userMap);
    }
    // Delete user from database
    public static boolean deleteUser(FirebaseUser firebaseUser, FirebaseFirestore firestore,
                                     DatabaseReference db) {
        LogUtil log = new LogUtil(TAG, "deleteUser");
        final boolean[] result = {true};

        db.child("users").child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    log.d("User data deleted from Realtime Database.");
                }
                else
                {
                    result[0] = false;
                }
            }
        });

        if (result[0] == true) {
            firestore.collection("users").document(firebaseUser.getUid())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            log.d("Firestore user data successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            log.w("Error deleting Firestore user", e);
                        }
                    });
        }
        return result[0];
    }
}
