package com.example.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String PIC_NAME = "temp";

    private String picDir;
    private Uri picUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View btn = findViewById(R.id.btn);
        btn.setOnClickListener(this);


        Log.i("xiong", Environment.getExternalStorageDirectory().getPath());
        Log.i("xiong", getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath());
        Log.i("xiong", getCacheDir().getPath());
        Log.i("xiong", getExternalCacheDir().getPath());
        Log.i("xiong", getFilesDir().getPath());

    }


    @Override
    public void onClick(View v) {

        if (CameraUtil.hasCameraApp(this)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    //如果用户之前拒绝过，弹提示解释为什么需要当前权限。
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
                }
            } else {
                openCamera();
            }
        } else {
            //吐司提示用户没有相机可用
        }
    }

    private void openCamera() {
        picDir = CameraUtil.getPicDir(this);

        if (picDir == null) {
            Toast.makeText(this, "存储空间不足，请清理后重试", Toast.LENGTH_LONG).show();
            return;
        } else {
            picUri = CameraUtil.getPicUri(this, picDir, PIC_NAME);
        }

        if (picUri == null) {
            Toast.makeText(this, "存储空间不足，请清理后重试", Toast.LENGTH_LONG).show();
            return;
        }

        startActivityForResult(CameraUtil.getCameraIntent(this, picUri), 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView iv = (ImageView) findViewById(R.id.iv);
        iv.setImageBitmap(CameraUtil.decodeImage(picDir + "/" + PIC_NAME, iv.getHeight(), iv.getWidth()));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("picUri", picUri);
        outState.putString("picDir", picDir);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            picUri = savedInstanceState.getParcelable("picUri");
            picDir = savedInstanceState.getString("picDir");
        }
    }
}
