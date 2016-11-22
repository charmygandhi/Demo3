package com.example.demo3;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSelectPhoto,btnSave,btnLoad;
    private ImageView viewImage;
    Images images = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewImage = (ImageView)findViewById(R.id.viewImage);

        btnSelectPhoto = (Button)findViewById(R.id.btnselectPhoto);
        btnSave = (Button)findViewById(R.id.btnSave);
        btnLoad = (Button)findViewById(R.id.btnLoad);

        btnSelectPhoto.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnLoad.setOnClickListener(this);

        images = new Images();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnselectPhoto:
                selectImage();
                break;
            case R.id.btnSave:
                save();
                break;
            case R.id.btnLoad:
                load();
                break;
        }
    }

    private void selectImage() {

        final CharSequence[] options = {"Choose from Gallery","Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(options[i].equals("Choose from Gallery")){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,2);
                }
                else if(options[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 2){
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage,filePath,null,null,null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                Log.d("path of image ", picturePath + "");
                viewImage.setImageBitmap(thumbnail);
                encodeBitmap(thumbnail);
            }
        }
    }

    private void encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        images.setImage(imageEncoded);
    }

    private Bitmap decodeBitmap(String image)
    {
        byte[] bytes;
        bytes = Base64.decode(image,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return bitmap;
    }

    private void save() {
        FirebaseApp app = FirebaseApp.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance(app);
        DatabaseReference ref = database.getReference();

        ref.child("Images").push().setValue(images);

        viewImage.setImageResource(R.drawable.ic_menu_camera);

        Toast.makeText(MainActivity.this,"Image Save to Firebase Successfully",Toast.LENGTH_LONG).show();
    }

    public void load(){
        FirebaseApp app = FirebaseApp.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance(app);
        DatabaseReference ref = database.getReference();

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Images i = ds.getValue(Images.class);
                    String image = i.getImage();
                    viewImage.setImageBitmap(decodeBitmap(image));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
}
