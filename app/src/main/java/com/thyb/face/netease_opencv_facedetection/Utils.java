package com.thyb.face.netease_opencv_facedetection;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Utils {


    /**
     * 将Assert目录下的fileName文件拷贝到app缓存目录
     * @param context
     * @param fileName
     * @return
     */
    public static String copyAssertAndWrite(Context context,String fileName){
        try{
            File cacheDir = context.getCacheDir();
            if (!cacheDir.exists()){
                cacheDir.mkdirs();
            }
             File outFile=new File(cacheDir,fileName);
            boolean res = outFile.createNewFile();
            if (res){
                InputStream is = context.getAssets().open(fileName);
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[is.available()];
                int byteCount;
                if ((byteCount=is.read(buffer))!=-1){
                    fos.write(buffer,0,byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
                return outFile.getAbsolutePath();
            }else {
                return outFile.getAbsolutePath();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
