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

import com.godmother.itemsplanner.CustomAdapters.MyReservationsAdapter;
import com.godmother.itemsplanner.R;
import com.godmother.itemsplanner.models.Booking;
import com.godmother.itemsplanner.models.BookingWrapper;
import com.godmother.itemsplanner.models.Interval;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class MyItemsReservations extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView titleTextView;
    ImageButton burgerBtn;

    FirebaseDatabase database;
    DatabaseReference myRefToDatabase;

    ArrayList<BookingWrapper> bookingsList = new ArrayList<BookingWrapper>();
    MyReservationsAdapter reservationsAdapter;

    JSONObject bookings;
    JSONObject userBookigs;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nav, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView list = (ListView) findViewById(R.id.list);
        Context context = MyItemsReservations.this;
        reservationsAdapter = new MyReservationsAdapter(bookingsList, context);
        list.setAdapter(reservationsAdapter);

        bookings = getAllBookings();

        setupToolbarAndDrawer();
    }

    private JSONObject getAllBookings() {

        final JSONObject[] bookings = {null};
        database = FirebaseDatabase.getInstance();
        myRefToDatabase = database.getReference("Bookings");
        myRefToDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Gson gson = new Gson();
                    String gsonString = gson.toJson(dataSnapshot.getValue());
                    try {
                        bookings[0] = new JSONObject(gsonString);
                        userBookigs = getItemsReservations(bookings[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return bookings[0];
    }

    private JSONObject getItemsReservations(final JSONObject bookings) {
        String userId = getUserId();

        final JSONObject[] userBookings = {null};
        database = FirebaseDatabase.getInstance();
        myRefToDatabase = database.getReference("Users").child(userId).child("bookings");
        myRefToDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Gson gson = new Gson();
                    String gsonString = gson.toJson(dataSnapshot.getValue());
                    try {
                        userBookings[0] = new JSONObject(gsonString);
                        setUpListAdapter(userBookings[0], bookings);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return userBookings[0];
    }

    public void setUpListAdapter(JSONObject userBookigs, JSONObject bookings){
        Iterator<String> iterator = userBookigs.keys();
        bookingsList.clear();
        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                JSONObject bookingJSONObj = new JSONObject(bookings.get(key).toString());
                DateFormat dateFormat = new SimpleDateFormat("yyyy MM dd");
                try {
                    Date from = dateFormat.parse(bookingJSONObj.getJSONObject("interval").getString("from"));
                    Date till = dateFormat.parse(bookingJSONObj.getJSONObject("interval").getString("till"));
                    Interval interval = new Interval(from, till);
                    Booking booking = new Booking(bookingJSONObj.get("descriere").toString(),
                                                  bookingJSONObj.get("user").toString(),
                                                  bookingJSONObj.get("itemName").toString(),
                                                  bookingJSONObj.get("itemId").toString(),
                                                  bookingJSONObj.get("categoryId").toString(),
                                                  bookingJSONObj.get("categoryName").toString(),
                                                  bookingJSONObj.getInt("cantitate"));
                    booking.setBookingId(key);
                    BookingWrapper bookingWrapper = new BookingWrapper(booking, interval);
                    bookingsList.add(bookingWrapper);
                    //reservationsAdapter.notifyDataSetChanged();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } catch (JSONException e) {
                // Something went wrong!
            }
        }
        reservationsAdapter.notifyDataSetChanged();
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
                        Intent nextActivity;
                        nextActivity = new Intent(getBaseContext(), ControlConturiActivity.class);
                        startActivity(nextActivity);
                        finish();
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
        titleTextView.setText("Rezervarile mele");
    }

    public String getUserId(){
        SharedPreferences sharedPreferences = getSharedPreferences("FirebaseUser", MODE_PRIVATE);
        return sharedPreferences.getString("id", null);
    }
}