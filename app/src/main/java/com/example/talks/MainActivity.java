package com.example.talks;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;


public class MainActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter mytabsAccessorAdapter;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference userref;
    private String UID;
    private boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userref = FirebaseDatabase.getInstance().getReference().child("Users");



        Initialize();




    }

    private void InitializeClient()
    {


        mtoolbar = (Toolbar) findViewById(R.id.main_page_toolbar2);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));



    }


    private void Initialize()
    {
        mtoolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Oroddho");

        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        mytabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(mytabsAccessorAdapter);

        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }



    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser == null)
        {
            mAuth.signOut();
            sendUserToLoginActivity();
        }




    }

    private void sendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_option)
        {
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        if(item.getItemId() == R.id.unreg_clients_options)
        {
            sendUserToPendingClientsActivity();
        }
        if(item.getItemId() == R.id.main_settings_option)
        {
            GoToSettings();
        }
        return true;
    }

    private void GoToSettings()
    {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void sendUserToPendingClientsActivity()
    {
        Intent pendingClientInt = new Intent(MainActivity.this, UnregClientsActivity.class);
        startActivity(pendingClientInt);
    }


    @Override
    public void onBackPressed() {
        if(exit)
        {
            super.onBackPressed();
            return;
        }
        exit = true;
        Toast.makeText(this, "Press Again to Exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                exit = false;
            }
        }, 2000);
    }



}












