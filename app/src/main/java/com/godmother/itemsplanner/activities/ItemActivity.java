package com.godmother.itemsplanner.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.drawerlayout.widget.DrawerLayout;

import com.godmother.itemsplanner.R;
import com.godmother.itemsplanner.Utils.SendGetRequest;
import com.godmother.itemsplanner.models.Booking;
import com.godmother.itemsplanner.models.BookingWrapper;
import com.godmother.itemsplanner.models.Interval;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.timessquare.CalendarPickerView;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    TextView itemDescriptionTextView;
    CarouselView carouselView;

    ArrayList<Date> intervalSelectat = new ArrayList<>();
    ArrayList<String> bookingsDetails = new ArrayList<>();
    ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();

    FirebaseDatabase database;
    DatabaseReference myRefToDatabase;
    FirebaseStorage storage;

    JSONObject item = null;
    JSONObject bookings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        setupToolbarAndDrawer();
        itemDescriptionTextView = (TextView)findViewById(R.id.itemDescription);

        carouselView = (CarouselView) findViewById(R.id.carouselView);

        String categoryId = getIntent().getStringExtra("CATEGORY_ID");
        final String itemId = getIntent().getStringExtra("ITEM_ID");
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        myRefToDatabase = database.getReference("Categories").child(categoryId).child("items").child(itemId);
        myRefToDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Gson gson = new Gson();
                    String gsonString = gson.toJson(dataSnapshot.getValue());
                    try {
                        item = new JSONObject(gsonString);
                        if(bitmaps.isEmpty())
                            getImages();
                        getAllBookingsDetails();
                        try {
                            itemDescriptionTextView.setText("Descriere: " + item.getString("descriere"));
                        } catch (JSONException e) {
                            e.printStackTrace();
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

        setupCalendar();

        scopRezervare = findViewById(R.id.scopRezervare);
        scopRezervare.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_NEXT || id == EditorInfo.IME_NULL) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

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
                } else {
                    intervalSelectat = new ArrayList<Date>(calendar.getSelectedDates());
                    String conflictBooking = checkAvailability();
                    if(conflictBooking == null){
                        Date from = intervalSelectat.get(0);
                        Date till = intervalSelectat.get(intervalSelectat.size() - 1);
                        Booking booking = new Booking(scopRezervare.getText().toString(), getUserId(),
                                (String) getIntent().getStringExtra("ITEM_NAME"), itemId,
                                getIntent().getStringExtra("CATEGORY_ID"), getIntent().getStringExtra("CATEGORY_NAME"));
                        Interval interval = new Interval(from, till);
                        writeNewBooking(booking, interval);
                    } else {
                        try {
                            final JSONObject detailsConflictBooking = new JSONObject(conflictBooking);

                            database = FirebaseDatabase.getInstance();
                            myRefToDatabase = database.getReference("Users").child(detailsConflictBooking.get("user").toString());
                            myRefToDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Gson gson = new Gson();
                                        String gsonString = gson.toJson(dataSnapshot.getValue());
                                        try {
                                            JSONObject user = new JSONObject(gsonString);
                                            try {
                                                AlertDialog.Builder builder = new AlertDialog.Builder((Context)ItemActivity.this);
                                                builder.setTitle("Suprapunere rezervari");
                                                builder.setMessage("Itemul este deja rezervat pentru intervalul dorit cu descrierea " + detailsConflictBooking.get("descriere")
                                                                    + " de catre colegul " + user.get("name")
                                                                    + " cu numarul de telefon " + user.get("phoneNumber")
                                                                    + "! \nRezervarea a esuat! "   );
                                                builder.setPositiveButton("OK", null);
                                                AlertDialog dialog = builder.create();
                                                dialog.show();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            getAllBookingsDetails();
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
                }
            }
        });
    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            if(bitmaps.get(position) != null)
                imageView.setImageBitmap(bitmaps.get(position));
        }
    };

    private void getImages(){
        String images;
        if(item.has("images")) {
            try {
                images = item.getString("images");
                JSONObject imagesJson = new JSONObject(images);
                Iterator<String> iterator = imagesJson.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    try {
                        JSONObject image = new JSONObject(imagesJson.get(key).toString());
                        String url = image.get("url").toString();
                        String uid = image.get("uid").toString();
                        getImage(uid);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.no_uploaded));
            carouselView.setImageListener(imageListener);
            carouselView.setPageCount(bitmaps.size());
        }
    }

    private void getImage(String uid) {
        final File localFile;
        try {
            StorageReference storageReference = storage.getReference();
            storageReference = storageReference.child("images/" + uid);
            localFile = File.createTempFile("image" + uid, "jpg");
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    bitmaps.add(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                    carouselView.setImageListener(imageListener);
                    carouselView.setPageCount(bitmaps.size());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeNewBooking(final Booking booking, final Interval interval){
        String categoryId = getIntent().getStringExtra("CATEGORY_ID");
        String itemId = getIntent().getStringExtra("ITEM_ID");
        database = FirebaseDatabase.getInstance();
        myRefToDatabase = database.getReference("Bookings");
        myRefToDatabase = myRefToDatabase.push();
        String generatedId = myRefToDatabase.getKey();
        myRefToDatabase = database.getReference("Bookings");
        myRefToDatabase.child(generatedId).setValue(booking)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        AlertDialog.Builder builder = new AlertDialog.Builder((Context)ItemActivity.this);
                        builder.setTitle("Rezervare efectuata");
                        builder.setMessage("Itemul a fost rezervat cu succes!\n"
                                            + "Puteti vedea toate rezervarile in meniul \"Rezervarile mele\"");
                        builder.setPositiveButton("OK", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();

                        sendEmail(booking, interval, scopRezervare.getText().toString());

                        Date today = new Date();
                        calendar.selectDate(today);
                        scopRezervare.setText("");
                        scopRezervare.clearFocus();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

        myRefToDatabase.child(generatedId).child("interval").setValue(interval)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

        myRefToDatabase = database.getReference("Categories");
        myRefToDatabase.child(categoryId).child("items").child(itemId).child("bookings").child(generatedId).setValue(generatedId);

        myRefToDatabase = database.getReference("Users");
        myRefToDatabase.child(getUserId()).child("bookings").child(generatedId).setValue(generatedId);
    }

    public String checkAvailability() {
        for(String bookingDetails: bookingsDetails){
            try {
                JSONObject details = new JSONObject(bookingDetails);
                JSONObject interval = details.getJSONObject("interval");
                DateFormat dateFormat = new SimpleDateFormat("yyyy MM dd");
                Date from = dateFormat.parse(interval.getString("from"));
                Date till = dateFormat.parse(interval.getString("till"));
                Date intervalSelectatFrom = intervalSelectat.get(0);
                Date intervalSelectatTill = intervalSelectat.get(intervalSelectat.size() - 1);

                if(!(till.before(intervalSelectatFrom) || intervalSelectatTill.before(from)))
                    return bookingDetails;

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void getAllBookingsDetails(){
        if(item.has("bookings")) {
            try {
                final JSONObject itemBookings = item.getJSONObject("bookings");

                database = FirebaseDatabase.getInstance();
                myRefToDatabase = database.getReference("Bookings");
                myRefToDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Gson gson = new Gson();
                            String gsonString = gson.toJson(dataSnapshot.getValue());

                            try {
                                bookings = new JSONObject(gsonString);
                                Iterator<String> iterator = itemBookings.keys();
                                while (iterator.hasNext()) {
                                    String key = iterator.next();
                                    bookingsDetails.add(bookings.getJSONObject(key).toString());
                                }
                                setHighLightedDates();

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
    }

    private void setupCalendar() {
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        Date today = new Date();
        calendar.init(today, nextYear.getTime())
                .withSelectedDate(today);
        calendar.init(today, nextYear.getTime()).inMode(RANGE);

        calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                Context context = getApplicationContext();
                hideKeyboardFrom(context, calendar);
            }

            @Override
            public void onDateUnselected(Date date) {
                Context context = getApplicationContext();
                hideKeyboardFrom(context, calendar);
            }
        });
        calendar.setOnInvalidDateSelectedListener(new CalendarPickerView.OnInvalidDateSelectedListener() {
            @Override
            public void onInvalidDateSelected(Date date) {
                Context context = getApplicationContext();
                hideKeyboardFrom(context, calendar);
            }
        });

        ScrollView parentScrollView = findViewById(R.id.parentScroll);
        parentScrollView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                findViewById(R.id.childScroll).getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
        calendar.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event)
            {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    private void setHighLightedDates() {
        for(String bookingDetails: bookingsDetails){
            try {
                JSONObject bookingDetailsObject = new JSONObject(bookingDetails);
                JSONObject interval = bookingDetailsObject.getJSONObject("interval");

                DateFormat dateFormat = new SimpleDateFormat("yyyy MM dd");
                Date from = dateFormat.parse(interval.getString("from"));
                Date till = dateFormat.parse(interval.getString("till"));

                ArrayList<Date> highlightedDates = new ArrayList<>();
                highlightedDates.add(from);
                while(from.before(till)) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(from);
                    c.add(Calendar.DATE, 1);
                    from = c.getTime();
                    highlightedDates.add(from);
                }
                highlightedDates.add(till);

                calendar.highlightDates(highlightedDates);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void sendEmail(Booking booking, Interval interval, String scopRezervare) {
        SharedPreferences sharedPreferences = getSharedPreferences("FirebaseUser", MODE_PRIVATE);

        String userName = sharedPreferences.getString("name", "");
        String phoneNumber = sharedPreferences.getString("phoneNumber", "");
        String email = sharedPreferences.getString("email", "");

        booking.setUserName(userName);
        BookingWrapper bookingWrapper = new BookingWrapper(booking, interval);
        bookingWrapper.setPhoneNumber(phoneNumber);

        String url = "https://us-central1-items-planner.cloudfunctions.net/sendMail?dest=" + email
                                                                                    + "&mesaj=" + bookingWrapper.toEmail();
        url = url.replaceAll(" ", "%20");

        SendGetRequest emailSender = null;
        try {
            emailSender = new SendGetRequest(new URL(url));
            emailSender.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                drawerLayout .openDrawer(Gravity.LEFT);
            }
        });

        titleTextView = (TextView) findViewById(R.id.barTitle);
        String itemName = (String) getIntent().getStringExtra("ITEM_NAME");
        titleTextView.setText(itemName);
    }

    public String getUserId(){
        SharedPreferences sharedPreferences = getSharedPreferences("FirebaseUser", MODE_PRIVATE);
        return sharedPreferences.getString("id", null);
    }

}
