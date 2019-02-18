package com.thyb.face.netease_opencv_facedetection;


import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;

public class CameraHelper  implements Camera.PreviewCallback {

    public static final int WIDTH=640;
    public static final int HEIGHT=480;
    private int mCameraId;
    private Camera mCamera;
    private Camera.PreviewCallback mPreviewCallback;

    public void setmPreviewCallback(Camera.PreviewCallback mPreviewCallback) {
        this.mPreviewCallback = mPreviewCallback;
    }

    private byte[] mBuffer;

    public CameraHelper(int mCameraId) {
        this.mCameraId = mCameraId;
    }

    public int getmCameraId() {
        return mCameraId;
    }

    public void switchCamera(){
        if (mCameraId== Camera.CameraInfo.CAMERA_FACING_BACK){
            mCameraId=Camera.CameraInfo.CAMERA_FACING_FRONT;
        }else {
            mCameraId= Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        stopPreview();
        startPreview();
    }

    public void startPreview() {
      try {
        //获取camera对象
         mCamera=Camera.open(mCameraId);
         //配置camera的属性
        Camera.Parameters parameters = mCamera.getParameters();
        //设置预览数据格式是nv21
        parameters.setPictureFormat(ImageFormat.NV21);
        //设置摄像头宽高
        parameters.setPreviewSize(WIDTH,HEIGHT);
        if (mCameraId== Camera.CameraInfo.CAMERA_FACING_BACK){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        //设置摄像头，图像传感器的角度、方向
        mCamera.setParameters(parameters);
        mBuffer =new byte[WIDTH * HEIGHT *3 /2];
        //数据缓存区
        mCamera.addCallbackBuffer(mBuffer);
        mCamera.setPreviewCallbackWithBuffer(this);

        //设置预览画面,离屏渲染
       // mCamera.setPreviewDisplay(surfaceHolder);
        SurfaceTexture surfaceTexture =new SurfaceTexture(11) ;

            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopPreview() {
        if (mCamera!=null){
            //预览数据回调
            mCamera.setPreviewCallback(null);
            //停止预览
            mCamera.stopPreview();
            //释放摄像头
            mCamera.release();
            mCamera=null;
        }
    }



    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
              //data数据是倒的
        mPreviewCallback.onPreviewFrame(data,camera);
        camera.addCallbackBuffer(mBuffer);
    }
}
