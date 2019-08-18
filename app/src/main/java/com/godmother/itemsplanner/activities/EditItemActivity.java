package com.godmother.itemsplanner.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.godmother.itemsplanner.models.ImageUpload;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageClickListener;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class EditItemActivity  extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView titleTextView;
    ImageButton burgerBtn;
    Button choosePhoto;
    Button newItem;
    EditText numeItem;
    EditText descriereItem;
    CarouselView carouselView;

    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseDatabase database;
    DatabaseReference myRefToDatabase;

    ArrayList<ImageUpload> imageUploads = new ArrayList<ImageUpload>();
    String categoryId;
    String itemId;

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

        carouselView = (CarouselView) findViewById(R.id.carouselViewAdminPanel);
        carouselView.setImageClickListener(imageClickListener);

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

        categoryId = getIntent().getStringExtra("CATEGORY_ID");
        itemId = getIntent().getStringExtra("ITEM_ID");

        myRefToDatabase = database.getReference("Categories").child(categoryId).child("items").child(itemId);
        myRefToDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Gson gson = new Gson();
                    String gsonString = gson.toJson(dataSnapshot.getValue());
                    try {
                        JSONObject item = new JSONObject(gsonString);
                        getImages(item);
                        try {
                            numeItem.setText(item.getString("name"));
                            descriereItem.setText(item.getString("descriere"));
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


    }

    private void getImages(JSONObject item){
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
                    imageUploads.add(new ImageUpload(null, BitmapFactory.decodeFile(localFile.getAbsolutePath())));
                    carouselView.setImageListener(imageListener);
                    carouselView.setPageCount(imageUploads.size());
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

    ImageClickListener imageClickListener = new ImageClickListener() {
        @Override
        public void onClick(final int position) {
            if(imageUploads.size() > 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditItemActivity.this);
                builder.setTitle("Stergere poza");
                builder.setMessage("Vrei sa stergi aceasta poza?");
                builder.setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imageUploads.remove(position);

                        carouselView.setImageListener(imageListener);
                        carouselView.setPageCount(imageUploads.size());
                    }
                });
                builder.setNegativeButton("Nu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    };

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
            Uri filePath;
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageUploads.add(new ImageUpload(filePath, bitmap));

                carouselView.setImageListener(imageListener);
                carouselView.setPageCount(imageUploads.size());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            if(imageUploads.get(position).getBitmap() != null)
                imageView.setImageBitmap(imageUploads.get(position).getBitmap());
        }
    };

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
            uploadImages();
        }
    }

    private void writeToDatabase(){
        String categoryId = (String) getIntent().getStringExtra("CATEGORY_ID");
        myRefToDatabase = database.getReference("Categories");
        myRefToDatabase = database.getReference("Categories");
        myRefToDatabase.child(categoryId).child("items").child(itemId).child("name").setValue(numeItem.getText().toString());
        myRefToDatabase.child(categoryId).child("items").child(itemId).child("descriere").setValue(descriereItem.getText().toString());
    }

    private void writeImageInfoToDatabase(Image image, String itemGeneratedId){
        String categoryId = getIntent().getStringExtra("CATEGORY_ID");
        myRefToDatabase = myRefToDatabase.push();
        String imageGeneratedId = myRefToDatabase.getKey();
        myRefToDatabase = database.getReference("Categories");
        myRefToDatabase.child(categoryId).child("items").child(itemGeneratedId).child("images").child(imageGeneratedId).setValue(image);
    }

    private void uploadImages() {
        writeToDatabase();
        boolean hasNewImages = false;
        for(ImageUpload imageUpload  : imageUploads) {
            if(imageUpload.getFilePath() != null)
            {
                hasNewImages = true;
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading...");
                progressDialog.show();
                final String uuid = UUID.randomUUID().toString();
                final String[] downloadUrl = {null};

                StorageReference ref = storageReference.child("images/" + uuid);
                try {

                    ref.putFile(imageUpload.getFilePath())
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            downloadUrl[0] = uri.toString();
                                            writeImageInfoToDatabase(new Image(uuid, downloadUrl[0]), itemId);
                                        }
                                    });
                                    progressDialog.dismiss();
                                    Toast.makeText(EditItemActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(EditItemActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        if(hasNewImages == false) {
            finish();
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

        titleTextView.setText("AdminPanel Editeaza Item ");
    }
}
