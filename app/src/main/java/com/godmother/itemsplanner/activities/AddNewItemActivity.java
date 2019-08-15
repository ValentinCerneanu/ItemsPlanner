package com.godmother.itemsplanner.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.godmother.itemsplanner.R;
import com.godmother.itemsplanner.models.Image;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class AddNewItemActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView titleTextView;
    ImageButton burgerBtn;
    Button choosePhoto;
    private Uri filePath;
    Button newItem;
    ImageView imageView;
    EditText numeItem;
    EditText descriereItem;

    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseDatabase database;
    DatabaseReference myRefToDatabase;

    private final int PICK_IMAGE_REQUEST = 71;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nav, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_item);
        setupToolbarAndDrawer();

        numeItem = findViewById(R.id.item_name);
        descriereItem = findViewById(R.id.descriere_item);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //
        imageView = (ImageView) findViewById(R.id.imgView);

        newItem = findViewById(R.id.add_new_item);
        newItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewItem();
            }
        });

        choosePhoto = findViewById(R.id.upload_image);
        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void addNewItem(){
        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(numeItem.getText().toString())) {
            numeItem.setError(getString(R.string.error_field_required));
            if(focusView == null )
                focusView = numeItem;
            cancel = true;
        }
        if (TextUtils.isEmpty(descriereItem.getText().toString())) {
            descriereItem.setError(getString(R.string.error_field_required));
            if(focusView == null )
                focusView = descriereItem;
            cancel = true;
        }
        if(cancel) {
            focusView.requestFocus();
        } else {
            uploadImage();
        }
    }

    private void writeToDatabase(Image image){
        String categorie = (String) getIntent().getStringExtra("CATEGORY_NAME");
        myRefToDatabase = database.getReference("Categories");
        myRefToDatabase = myRefToDatabase.push();
        String itemGeneratedId = myRefToDatabase.getKey();
        myRefToDatabase = database.getReference("Categories");
        myRefToDatabase.child(categorie).child("items").child(itemGeneratedId).child("name").setValue(numeItem.getText().toString());
        myRefToDatabase.child(categorie).child("items").child(itemGeneratedId).child("descriere").setValue(descriereItem.getText().toString());
        myRefToDatabase = myRefToDatabase.push();
        String imageGeneratedId = myRefToDatabase.getKey();
        myRefToDatabase = database.getReference("Categories");
        myRefToDatabase.child(categorie).child("items").child(itemGeneratedId).child("images").child(imageGeneratedId).setValue(image);
    }

    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            final String uuid = UUID.randomUUID().toString();
            final String[] downloadUrl = {null};

            StorageReference ref = storageReference.child("images/" + uuid);
            try {

                ref.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloadUrl[0] = uri.toString();
                                        writeToDatabase(new Image(uuid, downloadUrl[0]));
                                    }
                                });
                                progressDialog.dismiss();
                                Toast.makeText(AddNewItemActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(AddNewItemActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                        .getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            }
                        });
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
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
                if (menuItem.isChecked())
                    menuItem.setChecked(false);
                else
                    menuItem.setChecked(true);
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

        titleTextView.setText("AdminPanel Adauga Item ");
    }
}
