package com.example.musicquizplus;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.musicquizplus.fragments.ArtistsFragment;
import com.example.musicquizplus.fragments.HistoryFragment;
import com.example.musicquizplus.fragments.PlaylistFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PlaylistFragment();
            case 1:
                return new ArtistsFragment();
            case 2:
                return new HistoryFragment();
            default:
                return new PlaylistFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
