package com.godmother.itemsplanner.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.godmother.itemsplanner.R;
import com.godmother.itemsplanner.models.Category;
import com.godmother.itemsplanner.models.SearchedItem;
import com.godmother.itemsplanner.models.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView titleTextView;
    ImageButton burgerBtn;
    SearchView searchView;

    FirebaseDatabase database;
    DatabaseReference myRefToDatabase;

    ArrayList<Category> categoriesList = new ArrayList<Category>();
    ArrayAdapter<Category> adapter;
    ArrayList<SearchedItem> allItemsForSearch  = new ArrayList<>();

    JSONObject categories = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_nav, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        getDataForUser(user);

        searchView = (SearchView) findViewById(R.id.searchView);
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
                    categoriesList.clear();
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
                        getAllItems();
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
                            if(category.has("items"))
                                nextActivity.putExtra("ITEMS_LIST", category.get("items").toString());
                            nextActivity.putExtra("CATEGORY_ID", key);
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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                hideKeyboardFrom(getApplicationContext(), searchView);
                ArrayList<SearchedItem> searchResults = new ArrayList<>();
                for(SearchedItem item: allItemsForSearch) {
                    if(item.getName().toLowerCase().contains(query.toLowerCase()) || item.getDescriere().toLowerCase().contains(query.toLowerCase())) {
                        searchResults.add(item);
                    }
                }
                Intent nextActivity;
                nextActivity = new Intent(getBaseContext(), SearchResults.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("items", (Serializable) searchResults);

                nextActivity.putExtra("SEARCH_RESULTS", bundle);
                startActivity(nextActivity);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void getAllItems() {
        Iterator<String> iteratorCategories = categories.keys();
        while (iteratorCategories.hasNext()) {
            String keyCategory = iteratorCategories.next();
            try {
                JSONObject category = new JSONObject(categories.get(keyCategory).toString());
                JSONObject items = category.getJSONObject("items");
                Iterator<String> iterator = items.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    try {
                        JSONObject item = new JSONObject(items.get(key).toString());
                        SearchedItem itemForSearch = new SearchedItem(key, item.get("name").toString(),
                                                            item.get("descriere").toString(),
                                                            keyCategory, category.getString("name"));
                        allItemsForSearch.add(itemForSearch);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } catch (JSONException e) {
                // Something went wrong!
            }
        }

    }

    private void getDataForUser(final FirebaseUser firebaseUser){
        database = FirebaseDatabase.getInstance();
        myRefToDatabase = database.getReference("Users").child(firebaseUser.getUid());

        myRefToDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = null;
                    Gson gson = new Gson();
                    String gsonString = gson.toJson(dataSnapshot.getValue());
                    try {
                        JSONObject userJson = new JSONObject(gsonString);
                        if(userJson.has("blocked") && userJson.getString("blocked").equals("true")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Cont Blocat");
                            builder.setMessage("Contul tau a fost blocat de catre adminul aplicatie!");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth.getInstance().signOut();
                                    finishAffinity();
                                    return;

                                }
                            });
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    FirebaseAuth.getInstance().signOut();
                                    finishAffinity();
                                    return;
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                        user = new User(userJson.getString("name"), userJson.getString("phoneNumber"));
                        user.setIsAdmin(userJson.getString("isAdmin"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    SharedPreferences sharedPreferences = getSharedPreferences("FirebaseUser", MODE_PRIVATE);
                    SharedPreferences.Editor ed = sharedPreferences.edit();
                    ed.putString("id", firebaseUser.getUid());
                    ed.putString("name", user.getName());
                    ed.putString("email", firebaseUser.getEmail());
                    ed.putString("phoneNumber",  user.getPhoneNumber());
                    ed.putString("isAdmin",  user.getIsAdmin());
                    ed.commit();
                    setupToolbarAndDrawer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
                        break;
                    }

                    case R.id.nav_my_items_reservations: {
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), MyItemsReservations.class);
                        startActivity(nextActivity);
                        break;
                    }

                    case R.id.nav_admin_toate_rezervarile: {
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), AllBookingsAdminPanelActivity.class);
                        startActivity(nextActivity);
                        break;
                    }

                    case R.id.nav_admin_categorii_iteme: {
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), CategoriesAdminPanelActivity.class);
                        startActivity(nextActivity);
                        break;
                    }

                    case R.id.nav_admin_control_conturi: {
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), ControlConturiActivity.class);
                        startActivity(nextActivity);
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
        titleTextView.setText("Categorii");
    }
}
