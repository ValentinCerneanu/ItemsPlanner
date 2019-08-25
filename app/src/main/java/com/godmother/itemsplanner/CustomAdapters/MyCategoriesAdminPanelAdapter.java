package com.godmother.itemsplanner.CustomAdapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.godmother.itemsplanner.R;
import com.godmother.itemsplanner.models.Category;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class MyCategoriesAdminPanelAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<Category> list = new ArrayList<Category>();
    private Context context;
    FirebaseDatabase database;
    String categorie;

    public MyCategoriesAdminPanelAdapter(ArrayList<Category> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        //return list.get(pos).getId();
        return 0;
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.admin_panel_adapter_layout, null);
        }

        TextView bookingView= (TextView)view.findViewById(R.id.bookingView);
        bookingView.setText(list.get(position).toString());

        ImageButton editBtn= (ImageButton)view.findViewById(R.id.editBtn);

        editBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Editeaza Categoria");
                final EditText input = new EditText(context);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                final String categoryId = list.get(position).getId();
                final String oldCategoryName = list.get(position).getName();
                input.setText(oldCategoryName);
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String editedCategory = input.getText().toString();
                        editCategory(categoryId, oldCategoryName, editedCategory);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        ImageButton deleteBtn= (ImageButton)view.findViewById(R.id.deleteBtn);

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirmare stergere");
                builder.setMessage("Esti sigur ca vrei sa stergi aceasta categorie? \nToate Itemele si rezervarile acestora vor fi pierdute!");
                builder.setPositiveButton("Da", new    DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String categoryId = list.get(position).getId();
                        categorie = categoryId;
                        getCategoryInfo(categoryId);
                        list.remove(position);
                        notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("Nu", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return view;
    }

    private void editCategory(String categoryId, String oldCategoryName, final String editedCategory){
        database = FirebaseDatabase.getInstance();
        DatabaseReference myRefToDatabase = database.getReference("Categories").child(categoryId).child("name");
        myRefToDatabase.setValue(editedCategory);
        editBookings(oldCategoryName, editedCategory);
    }

    private void editBookings(final String oldCategoryName, final String editedCategory) {
        database = FirebaseDatabase.getInstance();
        DatabaseReference myRefToDatabase = database.getReference("Bookings");
        myRefToDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Gson gson = new Gson();
                    String gsonString = gson.toJson(dataSnapshot.getValue());
                    try {
                        JSONObject bookings = new JSONObject(gsonString);
                        Iterator<String> iterator = bookings.keys();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            JSONObject booking = new JSONObject(bookings.get(key).toString());
                            if(booking.getString("categoryName").equals(oldCategoryName)){
                                DatabaseReference refForEdit = database.getReference("Bookings").child(key).child("categoryName");
                                refForEdit.setValue(editedCategory);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getCategoryInfo(final String categoryId){
        database = FirebaseDatabase.getInstance();
        DatabaseReference myRefToDatabase = database.getReference("Categories").child(categoryId);
        myRefToDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Gson gson = new Gson();
                    String gsonString = gson.toJson(dataSnapshot.getValue());
                    try {
                        JSONObject category = new JSONObject(gsonString);
                        if(category.has("items")) {
                            JSONObject items = category.getJSONObject("items");
                            Iterator<String> itemsIterator = items.keys();
                            while (itemsIterator.hasNext()) {
                                String key = itemsIterator.next();
                                try {
                                    JSONObject item = items.getJSONObject(key);
                                    String itemId = key;
                                    if(item.has("bookings")) {
                                        JSONObject bookings = item.getJSONObject("bookings");
                                        Iterator<String> iterator = bookings.keys();
                                        while (iterator.hasNext()) {
                                            String bookingKey = iterator.next();
                                            try {
                                                String bookingId = bookings.get(bookingKey).toString();
                                                deleteBookingFromUser(bookingId, itemId);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        deleteItem(itemId);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        deleteCategory(categoryId);
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

    private void deleteBookingFromUser(final String bookingId, final String itemId) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRefToDatabase = database.getReference("Bookings/" + bookingId);
        myRefToDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Gson gson = new Gson();
                    String gsonString = gson.toJson(dataSnapshot.getValue());
                    try {
                        JSONObject booking = new JSONObject(gsonString);
                        String userId = booking.getString("user");
                        DatabaseReference refForDeleting = database.getReference("Users").child(userId).child("bookings").child(bookingId);
                        refForDeleting.removeValue().addOnSuccessListener(new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                deleteBooking(bookingId, itemId);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });
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

    private void deleteBooking(final String bookingId, final String itemId) {
        DatabaseReference refForDeleting = database.getReference("Bookings").child(bookingId);
        refForDeleting.removeValue().addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                deleteItem(itemId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void deleteItem(String itemId){
        DatabaseReference refForDeleting = database.getReference("Categories").child(categorie).child("items").child(itemId);
        refForDeleting.removeValue().addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
        }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void deleteCategory(String categoryId){
        database.getReference("Categories").child(categoryId).removeValue();
        succesDialog();
    }

    private void succesDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Stergere cu succes");
        builder.setMessage("Stergerea a fost efectuata cu succes!");
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}