
package com.apts.shoppingapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.apts.shoppingapp.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        // The shopping app does not need the template's floating button.
        binding.appBarMain.fab.setVisibility(View.GONE);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow
        )
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(
                this,
                R.id.nav_host_fragment_content_main
        );

        NavigationUI.setupActionBarWithNavController(
                this,
                navController,
                appBarConfiguration
        );

        NavigationUI.setupWithNavController(
                navigationView,
                navController
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(
                this,
                R.id.nav_host_fragment_content_main
        );

        return NavigationUI.navigateUp(
                navController,
                appBarConfiguration
        ) || super.onSupportNavigateUp();
    }
}