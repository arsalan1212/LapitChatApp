package com.example.arsalankhan.lapitchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSettingActivity extends AppCompatActivity {

    private TextView tv_displayName, tv_status;
    private CircleImageView profile_image;

    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mImageStorage;
    private static final int GALLERY_REQUEST_CODE = 100;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        tv_displayName = (TextView) findViewById(R.id.account_display_name);
        tv_status = (TextView) findViewById(R.id.account_status);
        profile_image = (CircleImageView) findViewById(R.id.setting_profile_image);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();

        //firbase image storage
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDatabase.keepSynced(true);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                tv_displayName.setText(name);
                tv_status.setText(status);

                //showing the image in imageView
                if (!image.equals("default")) {
                    //Picasso.with(AccountSettingActivity.this).load(image).placeholder(R.drawable.avatar_default).into(profile_image);
                    Picasso.with(AccountSettingActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).
                            placeholder(R.drawable.avatar_default).into(profile_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(AccountSettingActivity.this).load(image).placeholder(R.drawable.avatar_default).into(profile_image);
                        }
                    });
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mCurrentUser!=null){
            mDatabase.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabase.child("online").setValue(ServerValue.TIMESTAMP);
    }

    //changing profile image
    public void ChangeProfileImage(View view) {

        Intent imageIntent = new Intent();
        imageIntent.setType("image/*");
        imageIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(imageIntent, "Pick Image"), GALLERY_REQUEST_CODE);
    }

    //changeing status
    public void ChangeStatus(View view) {

        String status = tv_status.getText().toString();
        Intent statusIntent = new Intent(AccountSettingActivity.this, StatusActivity.class);
        statusIntent.putExtra("status_value", status);
        startActivity(statusIntent);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
        }

        //for image crop
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                progressDialog = new ProgressDialog(AccountSettingActivity.this);
                progressDialog.setTitle("Saving Image");
                progressDialog.setMessage("Please wait while we store and process your Image");
                progressDialog.setCancelable(false);
                progressDialog.show();

                //storing image having userId as its name
                String uid = mCurrentUser.getUid();

                //Compressed the Image
                Bitmap compressedImageBitmap = getCompressedBitmapImage(resultUri);

                //storing the bitmap image into firebase
                ByteArrayOutputStream byteArryoutputStream = new ByteArrayOutputStream();
                compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArryoutputStream);
                final byte[] thumb_byte = byteArryoutputStream.toByteArray();

                //storing bitmap image
                final StorageReference thumb_filePath = mImageStorage.child("profile_images").child("thumb_images").child(uid + ".jpg");

                //Storage Reference and store image into firebase storage
                StorageReference filepath = mImageStorage.child("profile_images").child(uid + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            @SuppressWarnings("VisibleForTests") final String downloadUri = task.getResult().getDownloadUrl().toString();

                            //for the bitmap image
                            UploadTask uploadtask = thumb_filePath.putBytes(thumb_byte);
                            uploadtask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    if(task.isSuccessful()){
                                        @SuppressWarnings("VisibleForTests") String bitmap_downloadUrl = task.getResult().getDownloadUrl().toString();

                                        //Now storing the image and thumb bitmap image download url into database
                                        Map update_map = new HashMap();
                                        update_map.put("image", downloadUri);
                                        update_map.put("thumb_image", bitmap_downloadUrl);

                                        mDatabase.updateChildren(update_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(AccountSettingActivity.this, "Updation Success", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(AccountSettingActivity.this, "There is some error in Updation", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else{
                                        Toast.makeText(AccountSettingActivity.this, "There is An Error in Storing thumb Image", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(AccountSettingActivity.this, "Image not store", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    //Compress the IMage and return the compressedImage
    private Bitmap getCompressedBitmapImage(Uri resultUri) {

        File myFile = new File(resultUri.getPath());
        Bitmap compressedImageBitmap = null;

        try {
            compressedImageBitmap = new Compressor(this).
                    setMaxWidth(200).
                    setMaxHeight(200).
                    setQuality(75).
                    compressToBitmap(myFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return compressedImageBitmap;
    }

}
