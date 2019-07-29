package com.example.itemsplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.itemsplanner.R;
import com.example.itemsplanner.models.Item;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ItemsActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView titleTextView;
    ImageButton burgerBtn;

    ArrayList<Item> itemsList = new ArrayList<Item>();
    ArrayAdapter<Item> adapter;
    JSONObject items = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbarAndDrawer();

        adapter=new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, itemsList);
        final ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);

        String itemsString = (String) getIntent().getSerializableExtra("ITEMS_LIST");
        if(itemsString != null) {
            try {
                items = new JSONObject(itemsString);
                Iterator<String> iterator = items.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    try {
                        JSONObject item = new JSONObject(items.get(key).toString());
                        itemsList.add(new Item(key, item.get("name").toString(), item.get("image_url").toString(),
                                item.get("descriere").toString()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent nextActivity;
                nextActivity = new Intent(getBaseContext(), ItemActivity.class);

                Item selectedItem = (Item) arg0.getItemAtPosition(position);
                Iterator<String> iterator = items.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    if(key.equals(selectedItem.getId())){
                        try {
                            JSONObject item = new JSONObject(items.get(key).toString());
                            nextActivity.putExtra("ITEM", item.toString());
                            nextActivity.putExtra("CATEGORY_ID", getIntent().getStringExtra("CATEGORY_ID"));
                            nextActivity.putExtra("ITEM_ID", key);
                            break;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                nextActivity.putExtra("ITEM_NAME", selectedItem.getName());
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
        SharedPreferences sharedPreferences = getSharedPreferences("FirebaseUser", MODE_PRIVATE);
        String userName = sharedPreferences.getString("name", "");
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
                    case R.id.nav_my_items_reservations: {
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), MyItemsReservations.class);
                        startActivity(nextActivity);
                    }

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
        String categorie = (String) getIntent().getStringExtra("CATEGORY_NAME");
        titleTextView.setText("Iteme din categoria " + categorie);
    }
}
