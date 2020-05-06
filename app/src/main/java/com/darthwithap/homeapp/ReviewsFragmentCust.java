package com.darthwithap.homeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ReviewsFragmentCust extends Fragment {

    private FirebaseRecyclerOptions<ReviewCard> options;
    FirebaseRecyclerAdapter<ReviewCard, ReviewsFragmentCust.RevCardViewHolder> adapter;
    private RecyclerView mReviewCards;
    private DatabaseReference revCardsRef;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_reviews_cust, container, false);
        revCardsRef= FirebaseDatabase.getInstance().getReference("Reviews").child("Technicians")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        revCardsRef.keepSynced(true);

        mReviewCards=root.findViewById(R.id.recycler_view_rev);
        mReviewCards.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        options=new FirebaseRecyclerOptions.Builder<ReviewCard>()
                .setQuery(revCardsRef, ReviewCard.class).build();

        adapter=new FirebaseRecyclerAdapter<ReviewCard, ReviewsFragmentCust.RevCardViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ReviewsFragmentCust.RevCardViewHolder holder, int position, @NonNull ReviewCard model) {
                holder.setName(model.getName());
                holder.setReview(model.getReview());
            }

            @NonNull
            @Override
            public ReviewsFragmentCust.RevCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_review, parent, false);
                return new ReviewsFragmentCust.RevCardViewHolder(view);
            }
        };

        adapter.startListening();
        mReviewCards.setAdapter(adapter);

        return root;
    }

    private class RevCardViewHolder extends RecyclerView.ViewHolder {

        TextView name, review;

        public RevCardViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.txt_name_card_review);
            review = itemView.findViewById(R.id.txt_review_card_review);

        }

        public void setName(String Name) {
            name.setText(Name);
        }

        public void setReview(String Review){
            review.setText(Review);
        }
    }

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

}