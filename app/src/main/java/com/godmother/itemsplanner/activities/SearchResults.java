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
import com.godmother.itemsplanner.models.SearchedItem;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchResults extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView titleTextView;
    ImageButton burgerBtn;

    ArrayList<SearchedItem> itemsList = new ArrayList<SearchedItem>();
    ArrayAdapter<SearchedItem> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        setupToolbarAndDrawer();

        final ListView list = (ListView) findViewById(R.id.list);
        Bundle itemsString = (Bundle) getIntent().getBundleExtra("SEARCH_RESULTS");
        itemsList = (ArrayList<SearchedItem>) itemsString.getSerializable("items");
        adapter = new ArrayAdapter<SearchedItem>(this, android.R.layout.simple_list_item_1, itemsList);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent nextActivity;
                nextActivity = new Intent(getBaseContext(), ItemActivity.class);

                SearchedItem selectedItem = (SearchedItem) arg0.getItemAtPosition(position);

                Gson gson = new Gson();
                String json = gson.toJson(selectedItem);
                try{
                    JSONObject item = new JSONObject(json);
                    nextActivity.putExtra("ITEM", item.toString());
                    nextActivity.putExtra("CATEGORY_ID", selectedItem.getCategoryId());
                    nextActivity.putExtra("ITEM_ID", selectedItem.getId());
                    nextActivity.putExtra("CATEGORY_NAME", selectedItem.getCategoryName());
                    nextActivity.putExtra("ITEM_NAME", selectedItem.getName());
                    startActivity(nextActivity);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

        titleTextView.setText("Resultatele cautarii ");
    }
}
