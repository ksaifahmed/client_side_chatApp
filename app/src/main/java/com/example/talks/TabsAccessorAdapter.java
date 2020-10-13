package com.example.talks;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i)
        {
            case 0:
                Chat chat = new Chat();
                return chat;

            case 1:
                MyClients myClients = new MyClients();
                return myClients;

            case 2:
                AllClients allClients = new AllClients();
                return allClients;

             default:
                return null;

        }

    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return "Chat";

            case 1:
                return "My Clients";

            case 2:
                return "All Clients";

            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
