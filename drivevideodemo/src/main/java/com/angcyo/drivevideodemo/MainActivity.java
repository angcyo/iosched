package com.angcyo.drivevideodemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import org.wysaid.camera.CameraInstance;
import org.wysaid.view.CameraRecordGLSurfaceView;

public class MainActivity extends AppCompatActivity {

    CameraRecordGLSurfaceView cameraView;

    public static final String effectConfigs[] = {
            "",
            "@beautify bilateral 10 4 1 @style haze -0.5 -0.5 1 1 1 @curve RGB(0, 0)(94, 20)(160, 168)(255, 255) @curve R(0, 0)(129, 119)(255, 255)B(0, 0)(135, 151)(255, 255)RGB(0, 0)(146, 116)(255, 255)",
            "#unpack @blur lerp 0.5", //可调节模糊强度
            "@blur lerp 1", //可调节混合强度
            "#unpack @dynamic wave 1", //可调节速度
            "@dynamic wave 0.5",       //可调节混合
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        cameraView = (CameraRecordGLSurfaceView) findViewById(R.id.myGLSurfaceView);

        initPreview();
    }

    protected void initPreview() {
        cameraView.setVisibility(View.VISIBLE);
        cameraView.presetCameraForward(true);
        cameraView.presetRecordingSize(1920, 480);
//        cameraView.setZOrderOnTop(false);
//        cameraView.setZOrderMediaOverlay(true);

        cameraView.setFilterWithConfig(effectConfigs[2]);

        cameraView.setOnCreateCallback(new CameraRecordGLSurfaceView.OnCreateCallback() {
            @Override
            public void createOver(boolean success) {
                if (success) {
                    Log.i("drivevideo", "view 创建成功");
                    cameraView.setFilterWithConfig(effectConfigs[2]);
                } else {
                    Log.e("drivevideo", "view 创建失败!");
                    CameraInstance.getInstance().stopCamera();
                    cameraView.tryOpenCameraAgain();
                    cameraView.onResume();
                }
            }
        });
    }
}
