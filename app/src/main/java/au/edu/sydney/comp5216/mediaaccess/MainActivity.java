package au.edu.sydney.comp5216.mediaaccess;

import java.util.List;
import java.util.UUID;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Main activity class controlling the main view. Displays all photos stored on the user's device.
 * Handles navigation to the Camera interface, and handles user driven synchronisation.
 */
public class MainActivity extends Activity {

    //Request codes
    private static final int REQUEST_CODE = 101;
    private static final int MY_READ_PERMISSION_CODE = 101;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;

    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);
    RecyclerView recyclerView;
    GalleryAdapter galleryAdapter;
    List<String> images;
    TextView gallery_number;

    /**
     * Handles the creation of the activity, and calling loadImages() to load the device's photos.
     * Permissions are also checked appropriately, and Firebase is initialised.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gallery_number = findViewById(R.id.gallery_number);
        recyclerView = findViewById(R.id.recyclerview_gallery_images);

        //Firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Permission check & call to load images
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_READ_PERMISSION_CODE);
        } else {
            loadImages();
        }
    }

    /**
     * Iterates through the list of URI's in the device and uploads them to Firebase Storage.
     * @param view
     */
    public void upload(View view) {

        //Iterates through the entire list of photo URI's
        for (int i = 0; i < ImagesGallery.listOfImages(this).size(); i++) {
            Uri uri = Uri.parse("file://"+ImagesGallery.listOfImages(this).get(i));
            System.out.println(uri);
            if(uri != null) {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading...");
                progressDialog.show();
                StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());

                //Adds the URI as a reference for Firebase Storage
                ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    }
                });
            }
        }
    }

    /**
     * Starts the CameraActivity to loads the camera interface when the "Camera" button is clicked
     * Asks for appropriate permissions.
     * @param view
     */
    public void onCameraClick(View view) {
        if (!marshmallowPermission.checkPermissionForCamera()
                || !marshmallowPermission.checkPermissionForExternalStorage()) {
            marshmallowPermission.requestPermissionForCamera();
        } else {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    /**
     * Receives the list of image URI's, passes it through the galleryAdapter, and
     * loads the images into the recyclerView.
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void loadImages() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        images = ImagesGallery.listOfImages(this);
        galleryAdapter = new GalleryAdapter(this, images, new GalleryAdapter.PhotoListener() {
            @Override
            public void onPhotoClick(String path) {
                Toast.makeText(MainActivity.this, "" + path, Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(galleryAdapter);
        gallery_number.setText("Photos (" + images.size() + ")");
    }

    /**
     * Given a request code received from returning from the CameraActivity,
     * reload the gallery with the updated images.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            loadImages();
            recreate();
        }
    }
}