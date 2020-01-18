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
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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

import java.util.Calendar;
import java.util.Date;

public class ScannedInstrumentsFragment extends Fragment {

    private View scannedInstrumentsView;
    private RecyclerView scannedInstrumentsList;
    private DatabaseReference scannedInstrumentsRef, surgeryRef;
    private FirebaseRecyclerAdapter mAdapter;
    FirebaseStorage storage;
    StorageReference storageReference;
    AppCompatButton startSurgeryButton;
    String bundleKeyString;

    public ScannedInstrumentsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        scannedInstrumentsView = inflater.inflate(R.layout.fragment_scanned_instruments, container, false);

        bundleKeyString = getArguments().getString("my_key_retrieved");

        startSurgeryButton = (AppCompatButton) scannedInstrumentsView.findViewById(R.id.start_surgery_button);
        scannedInstrumentsList = (RecyclerView) scannedInstrumentsView.findViewById(R.id.scanned_items_list);
        scannedInstrumentsList.setLayoutManager(new LinearLayoutManager(getContext()));

        scannedInstrumentsRef = FirebaseDatabase.getInstance().getReference().child("instruments");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        surgeryRef = FirebaseDatabase.getInstance().getReference().child("surgery_pending");
        surgeryRef.setValue("0");

        scannedInstrumentsList.setLayoutManager(new LinearLayoutManager(getActivity()));

        final Date currentTime = Calendar.getInstance().getTime();

        FirebaseRecyclerOptions<ScannedElements> options = new FirebaseRecyclerOptions.Builder<ScannedElements>()
                .setQuery(scannedInstrumentsRef, ScannedElements.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<ScannedElements, ScannedElementsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ScannedElementsViewHolder holder, final int position, @NonNull ScannedElements model) {

                final String ElementsPosition = getRef(position).getKey();

                scannedInstrumentsRef.child(ElementsPosition).addValueEventListener(new ValueEventListener() {
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
                                    dialog.getWindow().setLayout(1000,1000);
                                    dialog.setTitle(R.string.scanned_element_dialogbox);
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
                                    Intent editSingleIntent = new Intent(getActivity(), EditSingleElementActivity.class);
                                    editSingleIntent.putExtra("id", holder.elementId.getText().toString());
                                    startActivity(editSingleIntent);
                                }
                            });

                            holder.elementCardView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View view) {
                                    new AlertDialog.Builder(getContext())
                                            .setTitle(getString(R.string.scanned_elements_delete))
                                            .setMessage(getString(R.string.scanned_elements_are_u_sure))
                                            .setPositiveButton(getString(R.string.global_yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    scannedInstrumentsRef.child(holder.elementId.getText().toString()).removeValue();
                                                }
                                            })
                                            .setNegativeButton(getString(R.string.global_no), null)
                                            .show();
                                    return true;
                                }
                            });

                            holder.elementCardView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (holder.elementCardView.getCardBackgroundColor().getDefaultColor() == getResources().getColor(R.color.colorWhite)) {
                                        holder.elementCardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));

                                        scannedInstrumentsRef.child(holder.elementId.getText().toString()).child("status").setValue("0");

                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference()
                                                .child("surgeries")
                                                .child(bundleKeyString +" | "+currentTime)
                                                .child("instruments")
                                                .child(holder.elementId
                                                        .getText()
                                                        .toString())
                                                .child("id").setValue(holder.elementId.getText().toString());

                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference()
                                                .child("surgeries")
                                                .child(bundleKeyString +" | "+currentTime)
                                                .child("instruments")
                                                .child(holder.elementId
                                                        .getText()
                                                        .toString())
                                                .child("status").setValue("0");

                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference()
                                                .child("surgeries")
                                                .child(bundleKeyString +" | "+currentTime)
                                                .child("instruments")
                                                .child(holder.elementId
                                                        .getText()
                                                        .toString())
                                                .child("name").setValue(holder.elementName.getText().toString());

                                                holder.elementButton.setVisibility(View.INVISIBLE);
                                                holder.elementButtonImage.setVisibility(View.INVISIBLE);


                                    } else {
                                        holder.elementCardView.setCardBackgroundColor(getResources().getColor(R.color.colorWhite));
                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference()
                                                .child("surgeries")
                                                .child(bundleKeyString +" | "+currentTime)
                                                .child("instruments")
                                                .child(holder.elementId.getText().toString()).removeValue();

                                            holder.elementButton.setVisibility(View.VISIBLE);
                                            holder.elementButtonImage.setVisibility(View.VISIBLE);
                                    }
                                }
                            });

                            startSurgeryButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    SurgeryStartedFragment surgeryStartedFragment = new SurgeryStartedFragment();

                                    Bundle bundles = new Bundle();
                                    bundles.putString("currentSurgery", bundleKeyString +" | "+currentTime);
                                    surgeryStartedFragment.setArguments(bundles);

                                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                    fragmentTransaction.replace(R.id.coordinator_layout, surgeryStartedFragment, "tag");
                                    fragmentTransaction.addToBackStack("tag");
                                    fragmentTransaction.remove(ScannedInstrumentsFragment.this);
                                    fragmentTransaction.commit();
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

        scannedInstrumentsList.setAdapter(mAdapter);
        return scannedInstrumentsView;
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