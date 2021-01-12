package com.example.coco_classif;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 10001;
    private ImageView imageView;
    //private ListView listView;
    private ImageClassifier imageClassifier;
    private TextView textView;
    private TextView Suggestion_textView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inaializeUIElements(); 
    }

    private void inaializeUIElements() {
        imageView = findViewById(R.id.iv_capture);
       // listView = findViewById(R.id.lv_probabilities);
        textView = findViewById(R.id.textView2);
        Suggestion_textView = findViewById(R.id.textView3);

        Button takepicture = findViewById(R.id.bt_take_picture);



        try {
            imageClassifier = new ImageClassifier(this);
        } catch (IOException e) {
            Log.e("Error While Creating Image Classifier", "Error" +e);
        }

        takepicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasPermission()) {
                 //   listView.setAdapter(null);
                    imageView.setImageDrawable(null);
                    selectImage();

                }else{
                    requestPermission();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(Objects.requireNonNull(data).getExtras()).get("data");
            imageView.setImageBitmap(photo);

            List<ImageClassifier.Recognition> predictions = imageClassifier.recognizeImage(photo, 0);

            final List<String> predictionList = new ArrayList<>();
            for(ImageClassifier.Recognition recog: predictions){



             //   predictionList.add(recog.getName() + "   " + "Confidence: " + recog.getConfidance());
                predictionList.add(recog.getName());
                textView.setText(recog.getName());


            }

            ArrayAdapter<String> predictionsAdapter = new ArrayAdapter<>(
                    this,R.layout.support_simple_spinner_dropdown_item,predictionList);

           // listView.setAdapter(predictionsAdapter);



        }

            else if (requestCode == 2) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);



                    imageView.setImageBitmap(selectedImage);

                    List<ImageClassifier.Recognition> predictions = imageClassifier.recognizeImage(selectedImage, 0);

                    final List<String> predictionList = new ArrayList<>();
                    for(ImageClassifier.Recognition recog: predictions){
                        //   predictionList.add(recog.getName() + "   " + "Confidence: " + recog.getConfidance());
                        predictionList.add(recog.getName());
                        textView.setText(recog.getName());
                        suggest_pest();
                    }

                    ArrayAdapter<String> predictionsAdapter = new ArrayAdapter<>(
                            this,R.layout.support_simple_spinner_dropdown_item,predictionList);

                   // listView.setAdapter(predictionsAdapter);




                }catch (Exception e){

                    Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
                    Log.e("Error log", e.toString());
                }

            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(hasAllPermissions(grantResults)){
            selectImage();
        }else {
            requestPermission();
        }
    }

    private boolean hasAllPermissions(int[] grantResults) {
        for(int result : grantResults){
            if (result == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    private void requestPermission() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                Toast.makeText(this," Permission required",Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void openCamera() {
        Intent camerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camerIntent,CAMERA_REQUEST_CODE);
    }

    private boolean hasPermission() {
       if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
           return checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;
       }
       return true;
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent camerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(camerIntent,1);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 2);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void alertDialog() {
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);


        if(textView.getText().toString().equals("healthy")){
            String[] healthy={"healthy_Pesticide_name_1", "healthy_Pesticide_name_2", "healthy_Pesticide_name_3", "healthy_Pesticide_name_4", "healthy_Pesticide_name_5"};
            Random r=new Random();
            int randomNumber=r.nextInt(healthy.length);
            dialog.setMessage("Suggested Pesticide is: "+ healthy[randomNumber]);
        }else if(textView.getText().toString().contains("Unhealthy")){
            String[] unhealthy={"unhealthy_Pesticide_name_1", "unhealthy_Pesticide_name_2", "unhealthy_Pesticide_name_3", "unhealthy_Pesticide_name_4", "unhealthy_Pesticide_name_5"};
            Random r=new Random();
            int randomNumber=r.nextInt(unhealthy.length);
            dialog.setMessage("Suggested Pesticide is: "+ unhealthy[randomNumber]);
        }
        else{
            dialog.setMessage("Predict by tapping the button below");
        }
        dialog.setTitle("Dialog Box");

        dialog.setNegativeButton("cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog=dialog.create();
        alertDialog.show();
    }

    public void suggest_pest(){
        if(textView.getText().toString().equals("healthy")){
            String[] healthy={"healthy_Pesticide_name_1", "healthy_Pesticide_name_2", "healthy_Pesticide_name_3", "healthy_Pesticide_name_4", "healthy_Pesticide_name_5"};
            Random r=new Random();
            int randomNumber=r.nextInt(healthy.length);
            Suggestion_textView.setText("Suggested Pesticide is: "+ healthy[randomNumber]);

        }else if(textView.getText().toString().equals("Unhealthy")){
            String[] unhealthy={"unhealthy_Pesticide_name_1", "unhealthy_Pesticide_name_2", "unhealthy_Pesticide_name_3", "unhealthy_Pesticide_name_4", "unhealthy_Pesticide_name_5"};
            Random r=new Random();
            int randomNumber=r.nextInt(unhealthy.length);
            Suggestion_textView.setText("Suggested Pesticide is: "+ unhealthy[randomNumber]);
        }
        else{
            Suggestion_textView.setText("");
        }

    }


    public void perform_text(View view) {
       // alertDialog();

    }
}