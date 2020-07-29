package com.example.easywedding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/*
Implementation of view pager adapter that uses a Fragment to manage each page.
This class also handles saving and restoring of fragment's state.
 */
public class ViewPagerFragmentAdapter extends FragmentStateAdapter {

    public ViewPagerFragmentAdapter(@NonNull FragmentManager fragmentManager
            , @NonNull Lifecycle lifecycle){
        super(fragmentManager, lifecycle);

    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case MainActivity.FEATURES_POSITION:
                return new FeaturesFragment();
            case MainActivity.GUESTS_POSITION:
                return new GuestsFragment();
            case MainActivity.CHAT_POSITION:
                return new ChatFragment();
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return MainActivity.FRAGMENTS_COUNT;
    }


}
