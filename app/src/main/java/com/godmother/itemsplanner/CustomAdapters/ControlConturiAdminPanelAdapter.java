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
import com.godmother.itemsplanner.models.UserEmail;
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

public class ControlConturiAdminPanelAdapter  extends BaseAdapter implements ListAdapter {
    private ArrayList<UserEmail> list = new ArrayList<UserEmail>();
    private Context context;
    FirebaseDatabase database;

    public ControlConturiAdminPanelAdapter(ArrayList<UserEmail> list, Context context) {
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
            view = inflater.inflate(R.layout.admin_panel_control_conturi_layout, null);
        }

        TextView emailView= (TextView)view.findViewById(R.id.emailView);
        emailView.setText(list.get(position).getEmail());

        ImageButton deleteBtn= (ImageButton)view.findViewById(R.id.deleteBtn);

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirmare stergere");
                builder.setMessage("Esti sigur ca vrei sa stergi acest email? \nUtilizatorul cu acest email nu va mai putea folosi aplicatia!");
                builder.setPositiveButton("Da", new    DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO delete accout
                        database = FirebaseDatabase.getInstance();
                        DatabaseReference myRefToDatabase = database.getReference("UsersEmail");
                        myRefToDatabase.child(list.get(position).getKey()).removeValue();
                        final String emailToDelete = list.get(position).getEmail();

                        DatabaseReference myRefToDatabaseUsers = database.getReference("Users");
                        myRefToDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Gson gson = new Gson();
                                    String gsonString = gson.toJson(dataSnapshot.getValue());

                                    try {
                                        JSONObject usersJson = new JSONObject(gsonString);
                                        Iterator<String> iterator = usersJson.keys();
                                        while (iterator.hasNext()) {
                                            String key = iterator.next();
                                            try {
                                                JSONObject user = new JSONObject(usersJson.get(key).toString());
                                                if(user.has("email") && user.getString("email").equals(emailToDelete)) {
                                                    blockUser(key);
                                                    break;
                                                }
                                            } catch (JSONException e) {
                                                // Something went wrong!
                                            }
                                        }
                                        list.remove(position);
                                        notifyDataSetChanged();
                                    }
                                    catch (Exception e){
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

    private void blockUser(String key){
        DatabaseReference myRefToDatabaseUsers = database.getReference("Users").child(key);
        myRefToDatabaseUsers.child("blocked").setValue("true");

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