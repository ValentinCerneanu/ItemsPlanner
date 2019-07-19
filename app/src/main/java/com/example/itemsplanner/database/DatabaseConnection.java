package com.example.itemsplanner.database;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class DatabaseConnection {

    private FirebaseDatabase database;
    private DatabaseReference myRefToDatabase;

    public JSONObject getJSONObjectAtReference(String reference){
        final JSONObject[] objectAtReference = new JSONObject[0];

        try{

            database = FirebaseDatabase.getInstance();
            myRefToDatabase = database.getReference(reference);

            myRefToDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        Gson gson = new Gson();
                        String gsonString = gson.toJson(dataSnapshot.getValue());
                        try {
                            objectAtReference[0] = new JSONObject(gsonString);
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
        catch(Exception e){
            e.printStackTrace();
        }
        return objectAtReference[0];
    }
}
