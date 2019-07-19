package com.example.itemsplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.itemsplanner.R;
import com.example.itemsplanner.models.Category;
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

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView titleTextView;
    ImageButton burgerBtn;

    FirebaseDatabase database;
    DatabaseReference myRefToDatabase;

    ArrayList<Category> categoriesList = new ArrayList<Category>();
    ArrayAdapter<Category> adapter;

    JSONObject categories = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_nav, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbarAndDrawer();

        adapter = new ArrayAdapter<Category>(this, android.R.layout.simple_list_item_1, categoriesList);
        final ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);

        database = FirebaseDatabase.getInstance();
        myRefToDatabase = database.getReference("Categories");
        myRefToDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Gson gson = new Gson();
                    String gsonString = gson.toJson(dataSnapshot.getValue());

                    try {
                        categories = new JSONObject(gsonString);
                        Iterator<String> iterator = categories.keys();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            try {
                                JSONObject category = new JSONObject(categories.get(key).toString());
                                categoriesList.add(new Category(key, category.get("name").toString()));
                            } catch (JSONException e) {
                                // Something went wrong!
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent nextActivity;
                nextActivity = new Intent(getBaseContext(), ItemsActivity.class);

                Category selectedCategory = (Category) arg0.getItemAtPosition(position);
                Iterator<String> iterator = categories.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    if(key.equals(selectedCategory.getId())){
                        try {
                            JSONObject category = new JSONObject(categories.get(key).toString());
                            nextActivity.putExtra("ITEMS_LIST", category.get("items").toString());
                            break;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                nextActivity.putExtra("CATEGORY_NAME", selectedCategory.getName());
                startActivity(nextActivity);
            }
        });
    }

    private void setupToolbarAndDrawer(){
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.bringToFront();

        drawerLayout = findViewById(R.id.drawerlayout);

        View headerLayout = navigationView.getHeaderView(0);
        TextView userEditText = (TextView) headerLayout.findViewById(R.id.user);
        String userName = "";
        if (userName != null) {
            userEditText.setText("Hello, " + userName +"!");
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);
                //Closing drawer on item click
                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {

                    case R.id.nav_logout: {
                        FirebaseAuth.getInstance().signOut();
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), StartActivity.class);
                        startActivity(nextActivity);
                        finish();
                    }
                    return true;
                }
                return false;
            }
        });

        burgerBtn = (ImageButton) findViewById(R.id.hamburger_btn);
        burgerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout .openDrawer(Gravity.LEFT);
            }
        });

        titleTextView = (TextView) findViewById(R.id.barTitle);
        titleTextView.setText("Categorii");
    }
}
