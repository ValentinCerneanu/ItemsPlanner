package com.godmother.itemsplanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.godmother.itemsplanner.R;
import com.godmother.itemsplanner.models.Item;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
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
        setContentView(R.layout.activity_search_result);
        setupToolbarAndDrawer();

        adapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, itemsList);
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
                        itemsList.add(new Item(key, item.get("name").toString(),
                                                item.get("descriere").toString(),
                                                item.getInt("cantitate")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapter.sort(new Comparator<Item>() {
                    public int compare(Item arg0, Item arg1) {
                        return arg0.getName().compareTo(arg1.getName());
                    }
                });
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
                            nextActivity.putExtra("CATEGORY_NAME", getIntent().getStringExtra("CATEGORY_NAME"));
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
                        finishAffinity();
                        break;
                    }

                    case R.id.nav_admin_toate_rezervarile: {
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), AllBookingsAdminPanelActivity.class);
                        startActivity(nextActivity);
                        finishAffinity();
                        break;
                    }

                    case R.id.nav_admin_categorii_iteme: {
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), CategoriesAdminPanelActivity.class);
                        startActivity(nextActivity);
                        finishAffinity();
                        break;
                    }

                    case R.id.nav_admin_control_conturi: {
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), ControlConturiActivity.class);
                        startActivity(nextActivity);
                        finishAffinity();
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

        String categorie = (String) getIntent().getStringExtra("CATEGORY_NAME");
        titleTextView.setText("Iteme din categoria " + categorie);
    }
}
