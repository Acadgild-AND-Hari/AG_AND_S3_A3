package com.hari.aag.galleryimage;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryImageActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String LOG_TAG = GalleryImageActivity.class.getSimpleName();
    private static final String PREFS_NAME = GalleryImageActivity.class.getSimpleName();

    private final int REQUEST_IMAGE_SELECTOR = 101;

    private boolean isImageLoaded = false;
    private String imageUri;

    private File mCurrentPhoto;

    private static final String IMAGE_LOADED = "imageLoaded";
    private static final String IMAGE_URI = "imageUri";

    @BindView(R.id.image) ImageView imageIV;
    @BindView(R.id.pickImageBtn) Button pickImageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_image);
        ButterKnife.bind(this);

        pickImageBtn.setOnClickListener(this);

        Log.d(LOG_TAG, "Inside - onCreate");
        readValuesFromPrefs();
        updateValueToUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Inside - onPause");
        saveValuesToPrefs();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pickImageBtn:
                dispatchPhotoSelectionIntent();
                break;
        }
    }

    private void dispatchPhotoSelectionIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_SELECTOR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK &&
                requestCode == REQUEST_IMAGE_SELECTOR) {
            if (data != null && data.getData() != null) {
                isImageLoaded = getImageFromData(data);
                updateValueToUI();
            } else {
                isImageLoaded = false;
            }
        }
    }

    private boolean getImageFromData(Intent data){
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver()
                .query(data.getData(), filePathColumn, null, null, null);

        if (cursor == null || cursor.getCount() < 1) {
            return false;
        }

        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        if (columnIndex < 0) { // no column index
            return false;
        } else {
            imageUri = cursor.getString(columnIndex);
        }
        cursor.close();
        return true;
    }

    private void updateValueToUI() {
        if (isImageLoaded){
            Picasso.with(this)
                    .load(new File(imageUri))
                    .resize(600, 600)
                    .centerCrop()
                    .into(imageIV);
        }
    }

    private void readValuesFromPrefs() {
        SharedPreferences mySharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        isImageLoaded = mySharedPrefs.getBoolean(IMAGE_LOADED, false);
        imageUri = mySharedPrefs.getString(IMAGE_URI, "");

        Log.d(LOG_TAG, "Values Read from Prefs.");
        dumpPrefValues();
    }

    private void saveValuesToPrefs() {
        SharedPreferences.Editor prefsEditor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        prefsEditor.putBoolean(IMAGE_LOADED, isImageLoaded);
        prefsEditor.putString(IMAGE_URI, imageUri);
        prefsEditor.commit();

        Log.d(LOG_TAG, "Values Saved to Prefs.");
        dumpPrefValues();
    }

    private void dumpPrefValues() {
        Log.d(LOG_TAG, IMAGE_LOADED + " - " + isImageLoaded);
        Log.d(LOG_TAG, IMAGE_URI + " - " + imageUri);
    }
}
