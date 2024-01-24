package com.godmother.itemsplanner.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.godmother.itemsplanner.CustomAdapters.MyItemsAdminPanelAdapter;
import com.godmother.itemsplanner.R;
import com.godmother.itemsplanner.models.Item;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class ItemsAdminPanelActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView titleTextView;
    ImageButton burgerBtn;
    FloatingActionButton addNewItem;

    ArrayList<Item> itemsList = new ArrayList<Item>();
    MyItemsAdminPanelAdapter itemsAdminPanelAdapter;

    JSONObject items = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nav, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        setupToolbarAndDrawer();

        String categoryId = (String) getIntent().getStringExtra("CATEGORY_ID");
        getItems(categoryId);

        final ListView list = (ListView) findViewById(R.id.list);
        Context context = ItemsAdminPanelActivity.this;
        itemsAdminPanelAdapter = new MyItemsAdminPanelAdapter(itemsList, categoryId, context);
        list.setAdapter(itemsAdminPanelAdapter);

        addNewItem = findViewById(R.id.addBtn);
        addNewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nextActivity;
                nextActivity = new Intent(getBaseContext(), AddNewItemActivity.class);
                String categoryId = (String) getIntent().getStringExtra("CATEGORY_ID");
                nextActivity.putExtra("CATEGORY_ID", categoryId);
                startActivity(nextActivity);
            }
        });

    }

    private void getItems(String categoryId) {
        FirebaseDatabase database;
        DatabaseReference myRefToDatabase;
        database = FirebaseDatabase.getInstance();
        myRefToDatabase = database.getReference("Categories").child(categoryId).child("items");
        myRefToDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Gson gson = new Gson();
                    String gsonString = gson.toJson(dataSnapshot.getValue());
                    itemsList.clear();
                    try {
                        items = new JSONObject(gsonString);
                        Iterator<String> iterator = items.keys();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            try {
                                JSONObject item = new JSONObject(items.get(key).toString());
                                itemsList.add(new Item(key, item.get("name").toString(),
                                        item.get("descriere").toString(),
                                        item.getInt("cantitate")));
                            } catch (JSONException e) {
                                // Something went wrong!
                            }
                        }
                        Collections.sort(itemsList, new Comparator<Item>() {
                            @Override
                            public int compare(Item arg0, Item arg1) {
                                return arg0.getName().compareTo(arg1.getName());
                            }
                        });

                        itemsAdminPanelAdapter.notifyDataSetChanged();
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
                int id = menuItem.getItemId();
                if(id ==  R.id.nav_home) {
                    Intent nextActivity;
                    nextActivity = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(nextActivity);
                    finishAffinity();
                }

                if(id == R.id.nav_my_items_reservations) {
                    Intent nextActivity;
                    nextActivity = new Intent(getBaseContext(), MyItemsReservations.class);
                    startActivity(nextActivity);
                    finishAffinity();
                }

                if(id == R.id.nav_admin_toate_rezervarile) {
                    Intent nextActivity;
                    nextActivity = new Intent(getBaseContext(), AllBookingsAdminPanelActivity.class);
                    startActivity(nextActivity);
                    finishAffinity();
                }

                if(id == R.id.nav_admin_categorii_iteme) {
                    Intent nextActivity;
                    nextActivity = new Intent(getBaseContext(), CategoriesAdminPanelActivity.class);
                    startActivity(nextActivity);
                    finishAffinity();
                }

                if(id ==  R.id.nav_admin_control_conturi) {
                    Intent nextActivity;
                    nextActivity = new Intent(getBaseContext(), ControlConturiActivity.class);
                    startActivity(nextActivity);
                    finishAffinity();
                }

                if(id == R.id.nav_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Intent nextActivity = new Intent(getBaseContext(), StartActivity.class);
                    nextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(nextActivity);
                    finishAffinity();
                }
                return true;
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

        String categorie = (String) getIntent().getStringExtra("CATEGORY_NAME");
        titleTextView.setText("AdminPanel \nIteme " + categorie);
    }
}
