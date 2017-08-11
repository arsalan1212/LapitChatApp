package com.example.arsalankhan.lapitchatapp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.arsalankhan.lapitchatapp.fragments.ChatFragment;
import com.example.arsalankhan.lapitchatapp.fragments.FriendsFragment;
import com.example.arsalankhan.lapitchatapp.fragments.RequestFragment;

/**
 * Created by Arsalan khan on 7/29/2017.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                RequestFragment requestFragment=new RequestFragment();
                return requestFragment;

            case 1:
                ChatFragment chatFragment=new ChatFragment();
                return chatFragment;


            case 2:
                FriendsFragment friendsFragment=new FriendsFragment();
                return friendsFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){

            case 0:
                return "REQUEST";

            case 1:
                return "CHAT";

            case 2:
                return "FRIENDS";

            default:
                return null;
        }
    }
}
