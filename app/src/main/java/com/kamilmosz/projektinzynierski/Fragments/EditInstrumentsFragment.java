package com.kamilmosz.projektinzynierski.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
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
import com.kamilmosz.projektinzynierski.EditSingleElementActivity;
import com.kamilmosz.projektinzynierski.Models.ScannedElements;
import com.kamilmosz.projektinzynierski.Models.ScannedElementsViewHolder;
import com.kamilmosz.projektinzynierski.R;

public class EditInstrumentsFragment extends Fragment {

    private View scannedElementsView;
    private RecyclerView scannedElementsList;
    private DatabaseReference scannedElementsRef;
    private FirebaseRecyclerAdapter mAdapter;
    FirebaseStorage storage;
    StorageReference storageReference;

    public EditInstrumentsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        scannedElementsView = inflater.inflate(R.layout.fragment_edit_instruments, container, false);

        scannedElementsList = (RecyclerView) scannedElementsView.findViewById(R.id.scanned_items_list);
        scannedElementsList.setLayoutManager(new LinearLayoutManager(getContext()));

        scannedElementsRef = FirebaseDatabase.getInstance().getReference().child("instruments");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        scannedElementsList.setLayoutManager(new LinearLayoutManager(getActivity()));

        FirebaseRecyclerOptions<ScannedElements> options = new FirebaseRecyclerOptions.Builder<ScannedElements>()
                .setQuery(scannedElementsRef, ScannedElements.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<ScannedElements, ScannedElementsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ScannedElementsViewHolder holder, final int position, @NonNull ScannedElements model) {

                final String ElementsPosition = getRef(position).getKey();

                scannedElementsRef.child(ElementsPosition).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
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

                            holder.elementImage.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View view) {

                                    final Dialog dialog = new Dialog(getActivity());
                                    dialog.setContentView(R.layout.scanned_element_photo_layout);
                                    dialog.setTitle("Dialog Box");
                                    dialog.getWindow().setLayout(1000,1000);
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

                            holder.elementButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent mIntent = new Intent(getActivity(), EditSingleElementActivity.class);
                                    mIntent.putExtra("id", holder.elementId.getText().toString());
                                    startActivity(mIntent);
                                }
                            });

                            holder.elementCardView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View view) {
                                    new AlertDialog.Builder(getContext())
                                            .setTitle("Delete instrument")
                                            .setMessage("Are you sure you want to delete this  instrument?")
                                            .setPositiveButton(getString(R.string.global_yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    scannedElementsRef.child(holder.elementId.getText().toString()).removeValue();
                                                }
                                            })
                                            .setNegativeButton(getString(R.string.global_no), null)
                                            .show();
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
            }

            @NonNull
            @Override
            public ScannedElementsViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scanned_element_layout, parent, false);
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