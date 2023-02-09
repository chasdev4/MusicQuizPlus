package service.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import model.Settings;
import model.User;
import model.ValidationObject;
import model.item.Playlist;
import model.type.Severity;
import utils.LogUtil;
import utils.ValidationUtil;

public class UserService {
    private final static String TAG = "UserService.java";

    // Create a new user on both databases
    public static void createUser(FirebaseUser firebaseUser, DatabaseReference db, Map<String, String> playlistIds) {
        LogUtil log = new LogUtil(TAG, "createUser");
        List<ValidationObject> validationObjects = new ArrayList<>() {
            {
                add(new ValidationObject(firebaseUser, FirebaseUser.class, Severity.HIGH));
                add(new ValidationObject(db, DatabaseReference.class, Severity.HIGH));
            }
        };
        if (ValidationUtil.nullCheck(validationObjects, log)) {
            return;
        }

        User user = new User(firebaseUser, new Settings(), playlistIds);
        db.child("users").child(firebaseUser.getUid()).setValue(user);
    }


}
