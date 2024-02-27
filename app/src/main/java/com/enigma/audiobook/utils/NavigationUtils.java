package com.enigma.audiobook.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.enigma.audiobook.R;
import com.enigma.audiobook.activities.DarshanActivity;
import com.enigma.audiobook.activities.MenuActivity;
import com.enigma.audiobook.activities.MyFeedActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class NavigationUtils {

    public static BottomNavigationView setupNavigationDrawer(Activity activity,
                                             int navigationDrawerId,
                                             int currentSelectedItem) {
        BottomNavigationView bottomNavigationView = activity.findViewById(navigationDrawerId);

        bottomNavigationView.setSelectedItemId(currentSelectedItem);

        bottomNavigationView.setOnItemSelectedListener(
                new NavigationBarView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.menuItemDarshans) {
                            activity.startActivity(new Intent(activity, DarshanActivity.class));
                            activity.overridePendingTransition(0, 0);
                            return true;
                        } else if (item.getItemId() == R.id.menuItemMyFeed) {
                            activity.startActivity(new Intent(activity, MyFeedActivity.class));
                            activity.overridePendingTransition(0, 0);
                            return true;
                        } else if (item.getItemId() == R.id.menuItemMenu) {
                            activity.startActivity(new Intent(activity, MenuActivity.class));
                            activity.overridePendingTransition(0, 0);
                            return true;
                        }
                        return false;
                    }
                }
        );
        return bottomNavigationView;
    }

    public static void setMenuItemChecked(BottomNavigationView bottomNavigationView,
                                          int selectedMenuItemId) {
        Menu menu = bottomNavigationView.getMenu();
        MenuItem item = menu.findItem(selectedMenuItemId);
        item.setChecked(true);
    }
}
