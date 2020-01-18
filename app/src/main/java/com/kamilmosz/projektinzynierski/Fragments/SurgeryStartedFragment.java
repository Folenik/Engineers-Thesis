package com.kamilmosz.projektinzynierski.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kamilmosz.projektinzynierski.ChooseActionActivity;
import com.kamilmosz.projektinzynierski.Models.ScannedElements;
import com.kamilmosz.projektinzynierski.Models.ScannedElementsViewHolder;
import com.kamilmosz.projektinzynierski.R;

public class SurgeryStartedFragment extends Fragment {

    private View scannedElementsView;
    private RecyclerView scannedElementsList;
    private DatabaseReference scannedElementsRef, instrumentsRef, surgeryRef, instrumentsRescannedRef;
    private FirebaseRecyclerAdapter mAdapter;
    FirebaseStorage storage;
    StorageReference storageReference;
    AppCompatButton startSurgeryButton;
    AppCompatTextView startSurgeryTextView, endSurgeryTextView;
    Bundle mBundle;
    String test = "null";

    public SurgeryStartedFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        scannedElementsView = inflater.inflate(R.layout.fragment_surgery_started, container, false);

        startSurgeryButton = (AppCompatButton) scannedElementsView.findViewById(R.id.start_surgery_button);
        startSurgeryButton.setVisibility(View.INVISIBLE);

        startSurgeryTextView = (AppCompatTextView) scannedElementsView.findViewById(R.id.start_surgery_textview);
        startSurgeryTextView.setVisibility(View.VISIBLE);

        endSurgeryTextView = (AppCompatTextView) scannedElementsView.findViewById(R.id.end_surgery_textview);
        endSurgeryTextView.setVisibility(View.INVISIBLE);

        mBundle = new Bundle();
        mBundle = getArguments();
        test = mBundle.getString("currentSurgery");

        startSurgeryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(getActivity(), ChooseActionActivity.class);
                startActivity(mIntent);
            }
        });

        scannedElementsList = (RecyclerView) scannedElementsView.findViewById(R.id.scanned_items_list);
        scannedElementsList.setLayoutManager(new LinearLayoutManager(getContext()));

        scannedElementsRef = FirebaseDatabase.getInstance().getReference().child("surgeries").child(test).child("instruments");
        instrumentsRef = FirebaseDatabase.getInstance().getReference().child("instruments");
        instrumentsRescannedRef = FirebaseDatabase.getInstance().getReference().child("surgeries").child(test).child("instruments_rescanned");

        surgeryRef = FirebaseDatabase.getInstance().getReference().child("surgery_pending");
        surgeryRef.setValue("1");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        scannedElementsList.setLayoutManager(new LinearLayoutManager(getActivity()));

        FirebaseRecyclerOptions<ScannedElements> options = new FirebaseRecyclerOptions.Builder<ScannedElements>()
                .setQuery(scannedElementsRef, ScannedElements.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<ScannedElements, ScannedElementsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ScannedElementsViewHolder holder, int position, @NonNull ScannedElements model) {

                final String ElementsPosition = getRef(position).getKey();

                scannedElementsRef.child(ElementsPosition).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("status") && dataSnapshot.hasChild("id")) {
                            final String scannedElementId = dataSnapshot.child("id").getValue(String.class);

                            holder.elementId.setText(scannedElementId);
                            holder.elementImage.setImageResource(R.drawable.ic_image_not_set);

                            storageReference.child("images/" + scannedElementId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(getContext())
                                            .load(uri)
                                            .into(holder.elementImage);
                                }
                            });

                            if (dataSnapshot.hasChild("name")) {
                                String scannedElementName = dataSnapshot.child("name").getValue(String.class);
                                holder.elementName.setText(scannedElementName);
                            } else {

                            }

                            if (dataSnapshot.child("status").getValue().equals("1")) {
                                instrumentsRescannedRef.child(scannedElementId).child("status").setValue("1");
                                instrumentsRescannedRef.child(scannedElementId).child("id").setValue(scannedElementId);
                                if (dataSnapshot.hasChild("name")) {
                                    String scannedElementName = dataSnapshot.child("name").getValue(String.class);
                                    instrumentsRescannedRef.child(scannedElementId).child("name").setValue(scannedElementName);
                                }
                                scannedElementsRef.child(scannedElementId).removeValue();

                                if (mAdapter.getItemCount() == 1) {
                                    startSurgeryButton.setVisibility(View.VISIBLE);
                                    startSurgeryTextView.setVisibility(View.GONE);
                                    endSurgeryTextView.setVisibility(View.VISIBLE);
                                }
                            }

                            holder.elementImage.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View view) {

                                    final Dialog dialog = new Dialog(getActivity());
                                    dialog.setContentView(R.layout.scanned_element_photo_layout);
                                    dialog.getWindow().setLayout(1000,1000);
                                    dialog.setTitle("Dialog Box");
                                    final AppCompatImageView img = (AppCompatImageView) dialog.findViewById(R.id.img);
                                    storageReference.child("images/" + scannedElementId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Glide.with(getContext())
                                                    .load(uri)
                                                    .fitCenter()
                                                    .into(img);
                                        }
                                    });
                                    dialog.show();

                                    return true;
                                }
                            });



                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                instrumentsRef.child(ElementsPosition).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("status") && dataSnapshot.hasChild("id")) {
                            final String scannedElementId = dataSnapshot.child("id").getValue(String.class);

                            if (dataSnapshot.child("status").getValue().equals("1")) {
                                scannedElementsRef.child(scannedElementId).child("status").setValue("1");
                                scannedElementsRef.child(scannedElementId).child("status").removeValue();
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ScannedElementsViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.editable_element_layout, parent, false);
                return new ScannedElementsViewHolder(view);
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