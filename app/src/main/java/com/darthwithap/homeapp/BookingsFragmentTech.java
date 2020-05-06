package com.darthwithap.homeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BookingsFragmentTech extends Fragment {

    private DatabaseReference bookingRef;
    private FirebaseAuth mAuth;
    CardView bookingCard;
    private RecyclerView mBookingCards;
    private FirebaseRecyclerOptions<TechnicianCard> options;
    FirebaseRecyclerAdapter<TechnicianCard, BookingCardViewHolder> adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_bookings_tech, container, false);
        setBookingsCardsDatabaseRef();
        bookingRef.keepSynced(true);

        mBookingCards=root.findViewById(R.id.card);
        mBookingCards.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        return root;
    }

    private class BookingCardViewHolder extends RecyclerView.ViewHolder {

        TextView name, time;

        public BookingCardViewHolder(@NonNull View itemView) {
            super(itemView);

            name=itemView.findViewById(R.id.txt_name_booking);
            time=itemView.findViewById(R.id.txt_time_booking);

        }

        public void setName(TextView name) {
            this.name = name;
        }

        public void setTime(TextView time) {
            this.time = time;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter!=null) adapter.startListening();
    }

    @Override
    public void onStop() {
        if (adapter!=null) adapter.stopListening();
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter!=null) adapter.startListening();
    }

    private void setBookingsCardsDatabaseRef() {
        bookingRef= FirebaseDatabase.getInstance().getReference("Bookings").child("Customers").child(mAuth.getCurrentUser().getUid());
    }
}