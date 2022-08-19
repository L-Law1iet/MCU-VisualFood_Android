package com.imvision.mcu.visualfood_blockchain;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.File;
import java.util.List;

public class Vision extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private ImageView ivTest;
    private File cameraSavePath;//拍照照片路徑
    private Uri uri;
    private String[] permissions = {android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Button btnGetPicFromCamera, btnGetPicFromPhotoAlbum,btn_back_food;
    private TextView textView;
    private String text,text2;
    private float confidence;
    private Bitmap bitmapphoto;
    private String EatFood, EatTime, EatDate, EatCal;//返回食物頁面之資料(待完成)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EasyPermissions.requestPermissions(this, "需要獲取您的相冊及照相使用權限", 1, permissions);

        textView = (TextView)findViewById(R.id.textView);
        btnGetPicFromCamera = findViewById(R.id.btn_get_pic_from_camera);
        btnGetPicFromPhotoAlbum = findViewById(R.id.btn_get_pic_form_photo_album);
        btn_back_food = findViewById(R.id.btn_back_food);
        ivTest = (ImageView) findViewById(R.id.imageView);

            btnGetPicFromCamera.setOnClickListener(this);
            btnGetPicFromPhotoAlbum.setOnClickListener(this);
            btn_back_food.setOnClickListener(this);

        cameraSavePath = new File(Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() + ".jpg");

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_get_pic_from_camera:
                goCamera();
                break;
            case R.id.btn_get_pic_form_photo_album:
                goPhotoAlbum();
                break;
            case R.id.btn_back_food:
                goBacktoFood();
                break;
        }
    }

    private void goCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(Vision.this,"com.imvision.mcu.mlkit_automl.fileprovider", cameraSavePath);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(cameraSavePath);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        Vision.this.startActivityForResult(intent, 1);
    }

    //開啟相冊
    private void goPhotoAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }

    //返回食物頁面(待完成)
    private void goBacktoFood(){
        Intent it = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("EatFood", EatFood);
        bundle.putString("EatTime", EatTime);
        bundle.putString("EatDate", EatDate);
        bundle.putString("EatCal", EatCal);

        it.putExtras(bundle);
        setResult(RESULT_OK, it);
        finish();
    }

    private void AutoML_Vision(){
        FirebaseLocalModel localModel = new FirebaseLocalModel.Builder("LOCAL_MODEL_NAME")
                .setAssetFilePath("automl/manifest.json")
                .build();
        FirebaseModelManager.getInstance().registerLocalModel(localModel);
        FirebaseVisionOnDeviceAutoMLImageLabelerOptions labelerOptions =
                new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder()
                        .setLocalModelName("LOCAL_MODEL_NAME")    // Skip to not use a local model
                        .setConfidenceThreshold(0)  // Evaluate your model in the Firebase console
                        // to determine an appropriate value.
                        .build();
        try {
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmapphoto);
            FirebaseVisionImageLabeler labeler =
                    FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(labelerOptions);
            labeler.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                            int time = 0;
                            for (FirebaseVisionImageLabel label: labels) {
                                time = 1;
                                text = label.getText();
                                confidence = label.getConfidence();
                                if(time == 1)
                                    break;
                            }
                            textView.setText(text+"\n"+confidence);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    });
        }catch (Exception e){

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //權限框架
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    //成功打開權限
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

        Toast.makeText(this, "權限授權成功", Toast.LENGTH_SHORT).show();
    }

    //用户未同意權限
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "請同意相關權限，否則無法啟用成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String photoPath;
        if (requestCode == 1 && resultCode == RESULT_OK) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoPath = String.valueOf(cameraSavePath);
            } else {
                photoPath = uri.getEncodedPath();
            }
            Log.d("拍照返回圖片路徑:", photoPath);
            bitmapphoto = BitmapFactory.decodeFile(photoPath);
            ivTest.setImageBitmap(bitmapphoto);
            AutoML_Vision();
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            photoPath = getPhotoFromPhotoAlbum.getRealPathFromUri(this, data.getData());
            bitmapphoto = BitmapFactory.decodeFile(photoPath);
            ivTest.setImageBitmap(bitmapphoto);
            AutoML_Vision();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



}
