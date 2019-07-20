package com.example.itemsplanner.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;
import com.example.itemsplanner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class StartActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        new CountDownTimer(2000,1000){
            @Override
            public void onTick(long millisUntilFinished){}

            @Override
            public void onFinish(){
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                Intent nextActivity;
                if (user != null) {
                    // User is signed in
                    saveDateToSharedPreferences(user);
                    nextActivity = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(nextActivity);
                } else {
                    // No user is signed in
                    nextActivity = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(nextActivity);
                }
                finish();
            }
        }.start();
    }

    private void saveDateToSharedPreferences(FirebaseUser user){
        SharedPreferences sharedPreferences = getSharedPreferences("FirebaseUser", MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString("id", user.getUid());
        ed.putString("name", user.getDisplayName());
        ed.putString("email", user.getEmail());
        ed.putString("phoneNumber",  user.getPhoneNumber());
        ed.commit();
    }
}
