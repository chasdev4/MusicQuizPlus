package service;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import model.PhotoUrl;

public class ItemService {
    public static String getSmallestPhotoUrl(List<PhotoUrl> photoUrl) {
        PhotoUrl temp = null;
        for (PhotoUrl p : photoUrl) {
            if (temp == null || temp.getWidth() > p.getWidth() && temp.getHeight() > p.getHeight()) {
                temp = p;
            }
        }
        return temp == null || temp.getUrl().isEmpty() || temp.getUrl() == null ? null : temp.getUrl();
    }

    public static String formatAlbumSubtitle(String year) {
        return String.format("Album • %s", year);
    }
}
