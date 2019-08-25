package com.godmother.itemsplanner.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.godmother.itemsplanner.CustomAdapters.ControlConturiAdminPanelAdapter;
import com.godmother.itemsplanner.R;
import com.godmother.itemsplanner.models.UserEmail;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ControlConturiActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView titleTextView;
    ImageButton burgerBtn;
    FloatingActionButton addNewEmail;

    FirebaseDatabase database;
    DatabaseReference myRefToDatabase;

    ArrayList<UserEmail> emailList = new ArrayList<UserEmail>();
    ControlConturiAdminPanelAdapter controlConturiAdminPanelAdapter;

    JSONObject emails;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nav, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        final ListView list = (ListView) findViewById(R.id.list);
        Context context = ControlConturiActivity.this;
        controlConturiAdminPanelAdapter = new ControlConturiAdminPanelAdapter(emailList, context);
        list.setAdapter(controlConturiAdminPanelAdapter);

        getUsersEmails();
        setupToolbarAndDrawer();

        addNewEmail = findViewById(R.id.addBtn);
        addNewEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder((Context)ControlConturiActivity.this);
                builder.setTitle("Email Nou");
                final EditText input = new EditText((Context)ControlConturiActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newEmail = input.getText().toString();
                        writeNewCategory(newEmail);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

    }

    public void writeNewCategory(String newEmail){
        database = FirebaseDatabase.getInstance();
        myRefToDatabase = database.getReference("UsersEmail");
        myRefToDatabase = myRefToDatabase.push();
        myRefToDatabase.setValue(newEmail);
    }

    private void getUsersEmails() {
        database = FirebaseDatabase.getInstance();
        myRefToDatabase = database.getReference("UsersEmail");
        myRefToDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Gson gson = new Gson();
                    String gsonString = gson.toJson(dataSnapshot.getValue());
                    emailList.clear();
                    try {
                        emails = new JSONObject(gsonString);
                        Iterator<String> iterator = emails.keys();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            try {
                                String email = emails.get(key).toString();
                                emailList.add(new UserEmail(email, key));
                            } catch (JSONException e) {
                                // Something went wrong!
                            }
                        }
                        controlConturiAdminPanelAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setupToolbarAndDrawer(){
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.bringToFront();

        drawerLayout = findViewById(R.id.drawerlayout);

        View headerLayout = navigationView.getHeaderView(0);
        TextView userEditText = (TextView) headerLayout.findViewById(R.id.user);
        SharedPreferences sharedPreferences = getSharedPreferences("FirebaseUser", MODE_PRIVATE);
        String userName = sharedPreferences.getString("name", "");
        if (userName != null) {
            userEditText.setText("Hello, " + userName +"!");
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Closing drawer on item click
                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_home: {
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(nextActivity);
                        finishAffinity();
                        break;
                    }

                    case R.id.nav_my_items_reservations: {
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), MyItemsReservations.class);
                        startActivity(nextActivity);
                        finish();
                        break;
                    }

                    case R.id.nav_admin_toate_rezervarile: {
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), AllBookingsAdminPanelActivity.class);
                        startActivity(nextActivity);
                        finish();
                        break;
                    }

                    case R.id.nav_admin_categorii_iteme: {
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), CategoriesAdminPanelActivity.class);
                        startActivity(nextActivity);
                        finish();
                        break;
                    }

                    case R.id.nav_admin_control_conturi: {
                        break;
                    }

                    case R.id.nav_logout: {
                        FirebaseAuth.getInstance().signOut();
                        Intent nextActivity = new Intent(getBaseContext(), StartActivity.class);
                        nextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(nextActivity);
                        finishAffinity();
                    }
                    return true;
                }
                return false;
            }
        });

        Menu nav_Menu = navigationView.getMenu();
        if(sharedPreferences.getString("isAdmin", "").equals("false")){
            nav_Menu.findItem(R.id.submenu_admin_panels).setVisible(false);
        }

        burgerBtn = (ImageButton) findViewById(R.id.hamburger_btn);
        burgerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout .openDrawer(Gravity.LEFT);
            }
        });

        titleTextView = (TextView) findViewById(R.id.barTitle);
        titleTextView.setText("AdminPanel\nControl Conturi");
    }

}