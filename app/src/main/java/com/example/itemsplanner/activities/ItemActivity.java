package com.example.itemsplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.itemsplanner.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.timessquare.CalendarPickerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import static com.squareup.timessquare.CalendarPickerView.SelectionMode.RANGE;

public class ItemActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView titleTextView;
    ImageButton burgerBtn;
    CalendarPickerView calendar;
    AppCompatButton butonRezerva;
    EditText scopRezervare;

    ArrayList<Date> intervalSelectat = new ArrayList<>();
    ArrayList<String> bookingsDetails = new ArrayList<>();

    FirebaseDatabase database;
    DatabaseReference myRefToDatabase;

    JSONObject item = null;
    JSONObject bookings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        setupToolbarAndDrawer();


        String itemString = (String) getIntent().getSerializableExtra("ITEM");
        if(itemString != null) {
            try {
                item = new JSONObject(itemString);
            } catch (Exception ex) {

            }
        }

        TextView itemDescriptionTextView = (TextView)findViewById(R.id.itemDescription);
        try {
            itemDescriptionTextView.setText("Descriere: " + item.getString("descriere"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setupCalendar();
        getAllBookingsDetails();

        scopRezervare = findViewById(R.id.scopRezervare);

        butonRezerva = findViewById(R.id.btn_rezerva);
        butonRezerva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean cancel = false;
                View focusView = null;
                intervalSelectat = new ArrayList<Date>(calendar.getSelectedDates());
                if (TextUtils.isEmpty(scopRezervare.getText().toString())) {
                    scopRezervare.setError(getString(R.string.error_field_required));
                    if(focusView == null )
                        focusView = scopRezervare;
                    cancel = true;
                }
                if(intervalSelectat.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Selecteaza un interval din calendar",Toast.LENGTH_SHORT).show();
                    focusView = calendar;
                    cancel = true;
                }
                if(cancel) {
                    focusView.requestFocus();
                } else{
                    intervalSelectat = new ArrayList<Date>(calendar.getSelectedDates());
                    checkAvailability();
                }
            }
        });
    }

    public boolean checkAvailability(){
        for(String bookingDetails: bookingsDetails){
            try {
                JSONObject details = new JSONObject(bookingDetails);
                JSONObject interval = details.getJSONObject("interval");
                DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
                Date from = dateFormat.parse(interval.getString("from"));
                Date till = dateFormat.parse(interval.getString("till"));
                if(intervalSelectat.size() == 1){
                    if(till.after(intervalSelectat.get(0))){
                        return false;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void getAllBookingsDetails(){
        try {
            final JSONObject itemBookings = item.getJSONObject("bookings");

            database = FirebaseDatabase.getInstance();
            myRefToDatabase = database.getReference("Bookings");
            myRefToDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        Gson gson = new Gson();
                        String gsonString = gson.toJson(dataSnapshot.getValue());

                        try {
                            bookings = new JSONObject(gsonString);
                            Iterator<String> iterator = itemBookings.keys();
                            while(iterator.hasNext()){
                                String key = iterator.next();
                                bookingsDetails.add(bookings.getJSONObject(key).toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupCalendar(){
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        Date today = new Date();
        calendar.init(today, nextYear.getTime())
                .withSelectedDate(today);
        calendar.init(today, nextYear.getTime()).inMode(RANGE);
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
        String itemName = (String) getIntent().getStringExtra("ITEM_NAME");
        titleTextView.setText(itemName);
    }
}
