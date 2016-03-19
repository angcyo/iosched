package com.ui.jerry;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class MainActivity extends Activity {
    public static final String TAG ="MainActivity";
    private StartTakePhotoView startTakePhotoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startTakePhotoView = (StartTakePhotoView) findViewById(R.id.startTakePhotoView);
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(800, 800);
//
//        startTakePhotoView.setLayoutParams(params);
    }

    public void toCameraClicked(View view){
        //启动之前释放当前camera
        startTakePhotoView.releaseCamera();
        CameraManager.getInstance(this).openCameraActivity(this);
    }
}
