package com.kamilmosz.projektinzynierski;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import static com.kamilmosz.projektinzynierski.Utility.Constants.CAMERA;
import static com.kamilmosz.projektinzynierski.Utility.Constants.GALLERY;
import static com.kamilmosz.projektinzynierski.Utility.Constants.PICK_IMAGE_FROM_GALLERY_REQUEST;
import static com.kamilmosz.projektinzynierski.Utility.Constants.TAKE_PICTURE_REQUEST;

public class EditSingleElementActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    Toolbar editItemToolbar;
    Bundle bundleElementId;
    String elementId;
    Boolean isPhotoChanged = false;
    AppCompatTextView elementIdContent;
    AppCompatEditText elementNameContent;
    AppCompatButton buttonNameChange;
    AppCompatImageView elementImage;
    DatabaseReference editElementsRef;
    FirebaseStorage storage;
    StorageReference storageReference;
    Uri filePath;
    Integer isPhotoFromGallery;
    byte[] dataByteArrayOutputStream;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_single_element);

        init();
        getDataFromDatabase();
    }

    public void init() {
        editItemToolbar = (Toolbar) findViewById(R.id.edit_element_toolbar);
        elementNameContent = (AppCompatEditText) findViewById(R.id.element_name_content);
        elementIdContent = (AppCompatTextView) findViewById(R.id.element_id_content);
        buttonNameChange = (AppCompatButton) findViewById(R.id.button_change_name);
        elementImage = (AppCompatImageView) findViewById(R.id.element_image);
        buttonNameChange.setOnClickListener(this);
        elementImage.setImageResource(R.drawable.ic_image_not_set);
        elementImage.setOnLongClickListener(this);


        setSupportActionBar(editItemToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        bundleElementId = getIntent().getExtras();
        elementId = bundleElementId.getString("id");
        elementIdContent.setText(elementId);

        editElementsRef = FirebaseDatabase.getInstance().getReference().child("instruments").child(elementId);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    public void getDataFromDatabase() {
        editElementsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("name")) {
                    elementNameContent.setText(snapshot.child("name").getValue().toString());
                } else {
                    elementNameContent.setText(R.string.edit_element_set_name);
                }

                storageReference.child("images/" + elementIdContent.getText().toString()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getApplicationContext())
                                .load(uri)
                                .into(elementImage);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == buttonNameChange) {
            editElementsRef.child("name").setValue(elementNameContent.getText().toString());
            if(isPhotoChanged == true) {
                uploadImage();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onLongClick(View view) {
        if (view == elementImage) {
            pickImageOrTakePhoto();
        }
        return true;
    }

    public void uploadImage() {
        switch (isPhotoFromGallery) {
            case 0:
                final ProgressDialog progressDialogPhoto = new ProgressDialog(this);
                progressDialogPhoto.setTitle(getString(R.string.edit_single_uploading));
                progressDialogPhoto.show();

                storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference imagesRef = storageReference.child("images/" + elementIdContent.getText().toString());

                UploadTask uploadTask = imagesRef.putBytes(dataByteArrayOutputStream);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialogPhoto.dismiss();
                    Toast.makeText(EditSingleElementActivity.this, R.string.edit_single_uploaded, Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialogPhoto.dismiss();
                            Toast.makeText(EditSingleElementActivity.this, getString(R.string.edit_single_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialogPhoto.setMessage(R.string.edit_single_uploaded + (int) progress + "%");
                        }
                    });
                break;
            case 1:
                if (filePath != null) {
                    final ProgressDialog progressDialogImage = new ProgressDialog(this);
                    progressDialogImage.setTitle(getString(R.string.edit_single_uploading));
                    progressDialogImage.show();

                    StorageReference ref = storageReference.child("images/" + elementIdContent.getText().toString());
                    ref.putFile(filePath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progressDialogImage.dismiss();
                                    Toast.makeText(EditSingleElementActivity.this, R.string.edit_single_uploaded, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialogImage.dismiss();
                                    Toast.makeText(EditSingleElementActivity.this, getString(R.string.edit_single_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                            .getTotalByteCount());
                                    progressDialogImage.setMessage(R.string.edit_single_uploaded + (int) progress + "%");
                                }
                            });
                }
                break;
        }
    }

    public void pickImageOrTakePhoto() {
        String[] imageOptions = {GALLERY, CAMERA};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.edit_single_take_photo));
        builder.setItems(imageOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        pickImageIntent();
                        break;
                    case 1:
                        takePhotoIntent();
                        break;
                }
            }
        });
        builder.show();
    }

    public void takePhotoIntent() {
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePhoto, TAKE_PICTURE_REQUEST);
    }

    public void pickImageIntent() {
        Intent pickFromGallery = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickFromGallery, PICK_IMAGE_FROM_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {

                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    dataByteArrayOutputStream = byteArrayOutputStream.toByteArray();
                    elementImage.setImageBitmap(bitmap);

                    isPhotoFromGallery = 0;
                    isPhotoChanged = true;

                }
                break;
            case 1:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    elementImage.setImageURI(selectedImage);
                    filePath = data.getData();

                    isPhotoFromGallery = 1;
                    isPhotoChanged = true;
                }
                break;
        }
    }
}



