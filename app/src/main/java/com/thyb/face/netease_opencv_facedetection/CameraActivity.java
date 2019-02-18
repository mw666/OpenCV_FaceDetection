package com.thyb.face.netease_opencv_facedetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private CameraHelper mCameraHeiler;
    private int mCameraId=Camera.CameraInfo.CAMERA_FACING_BACK;
    private String mPath;

    static {
        System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);
        mCameraHeiler = new CameraHelper(mCameraId);
        mCameraHeiler.setmPreviewCallback(this);

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},10);
        }

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},11);
        }
        mPath = Utils.copyAssertAndWrite(this, "lbpcascade_frontalface.xml");

    }

    @Override
    protected void onResume() {
        super.onResume();
        //初始化跟踪器
        init(mPath);
        mCameraHeiler.startPreview();
    }


    @Override
    protected void onStop() {
        super.onStop();
        //释放跟踪器
        release();
        mCameraHeiler.stopPreview();
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            //设置suiface用于显示
        setSurface(surfaceHolder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
          //传输数据
        faceDetected(data,CameraHelper.WIDTH,CameraHelper.HEIGHT,mCameraId);
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /**
     * 初始化 追踪器
     * @param model
     */
    public native void init(String model);

    /**
     * 设置画布
     * ANativewindow
     * @param surface
     */
    public native  void  setSurface(Surface surface);

    /**
     * 处理摄像图数据
     * @param data
     * @param width
     * @param height
     * @param mCameraId
     */
    public native  void  faceDetected(byte[] data, int width, int height, int mCameraId);

    /**
     * 释放
     */
    public native void release();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction()==MotionEvent.ACTION_UP){
            mCameraHeiler.switchCamera();
            mCameraId=mCameraHeiler.getmCameraId();
        }
        return super.onTouchEvent(event);
    }
}
