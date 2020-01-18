package com.kamilmosz.projektinzynierski.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kamilmosz.projektinzynierski.Models.ScannedElements;
import com.kamilmosz.projektinzynierski.Models.SurgeriesViewHolder;
import com.kamilmosz.projektinzynierski.R;
import com.kamilmosz.projektinzynierski.Utility.Constants;

public class BrowseSurgeriesFragment extends Fragment {

    private View scannedElementsView;
    private RecyclerView scannedElementsList;
    private DatabaseReference scannedElementsRef;
    private FirebaseRecyclerAdapter mAdapter;
    FirebaseStorage storage;
    StorageReference storageReference;

    public BrowseSurgeriesFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        scannedElementsView = inflater.inflate(R.layout.fragment_browse_surgeries, container, false);

        scannedElementsList = (RecyclerView) scannedElementsView.findViewById(R.id.scanned_items_list);
        scannedElementsList.setLayoutManager(new LinearLayoutManager(getContext()));

        scannedElementsRef = FirebaseDatabase.getInstance().getReference().child("surgeries");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        scannedElementsList.setLayoutManager(new LinearLayoutManager(getActivity()));

        FirebaseRecyclerOptions<ScannedElements> options = new FirebaseRecyclerOptions.Builder<ScannedElements>()
                .setQuery(scannedElementsRef, ScannedElements.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<ScannedElements, SurgeriesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final SurgeriesViewHolder holder, int position, @NonNull ScannedElements model) {

                final String ElementsPosition = getRef(position).getKey();

                scannedElementsRef.child(ElementsPosition).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("instruments_rescanned")) {
                            final String scannedInstrumentId = dataSnapshot.getKey();
                            String scannedInstrumentIdSubString = scannedInstrumentId.substring(0, scannedInstrumentId.length() - 30);
                            final String scannedInstrumentDate = Constants.DATE + scannedInstrumentId.substring(scannedInstrumentId.length() - 28, scannedInstrumentId.length());


                            String instrumentsNotScanned = "Not rescanned: ";
                            String rescannedInstrumentHelpString = String.valueOf(dataSnapshot.child("instruments_rescanned").getChildrenCount());
                            int rescannedInstrumentHelpInt = Integer.valueOf(rescannedInstrumentHelpString);
                            String childrenCountString = String.valueOf(dataSnapshot.child("instruments").getChildrenCount());
                            int childrenCountInt = Integer.valueOf(childrenCountString);

                            if (rescannedInstrumentHelpInt != rescannedInstrumentHelpInt - childrenCountInt) {
                                for (DataSnapshot ds : dataSnapshot.child("instruments").getChildren()) {
                                    instrumentsNotScanned += ds.getKey().toString() + " ";
                                }
                                holder.surgeryInstrumentNotChecked.setText(instrumentsNotScanned);
                            }

                            holder.surgeryName.setText(scannedInstrumentIdSubString);
                            holder.surgeryDate.setText(scannedInstrumentDate);
                            holder.surgeryInstrument.setText(Constants.INSTRUMENTS_USED + rescannedInstrumentHelpInt);
                            holder.surgeryInstrumentCheck.setText(Constants.INSTRUMENTS_SCANNED_OFF + String.valueOf(rescannedInstrumentHelpInt - childrenCountInt));
                            holder.surgeryDate.setVisibility(View.GONE);
                            holder.surgeryInstrument.setVisibility(View.GONE);
                            holder.surgeryInstrumentCheck.setVisibility(View.GONE);
                            holder.surgeryInstrumentNotChecked.setVisibility(View.GONE);

                            holder.surgeryName.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (holder.surgeryDate.getVisibility() == View.VISIBLE) {
                                        holder.surgeryDate.setVisibility(View.GONE);
                                        holder.surgeryInstrument.setVisibility(View.GONE);
                                        holder.surgeryInstrumentCheck.setVisibility(View.GONE);
                                    } else {
                                        holder.surgeryDate.setVisibility(View.VISIBLE);
                                        holder.surgeryInstrument.setVisibility(View.VISIBLE);
                                        holder.surgeryInstrumentCheck.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public SurgeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                          int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_surgery_layout, parent, false);
                return new SurgeriesViewHolder(view);
            }
        };

        scannedElementsList.setAdapter(mAdapter);
        return scannedElementsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}