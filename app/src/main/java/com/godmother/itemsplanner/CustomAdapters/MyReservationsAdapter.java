package com.godmother.itemsplanner.CustomAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.godmother.itemsplanner.R;
import com.godmother.itemsplanner.models.BookingWrapper;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MyReservationsAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<BookingWrapper> list = new ArrayList<BookingWrapper>();
    private Context context;
    FirebaseDatabase database;

    public MyReservationsAdapter(ArrayList<BookingWrapper> list, Context context) {
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
            view = inflater.inflate(R.layout.my_reservations_adapter_layout, null);
        }

        //Handle TextView and display string from your list
        TextView bookingView= (TextView)view.findViewById(R.id.bookingView);
        bookingView.setText(list.get(position).toString());

        //Handle buttons and add onClickListeners
        ImageButton deleteBtn= (ImageButton)view.findViewById(R.id.deleteBtn);

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();
                String bookingId = list.get(position).getBooking().getBookingId();
                String itemId = list.get(position).getBooking().getItemId();
                String categoryId = list.get(position).getBooking().getCategoryId();
                String userId = list.get(position).getBooking().getUser();
                database.getReference("Bookings").child(bookingId).removeValue();
                database.getReference("Users").child(userId).child("bookings").child(bookingId).removeValue();
                database.getReference("Categories").child(categoryId).child("items").child(itemId).child("bookings").child(bookingId).removeValue();
                list.remove(position);
                notifyDataSetChanged();
            }
        });

        return view;
    }
}