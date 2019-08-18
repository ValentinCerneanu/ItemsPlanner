package com.godmother.itemsplanner.CustomAdapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.godmother.itemsplanner.R;
import com.godmother.itemsplanner.models.Item;
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

public class MyItemsAdminPanelAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<Item> list = new ArrayList<Item>();
    private Context context;
    private String categoryId;

    FirebaseDatabase database;

    public MyItemsAdminPanelAdapter(ArrayList<Item> list, String categoryId, Context context) {
        this.list = list;
        this.context = context;
        this.categoryId = categoryId;
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

        ImageButton deleteBtn= (ImageButton)view.findViewById(R.id.deleteBtn);

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirmare stergere");
                builder.setMessage("Esti sigur ca vrei sa stergi acest item? \nToate rezervarile acestui item vor fi pierdute!");
                builder.setPositiveButton("Da", new    DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String itemId = list.get(position).getId();

                        database = FirebaseDatabase.getInstance();
                        DatabaseReference myRefToDatabase = database.getReference("Categories").child(categoryId).child("items").child(itemId);
                        myRefToDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    Gson gson = new Gson();
                                    String gsonString = gson.toJson(dataSnapshot.getValue());
                                    try {
                                        JSONObject item = new JSONObject(gsonString);
                                        if(item.has("bookings")) {
                                            JSONObject bookings = item.getJSONObject("bookings");
                                            Iterator<String> iterator = bookings.keys();
                                            while (iterator.hasNext()) {
                                                String key = iterator.next();
                                                try {
                                                    String bookingId = bookings.get(key).toString();
                                                    deleteBookingFromUser(bookingId, itemId);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        } else {
                                            deleteItem(itemId);
                                        }
                                        list.remove(position);
                                        notifyDataSetChanged();
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
                });
                builder.setNegativeButton("Nu", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        return view;
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
        DatabaseReference refForDeleting = database.getReference("Categories").child(categoryId).child("items").child(itemId);
        refForDeleting.removeValue().addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Stergere cu succes");
                builder.setMessage("Stergerea a fost efectuata cu succes!");
                builder.setPositiveButton("OK", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }
}